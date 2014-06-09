(ns mud.handlers.login
  (:require [mud.command-parser :as cp]
            [datomic.api :as db]))

(defn register
  [conn email password]
  (if-not (empty?
           (db/q '[:find ?u :in $ ?e :where [?u :user/email ?e]]
                 (db/db (:db/c conn))
                 email))
    [:send "There is already an account with that email address."]
    (do
      (db/transact (:db/c conn)
                   [{:db/id #db/id[:db.part/user]
                     :user/email email
                     :user/password password}]) ;; FIXME hash passwords
      [:send "Account created (maybe). Try to log in!"])))

(defn login
  [conn name password]
  (let [accts (db/q '[:find ?u :in $ ?e ?p
                      :where [?u :user/email ?e]
                      [?u :user/password ?p]]
                    (db/db (:db/c conn))
                    name password)] ;; FIXME
    (if (empty? accts)
      [:send "Nope."]
      [:transition :char-select {:user (ffirst accts)}])))

(defn huh
  [conn parse]
  [:send "Huh?"])


(def handler
  (cp/create-handler [register login] huh nil))
