(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :as sut]))

(deftest int->id-test
  (testing "Legal args."


    ))

(deftest int->id-test
  (testing "Legal args."
    (are [expected input] (= expected (sut/int->id input))
      "0" 0
      "2q3Rktod" 9999999999999
      "AzL8n0Y58W7" Long/MAX_VALUE
      "10" 62
      "" -10))
  (testing "Illegal args."
    (is (thrown? ClassCastException (sut/int->id "ass")))))

(deftest id->int-test
  (testing "Legal args."
    (are [expected input] (= expected (sut/id->int input))
                          0 "0"
                          9999999999999 "2q3Rktod"
                          62 "10"
                          0 ""))
  (testing "Illegal args."
    (is (thrown? IllegalArgumentException (sut/id->int 45) ))))

(deftest bidirectional-test
  (testing "Should encode and decode values"
    (is (= (sut/id->int (sut/int->id 34)) 34))))
