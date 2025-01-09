(ns spec-faker.core
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup.core :refer [html]]
   [hiccup.util :refer [url]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :refer [redirect]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.spec.alpha :as s]
   [spec-tools.json-schema :as js]
   [spec-tools.core :as st]
   [clojure.spec.gen.alpha :as gen])
  (:gen-class))

(defn page [title & body]
  [:html
   [:head
    [:title title]]
   [:body
    body]])

(comment

  (s/def ::id int?)
  (s/def ::description string?)
  (s/def ::amount pos-int?)
  (s/def ::delivery inst?)
  (s/def ::tags (s/coll-of keyword? :into #{}))
  (s/def ::item (s/keys :req-un [::description ::tags ::amount]))
  (s/def ::items (s/map-of ::id ::item))
  (s/def ::location (s/tuple double? double?))
  (s/def ::order (s/keys :req-un [::id ::items ::delivery ::location]))

  (gen/generate (s/gen ::order))
  (gen/sample (s/gen ::order))

  (gen/generate (js/transform ::order))

(gen/sample (s/gen #{:club :diamond :heart :spade}))

  (def order
    {:id 123,
     :items {1 {:description "vadelmalimsa"
                :tags #{:good :red}
                :amount 10},
             2 {:description "korvapuusti"
                :tags #{:raisin :sugar}
                :amount 20}},
     :delivery #inst"2007-11-20T20:19:17.000-00:00",
     :location [61.499374 23.7408149]})

  (s/gen ::order)

  (s/valid? (s/spec (js/transform ::order)) order)
  (generate-string (s/spec (js/transform ::order)))

  (st/coerce
   ::order
   {:id 123,
    :items {1 {:description "vadelmalimsa"
               :tags #{:good :red}
               :amount 10},
            2 {:description "korvapuusti"
               :tags #{:raisin :sugar}
               :amount 20}},
    :delivery #inst"2007-11-20T20:19:17.000-00:00",
    :location [61.499374 23.7408149]}
   st/json-transformer)

  (st/serialize ::id)

  (s/explain (s/spec (js/transform ::order)) order)

  (st/deserialize "(clojure.spec.alpha/keys :req-un [:spec-faker.core/id :spec-faker.core/items :spec-faker.core/delivery :spec-faker.core/location])")

  (s/form ::order)
  (s/gen ::order))

(defn valid? [spec]
  true)

(defn gen-data-for-spec [spec]
  [])

(defroutes router
  (GET "/" [spec]
    (html
     (page
      "Spec gen"
      (if (nil? spec) [:form {:method "POST"}
                       [:label "Spec: "]
                       [:textarea {:name "spec" :rows "10" :cols "30"}]
                       [:button {:type "submit"} "Go..."]]
          [:h2  (str (parse-string spec))]))))

  (POST "/" [spec]
    (when (valid? spec)
      (redirect (str (url "/" {:spec (generate-string spec)})))))

  (route/not-found
   (html
    (page
     "Page not found"
     [:h1 "Oops!"]))))

(def app
  (-> #'router
      wrap-keyword-params
      wrap-params))

(comment
  (def dev-server (run-jetty (wrap-reload #'app) {:join? false
                                                  :port 8000}))
  (. dev-server stop))

(defn -main
  [& args]
  (run-jetty

   (wrap-reload app)

   {:port 8000}))
