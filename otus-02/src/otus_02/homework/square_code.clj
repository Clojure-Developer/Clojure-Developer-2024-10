(ns otus-02.homework.square-code
  (:require [clojure.string :as str]
            [clojure.string :as string]))

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



(defn get-colls-rows [input-length]
  (let [sqrt-len (Math/sqrt input-length)
        c (Math/ceil sqrt-len)
        r (Math/floor sqrt-len)]
    (if (> (* r c) input-length)
      [(int c) (int r)]
      [(int c) (int (inc r))])))

(comment
  (map #(vector % (get-colls-rows %)) (range 52 68 2)))

(defn normalize-str [input]
  (string/replace (string/lower-case input) #"[^\w]" ""))

(defn div-string [normalized c]
  (->> (partition-all c normalized)
       (map #(apply str %))
       (map #(str % (apply str (repeat (- c (count %)) " "))))))

(comment
  (partition-all 3 "aaasssdddf")
  (div-string "If man was meant to stay on the ground, god would have given us roots."))

(defn next-symbol [i c symbol]
  (if
   (and (not (= i (dec c))) (= symbol \space)) ""
   symbol))

(comment
  (next-symbol 2 4 \space))


(defn buid-str [c divided-str]
  (->> (range c)
       (map (fn [i] (apply str (map #(nth % i "") divided-str))))
       (apply str)
       normalize-str))

(defn encode-string [input]
  (let [normalized (normalize-str input)
        [c _] (get-colls-rows (count normalized))
        divided (div-string normalized c)]
    (buid-str c divided)))


(comment
  (encode-string "If man was meant to stay on the ground, god would have given us roots.")
  (count (encode-string "If man was meant to stay on the ground, god would have given us roots."))
  (div-string "imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau" 7))

(defn split-string [s index]
  [(subs s 0 index) (subs s index)])

(comment
  (split-string "aaaabbbb" 4))

(defn div-encoded-string [string c r]
  (let [short-rows-count (- (* c r) (count string))
        full-rows-count (- r short-rows-count)
        splited-str (split-string string (* full-rows-count c))
        full-rows (div-string (first splited-str) c)
        short-rows (div-string (second splited-str) (- c 1))
        ext-short-rows (map #(str % " ") short-rows)]
    (concat full-rows ext-short-rows)))

(comment
  (div-encoded-string "aaaabbbbcc" 4 3))

(defn decode-string [input]
  (let [normalized (normalize-str input)
        [c r] (get-colls-rows (count normalized))
        divided (div-encoded-string normalized r c)]
    (buid-str c divided)))

(comment
  (decode-string "imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"))

(defn encode-string-2 [input]
  (let [normalized (normalize-str input)
        [cols rows] (-> normalized count get-colls-rows)]
    (->> normalized
         (format (str "%-" (* rows cols) "s"))
         (partition cols)
         (apply map str)
         (str/join " "))))

(comment
  (encode-string-2 "If man was meant to stay on the ground, god would have given us roots.")
  (println (normalize-str "If man was meant to stay on the ground, god would have given us roots."))
  ;ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots
  (println (-> "ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots" count get-colls-rows))
  ;[8 7]
  (println (str "!" (format (str "%-" (* 8 7) "s") "ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots") "!"))
  (println (partition 8 "ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots  "))
  )
