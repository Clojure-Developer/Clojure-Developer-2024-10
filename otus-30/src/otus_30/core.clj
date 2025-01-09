(ns otus-30.core
  (:gen-class)
  (:require
   [org.httpkit.server :as http-kit]
   [com.brunobonacci.mulog :as mulog]
   [nrepl.server :as nrepl]))


;; package otus_30;
;; public class core {
;;   public static void main(String[] var0) {
;; }}

(defn -main [& args]
  (println "Hello world"))

(defn start-repl-server [port]
  (nrepl/start-server :port port
                      :bind "0.0.0.0"))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello guys!"})

#_(defn -main [& args]
  (start-repl-server 8888)

  (let [stop-mulog (mulog/start-publisher! {:type :console
                                            :pretty? true})
        server (http-kit/run-server app {:port 3000
                                         :legacy-return-value? false})]

    (mulog/log ::app-started
               :level :info
               :timestamp (System/currentTimeMillis)
               :message "App started"
               :data {:args "args"}) 
    
    (println "[ APP STARTED ]")
    (println (http-kit/server-status server))
    
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. (fn []
                (stop-mulog)
                (http-kit/server-stop! server)
                (println "[ APP STOPPED ]"))))))

(comment
  (-main)
  (System/exit 0))
