(ns mud.connection
  (:require [lamina.core :as lamina]
            [taoensso.timbre :as log]
            [mud.handlers.login :as login]
            [mud.handlers.char-select :as cs]
            [mud.db :as db]
            [datomic.api :as d]))

(comment
  (defn game-handler
    [connection message]
    (lamina/enqueue (:channel @connection) (str "You " message ".\n"))))

(comment (defn login-handler
           [connection message]
           (alter connection into {:handler game-handler :name message})
           (log/info "logged in: " message)))

(def welcome-message
  "hi\nUsername: ")

(def handlers
  {:login #(login/handler %1 %2)
   :char-select #(cs/handler %1 %2)})

(defn send
  [conn message]
  (lamina/enqueue (:channel conn) (str message "\n")))

(defn receive-message
  [connection message]
  (if (nil? message)
    (log/info "Client disconnected " (:client-info @connection))
    (try
      (let [action ((:handler @connection) @connection message)]
        (when action
          (case (first action)
            :send (send @connection (second action))
            :transition (dosync
                         (alter connection into
                                (merge (get action 2)
                                       {:handler ((second action) handlers)}))))))
      (catch Exception e
        (log/error e "handler threw exception")))))

(defn close-connection
  [connection]
  (receive-message connection nil))

(defn new-connection
  [channel client-info dburi]
  (log/info "Client connected " client-info)
  (let [connection (ref {:client-info client-info
                         :channel channel
                         :handler (:login handlers)
                         :db/c (d/connect dburi)})]
    (lamina/receive-all channel #(receive-message connection %))
    (lamina/on-closed channel #(close-connection connection)))
  (lamina/enqueue channel welcome-message))
