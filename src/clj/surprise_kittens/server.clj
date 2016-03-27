(ns surprise-kittens.server
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [image-resizer.core :refer [resize]]
            [image-resizer.format :as format]
            [surprise-kittens.kittens :refer [kitten]])
  (:gen-class))

(defn kitten-image []
  (let [{:keys [link type]} (kitten)
        short-type (last (string/split type #"/"))]
    {:status 200
     :headers {"Content-Type" type}
     :body (format/as-stream
             (resize (io/input-stream link) 400 400) short-type)}))

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (GET "/kittens/random" _
    (kitten-image))
  (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-jetty http-handler {:port port :join? false})))
