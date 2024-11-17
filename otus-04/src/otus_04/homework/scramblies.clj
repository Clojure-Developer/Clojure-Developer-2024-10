(ns otus-04.homework.scramblies)

;; 1-st attempt
;; первая попытка не учитывала что в letters может быть не достаточно букв для
;; построения word
(defn scramble?
  [letters word]
  (let [letters-set (set letters)]
    (every? true?
            (map (comp not nil? letters-set) word))))

;; 2-nd attempt
;; вторая попытка учитывает кол-во букв, но результат получаем двумя проходами по
;; последовательности ключей, решил заменить на один проход при помощи reduce
(defn reducer [acc val]
  (if (acc val)
    (update acc val inc)
    (assoc acc val 1)))

(defn word->map
  [word]
  (reduce reducer {} word))

(defn scramble?
  [letters word]
  (let [letters-map (word->map letters)
        word-map (word->map word)
        word-map-keys (keys word-map)]

    (every? false?
            (map #(< (letters-map %1 0) (word-map %1 0)) word-map-keys))))

;; 3-rd attempt
(defn scramble?
  [letters word]
  (let [letters-map (word->map letters)
        word-map (word->map word)
        word-map-keys (keys word-map)]

    (reduce #(if (< (letters-map %2 0) (word-map %2 0)) (not %1) %1) true word-map-keys)))
