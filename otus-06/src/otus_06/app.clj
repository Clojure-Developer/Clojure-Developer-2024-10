(ns otus-06.app
  (:require [otus-06.db-engine :as db]
            [otus-06.queries :as q]
            [clojure.edn :as edn]))

(def migration (edn/read-string (slurp "resources/homework/db-migration.edn")))

(db/init-db migration)

(comment
  "Check the db is filled"
  (db/load-data :customers))

(defn print-table [data]
  (let [keys (keys (first data))]
    (if (not keys) (println "")
                   (do
                     (println (clojure.string/join "\t" keys))
                     (doseq [row data]
                       (println (clojure.string/join "\t" (map row keys))))))
    ))

(def actions
  {1 {:description "Display Customer Table"
      :action      (fn [] (print-table (db/exec-query q/select-customers-query)))}
   2 {:description "Display Product Table"
      :action      (fn [] (print-table (db/exec-query q/select-products-query)))}
   3 {:description "Display Sales Table"

      :action      (fn [] (print-table (db/exec-query q/select-sales-query)))}
   4 {:description "Total Sales for Customer"
      :action      (fn []
                     (do (println "Enter customer name:")
                         (let [name (read-line)]
                           (print-table (db/exec-query (q/select-total-for-customer-query name))))))}
   5 {:description "Total Count for Product"
      :action      (fn [] (do (println "Enter product description:")
                              (let [product (read-line)]
                                (print-table (db/exec-query (q/select-total-count-for-product product))))))}
   6 {:description "Exit"
      :action      #(println "Goodbye")}})



(defn print-menu []
  (println "*** Sales Menu ***")
  (println "------------------")
  (doseq [[k v] actions]
    (println (str k ". " (:description v))))
  (println "Enter an option?"))

(defn main-loop []
  (loop []
    (print-menu)
    (let [option (db/safe-parse-int (read-line))]
      (if-let [action (:action (get actions option))]
        (do (action) (when (not (= option 6)) (recur)))
        (do (println "Invalid option, please try again.") (recur))))))

(comment
  (main-loop))

(defn -main []
  (main-loop))