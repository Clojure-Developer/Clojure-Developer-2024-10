(ns spec-faker.schema-validator
  (:require [clojure.spec.alpha :as s]))

(s/def ::type #{"string" "integer" "boolean" "object"})
(s/def ::format (s/nilable #{"email" "uuid"}))
(s/def ::minLength pos-int?)
(s/def ::maxLength pos-int?)
(s/def ::minimum number?)
(s/def ::maximum number?)

(s/def ::property
  (s/keys :req-un [::type]
          :opt-un [::format ::minLength ::maxLength ::minimum ::maximum]))

(s/def ::properties (s/map-of keyword? ::property))
(s/def ::required (s/coll-of string? :kind vector?))

(s/def ::openapi-schema
  (s/keys :req-un [::type ::properties]
          :opt-un [::required]))

(defn validate-schema [schema]
  (s/valid? ::openapi-schema schema))

(defn explain-validation-error [schema]
  (s/explain-str ::openapi-schema schema))