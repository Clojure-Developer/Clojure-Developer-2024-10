(ns otus-04.homework.magic-square)

(defn next-index-row-factory
  [n]
  (fn [index] (if (= index 0) (dec n) (dec index))))

(defn next-index-col-factory
  [n]
  (fn [index] (if (= index (dec n)) 0 (inc index))))

(defn next-coords
  [next-index-col next-index-row {:keys [col row]}]
  {:row (next-index-row row)
   :col (next-index-col col)})

(defn build-square-map
  [square-map coords next-coords-fn value square-size]
  (if (zero? square-size)
    square-map
    (let [new-value (inc value)
          next-map (assoc square-map coords new-value)
          next-coords (next-coords-fn coords)
          cell-empty? (nil? (get next-map next-coords))
          next-coords (if cell-empty? next-coords {:row (inc (:row coords)) :col (:col coords)})]

      (build-square-map next-map next-coords next-coords-fn new-value (dec square-size)))))

(defn repeat-n-vec
  [item n]
  (->> item
       (repeat n)
       (vec)))

(defn empty-square-vec
  [n]
  (repeat-n-vec (repeat-n-vec nil n) n))

(defn magic-square
  [n]
  {:pre [(odd? n)]}

  (let [initial-index (quot n 2)
        initial-coords {:row 0 :col initial-index}
        initial-value 0
        next-index-col (next-index-col-factory n)
        next-index-row (next-index-row-factory n)
        next-coords-fn (partial next-coords next-index-col next-index-row)
        empty-vec (empty-square-vec n)
        square-map (build-square-map {} initial-coords next-coords-fn initial-value (* n n))]

    (reduce (fn [acc {:keys [row col] :as val}]
              (let [square-val (get square-map val)
                    square-row (nth acc row)
                    new-row (assoc square-row col square-val)]

                (assoc acc row new-row)))
            empty-vec (keys square-map))))
