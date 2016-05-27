(ns surprise-kittens.server
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [cognitect.transit :as transit]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [surprise-kittens.kittens :refer [kitten]]
            [surprise-kittens.parser :refer [parser]])
  (:import [java.io ByteArrayOutputStream])
  (:gen-class))

(defn read-transit
  [t]
  (let [reader (transit/reader t :json {})]
    (transit/read reader)))

(defn write-str
  "Writes a transit value to a string."
  ([o type] (write-str o type {}))
  ([o type opts]
   (let [out (ByteArrayOutputStream.)
         writer (transit/writer out type opts)]
     (transit/write writer o)
     (.toString out "UTF-8"))))

(defn handle-query [req q]
  (let [query (read-transit q)]
    {:status 200
     :headers {"Content-Type" "application/transit+json"}
     :body (write-str (parser req query) :json)}))

(defn kitten-image []
  {:status 200
   :headers {"Content-Type" "application/transit+json"}
   :body (write-str (kitten) :json)})

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (GET "/kittens/random" _
    (kitten-image))
  (POST "/query" req
    (if-let [body (get req :body)]
      (handle-query req body)
      {:status 400
       :body (write-str {:error "Invalid query"} :json)}))
  (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))
