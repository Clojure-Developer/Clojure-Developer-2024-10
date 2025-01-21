(ns otus-16.clojure-async
  (:require [clojure.core.async :refer [chan <!! >!! >! <! thread close! go pipeline go-loop]]
            [otus-16.file-utils :as f]
            [otus-16.collectors :as c]))

(def channels (atom {}))
(def result-ch (atom nil))

(defn close-ch []
  (doseq [ch (vals @channels)]
    (close! ch)))

(defn reset-ch []
  (close-ch)
  (reset! channels {:file  (chan 100)
                    :lines (chan 100 (map c/parse-log-line))}))

(defn push-file [file]
  (go
    (>! (:file @channels) file)))


(defn process-files []
  (go
    (loop []
      (when-let [f (<! (:file @channels))]
        (go
          (println (str "Process file: " f))
          (f/with-file f lines
                       (doseq [l lines] (>! (:lines @channels) l)))))
      (recur))))

(defn collect-data [reducer]
  (clojure.core.async/reduce reducer {} (:lines @channels)))

(defn solution []
  (let [files (f/files "C:\\Users\\Anton\\IdeaProjects\\Clojure-Developer-2024-10\\otus-16\\static\\apache_logs.txt")]
    (reset-ch)
    (process-files)
    (doseq [f files] (push-file f))
    (reset! result-ch (collect-data (c/create-collectors)))))


(comment

  (solution)
  (close-ch)
  (println (<!! @result-ch))

  )


