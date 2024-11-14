(ns otus-02.homework.pangram
  (:require [clojure.string :as string]))

(defn is-pangram [test-string]
  (let [str (string/replace (string/lower-case test-string) #"[^\w]" "")]
    (= (count (distinct (seq str))) 26)))

