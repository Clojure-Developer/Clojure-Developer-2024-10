(ns url-shortener.ports.url-db)

(defprotocol UrlsDB
  (list-all   [this])
  (find-by-id [this id])
  (save       [this id url]))
