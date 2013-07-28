(ns mud.connection
  (:require [lamina.core :as lamina]
            [taoensso.timbre :as timbre :refer (info)]))

(defn new-connection [channel client-info]
  (info "Client connected " client-info)
  (lamina/enqueue channel "test"))
