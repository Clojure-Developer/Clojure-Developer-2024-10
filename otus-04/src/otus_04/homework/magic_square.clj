(ns otus-04.homework.magic-square)

(defn get-item
  [square-vec {:keys [x y]}]
  (let [row (nth square-vec y)]

    (nth row x)))

(defn set-item
  [square-vec item {:keys [x y]}]
  (let [row (nth square-vec y)
        new-row (assoc row x item)]

    (assoc square-vec y new-row)))

(defn repeat-n-vec
  [item n]
  (->> item
       (repeat n)
       (vec)))

(defn empty-square-creator
  [n]
  (repeat-n-vec (repeat-n-vec nil n) n))

(defn index-overflow-creator
  [n]
  (partial < (dec n)))

(defn inc-coords
  [{:keys [x y]} index-overflow?]

  {:x (if (index-overflow? (inc x)) 0 (inc x))
   :y (if (index-overflow? (inc y)) 0 (inc y))})

;; ============================================================

(defn magic-square-iterate [])

(defn magic-square
  [n]
  {:pre [(odd? n)]}

  (let [initial-vector  (empty-square-creator 3) ;;(vec (repeat n (vector)))
        initial-index (quot n 2)
        initial-coords {:y 0 :x initial-index}]

    (magic-square-iterate)))

(comment
  (magic-square 3)
  (get [1 2 3 4 5 6 7 8 9] 4)
  (def n 3)
  (def index-overflow? (index-overflow-creator n))
  (index-overflow? 3)
  (defn iterate-fn [n m]
    (if (= m 0)
      n
      (iterate-fn (* n 2) (dec m))))

  (= (iterate-fn 1 10) 1024)

  (vec (repeat n nil))

  ;; params destructuring

  (defn test [a b {:keys [x y]}]
    [x y])

  (test 1 2 {:x 2 :y 4}))

(comment
  (def test-vec-3 [[] [8 7 6] []])

  (def x 2)
  (def y 0)
  (def item 777)

  (assoc test-vec-3 x
         (assoc (get test-vec-3 y) y item))

  (def n 5)

  (def empty-square-vec
    (repeat-n-vec
     (repeat-n-vec nil n) n))
  empty-square-vec)

;;      x
;; [    |
;;  [1 22 15] ;; <- y
;;  [6 88 42]
;;  [0 54 65]
;; ]

(def n 3)

(def empty-square-vec
  (repeat-n-vec
   (repeat-n-vec nil n) n))

empty-square-vec

(def sample-vec
  (set-item
   (set-item
    empty-square-vec
    77 {:x 1 :y 0})
   42 {:x 2 :y 0}))

sample-vec
;; [[nil 77 42] [nil nil nil] [nil nil nil]]

(def index-overflow? (index-overflow-creator n))

(get-item sample-vec {:x 2 :y 0}) ;; 42

;; данная функция не подходит для итеративного процесса напрямую
;; вместо неё создаем в let диагональные-координаты и шаг-вниз-координаты
;; проверяем ячейку квадрата на nil и делаем выбор следующих координат

(defn next-coords
  [square-vec coords index-overflow?]
  ;; (let [x (coords :x)
  ;;       y (coords :y)]

  (let [new-diag-coords {}
        new-down-coords {}]

    (if (nil? (get-item square-vec new-diag-coords))
      (new-diag-coords) (new-down-coords))))

;; перенеси в let в функции next-coords
(def diag-coords {:x :y})
(def step-down-coords {:x :y})

(next-coords sample-vec {:x 1 :y 0} index-overflow?)

(index-overflow? 0) ;; false
(index-overflow? 2) ;; false
(index-overflow? 3) ;; true

(def test-a {:x 1 :y 0})

(inc-coords test-a index-overflow?)

;; (update test-a :a (if (index-overflow? (test-a :a)) * inc))

;; Создать hash-map с ключачи парами ячеек квадрата, а далее преобразовать в вектор векторов
(def test-x
  {{:x 0 :y 1} 1
   {:x 1 :y 2} 22})

(get test-x {:x 0 :y 1}) ; 1
(get test-x {:x 1 :y 4}) ; nil
