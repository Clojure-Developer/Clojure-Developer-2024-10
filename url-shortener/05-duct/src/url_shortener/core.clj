(ns url-shortener.core
  (:require [clojure.string :as string]
            [url-shortener.ports.url-db :as url-db]
            [url-shortener.ports.counter :as counter]
            [url-shortener.ports.use-case :as urls]
            [integrant.core :as ig]))

;; Consts
(def ^:const alphabet-size 62)

(def ^:const alphabet
  "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")

;; Logic
(defn- get-idx [i]
  (Math/floor (/ i alphabet-size)))

(defn- get-character-by-idx [i]
  (get alphabet (rem i alphabet-size)))

(defn int->id [id]
  (if (< id alphabet-size)
    (str (get-character-by-idx id))
    (let [codes (->> (iterate get-idx id)
                     (take-while pos?)
                     (map get-character-by-idx))]
      (string/join (reverse codes)))))

(comment
  (int->id 0)               ; => "0"
  (int->id alphabet-size)   ; => "10"
  (int->id 9999999999999)   ; => "2q3Rktod"
  (int->id Long/MAX_VALUE)) ; => "AzL8n0Y58W7"


(defn id->int [id]
  (reduce (fn [id ch]
            (+ (* id alphabet-size)
               (string/index-of alphabet ch)))
          0
          id))

(comment
  (id->int "0")        ; => 0
  (id->int "z")        ; => 61
  (id->int "clj")      ; => 149031
  (id->int "Clojure")) ; => 725410830262

(defn- shorten*
  ([db url]
   (let [id (int->id (counter/next-value db))]
     (or (shorten* db id url)
         (recur db url))))
  ([db id url]
   (url-db/save db id url)))

(defrecord ShortenerImpl [db]
  urls/Shortener

  (shorten [_ url]
    (shorten* db url))
  (shorten [_ id url]
    (shorten* db id url))
  (url-for [_ id]
    (url-db/find-by-id db id))
  (list-all [_]
    (url-db/list-all db)))

(defmethod ig/init-key ::shortener
  [_ {:keys [db]}]
  (->ShortenerImpl db))


;; Now we can tests our business logic with simple mocks.
(comment
  (defrecord MockDBBoundary [spec])
  
  (extend-protocol url-db/UrlsDB
    MockDBBoundary

    (list-all [{db :spec}]
      (:urls @db))

    (find-by-id [{db :spec} id]
      (get (:urls @db) id))

    (save [{db :spec} id url]
      (when-not (contains? (:urls @db) id)
        (swap! db update :urls assoc id url))))
  
  (extend-protocol counter/Counter
    MockDBBoundary

    (next-value [{db :spec}]
      (swap! db update :counter inc)
      (:counter @db)))

  (let [db-mock   (->MockDBBoundary (atom {:urls {} :counter 0}))
        shortener (->ShortenerImpl db-mock)]
    (urls/shorten shortener "https://steamcommunity.com/id/megoRU")
    (urls/shorten shortener "clj" "https://clojure.ru")
    (urls/shorten shortener "clj" "duplicate")
    (urls/url-for shortener "clj"))
  )
