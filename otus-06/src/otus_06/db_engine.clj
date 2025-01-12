(ns otus-06.db-engine
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defonce db
  (atom {}))

(defn safe-parse-int [s]
  (cond
    (number? s)
    s
    (nil? s)
    0
    :default (try
               (Integer/parseInt s)
               (catch Exception e
                 (println "Warning: Unable to parse to int:" s " - defaulting to 0")
                 0))))                                      ;; Возвращаем 0 по умолчанию

(defn safe-parse-double [s]
  (cond
    (number? s)
    (double s)
    (nil? s)
    0.0
    :default (try
               (Double/parseDouble s)
               (catch Exception e
                 (println "Warning: Unable to parse to double:" s " - defaulting to 0")
                 0.0))))

(comment
  (safe-parse-int "3")
  (safe-parse-int 3))


(def data-converters
  {:int #(safe-parse-int %)
   :double #(safe-parse-double %)
   :string identity})



(defn load-whole-data [file-path schema]
  (with-open [reader (io/reader file-path)]
    (mapv (fn [line]
           (let [values (str/split line #"\|")
                 columns (map first schema)]
             (zipmap columns
                     (map (fn [[_ type] value] ((type data-converters) value)) schema values))))
         (line-seq reader))))


(defn init-db [config]
  "config is a vector of vectors [table-name path schema]"
  (let [loaded-data (reduce (fn [acc [table-name path schema]]
                              (assoc acc table-name (load-whole-data path schema))) {} config)]
    (reset! db loaded-data)))



(defn
  load-data [table-key]
  (table-key @db))

(defn get-join-func [type index left-key]
  (case type
    :left (fn [row] (or (get index (left-key row)) [{}]))
    :right (fn [row] (or (get index (left-key row)) [{}]))
    ;; Default to inner join
    (fn [row] (get index (left-key row)))))

;; Process joins
(defn apply-joins [dataset joins]
  (if joins
    (reduce
      (fn [current-dataset {:keys [table on type]}]
        (let [join-table (load-data table)
              [left-key right-key] on
              index (group-by right-key join-table)
              join-fn (get-join-func type index left-key)]
          (for [row current-dataset
                join-row (join-fn row)
                :when join-row]
            (merge row join-row))))
      dataset
      joins)
    dataset))

(defn apply-where [dataset predicate]
  (if predicate
    (filter predicate dataset)
    dataset))

(defn apply-group-by [dataset group-cols]
  (if group-cols
    (group-by (fn [row] (select-keys row group-cols)) dataset)
    dataset))

(def row-aggregate-functions
  {:product (fn [row [key1 key2]]
              (* (key1 row) (key2 row)))})

(defn apply-row-aggregates [data row-aggregates]
  (if (nil? row-aggregates)
    data
    (mapv (fn [row]
            (reduce (fn [acc [agg-name keys alias]]
                      (assoc acc alias ((agg-name row-aggregate-functions) acc keys)))
                    row
                    row-aggregates))
          data)))

(comment
  (apply-row-aggregates
    [{:cost 3 :items 4}
     {:cost 3 :items 2}]
    [[:product [:cost :items] :total]]))

(def group-aggregate-functions
  {:sum      (fn [rows [key]]
               (reduce + (map #(get % key 0) rows)))
   :product  (fn [rows [key1 key2]]
               (reduce (fn [acc row]
                         (+ acc (* (get row key1 1) (get row key2 1))))
                       0
                       rows))
   :average  (fn [rows [key]]
               (if (seq rows)
                 (/ (reduce + (map #(get % key 0) rows))
                    (count rows))
                 0))
   :max      (fn [rows [key]]
               (reduce (fn [a b]
                         (if (> (get a key 0) (get b key 0)) a b))
                       rows))
   :min      (fn [rows [key]]
               (reduce (fn [a b]
                         (if (< (get a key 0) (get b key 0)) a b))
                       rows))
   :first    (fn [rows [key]]
               (get (first rows) key))
   :last     (fn [rows [key]]
               (get (last rows) key))
   :identity identity
   ;; Добавьте другие необходимые агрегаты
   })


(defn apply-group-aggregates [grouped-data group-aggregates]
  (if (nil? group-aggregates)
    grouped-data
    (mapv (fn [[group rows]]
            (let [aggregates (reduce (fn [acc [agg-name keys alias]]
                                       (let [agg-fn (get group-aggregate-functions agg-name)]
                                         (if agg-fn
                                           (assoc acc alias (agg-fn rows keys))
                                           (throw (ex-info (str "Unknown group aggregate function: " agg-name) {})))))
                                     {}
                                     group-aggregates)]
              (merge group aggregates))
            ) grouped-data)))


(defn apply-select [data select-cols]
  (mapv (fn [row]
          (select-keys row select-cols))
        data))

(defn add-record [db table-key record]
  (swap! db update table-key conj record))

(defn exec-query [query]
  (-> (load-data (:from query))
      (apply-joins (:join query))
      (apply-row-aggregates (:row-aggregate query))
      (apply-where (:where query))
      (apply-group-by (:group-by query))
      (apply-group-aggregates (:group-aggregate query))
      (apply-select (:select query))))



(comment
  (load-data :customers)
  (map #(select-keys % [:id :customer-id]) (filter #(= (:customer-id %) "2") (:sales @db)))
  (group-by :customer-id (load-data :sales))
  (apply-joins (load-data :customers) [{:table :sales :on [:customer-id :customer-id]}])
  (apply-where (load-data :sales) #(= (:customer-id %) "2"))
  (apply-group-by (load-data :sales) [:customer-id])
  (select-keys (first (load-data :customers)) [:customer-id :name])
  (apply-select (load-data :customers) [:customer-id :name]))
