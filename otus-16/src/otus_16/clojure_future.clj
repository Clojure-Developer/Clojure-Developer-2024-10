(ns otus-16.clojure-future
  (:require [otus-16.file-utils :as f]
            [otus-16.collectors :as c]))

(defn process-lines-task [reducer lines]
  (future (c/apply-pipeline reducer lines)))

(defn process-file-task [reducer lines batch-size]
  (future
    (let [chunks (partition-all batch-size lines)
          results (map #(process-lines-task reducer %) chunks)]
      (mapv #(deref %) results))))

(defn process-file [reducer file batch-size]
  (f/with-file file lines
               (let [f (process-file-task reducer lines batch-size)
                     result (.get f)]
                 result)))

(defn solution [& {:keys [url referrer]
                   :or   {}}]
  (let [reducers (c/create-collectors {:url url :referrer referrer})
        files (f/files "C:\\Users\\Anton\\IdeaProjects\\Clojure-Developer-2024-10\\otus-16\\static")
        results (map #(process-file reducers % 50) files)
        result (c/merge-results results)]
    (println result)))

(comment
  (solution))