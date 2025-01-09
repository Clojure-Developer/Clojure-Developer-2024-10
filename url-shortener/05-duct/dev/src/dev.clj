(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :as c.t.n]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]]))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "url_shortener/config.edn")))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

(comment
  (c.t.n/clear)
  (c.t.n/refresh-all)

  config   ; to check eventual config
  system   ; to check current system
  
  ;; system's lifecycle
  (go)     ; prep and init — start the system
  (reset)  ; halt the system, refresh all changed code (with tools.namespace), start the system again
  (halt)   ; stop the system
  
  (System/exit 0)) ; exit!

(comment
  (let [handler (:url-shortener.adapters.rest-api/handler system)]
    #_(handler {:uri "/urls" :request-method :post :params {:url "https://clojure.org"}})
    #_(handler {:uri "/urls/clj" :request-method :get})
    (handler {:uri "/urls" :request-method :get})))
