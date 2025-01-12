(ns spec-faker.data-generator
  (:require [clojure.test.check.generators :as gen]))

(defn gen-integer
  "Генерирует целое число в интервале [min, max], если указаны.
   Если min/max не указаны, применяем некий разумный диапазон по умолчанию."
  [schema]
  (let [minimum (:minimum schema 0)
        maximum (:maximum schema Integer/MAX_VALUE)]
    (gen/choose minimum maximum)))

(defn gen-boolean
  "Генерирует случайное boolean-значение."
  []
  gen/boolean)

(defn gen-random-string
  "Вспомогательная функция: генерирует строку из букв/цифр ограниченной длины.
   При необходимости усечёт строку до max-length."
  [min-len max-len]
  (gen/fmap
    (fn [s]
      (let [cut (subs s 0 (min max-len (count s)))]
        cut))
    (gen/such-that #(>= (count %) min-len) gen/string-alphanumeric)))

(defn gen-email
  "Простейший генератор email-адреса. Игнорируем minLength/maxLength,
   но по желанию можно усложнить формат."
  [min-len max-len]
  (gen/fmap
    (fn [[user domain tld]]
      (let [email (str user "@" domain "." tld)]
        (subs email 0 (min (count email) max-len))))
    (gen/tuple
      (gen/such-that #(>= (count %) (max 1 (dec min-len))) gen/string-alphanumeric)
      (gen/such-that #(pos? (count %)) gen/string-alphanumeric)
      (gen/elements ["com" "org" "net" "ru" "test"]))))

(defn gen-string
  "Генерирует строку с учётом minLength, maxLength и формата (например, email)."
  [schema]
  (let [min-length (or (get schema :minLength) 0)
        max-length (or (get schema :maxLength) (max 10 min-length)) ; чтобы max >= min
        format     (get schema :format)]
    (case format
      "email" (gen-email min-length max-length)
      (gen-random-string min-length max-length))))

(defn gen-object
  "Принимает карту (properties) вида {\"propertyName\" propertySchema, ...}
   Возвращает генератор, создающий Clojure map:
   {:propertyName <случайное значение>}."
  [properties]
  (let [
        prop-gens
        (map
          (fn [[prop-name prop-spec]]
            (let [t (:type prop-spec)]
              [prop-name
               (case t
                 "integer" (gen-integer prop-spec)
                 "boolean" (gen-boolean)
                 "string"  (gen-string prop-spec)
                 (gen/return nil))]))
          properties)]
    (apply gen/hash-map (mapcat identity prop-gens))))

(defn gen-schema
  "Рекурсивно обходит schema и возвращает генератор данных для неё."
  [schema]
  (let [t (:type schema)]
    (case t
      "object"
      (let [props (:properties schema {})]
        (gen-object props))

      "integer"
      (gen-integer schema)

      "boolean"
      (gen-boolean)

      "string"
      (gen-string schema)

      (gen/return nil))))

(defn gen-example
  ([schema count]
   (gen/sample (gen-schema schema) count))
  ([schema]
   (first (gen-example schema 1))))



(def example-spec
  {:type "object"
   :properties
   {:age      {:type "integer" :minimum 18 :maximum 80}
    :email    {:type "string" :format "email" :minLength 6 :maxLength 30}
    :isActive {:type "boolean"}
    :nickname {:type "string" :minLength 3 :maxLength 10}}})

(comment
  (let [gen-example (gen-schema example-spec)]
    (doseq [value (gen/sample gen-example 1)]
      (prn value))))

(comment
  (gen/sample (gen-schema example-spec) 1)
  (gen/sample (gen-integer {:minimum 10 :maximum 300}) 1)
  (gen/sample (gen-random-string 2 10))
  (gen/sample gen/string-alphanumeric 5)
  (gen/sample (gen/such-that #(>= (count %) 2) gen/string-alphanumeric) 5)
  (gen/sample (gen-string {:minLength 3 :maxLength 10}))
  (gen/vector gen/char-alphanumeric 2 4)
  )
