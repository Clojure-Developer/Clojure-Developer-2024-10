(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))


(defn prepare-str [s]
  (string/replace (string/lower-case s) #"[^\w]" ""))

(defn is-palindrome [test-string]
  (let [str (prepare-str test-string)]
    (= str (string/reverse str))))


(defn is-palindrome-generic [input]
  (let [prepared
        (if (string? input)
          (seq (prepare-str input))
          input)]
    (= prepared (reverse prepared))))
