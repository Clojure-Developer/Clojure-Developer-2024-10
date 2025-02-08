(ns otus-06.homework
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

;; Загрузить данные из трех файлов на диске.
;; Эти данные сформируют вашу базу данных о продажах.
;; Каждая таблица будет иметь «схему», которая указывает поля внутри.
;; Итак, ваша БД будет выглядеть так:

;; cust.txt: это данные для таблицы клиентов. Схема:
;; <custID, name, address, phoneNumber>

;; Примером файла cust.txt может быть:
;; 1|John Smith|123 Here Street|456-4567
;; 2|Sue Jones|43 Rose Court Street|345-7867
;; 3|Fan Yuhong|165 Happy Lane|345-4533

;; Каждое поле разделяется символом «|». и содержит непустую строку.

;; prod.txt: это данные для таблицы продуктов. Схема
;; <prodID, itemDescription, unitCost>

;; Примером файла prod.txt может быть:
;; 1|shoes|14.96
;; 2|milk|1.98
;; 3|jam|2.99
;; 4|gum|1.25
;; 5|eggs|2.98
;; 6|jacket|42.99

;; sales.txt: это данные для основной таблицы продаж. Схема:
;; <salesID, custID, prodID, itemCount>.
;;
;; Примером дискового файла sales.txt может быть:
;; 1|1|1|3
;; 2|2|2|3
;; 3|2|1|1
;; 4|3|3|4

;; Например, первая запись (salesID 1) указывает, что Джон Смит (покупатель 1) купил 3 пары обуви (товар 1).

;; Задача:
;; Предоставить следующее меню, позволяющее пользователю выполнять действия с данными:

;; *** Sales Menu ***
;; ------------------
;; 1. Display Customer Table
;; 2. Display Product Table
;; 3. Display Sales Table
;; 4. Total Sales for Customer
;; 5. Total Count for Product
;; 6. Exit

;; Enter an option?


;; Варианты будут работать следующим образом

;; 1. Вы увидите содержимое таблицы Customer. Вывод должен быть похож (не обязательно идентичен) на

;; 1: ["John Smith" "123 Here Street" "456-4567"]
;; 2: ["Sue Jones" "43 Rose Court Street" "345-7867"]
;; 3: ["Fan Yuhong" "165 Happy Lane" "345-4533"]

;; 2. То же самое для таблицы prod.

;; 3. Таблица продаж немного отличается.
;;    Значения идентификатора не очень полезны для целей просмотра,
;;    поэтому custID следует заменить именем клиента, а prodID — описанием продукта, как показано ниже:
;; 1: ["John Smith" "shoes" "3"]
;; 2: ["Sue Jones" "milk" "3"]
;; 3: ["Sue Jones" "shoes" "1"]
;; 4: ["Fan Yuhong" "jam" "4"]

;; 4. Для варианта 4 вы запросите у пользователя имя клиента.
;;    Затем вы определите общую стоимость покупок для этого клиента.
;;    Итак, для Сью Джонс вы бы отобразили такой результат:
;; Sue Jones: $20.90

;;    Это соответствует 1 паре обуви и 3 пакетам молока.
;;    Если клиент недействителен, вы можете либо указать это в сообщении, либо вернуть $0,00 за результат.

;; 5. Здесь мы делаем то же самое, за исключением того, что мы вычисляем количество продаж для данного продукта.
;;    Итак, для обуви у нас может быть:
;; Shoes: 4

;;    Это представляет три пары для Джона Смита и одну для Сью Джонс.
;;    Опять же, если продукт не найден, вы можете либо сгенерировать сообщение, либо просто вернуть 0.

;; 6. Наконец, если выбрана опция «Выход», программа завершится с сообщением «До свидания».
;;    В противном случае меню будет отображаться снова.


;; *** Дополнительно можно реализовать возможность добавлять новые записи в исходные файлы
;;     Например добавление нового пользователя, добавление новых товаров и новых данных о продажах


;; Файлы находятся в папке otus-06/resources/homework
(defn- split-line [line]
  (string/split line #"\|"))

(defn- fmt-line [line]
  (-> line
      split-line
      rest
      vec))
(def ^:private tables {:customer "resources/homework/cust.txt"
                       :product "resources/homework/prod.txt"
                       :sales "resources/homework/sales.txt"})
(defn- print-table [fmt-fn name]
  (with-open [rdr (io/reader (tables name))]
     (doseq [lines (line-seq rdr)]
       (println (fmt-fn lines)))))

(defn- table-value [table-key id & [value-col id-col]]
  (with-open [rdr (io/reader (tables table-key))]
    (loop [lines (line-seq rdr)]
      (let [line-vec (split-line (first lines))]
        (cond (= (line-vec (or id-col 0)) id) (line-vec value-col)
              (empty? (rest lines)) nil
              :else (recur (rest lines)))))))

(defn- fmt-sales-line [line]
  (let [line-vec (fmt-line line)
        [customer-id product-id count] line-vec]
    (vector (table-value :customer customer-id 1)
            (table-value :product product-id 1)
            count)))

(defn- get-product-price [id]
  (let [price-col 2]
    (parse-double (table-value :product id price-col))))

(defn- get-total-product-price [[_ product-id product-count-str]]
  (let [product-count (Integer/parseInt product-count-str)]
    (* (get-product-price product-id)
       product-count)))

(defn- get-total-product-count [[_ _ product-count]]
  (Integer/parseInt product-count))

(defn- print-total [name table-key get-total-value]
  (let [fomat-str {:customer "%s: %.2f"
                   :product "%s: %d"}
        sales-columns {:customer 0
                       :product 1}
        id (table-value table-key name 0 1)]
    (with-open [rdr (io/reader (tables :sales))]
      (loop [lines (line-seq rdr)
             total 0]
        (if (empty? lines)
          (println (format (fomat-str table-key) name total))
          (let [line (fmt-line (first lines))
                line-id (line (sales-columns table-key))
                total-product (get-total-value line)
                rest-lines (rest lines)]
            (recur rest-lines (if (= id line-id)
                                (+ total total-product)
                                total))))))))

(defn- print-menu []
  (println "
*** Sales Menu ***
------------------
1. Display Customer Table
2. Display Product Table
3. Display Sales Table
4. Total Sales for Customer
5. Total Count for Product
6. Exit

Enter an option?
"))

(defn- make-action [input]
  (cond (= input "1") (do (print-table fmt-line :customer)
                          true)
        (= input "2") (do (print-table fmt-line :product)
                          true)
        (= input "3") (do (print-table fmt-sales-line :sales)
                          true)
        (= input "4") (do (println "Input customer name:")
                          (print-total (read-line) :customer get-total-product-price)
                          true)
        (= input "5") (do (println "Input product name:")
                          (print-total (read-line) :product get-total-product-count)
                          true)
        (= input "6") nil
        :else true))

(defn -main []
  (print-menu)
  (loop [num (read-line)]
    (and (make-action num)
         (do (print-menu)
             (recur (read-line))))))

(comment
  (table-value :product "3" 2)
  (table-value :customer "Sue Jones" 0 1)
  (table-value :customer "3" 1)
  (print-table fmt-line :product)
  (print-table fmt-line :customer)
  (print-table fmt-line :sales)
  (print-total "Sue Jones" :customer get-total-product-price)
  (print-total "shoess" :product get-total-product-count)
  (-main)
  ())
