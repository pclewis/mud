(ns mud.handlers.game
  (:require [mud.command-parser :as cp]))

(defn say [conn text]
  [:transact
   {:db/id #db/id[:db.part/tx]
    :event/type :event.type/speech
    :event/source (:character conn)
    :event/content text}])

(defn huh [conn p]
  [:msg :huh])

(def handler
  (cp/create-handler [say] huh {:text str}))
