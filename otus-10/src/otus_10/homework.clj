(ns otus-10.homework
  (:require [clojure.java.io :as io])
  (:import (java.io InputStream)
           (java.nio.charset StandardCharsets)))


(defn read-n-bytes
  [^InputStream is n]
  (let [buffer (byte-array n)]
    (if (= n (.read is buffer))
      buffer
      (throw (Exception. "Unexpected end of stream while reading")))))

(defn parse-size
  [size-bytes]
  (reduce
    (fn [acc byte]
      (+ (bit-shift-left acc 7) (bit-and byte 0x7F)))
    0
    size-bytes))


(defn read-id3-header
  [file-path]
  (with-open [is (io/input-stream file-path)]
    (let [header-bytes (read-n-bytes is 10)]
      (if (= (apply str (map char (take 3 header-bytes))) "ID3")
        (let [size (parse-size (take 4 (drop 6 header-bytes)))
              tag-data (vec (read-n-bytes is size))]
          {:header (vec header-bytes)
           :size   size
           :data   tag-data})
        (throw (Exception. "No ID3v2 tag found"))))))

(defn decode-text
  "0x00 - ISO-8859-1
  0x01 - UTF-16 (with BOM)
  0x02 - UTF-16BE (Big Endian, without BOM)
  0x03 - UTF-8"
  [encoding-byte data-bytes]
  (let [result (case encoding-byte
                 0x00 (String. (byte-array data-bytes) StandardCharsets/ISO_8859_1)
                 0x01 (String. (byte-array data-bytes) StandardCharsets/UTF_16)
                 0x02 (String. (byte-array data-bytes) StandardCharsets/UTF_16BE)
                 0x03 (String. (byte-array data-bytes) StandardCharsets/UTF_8)
                 (throw (Exception. (str "Unsupported encoding byte: " encoding-byte))))]
    (clojure.string/replace result "\u0000" "")))


;; Example usage
(comment
  (println (read-id3-header "sample.mp3"))
  (println (read-id3-header "sample-3s.mp3"))
  )


(defmulti decode-frame
          (fn [frame-id frame-bytes] frame-id))

(defmethod decode-frame "TALB" [_ frame-bytes]
  {:album (decode-text (first frame-bytes) (rest frame-bytes))})

(defmethod decode-frame "TPE1" [_ frame-bytes]
  {:artist (decode-text (first frame-bytes) (rest frame-bytes))})

(defmethod decode-frame "TIT2" [_ frame-bytes]
  {:title (decode-text (first frame-bytes) (rest frame-bytes))})

(defmethod decode-frame "TYER" [_ frame-bytes]
  {:year (decode-text (first frame-bytes) (rest frame-bytes))})

(defmethod decode-frame "TCON" [_ frame-bytes]
  {:genre (decode-text (first frame-bytes) (rest frame-bytes))})

(defmethod decode-frame :default [frame-id _]
  {:unknown-frame frame-id})

(defn parse-frames
  [data]
  (loop [remaining-data data
         frames {}]
    (if (or (empty? remaining-data)
            (< (count remaining-data) 10))
      frames
      (let [frame-id (apply str (map char (take 4 remaining-data)))
            size (parse-size (take 4 (drop 4 remaining-data)))
            frame-data (vec (take size (drop 10 remaining-data)))
            remaining (drop (+ 10 size) remaining-data)]
        (recur remaining
               (merge frames (decode-frame frame-id frame-data)))))))


(defn read-id3-tag
  [file-path]
  (let [{:keys [size data]} (read-id3-header file-path)]
    (parse-frames (take size data))))

(comment
  (let [file-path "sample-3s.mp3"]
    (println (read-id3-tag file-path))))