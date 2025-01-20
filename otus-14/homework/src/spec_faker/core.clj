(ns spec-faker.core
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup.core :refer [html]]
   [hiccup.util :refer [url]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :refer [redirect response content-type]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [cheshire.core :refer [generate-string]]
   [ring.swagger.swagger-ui :refer [wrap-swagger-ui]]
   [spec-faker.spec :as sspec])
  (:gen-class))

(defn- page [title & body]
  [:html
   [:head
    [:title title]]
   (conj [:body body]
         [:a {:href (url "/")} "Home"]
         [:br]
         [:a {:href (url "/swagger")} "Swagger"])])

(defn- jsonify [something]
  (-> something
      generate-string
      response
      (content-type "application/json")))

(defroutes router
  (GET "/swagger.json" []
    (jsonify sspec/swagger-json))

  (GET "/" [spec]
    (if (nil? spec) (html (page "Spec generator" [:form {:method "POST"}
                                                  [:label "Spec: "]
                                                  [:textarea {:name "spec" :rows "10" :cols "30"}
                                                   "[{\"name\": \"id\", \"type\": \"integer\"},{\"name\": \"sample\", \"type\": \"string\"}]"]
                                                  [:button {:type "submit"} "Go..."]]))

        (when (sspec/valid? spec)
          (jsonify (sspec/gen-data spec)))))

  (POST "/" [spec]
    (if (sspec/valid? spec)
      (redirect (str (url "/" {:spec spec})))
      (html
       (page
        "Invalid spec"
        [:h1 "Invalid spec"]))))

  (route/not-found
   (html
    (page
     "Page not found"
     [:h1 "Oops!"]))))

(def app
  (-> #'router
      wrap-keyword-params
      wrap-params
      (wrap-swagger-ui {:path "/swagger"})))

(comment
  (def dev-server (run-jetty (wrap-reload #'app) {:join? false
                                                  :port 8000}))
  (. dev-server stop))

(defn -main
  [& args]
  (run-jetty
   (wrap-reload app)
   {:port 8000}))
