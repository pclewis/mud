(defproject mud "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "FIXME: url"
  :license {:name "CC0"
            :url "https://creativecommons.org/publicdomain/zero/1.0/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.2"]
                 [com.datomic/datomic-free "0.9.4815"]
                 [com.taoensso/timbre "3.2.1"]
                 [ns-tracker "0.2.2"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [instaparse "1.3.2"]
                 [datomic-schema "1.1.0"]
                 [prismatic/plumbing "0.3.1"]]
  :main mud.core)
