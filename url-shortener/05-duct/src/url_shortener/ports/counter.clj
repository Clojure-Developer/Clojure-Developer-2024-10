(ns url-shortener.ports.counter)

(defprotocol Counter
  (next-value [this]))
