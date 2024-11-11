(ns otus-02.homework.common-child)


;; Строка называется потомком другой строки,
;; если она может быть образована путем удаления 0 или более символов из другой строки.
;; Буквы нельзя переставлять.
;; Имея две строки одинаковой длины, какую самую длинную строку можно построить так,
;; чтобы она была потомком обеих строк?

;; Например 'ABCD' и 'ABDC'

;; Эти строки имеют два дочерних элемента с максимальной длиной 3, ABC и ABD.
;; Их можно образовать, исключив D или C из обеих строк.
;; Ответ в данном случае - 3

;; Еще пример HARRY и SALLY. Ответ будет - 2, так как общий элемент у них AY

;    A B C D
;  0 0 0 0 0
;A 0 1 1 1 1
;B 0 1 2 2 2
;D 0 1 2 2 3
;C 0 1 2 3 3

;  A D
;A
;B

(defn common-child-length [s1 s2]
  (let [memo-lcs (memoize
                   (fn lcs [i j]
                     (println (str i " " j))
                     (println (str (nth s1 (dec i) "-") " " (nth s2 (dec j) "-")))
                     (cond
                       (or (zero? i) (zero? j)) 0
                       (= (nth s1 (dec i)) (nth s2 (dec j)))
                       (inc (lcs (dec i) (dec j)))
                       :else
                       (max (lcs i (dec j)) (lcs (dec i) j)))))]
    (memo-lcs (count s1) (count s2))))

(defn common-child-length-2 [s1 s2]
  (let [n (count s1)
        m (count s2)
        dp (vec (repeat (inc n) (vec (repeat (inc m) 0))))]
    (loop [i 1 dp dp]
      (if (> i n)
        (get-in dp [n m])
        (recur (inc i)
               (loop [j 1 dp dp]
                 (if (> j m)
                   dp
                   (recur (inc j)
                          (assoc-in dp [i j]
                                    (if (= (nth s1 (dec i)) (nth s2 (dec j)))
                                      (inc (get-in dp [(dec i) (dec j)]))
                                      (max (get-in dp [(dec i) j])
                                           (get-in dp [i (dec j)]))))))))))))