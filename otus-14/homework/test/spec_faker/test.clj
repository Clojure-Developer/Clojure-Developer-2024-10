(ns spec-faker.test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [spec-faker.core :refer [app]])
  (:import (java.net URLEncoder)))

(deftest test-get-root
  (testing "GET / without spec"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response)))
      (is (.contains (:body response) "Insert OpenAPI specification")))))

(deftest test-get-with-spec
  (testing "GET / with valid spec"
    (let [spec "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}"
          response (app (mock/request :get "/" {:spec spec}))
          body (slurp (:body response))]
      (is (= 200 (:status response)))
      (is (.contains body "\"name\""))))

  (testing "GET / with invalid spec"
    (let [spec "{\"type\":\"invalid\"}"
          response (app (mock/request :get "/" {:spec spec}))
          body (slurp (:body response))]
      (is (= 400 (:status response)))
      (is (.contains body "Invalid spec"))))

  (testing "GET / with invalid JSON"
    (let [spec "{\"type\":\"object\",\"properties\":{\"name\"}"
          response (app (mock/request :get "/" {:spec spec}))
          body (slurp (:body response))]
      (is (= 400 (:status response)))
      (is (.contains body "Invalid JSON")))))

(deftest test-post-root
  (testing "POST / with valid spec"
    (let [spec "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}"
          response (app (-> (mock/request :post "/")
                            (mock/content-type "application/x-www-form-urlencoded")
                            (mock/body (str "spec=" (URLEncoder/encode spec "UTF-8")))))]
      (is (= 303 (:status response)))
      (is (= (str "/?spec=" (URLEncoder/encode spec "UTF-8"))
             (get-in response [:headers "Location"])))))

  (testing "POST / with invalid spec"
    (let [spec "{\"type\":\"invalid\"}"
          response (app (-> (mock/request :post "/")
                            (mock/content-type "application/x-www-form-urlencoded")
                            (mock/body (str "spec=" (URLEncoder/encode spec "UTF-8")))))
          body (slurp (:body response))]
      (is (= 400 (:status response)))
      (is (.contains body "Invalid Swagger spec"))))

  (testing "POST / with invalid JSON"
    (let [spec "{\"type\":\"object\",\"properties\":{\"name\"}"
          response (app (-> (mock/request :post "/")
                            (mock/content-type "application/x-www-form-urlencoded")
                            (mock/body (str "spec=" (URLEncoder/encode spec "UTF-8")))))
          body (slurp (:body response))]
      (is (= 400 (:status response)))
      (is (.contains body "Invalid JSON")))))


(deftest test-not-found
  (testing "GET /unknown"
    (let [response (app (mock/request :get "/unknown"))]
      (is (= 404 (:status response)))
      (is (= "Not Found" (:body response))))))