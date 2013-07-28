(ns mud.core
  (:require [ns-tracker.core :as tracker]
            [taoensso.timbre :as timbre :refer (info)]
            [aleph.tcp :as tcp]
            [gloss.core :as gloss]
            [mud.connection :as connection]
            [clojure.tools.nrepl.server :as nrepl])
  (:gen-class))

(defn check-namespace-changes [track]
 (try
   (doseq [ns-sym (track)]
     (info "Reloading namespace:" ns-sym)
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
  ;; DISABLED: this breaks aleph for some reason
  ;;; work around dangerous default behaviour in Clojure
  ;;; (alter-var-root #'*read-eval* (constantly false))
  (start-nstracker)
  ; call new-connection through lamba so it will use new version after reload
  (tcp/start-tcp-server #(connection/new-connection %1 %2) {:port 34567, :decoder (gloss/string :utf-8 :delimiters ["\r\n"])})
  (info "Listening on port 34567.")
  (nrepl/start-server :port 37888)
  (info "nrepl listening on port 37888."))
