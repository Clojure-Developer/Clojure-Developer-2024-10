(ns otus-16.clojure-reducers
  (:require [clojure.core.reducers :as r]
            [otus-16.file-utils :as f]
            [otus-16.collectors :as c]))

(defn process-line [reducer]
  (fn
    ([] [])
    ([acc line] (reducer acc (c/parse-log-line line)))))


(defn process-file [reducer file batch-size]
  (f/with-file file lines
               (r/fold
                 batch-size
                 c/merge-result-reducer
                 (process-line reducer)
                 lines)))

(defn solution [& {:keys [url referrer]
                   :or   {}}]
  (let [reducers (c/create-collectors {:url url :referrer referrer})
        result (->>
                 (f/files "C:\\Users\\Anton\\IdeaProjects\\Clojure-Developer-2024-10\\otus-16\\static")
                 (pmap #(process-file reducers % 50))
                 (reduce c/merge-result-reducer))]
    (println result)))

(comment
  (solution)

  )
