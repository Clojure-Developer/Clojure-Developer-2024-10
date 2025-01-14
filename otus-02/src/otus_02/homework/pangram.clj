(ns otus-02.homework.pangram
  (:require [otus-02.homework.palindrome :as pal]))

(def alphabet-count 26)

(defn is-pangram [test-string]
  (-> test-string
      (pal/transform-string)
      (set)
      (count)
      (== alphabet-count)))

(comment
  (is-pangram "The quick brown fox jumps over the lazy dog")
  )
