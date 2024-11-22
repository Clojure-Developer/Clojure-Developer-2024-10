(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method


(defn init-square [start-x]
  {[start-x 0] 1})

(defn shift [[x y] square n]
  (loop [next-y (mod (inc y) n)]
    (if (square [x next-y])
      (recur (mod (inc next-y) n))
      [x next-y])))

(defn calc-next-pos [[x y] n]
  [(mod (inc x) n) (mod (dec y) n)])

(defn fill-cell [square val prev-pos n]
  (if (> val (* n n))
    square
    (let [nex-pos (calc-next-pos prev-pos n)
          shifted-pos (if (square nex-pos) (shift prev-pos square n) nex-pos)]
      (fill-cell (assoc square shifted-pos val) (inc val) shifted-pos n))))

(defn map-to-vector [m n]
  (vec (for [y (range n)]
         (vec (for [x (range n)]
                (m [x y]))))))

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  (let [start-x (quot n 2)
        square (init-square start-x)]
    (map-to-vector (fill-cell square 2 [start-x 0] n) n)))


(comment
  (mod 2 3)
  (init-square 3)
  (quot 5 2)
  (get {[0 1] 1 [1 1] 2 [2 2] 3} [1 1])
  (get {[0 1] 1 [1 1] 2 [2 2] 3} [1 2])
  (calc-next-pos [2 0] 3)
  (calc-next-pos [2 2] 3)
  (calc-next-pos [0 0] 3)
  (calc-next-pos [0 1] 3)
  (shift [1 2] {} 3)
  (shift [1 2] {[1 0] 3} 3)
  (magic-square 3)
  (magic-square 5)
  (magic-square 1))