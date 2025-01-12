(ns otus-06.app
  (:require [otus-06.db-engine :as db]
            [otus-06.queries :as q]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def migration
  (->
    "resources/homework/db-migration.edn"
    slurp
    edn/read-string))

(db/init-db migration)

(comment
  "Check the db is filled"
  (db/load-data :customers))

(defn print-table [data]
  (let [table-keys (keys (first data))]
    (if (not table-keys)
      (println "")
      (do
        (-> table-keys (str/join "\t") println)
        (doseq [row data]
          (->>
            (map row table-keys)
            (str/join "\t")
            println))))
    ))

(def actions
  {1 {:description "Display Customer Table"
      :action      (fn [] (-> q/select-customers-query db/exec-query print-table))}
   2 {:description "Display Product Table"
      :action      (fn [] (-> q/select-products-query db/exec-query  print-table))}
   3 {:description "Display Sales Table"

      :action      (fn [] (-> q/select-sales-query db/exec-query print-table))}
   4 {:description "Total Sales for Customer"
      :action      (fn []
                     (println "Enter customer name:")
                     (let [name (read-line)]
                       (-> name q/select-total-for-customer-query db/exec-query print-table)))}
   5 {:description "Total Count for Product"
      :action      (fn [] (println "Enter product description:")
                     (let [product (read-line)]
                       (-> product q/select-total-count-for-product db/exec-query print-table)))}
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
        (do (action) (when-not (= option 6) (recur)))
        (do (println "Invalid option, please try again.") (recur))))))

(comment
  (main-loop))

(defn -main []
  (main-loop))