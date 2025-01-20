(ns spec-faker.spec-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [spec-faker.spec :as subject]))

(deftest test-valid?
  (testing "valid? function"
    (let [valid-spec "[{\"name\": \"age\", \"type\": \"integer\"}]"
          invalid-spec "[{\"name\": \"age\", \"type\": \"not-an-integer\"}]"]
      (is (subject/valid? valid-spec))
      (is (not (subject/valid? invalid-spec))))))

(deftest test-gen-data
  (testing "gen-data function"
    (let [spec-data "[{\"name\": \"user-age\", \"type\": \"integer\"}]"]
      (let [result (subject/gen-data spec-data)]
        (is (= (keys result) ["user-age"]))
        (is (integer? (get result "user-age")))))

    (let [spec-data "[{\"name\": \"user-name\", \"type\": \"string\"}]"]
      (let [result (subject/gen-data spec-data)]
        (is (= (keys result) ["user-name"]))
        (is (string? (get result "user-name")))))

    (let [spec-data "[{\"name\": \"user-birthdate\", \"type\": \"date\"}]"]
      (let [result (subject/gen-data spec-data)]
        (is (= (keys result) ["user-birthdate"]))
        (is (inst? (get result "user-birthdate")))))

    (let [spec-data "[{\"name\": \"user-rating\", \"type\": \"float\"}]"]
      (let [result (subject/gen-data spec-data)]
        (is (= (keys result) ["user-rating"]))
        (is (float? (get  result "user-rating")))))))
