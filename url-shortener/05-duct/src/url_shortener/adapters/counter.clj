(ns url-shortener.adapters.counter
  (:require [duct.database.sql]
            [next.jdbc.sql :as jdbc.sql]
            [url-shortener.ports.counter :refer [Counter]]))

(def ^:private next-val-sql
  ["select nextval('counter')"])

(extend-protocol Counter
  duct.database.sql.Boundary

  (next-value [{db :spec}]
    (-> (jdbc.sql/query db next-val-sql)
        (first)
        :nextval)))
