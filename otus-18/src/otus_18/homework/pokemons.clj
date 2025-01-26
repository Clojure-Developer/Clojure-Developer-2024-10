(ns otus-18.homework.pokemons
  (:require [clojure.core.async :as a :refer [<!!]]
            [otus-18.homework.core :as c]))

(defn get-pokemons
  "Асинхронно запрашивает список покемонов и название типов в заданном языке. Возвращает map, где ключами являются
  имена покемонов (на английском английский), а значения - коллекция названий типов на заданном языке."
  [& {:keys [limit lang] :or {limit 50 lang "ja"}}]
  (println (<!! (c/get-pokemons-async limit lang))))

(comment (println (get-pokemons)))