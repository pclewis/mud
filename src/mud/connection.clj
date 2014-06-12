(ns mud.connection
  (:require [lamina.core :as lamina]
            [taoensso.timbre :as log]
            [mud.handlers.login :as login]
            [mud.handlers.char-select :as cs]
            [mud.handlers.game :as game]
            [mud.messages :as msg]
            [mud.db :as db]
            [datomic.api :as d]
            [clojure.java.io :as io]))

(def connections (atom []))

;; lambdas so ns reload works
(def handlers
  {:login #(login/handler %1 %2)
   :char-select #(cs/handler %1 %2)
   :game #(game/handler %1 %2)})

(defn send-msg
  [channel & m]
  (lamina/enqueue channel (str (apply msg/fmt m) "\n")))

(defn send
  [conn message]
  (lamina/enqueue (:channel conn) (str message "\n")))

(defn exec-action
  [connection action]
  (case (first action)
    :send (send @connection (second action))
    :msg (send-msg (:channel @connection) (second action) (drop 2 action))
    :transact @(d/transact (:db/c @connection) (rest action))
    :transition (dosync
                 (alter connection into
                        (merge (get action 2)
                               {:handler ((second action) handlers)})))))

(defn receive-message
  [connection message]
  (if (nil? message)
    (do
      (log/info "Client disconnected " (:client-info @connection))
      (swap! connections (partial filterv (complement #{connection}))))
    (try
      (let [action ((:handler @connection) @connection message)]
        (when action
          (if (coll? (first action))
            (doall (map (partial exec-action connection) action))
            (exec-action connection action))))
      (catch Exception e
        (log/error e "handler threw exception")))))

(defn close-connection
  [connection]
  (receive-message connection nil))

(defn new-connection
  [channel client-info dburi]
  (log/info "Client connected " client-info)
  (let [dbc (d/connect dburi)
        connection (ref {:client-info client-info
                         :channel channel
                         :handler (:login handlers)
                         :db/c dbc
                         :db (d/db dbc)})]
    (swap! connections conj connection)
    (lamina/receive-all channel #(receive-message connection %))
    (lamina/on-closed channel #(close-connection connection)))
  (send-msg channel :welcome))

(defn step-connection
  [conn db]
  (log/debugf "Stepping from %d to %d" (dec (d/as-of-t db)) (d/as-of-t db))
  (doseq [event-ent (d/q '[:find ?e :where [?e :event/type _]]
                          (d/since db (dec (d/as-of-t db))))
          :let [event (d/entity db (first event-ent))]]
    (case (:event/type event)
      :event.type/speech (send-msg (:channel @conn) :say/other
                                   {:source (:entity/name (:event/source event))
                                    :message (:event/content event)}))))

(defn update-connection
  [conn]
  (let [old-db (:db @conn)
        new-db (d/db (:db/c @conn))
        old-t (d/basis-t old-db)
        new-t (d/basis-t new-db)]
    (when (< old-t new-t)
      (log/debugf "Tick conn, old t = %d, new t = %d" old-t new-t)
      (doseq [t (range old-t new-t)]
        (step-connection conn (d/as-of new-db (inc t))))
      (dosync (alter conn assoc :db new-db)))))

(defn update-connections
  []
  (doseq [conn @connections]
    (update-connection conn)))
