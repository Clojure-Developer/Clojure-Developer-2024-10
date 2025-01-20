(ns spec-faker.spec
  (:require
   [clojure.spec.gen.alpha :as gen]
   [cheshire.core :as json]
   [clojure.spec.alpha :as s]
   [spec-tools.swagger.core :as swagger]))

(s/def :faker/name string?)
(s/def :faker/type #{"integer" "string" "date" "float"})
(s/def :faker/spec (s/coll-of (s/keys :req-un [:faker/name :faker/type])))

(s/def :user-spec/string string?)
(s/def :user-spec/integer integer?)
(s/def :user-spec/date inst?)
(s/def :user-spec/float float?)

(defn valid? [spec]
  (s/valid? :faker/spec (json/parse-string spec true)))

(defn gen-data [raw-spec]
  (into {} (for [item (json/parse-string raw-spec true)]
             {(:name item) (gen/generate (s/gen (keyword "user-spec" (:type item))))})))

(def swagger-json
  (swagger/swagger-spec
   {:swagger "2.0"
    :info {:version "1.0.0"
           :title "Spec generator"
           :description "Description"
           :termsOfService "http://helloreverb.com/terms/"
           :contact {:name "My API Team"
                     :email "foo@example.com"
                     :url "http://www.metosin.fi"}
           :license {:name "Eclipse Public License"
                     :url "http://www.eclipse.org/legal/epl-v10.html"}}
    :tags [{:name "spec"
            :description "Spec"}]
    :paths {"/" {:post {:tags ["spec"]
                        ::swagger/parameters {:spec (s/keys :req-un [:faker/spec])}
                        ::swagger/responses {200 {:spec :faker/spec
                                                  :description "Good schema"}
                                             404 {:description "Not found"}}}}}}))
