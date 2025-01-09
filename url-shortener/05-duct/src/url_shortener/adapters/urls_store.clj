(ns url-shortener.adapters.urls-store
  (:require [duct.database.sql]
            [honey.sql :as sql]
            [next.jdbc.sql :as jdbc.sql]
            [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
            [url-shortener.ports.url-db :refer [UrlsDB]])
  (:import [org.h2.jdbc JdbcSQLIntegrityConstraintViolationException]))

(def ^:private all-urls-sql
  (sql/format {:select [:id :url]
               :from :urls}))

(extend-protocol UrlsDB
  duct.database.sql.Boundary

  (list-all [{db :spec}]
    (jdbc.sql/query db all-urls-sql {:builder-fn as-unqualified-lower-maps}))
  
  (find-by-id [{db :spec} id]
    (-> (jdbc.sql/get-by-id db :urls id)
        :urls/url))
  
  (save [{db :spec} id url]
    (try
      (-> (jdbc.sql/insert! db :urls {:id id :url url})
          :urls/id)
      (catch JdbcSQLIntegrityConstraintViolationException _
        nil))))
