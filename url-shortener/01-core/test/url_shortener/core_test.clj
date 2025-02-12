(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :refer :all]))

(deftest test-int->id
  (testing "Legal arguments"
    (are [res value] (= res  (int->id value))
      "1" 1
      "10" 62
      "2q3Rktod" 9999999999999
      "AzL8n0Y58W7" Long/MAX_VALUE))
  (testing "Illegal arguments"
    (is (thrown? java.lang.ClassCastException
                 (int->id "_")))))

(deftest id->int-test
  (testing "Legal arguments"
    (are [res value] (= res (id->int value))
      1 "1"
      61 "z"
      725410830262 "Clojure"
      149031 "clj"))
  (testing "Illegal arguments"
    (is (thrown? java.lang.NullPointerException
                 (id->int "_")))))
