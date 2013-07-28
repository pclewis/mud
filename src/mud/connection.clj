(ns mud.connection
  (:require [lamina.core :as lamina]
            [taoensso.timbre :as timbre :refer (info)]))

(defn game-handler
  [connection message]
  (lamina/enqueue (:channel @connection) (str "You " message ".\n")))

(defn login-handler
  [connection message]
  (alter connection into {:handler game-handler :name message})
  (info "logged in: " message))

(def welcome-message
  "hi\nUsername: ")

(defn receive-message
  [connection message]
  (when (nil? message)
    (info "Client disconnected " (:client-info @connection)))
  (dosync
   ((:handler @connection) connection message)))

(defn close-connection
  [connection]
  (receive-message connection nil))

(defn new-connection
  [channel client-info]
  (info "Client connected " client-info)
  (let [connection (ref {:client-info client-info
                         :channel channel
                         :handler login-handler})]
    (lamina/receive-all channel #(receive-message connection %))
    (lamina/on-closed channel #(close-connection connection)))
  (lamina/enqueue channel welcome-message))
