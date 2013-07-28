(ns mud.command-parser
  (:require [instaparse.core :as insta]))

; this is maybe ridiculous
(def parser
  (insta/parser
   "S = command ( <whitespace>+ argument )* <whitespace>*

    command = word

    argument = argument_
    <argument_> = string | quoted-string | argument_ ( string | quoted-string )

    <quoted-string> = single-quoted-string | double-quoted-string

    <single-quoted-string> = <single-quote> not-single-quote* <single-quote>
    <double-quoted-string> = <double-quote> ( escaped-char | not-double-quote )* <double-quote>

    <string> = ( escaped-char | string-component )+

    <string-component> = #'[^\\'\"\\s]'

    <single-quote> = \"'\"
    <double-quote> = '\"'
    <backslash> = '\\\\'

    <not-single-quote> = #'[^\\']'
    <not-double-quote> = #'[^\"]'

    <escaped-char> = <backslash> #'.'

    <whitespace> = #'\\s'

    <word> = letter | word letter | word ( '_' | '-' )
    <letter> = #'[a-zA-Z]'"))

(defn parse-command
  [input]
  (rest  (insta/transform {:command str :argument str} (parser input))))
