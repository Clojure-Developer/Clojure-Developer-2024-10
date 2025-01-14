(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))

(defn transform-string [s]
  (-> s
    (string/replace #"[,.-:;!?\"' ]" "")
    (string/lower-case)))

(defn recur-palindrom [test-vec]
  (let [go-next (= (get test-vec 0) (get test-vec (dec (count test-vec))))]
    (cond
      (empty? test-vec) true
      (== (count test-vec) 1) true
      (not go-next) false
      :else (recur (subvec test-vec 1 (dec (count test-vec)))))))

(defn is-palindrome [test-string]
  (let [s (transform-string test-string)
        s-vec (vec s)]
    (recur-palindrom s-vec)))

(comment
  (is-palindrome "Civ, ic!"))
