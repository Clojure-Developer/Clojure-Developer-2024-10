(ns otus-02.homework.square-code
  (:require [clojure.string :as string])
  (:require [clojure.math :as math])
  (:require [otus-02.homework.palindrome :as pal]))

;; Реализовать классический метод составления секретных сообщений, называемый `square code`.
;; Выведите закодированную версию полученного текста.

;; Во-первых, текст нормализуется: из текста удаляются пробелы и знаки препинания,
;; также текст переводится в нижний регистр.
;; Затем нормализованные символы разбиваются на строки.
;; Эти строки можно рассматривать как образующие прямоугольник при печати их друг под другом.

;; Например,
"If man was meant to stay on the ground, god would have given us roots."
;; нормализуется в строку:
"ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots"

;; Разбиваем текст в виде прямоугольника.
;; Размер прямоугольника (rows, cols) должен определяться длиной сообщения,
;; так что c >= r и c - r <= 1, где c — количество столбцов, а r — количество строк.
;; Наш нормализованный текст имеет длину 54 символа
;; и представляет собой прямоугольник с c = 8 и r = 7:
"ifmanwas"
"meanttos"
"tayonthe"
"groundgo"
"dwouldha"
"vegivenu"
"sroots  "

;; Закодированное сообщение получается путем чтения столбцов слева направо.
;; Сообщение выше закодировано как:
"imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"

;; Полученный закодированный текст разбиваем кусками, которые заполняют идеальные прямоугольники (r X c),
;; с кусочками c длины r, разделенными пробелами.
;; Для фраз, которые на n символов меньше идеального прямоугольника,
;; дополните каждый из последних n фрагментов одним пробелом в конце.
"imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau "

;; Обратите внимание, что если бы мы сложили их,
;; мы могли бы визуально декодировать зашифрованный текст обратно в исходное сообщение:

"imtgdvs"
"fearwer"
"mayoogo"
"anouuio"
"ntnnlvt"
"wttddes"
"aohghn "
"sseoau "


(defn trainling-spaces [s col]
  (format (str "%-" col "s") s))

(defn split-for-encode [col initial s]
    (cond
      (empty? s) initial
      (<= (count s) col) (conj initial (trainling-spaces s col))
      :else (recur col (conj initial (subs s 0 col)) (subs s col))))

(defn transpose-string-v [v]
  (->> (map vec v)
       (apply mapv vector)
       (map #(apply str %))))

(defn get-square-length [s]
  (-> s
      (count)
      (math/sqrt)
      (math/ceil)
      (int)))

(defn code-s [col s]
  (->> s
       (split-for-encode col [])
       (transpose-string-v)))

(defn encode-string [input]
  (let [s (pal/transform-string input)
        col (get-square-length s)]
    (->> s
         (code-s col)
         (string/join " "))))

(defn decode-string [input]
  (let [col (get-square-length input)]
    (->> input
         (code-s col)
         (string/join)
         (string/trim))))

(comment
  (def s "If man was meant to stay on the ground, god would have given us roots.")
  (decode-string (encode-string s))
  (trainling-spaces "some" 8)
  (math/sqrt (count s)))
