(ns surprise-kittens.kittens
  (:require [clj-http.client :as http]
            [environ.core :refer [env]]))

(def request-opts {:headers {:authorization (str "Client-ID " (env :imgur-client-id))}
                   :as :json})

(defn albums-and-images
  "Returns a list of albums and images"
  []
  (->
    (http/get "https://api.imgur.com/3/gallery/search?q=kitten" request-opts)
    :body
    :data))

(defn images
  "Returns a list of images in the gallery at `url`"
  [album-id]
  (let [url (str "https://api.imgur.com/3/gallery/album/" album-id)]
    (->
      (http/get url request-opts)
      :body
      :data
      :images)))

(defn- not-nsfw
  [el]
  (not (:nsfw el)))

(defn- vec-remove
  "Remove the element at index `i` from `v`"
  [v i]
  (vec (concat
         (subvec v 0 i)
         (subvec v (inc i)))))

(defn sample
  "Returns an element from `v` if `f` returns `true` for the element"
  [f v]
  (loop [col v]
    (let [el (rand-nth col)
          i (.indexOf col el)]
      (if (f el)
        el
        (recur (vec-remove col i))))))

(defn kitten
  "Initiates request for getting a kitten image"
  []
  (let [ai (sample not-nsfw (albums-and-images))]
    (if (:is_album ai)
      (sample not-nsfw (images (:id ai)))
      ai)))
