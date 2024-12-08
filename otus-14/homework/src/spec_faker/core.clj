(ns spec-faker.core
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup.core :refer [html]]
   [hiccup.util :refer [url]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :refer [redirect]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [cheshire.core :refer [generate-string parse-string]])
  (:gen-class))

(defn page [title & body]
  [:html
   [:head
    [:title title]]
   [:body
    body]])

(comment
  (defn set-atom [o t v]
    (Thread/sleep t)
    (println (str "set v " v))
    (+ o v))
  (def a (atom 0))

  (do
    (future (swap! a set-atom 5000 100))
    (Thread/sleep 1000)
    (future (swap! a set-atom 1000 1))
   )
    (Thread/sleep 8000)
    (println @a))

(defroutes router
  (GET "/" [spec]
    (html
     (page
      "spec"
      (if (nil? spec) [:form {:method "POST"}
                       [:label "Spec: "]
                       [:textarea {:name "spec" :rows "10" :cols "30"}]
                       [:button {:type "submit"} "Go..."]]
          [:h1 (str (parse-string spec))]))))

  (POST "/" [spec]
    (redirect (str (url "/" {:spec (generate-string spec)}))))

  (route/not-found
   (html
    (page
     "Page not found"
     [:h1 "Oops!"]))))

(def app
  (-> #'router
      wrap-keyword-params
      wrap-params))

(defn -main
  [& args]
  (run-jetty

   (wrap-reload app)

   {:port 8000}))
