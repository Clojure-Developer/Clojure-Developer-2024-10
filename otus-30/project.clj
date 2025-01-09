(defproject otus-30 "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.11.1"]

                 [org.slf4j/slf4j-api "1.7.32"]
                 [ch.qos.logback/logback-classic "1.2.6"]
                 [org.clojure/tools.logging "1.2.4"]

                 [com.brunobonacci/mulog "0.9.0"]

                 [http-kit/http-kit "2.8.0"]

                 [nrepl "0.9.0"]

                 [com.amazonaws/aws-lambda-java-runtime-interface-client "2.4.0"]

                 [com.github.clj-easy/graal-build-time "1.0.5"]]

  :uberjar-name "production-app.jar"

  :main ^:skip-aot otus-30.core

  :repl-options {:init-ns otus-30.core}

  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-shell "0.5.0"]]}}

  :aliases
  {"native"
   ["do" "clean," "uberjar"
    ["shell" "native-image"
     "--report-unsupported-elements-at-runtime"
     "--features=clj_easy.graal_build_time.InitClojureClasses"
     "-jar" "./target/production-app.jar"
     "-H:Name=./target/native-app"]]})
