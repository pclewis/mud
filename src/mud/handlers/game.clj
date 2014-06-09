(ns mud.handlers.game
  (:require [mud.command-parser :as cp]))

(defn say [conn text]
  [:send (str "You say, \"" text "\"")])

(defn huh [conn p]
  [:send (str "Huh?")])

(def handler
  (cp/create-handler [say] huh {:text str}))
