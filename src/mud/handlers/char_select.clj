(ns mud.handlers.char-select
  (:require [mud.command-parser :as cp]))

(defn create [conn name])
(defn use [conn name])

(defn huh [conn p]
  [:send "do what now"])

(def handler
  (cp/create-handler [create use] huh nil))
