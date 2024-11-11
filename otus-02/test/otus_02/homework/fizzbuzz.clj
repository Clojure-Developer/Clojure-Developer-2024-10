(ns otus-02.homework.fizzbuzz
  (:require
   [clojure.test :refer :all]))


(defn replace-num [n]
  (cond
    (and (= (mod n 3) 0) (= (mod n 5) 0))  "FizzBuzz"
    (= (mod n 3) 0) "Fizz"
    (= (mod n 5) 0) "Buzz"
    :else n))

(defn fizz-buzz [n]
  "Создайте программу, которая выводит числа от 1 до n.
   - Если число делится на 3, выведите 'Fizz';
   - если число делится на 5, выведите 'Buzz';
   - если число делится и на 3 и на 5, выведите 'FizzBuzz'."
  (map replace-num (range 1 (+ 1 n))))


(deftest fizz-buzz-test
  (is (= (fizz-buzz 10)
         '(1 2 "Fizz" 4 "Buzz" "Fizz" 7 8 "Fizz" "Buzz"))))
