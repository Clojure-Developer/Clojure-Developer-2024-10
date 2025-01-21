(ns otus-16.file-utils
  (:require [clojure.java.io :as io]))


(defn files [^String folder]
  (filter #(not (.isDirectory %)) (file-seq (io/file folder))))

(defn with-folder [^String folder fn]
  (fn (files folder)))

(defmacro with-file [file binding & body]
  `(with-open [rdr# (clojure.java.io/reader ~file)]
     (let [~binding (line-seq rdr#)]
       ~@body)))

