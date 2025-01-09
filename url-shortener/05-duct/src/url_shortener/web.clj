(ns url-shortener.web
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty])
  (:import [org.eclipse.jetty.server Server]))

(defmethod ig/init-key ::server
  [_ {:keys [port handler]}]
  (println "Server started on port:" port)
  (let [server (jetty/run-jetty handler {:port  (or port 3000)
                                         :join? false})]
    server))

(defmethod ig/halt-key! ::server
  [_ server]
  (when server
    (println "Server stopped!")
    (.stop ^Server server)))


;; https://httpie.io/

;; http post :8000/urls url='https://clojure.org'
;; http post :8000/urls url='https://clojurescript.org'
;; http put :8000/urls/atm url='https://clojure.org/reference/atoms'
;; http :8000/urls/1
;; http :8000/urls/atm
;; http :8000/urls

