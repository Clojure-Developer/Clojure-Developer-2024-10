(ns otus-04.homework.magic-square)

(defn index-overflow-creator [n] (partial < (dec n)))
(defn next-coord [sq-vec curr-coord] [sq-vec curr-coord])

(defn magic-square-iterate
  [n m sq-vec coord]
  (let [square-size n]

    [n m sq-vec coord square-size next-coord]))

(defn magic-square
  [n]
  {:pre [(odd? n)]}

  (let [magic-vector (vec (repeat n (vector)))
        initial-index (quot n 2)
        coordinates {:x 0 :y initial-index}]

    [coordinates magic-vector]))

(comment
  (magic-square 9)
  (get [1 2 3 4 5 6 7 8 9] 4)
  (def n 3)
  (def index-overflow? (index-overflow-creator n))
  (index-overflow? 3)
  (defn iterate-fn [n m]
    (if (= m 0)
      n
      (iterate-fn (* n 2) (dec m))))

  (= (iterate-fn 1 10) 1024)
)
