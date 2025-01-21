(ns otus-16.collectors)
(def log-line
  "127.0.0.1 - frank [10/Jan/2025:13:55:36 +0000] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\"")

(def log-pattern
  (re-pattern
    (str
      ;; IP адрес
      "^(\\S+) "                                            ;; %h: IP-адрес
      "(\\S+) "                                             ;; %l: идентификатор клиента (обычно "-")
      "(\\S+) "                                             ;; %u: имя пользователя (или "-")
      "\\[([^\\]]+)\\] "                                    ;; %t: дата/время в квадратных скобках
      "\"([^\"]+)\" "                                       ;; %r: HTTP-запрос в кавычках
      "(\\d{3}) "                                           ;; %>s: код состояния HTTP
      "(\\S+) "                                             ;; %b: размер ответа (или "-")
      "\"([^\"]*)\" "                                       ;; %{Referer}i: реферер в кавычках
      "\"([^\"]*)\"$")))                                    ;; %{User-Agent}i: пользовательский агент в кавычках

(defn parse-log-line-to-map [line]
  (when-let [matches (re-matches log-pattern line)]
    (zipmap [:ip :client-identity :user :timestamp :request :status :bytes :referer :user-agent]
            (rest matches))))

(defn parse-log-line [line]
  (when-let [matches (re-matches log-pattern line)]
    (rest matches)))

(defn to-int [v]
  (try
    (Integer/parseInt v)
    (catch Exception _ 0)))

(defn get-bytes [data]
  (to-int (nth data 6)))

(comment
  (get-bytes (parse-log-line log-line)))

(defn get-url [data]
  (let [arr (clojure.string/split (nth data 4) #"\s+")]
    (nth arr 1)))


(defn total-bytes-collector [acc data]
  (let [bytes (get-bytes data)]
    (update acc :total-bytes (fnil + 0) bytes)))

(defn bytes-by-url-collector [target-url]
  (fn [acc data]
    (when (= (get-url data) target-url)
      (update-in acc [:bytes-by-url target-url]
                 (fnil + 0) (get-bytes data)))))

(defn urls-by-refer-collectors [target-refer]
  (fn [acc data]
    (when (= (nth data 7) target-refer)
      (update-in acc [:urls-by-referrer target-refer] (fnil conj #{}) (get-url data)))))

(defn transform-urls-by-referrer [data]
  (update data :urls-by-referrer
          (fn [urls-by-referrer]
            (when urls-by-referrer
              (into {}
                    (map (fn [[referrer urls]]
                           [referrer (count urls)])
                         urls-by-referrer))))))

(defn combine-collectors [collectors]
  (fn
    ([] [])
    ([acc] (transform-urls-by-referrer acc))
    ([acc data] (reduce (fn [a collector] (collector a data)) acc collectors))))

(defn apply-pipeline [collectors lines]
  (transduce (map parse-log-line) collectors {} lines))

(defn merge-result-reducer
  ([] {})
  ([acc m]
   (-> acc
       (update :total-bytes + (:total-bytes m 0))
       (update :bytes-by-url merge (:bytes-by-url m {}))
       (update :urls-by-referrer merge (:urls-by-referrer m {})))))

(defn merge-results [maps]
  (reduce merge-result-reducer
          {:total-bytes 0, :bytes-by-url {}, :urls-by-referrer {}}
          (apply concat maps)))

(defn create-collectors [& {:keys [url referrer]}]
  (let [collectors (cond-> [total-bytes-collector]
                           url (conj (bytes-by-url-collector url))
                           referrer (conj (urls-by-refer-collectors referrer)))]
    (combine-collectors collectors)))




