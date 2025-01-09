(ns url-shortener.adapters.rest-api
  (:require [ring.middleware.json :as middleware.json]
            [ring.middleware.params :as middleware.params]
            [ring.middleware.resource :as middleware.resource]
            [ring.middleware.keyword-params :as middleware.keyword-params]
            [ring.util.response :as ring.resp]
            [compojure.core :as compojure]
            [integrant.core :as ig]
            [url-shortener.ports.use-case :as urls]))

(defn- retain
  ([shortener url]
   (let [id (urls/shorten shortener url)]
     (ring.resp/created id {:id id})))
  ([shortener id url]
   (if-let [id (urls/shorten shortener id url)]
     (ring.resp/created id {:id id})
     {:status 409 :body {:error (format "Short URL %s is already taken" id)}})))

(defmethod ig/init-key ::handler
  [_ {:keys [shortener]}]
  (compojure/defroutes router
    (compojure/GET "/" []
      (ring.resp/resource-response "index.html" {:root "public"}))

    (compojure/GET "/urls" []
      (ring.resp/response {:urls (urls/list-all shortener)}))

    (compojure/GET "/urls/:id" [id]
      (if-let [url (urls/url-for shortener id)]
        (ring.resp/redirect url)
        (ring.resp/not-found {:error "Requested URL not fount."})))

    (compojure/POST "/urls" [url]
      (if (empty? url)
        (ring.resp/bad-request {:error "No `url` parameter provided"})
        (retain shortener url)))

    (compojure/PUT "/urls/:id" [id url]
      (if (empty? url)
        (ring.resp/bad-request {:error "No `url` parameter provided"})
        (retain shortener id url))))

  (-> router
      (middleware.resource/wrap-resource "public")
      (middleware.params/wrap-params)
      (middleware.keyword-params/wrap-keyword-params)
      (middleware.json/wrap-json-params)
      (middleware.json/wrap-json-response)))
