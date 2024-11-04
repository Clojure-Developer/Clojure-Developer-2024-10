(ns otus.homework-test
  (:require [clojure.test :refer :all]
            [otus.homework :as sut]))


(deftest s-expression-solution-test
  (is (= -0.2466666666666667 (sut/solution))
      "solution function returns a correct answer"))
