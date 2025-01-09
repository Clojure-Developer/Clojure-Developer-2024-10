# Запуск Clojure приложения в продакшен

## Сборка uberjar с помощью lein

### Конфигурация для сборки в project.clj

```clojure
:main ^:skip-aot otus-30.core

{:uberjar {:aot :all}}
```

### Почитать про компиляцию

- <https://clojure.org/reference/compilation>

### Сборка jar файла

```shell
lein uberjar
```

### Запуск

```shell
java -jar target/production-app.jar
```

## Docker

### Dockerfile

- <https://docs.docker.com/reference/dockerfile>

### Сборка Docker образа

```shell
docker build . --tag otus-clojure/simple-app:1.0 -f Dockerfile.simple
```

### Запуск Docker контейнера

```shell
docker run -p 8080:8080 otus-clojure/simple-app:1.0
```

### Сборка и запуск prod

```shell
docker build . --tag otus-clojure/prod-app

docker run --name prod-app \
    -d \
    -p 8888:8888 \
    -p 8080:8080 \
    -p 3000:3000 \
    otus-clojure/prod-app
```

## JVM options

[The best HotSpot JVM options and switches for Java 11 through Java 17](https://blogs.oracle.com/javamagazine/post/the-best-hotspot-jvm-options-and-switches-for-java-11-through-java-17)

- -XX:InitialRAMPercentage
- -XX:MaxRAMPercentage
- -XX:+UseSerialGC
- -XX:+UseParallelGC
- -XX:+UseZGC
- -XX:+UnlockExperimentalVMOptions
- -XX:+UseContainerSupport

[Про ZGC](https://habr.com/ru/articles/680038/)

## Логирование

### Java logging frameworks

[Logging in Clojure: Making Sense of the Mess](https://lambdaisland.com/blog/2020-06-12-logging-in-clojure-making-sense-of-the-mess)

### Pure Clojure logging

- <https://github.com/BrunoBonacci/mulog>
- <https://github.com/taoensso/telemere>

### Сбор JMX метрик

- <https://prometheus.io>
- <https://github.com/prometheus/jmx_exporter>
- <https://opentelemetry.io/docs/collector>

### APM
- <https://www.elastic.co/observability/application-performance-monitoring>

## Сборка проекта с помощью GraalVM

- <https://www.graalvm.org/latest/getting-started>
- <https://github.com/graalvm/graalvm-ce-builds/releases>
- <https://github.com/clj-easy/graalvm-clojure>

### Установка (MacOS Catalina)

- <https://www.graalvm.org/22.0/docs/getting-started/macos/index.html>

```shell
export PATH=/Library/Java/JavaVirtualMachines/graalvm-ce-java11-22.3.3/Contents/Home/bin:$PATH
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-ce-java11-22.3.3/Contents/Home
```

### initialize-at-build-time issue

- <https://github.com/clj-easy/graal-build-time>

### Сборка и запуск

```shell
lein do clean, uberjar

native-image --report-unsupported-elements-at-runtime \
             --features=clj_easy.graal_build_time.InitClojureClasses \
             -jar ./target/production-app.jar \
             -H:Name=./target/native-app
             
./target/native-app
```

### Время запуска

```shell
time java -jar ./target/production-app.jar

time ./target/native-app
```

## AWS Lambda

[Required Java interface](https://github.com/aws/aws-lambda-java-libs/blob/main/aws-lambda-java-runtime-interface-client/README.md)

## Homework

Применить принципы hexagonal architecture в проекте на выбор (Pokemon app)

### Последовательность шагов

- Разделить код проекта на доменные модули
- Выделить в сервисы код взаимодействующий с внешними системами
- Подключить к проекту фреймворк Duct
- Написать конфигурации для двух режимов запуска приложения (dev, production)
- Применить dependency injection для тестирования логики приложения
- Настроить production сборку для проекта
  - Создать Dockerfile для сборки и запуска
  - Создать entrypoint.sh фаил
  - Настроить логирование и сборку метрик (JMX)
