(ns mud.core
  (:require [ns-tracker.core :as tracker]
            [taoensso.timbre :as log]
            [aleph.tcp :as tcp]
            [gloss.core :as gloss]
            [mud.connection :as connection]
            [mud.db :as db]
            [clojure.tools.nrepl.server :as nrepl]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class))

(defn check-namespace-changes [track]
 (try
   (doseq [ns-sym (track)]
     (log/info "Reloading namespace:" ns-sym)
     (require ns-sym :reload))
   (catch Throwable e (.printStackTrace e)))
   (Thread/sleep 500))

(defn start-nstracker []
 (let [track (tracker/ns-tracker ["src" "checkouts"])]
   (doto
     (Thread.
       #(while true
         (check-namespace-changes track)))
     (.setDaemon true)
     (.start))))

(defn -main
  [& args]
  (start-nstracker)
  (let [config (edn/read-string (slurp (io/resource "config.edn")))]
    (db/initialize-database (:db/uri config))
    ;; call new-connection through lamba so it will use new version after reload
    (tcp/start-tcp-server #(connection/new-connection %1 %2 (:db/uri config))
                          {:port (:port config)
                           :decoder (gloss/string :utf-8 :delimiters ["\r\n"])})
    (log/infof "Listening on port %d." (:port config))
    (when (:nrepl/enabled? config)
      (nrepl/start-server :port (:nrepl/port config))
      (log/infof "nrepl listening on port %d." (:nrepl/port config)))))
