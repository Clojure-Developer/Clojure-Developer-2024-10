(ns url-shortener.ports.use-case)

(defprotocol Shortener
  (shorten  [this url] [this id url])
  (url-for  [this id])
  (list-all [this]))
