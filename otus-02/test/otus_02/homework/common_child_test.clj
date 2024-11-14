(ns otus-02.homework.common-child-test
  (:require
   [clojure.test :refer :all]
   [otus-02.homework.common-child :as sut]))


(deftest common-child-test

  (is (= (sut/common-child-length-2 "SHINCHAN" "NOHARAAA")
         3))

  (is (= (sut/common-child-length-2 "HARRY" "SALLY")
         2))

  (is (= (sut/common-child-length-2 "AA" "BB")
         0))

  (is (= (sut/common-child-length-2 "ABCDEF" "FBDAMN")
         2)))
