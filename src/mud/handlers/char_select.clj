(ns mud.handlers.char-select
  (:require [mud.command-parser :as cp]
            [datomic.api :as d]))

(defn create [conn name]
  (let [existing
        (d/q '[:find ?c :in $ ?n
               :where [?c :entity/name ?n]
               [?c :entity/type :entity.type/creature]]
             (d/db (:db/c conn))
             name)]
    (if (empty? existing)
      [[:transact {:db/id #db/id[:db.part/user]
                   :entity/name name
                   :entity/type :entity.type/creature
                   :user/_character (:user conn)}]
       [:send "Created character!"]]
      [:send "Character already exists."])))

(defn use [conn name]
  (let [db (d/db (:db/c conn))
        existing
        (d/q '[:find ?c :in $ ?n ?u
               :where [?c :entity/name ?n]
               [?c :entity/type :entity.type/creature]
               [?u :user/character ?c]]
             db
             name
             (:user conn))]
    (if (empty? existing)
      [:send "No such character!"]
      [:transition :game {:character (ffirst existing)}])))

(defn huh [conn p]
  [:send "do what now"])

(def handler
  (cp/create-handler [create use] huh nil))
