(ns mud.db
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [datomic-schema.schema :as s]
            [plumbing.core :as p]))

(defn- make-fields-map
  "Convert fields from :name -> [:type :extra] to 'name' -> [:type #{:extra}]
   for datomic-schema"
  [fields]
  (p/map-keys name (p/map-vals #(vector (first %) (set (rest %))) fields)))

(defn initialize-database
  "Make sure database exists are uri and create schema."
  [uri]
  (d/create-database uri)
  (d/transact (d/connect uri)
              (s/generate-schema
               d/tempid
               (map #(hash-map :namespace (name (key %))
                               :fields (make-fields-map (val %)))
                    (edn/read-string (slurp (io/resource "schema.edn")))))))

(comment
  (d/transact (d/connect uri)
              [{:db/id #db/id[:db.part/user]
                :entity/name "The World"
                :entity/description "The whole world"
                :entity/type :entity.type/world
                :world/time 0}])

  (d/transact (d/connect uri)
              [{:db/id #db/id[:db.part/user]
                :entity/name "A Room"
                :entity/description "It is a room"
                :entity/type :entity.type/room}])

  (d/transact (d/connect uri)
              [{:db/id #db/id[:db.part/tx]
                :event "hi"}])

  )
