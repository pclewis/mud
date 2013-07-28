(defproject mud "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "FIXME: url"
  :license {:name "CC0"
            :url "https://creativecommons.org/publicdomain/zero/1.0/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.0-rc2"]
                 [com.datomic/datomic-free "0.8.4020.26"]
                 [com.taoensso/timbre "2.4.1"]
                 [ns-tracker "0.2.1"]
                 [org.clojure/tools.nrepl "0.2.3"]]
  :main mud.core)
