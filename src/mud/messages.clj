(ns mud.messages
  (:require [clojure.java.io :as io]))

;; https://stackoverflow.com/questions/7777882/loading-configuration-file-in-clojure-as-data-structure
(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) v])))))

;; TODO: figure out how to reload on change instead of always
(defn get-message [msg]
  (msg (load-props (io/resource "messages.properties"))))

(defn interpolate
  [msg args]
  (clojure.string/replace msg
                          #"(?:\{([a-zA-Z0-9./_-]+)\})"
                          (fn [[o a]] (get args (keyword a) o))))

(def colors {:k "30;0" :r "31;0" :g "32;0" :y "33;0" :b "34;0" :m "35;0" :c "36;0" :w "37;0"
             :K "30;1" :R "31;1" :G "32;1" :Y "33;1" :B "34;1" :M "35;1" :C "36;1" :W "37;1"
             :n "0"})

(defn colorize
  [msg]
  (clojure.string/replace msg
                          #"%([a-zA-Z])"
                          (fn [[o l]]
                            (let [k (keyword l)]
                              (if (contains? colors k)
                                (str "\u001b[" (k colors) "m")
                                o)))))

(defn fmt
  ([msg] (fmt msg {}))
  ([msg args]
     (-> (get-message msg)
         (interpolate args)
         colorize)))
