(ns mud.command-parser
  (:require [instaparse.core :as insta]
            [instaparse.combinators :as ic]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [plumbing.core :as p]))


(defmacro map-var [ss] (cons 'list (map #(identity `(var ~%)) ss)))
(defmacro map-meta [ss] `(map meta (map-var ~ss)))
(defmacro map-with-meta [ss] `(map with-meta ~ss (map-meta ~ss)))

(def base-parser (ic/ebnf (slurp (io/resource "grammar.ebnf"))))

(defn- parser-from-arglist [base arglist]
  (if (empty? arglist)
    '()
    (apply ic/cat
           (ic/hide (ic/plus (ic/nt :whitespace)))
           (interpose (ic/hide (ic/plus (ic/nt :whitespace)))
                      (map (fn [a] (if ((keyword a) base)
                                    (ic/nt (keyword a))
                                    (ic/nt :argument)))
                           arglist)))))

(defn- parser-from-arglists [base arglists]
  (apply ic/alt (map (partial parser-from-arglist base) arglists)))

(defn- parser-from-meta [base m]
  {(keyword (name (:name m)))
   (ic/cat (ic/hide (ic/string (name (:name m))))
           (parser-from-arglists base (map rest (:arglists m))))})

(defn create-handler*
  [command-fns failure-fn transforms]
  (let [parsers (apply merge (map (partial parser-from-meta base-parser) (map meta command-fns)))
        parser (insta/parser (merge base-parser parsers
                                    {:S (ic/hide-tag (apply ic/alt (map ic/nt (keys parsers))))})
                             :start :S)
        fns (p/map-from-vals (comp keyword :name meta) command-fns)]
    (fn [o line]
      (let [result (insta/transform
                    (into {:argument str} transforms)
                    (insta/parse parser line :total true))]
        (if (insta/failure? result)
          (failure-fn o result)
          (apply (fns (ffirst result)) o (rest (first result))))))))

(defmacro create-handler
  "Create a handler that takes [o line], parses line, and calls the corresponding command."
  [command-fns failure-fn transforms]
  `(create-handler* (map-with-meta ~command-fns) ~failure-fn ~transforms))

;; throw big sword at rogue
;; look in rogue's backpack
;; say ~whatever~
;; get 3 arrows

;; disambiguation handler?
;; > throw sword at rogue
;; Which sword?
;;  (1) big sword
;;  (2) crappy sword
;; >> 2
