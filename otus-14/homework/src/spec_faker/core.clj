(ns spec-faker.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [cheshire.core :as json]
            [compojure.api.sweet :refer :all]
            [compojure.route :as route]
            [spec-faker.schema-validator :refer [validate-schema explain-validation-error]]
            [spec-faker.data-generator :refer [gen-example]]
            [ring.util.http-response :refer :all]
            [hiccup.core :refer [html]]
            [schema.core :as s])
  (:gen-class)
  (:import (com.fasterxml.jackson.core JsonParseException)
           (java.net URLEncoder)))

;; Convert JSON string to EDN
(defn json-to-edn [json-str]
  (json/parse-string json-str true))

(def bad-json-response
  (bad-request {:error "Invalid JSON\n"}))

(defn invalid-spec-response [decoded-spec]
  (bad-request {:error (str "Invalid spec\n" (explain-validation-error decoded-spec))}))

(def input-form-response
  (html
    [:html
     [:head [:title "Spec Faker"]]
     [:body
      [:h1 "Insert OpenAPI specification"]
      [:form {:method "post" :action "/"}
       [:textarea {:name "spec" :rows 10 :cols 50}]
       [:br]
       [:input {:type "submit" :value "Generate"}]]]]))

(defn gen-data-response [spec]
  (let [decoded-spec (json-to-edn spec)]
    (if (validate-schema decoded-spec)
      (ok (gen-example decoded-spec))
      (invalid-spec-response decoded-spec))))

(defn redirect-response [spec-json]
  (see-other (str "/?spec=" (URLEncoder/encode ^String spec-json "UTF-8"))))

(defn invalid-spec-form-response [spec-json]
  (let [decoded-spec (json-to-edn spec-json)]
    (bad-request {:error (str "Invalid Swagger spec: " (explain-validation-error decoded-spec))})))


;; Define the API
(defapi app
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:title "Spec Faker API"
                           :description "Generate example data from OpenAPI specifications"}
                    :tags [{:name "faker" :description "APIs for generating example data"}]}}}

  (context "/" []
    :tags ["faker"]

    ; GET endpoint to fetch form or generate data
    (GET "/" []
      :query-params [{spec :- (s/maybe s/Str) nil}]
      (if spec
        (try
          (gen-data-response spec)
          (catch JsonParseException _ bad-json-response))
        (content-type (ok input-form-response) "text/html")))


    ;; POST endpoint to validate and process spec
    (POST "/" []
      :form-params [spec :- s/Str]
      :return {:redirect s/Str}
      (try
        (if (validate-schema (json-to-edn spec))
          (redirect-response spec)
          (invalid-spec-form-response spec))
        (catch JsonParseException _ bad-json-response))))

  (undocumented
    (ANY "*" []
      (not-found "Not Found"))))

(comment
  (run-jetty #'app {:join? false
                    :port  8000}))

;; Start the server
(defn -main
  [& args]
  (run-jetty app {:port 8000 :join? false}))