(ns otus-18.homework.requests
  (:require [clj-http.client :as c]
            [clojure.core.memoize :as memo]))

(def ^:private base-url     "https://pokeapi.co/api/v2")
(def ^:private pokemons-url (str base-url "/pokemon"))
(def ^:private type-path    (str base-url "/type"))

(defn- fetch-page
  [url]
  (println (str "Fetching: " url))
  (try
    (let [response (c/get url {:as :json})
          {:keys [count next previous results]} (:body response)]
      {:url url
       :count count
       :next next
       :previous previous
       :results results})
    (catch Exception e
      (throw (ex-info "Failed to fetch page"
                      {:url url
                       :error (.getMessage e)})))))

(defn- fetch-item [url]
  (println (str "Fetching: " url))
  (try
    (:body (c/get url {:as :json}))
    (catch Exception e
      (throw (ex-info "Failed to fetch item"
                      {:url url
                       :error (.getMessage e)})))))
(def fetch-item-memoized
  "Memoized version of fetch-item with time-based caching."
  (memo/ttl fetch-item
            :ttl/threshold 3600000))


(def fetch-page-memoized
  "Memoized version of fetch-page with time-based caching."
  (memo/ttl fetch-page
            :ttl/threshold 3600000)) ; 1-hour cache in milliseconds

(defn fetch-pokemon [url]
  (fetch-item-memoized url))

(defn fetch-type [url]
  (fetch-item-memoized url))

(defn pokemon-types-seq
  ([]
   (pokemon-types-seq type-path))
  ([initial-url]
   (let [page (fetch-page-memoized initial-url)]
     (lazy-seq
       (concat
         (:results page)
         (when-let [next-url (:next page)]
           (pokemon-types-seq next-url)))))))

(defn pokemons-seq ([]
                (pokemons-seq pokemons-url))
  ([initial-url]
   (let [page (fetch-page-memoized initial-url)]
     (lazy-seq
       (concat
         (:results page)
         (when-let [next-url (:next page)]
           (pokemons-seq next-url)))))))

(comment
  (filter #(= "shadow" (:name %)) (take 50 (pokemon-types-seq)))

  (take 1 (pokemons-seq))

  (:types (fetch-pokemon "https://pokeapi.co/api/v2/pokemon/1/"))

  (:names (fetch-item-memoized "https://pokeapi.co/api/v2/type/4/"))

  (:names (:body (c/get "https://pokeapi.co/api/v2/type/10002/" {:as :json})))

  (fetch-pokemon "https://pokeapi.co/api/v2/pokemon/44/")
  )