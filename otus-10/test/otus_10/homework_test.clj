(ns otus-10.homework-test
  (:require [clojure.test :refer :all]
            [otus-10.homework :as sut]))

(deftest test-read-id3-tag
  (let [result (sut/read-id3-tag "sample-3s.mp3")]
    (is (= (:artist result) "Eric Cartman"))
    (is (= (:album result) "South Park"))
    (is (= (:title result) "Kyle’s Mom’s A Bitch"))
    (is (= (:year result) "1999"))
    (is (= (:genre result) "Country"))
    ))
