(ns otus-16.java-threads
  (:require [otus-16.file-utils :as f]
            [otus-16.collectors :as c])
  (:import (java.util.concurrent Executors Future)))


(defonce executor-atom (atom nil))

(defn init-executor! [threads]
  (reset! executor-atom (Executors/newFixedThreadPool ^Integer threads)))

(defn shutdown-executor! []
  (when-let [exec @executor-atom]
    (.shutdown exec)
    (reset! executor-atom nil)))

(defmacro submit-task [& body]
  `(let [task# (reify Callable
                 (call [_] ~@body))]
     (.submit @executor-atom task#)))

(defn process-lines-task [reducer lines]
  (submit-task (c/apply-pipeline reducer lines)))

(defn process-file-task [reducer lines batch-size]
  (submit-task
    (let [chunks (partition-all batch-size lines)
          results (map #(process-lines-task reducer %) chunks)]
      (mapv #(.get ^Future %) results))))

(defn process-file [reducer file batch-size]
  (f/with-file file lines
               (let [^Future f (process-file-task reducer lines batch-size)
                     result (.get f)]
                 result)))

(defn solution [& {:keys [url referrer]
                   :or   {}}]
  (init-executor! 16) ;; Initialize the global executor with 16 threads
  (try
    (let [reducers (c/create-collectors {:url url :referrer referrer})
          files (f/files "C:\\Users\\Anton\\IdeaProjects\\Clojure-Developer-2024-10\\otus-16\\static")
          results (map #(process-file reducers % 50) files)
          result (c/merge-results results)]
      (println result))
    (finally
      (shutdown-executor!)))) ;; Ensure the executor is shut down when done


(comment

  (solution)

  (with-executor 8 exec
                 (f/with-folder "C:\\Users\\Anton\\Downloads\\Telegram Desktop\\logs"
                                (fn [files]
                                  (let [futures (map #(submit-task exec (.getName %)) files)
                                        results (map #(.get %) futures)]
                                    (doseq [r results]
                                      (println r))))))

  (with-executor 8 exec
                 (let [files (f/files "C:\\Users\\Anton\\Downloads\\Telegram Desktop\\logs")]
                   (let [futures (map #(submit-task exec (.getName %)) files)
                         results (map #(.get %) futures)]
                     (doseq [r results]
                       (println r)))))

  (with-executor 16 exec
                 (let [reducers (c/combine-collectors [c/total-bytes-collector])
                       files (f/files "C:\\Users\\Anton\\IdeaProjects\\Clojure-Developer-2024-10\\otus-16\\static")
                       results (map #(process-file exec reducers % 50) files)
                       result (c/merge-results results)]
                   (println result)))
  )
