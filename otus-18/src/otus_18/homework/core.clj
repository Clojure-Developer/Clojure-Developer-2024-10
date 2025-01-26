(ns otus-18.homework.core
  (:require [clojure.core.async :as a :refer [<! <!! go]]
            [otus-18.homework.requests :as r]))

(def pokemons-seq (r/pokemons-seq))
(def types (r/pokemon-types-seq))

(defn extract-pokemon-name [pokemon]
  (:name pokemon))

(defn extract-pokemon-types-url [pokemon]
  (let [p (r/fetch-pokemon (:url pokemon))]
    (map #(get-in % [:type :url]) (:types p))))


(defn extract-type-name [pokemon-type lang]
  (->> (:names pokemon-type)
       (filter (fn [type-name] (= lang (-> type-name :language :name))))
       (first)
       :name))

(defn process-type [url lang]
  (extract-type-name (r/fetch-type url) lang))

(defn process-pokemon [pokemon lang]
  (let [pokemon-name (extract-pokemon-name pokemon)
        type-urls (extract-pokemon-types-url pokemon)
        types (map #(process-type % lang) type-urls)]
    (println types)
    {:name  pokemon-name
     :types types}))


(defn get-pokemons [limit lang]
  (let [pokemons (take limit pokemons-seq)]
    (map #(process-pokemon % lang) pokemons)))

(comment
  (take 10 types)
  (get-pokemons 4 "en")
  (take 2 pokemons-seq)
  )


;;; async solution

(defn process-pokemon-async [pokemon lang]
  (let [pokemon-name (extract-pokemon-name pokemon)
        type-urls (extract-pokemon-types-url pokemon)
        type-channels (mapv #(go (process-type % lang)) type-urls)
        types-chan (a/into [] (a/merge type-channels))]
    (go
      (let [types (<! types-chan)]
        {:name  pokemon-name
         :types types}))))

(defn get-pokemons-async [limit lang]
  (let [pokemons-ch (go (take limit pokemons-seq))]
    (go
      (let [pokemos (<! pokemons-ch)
            processed-ch (mapv #(process-pokemon-async % lang) pokemos)]
        (<! (a/into [] (a/merge processed-ch)))))))

(comment
  (let [p (first pokemons-seq)]
    (<!! (process-pokemon-async p "en")))

  (<!! (get-pokemons-async 10 "en")))

(comment
  (retrieve-pokemons! 4 "en")

  (let [c (go
            (process-type "https://pokeapi.co/api/v2/type/5/" "en"))]
    (<!! c))

  )
