(ns surprise-kittens.petfinder
  (:import [java.security MessageDigest]
           [java.math BigInteger])
  (:require [clj-http.client :as http]
            [clojure.string :as string]
            [environ.core :refer [env]]
            [ring.util.codec :refer [form-encode]]))

(def request-opts {:as :json})

(defn- md5 [s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        size (* 2 (.getDigestLength algorithm))
        raw (.digest algorithm (.getBytes s))
        sig (.toString (BigInteger. 1 raw) 16)
        padding (apply str (repeat (- size (count sig)) "0"))]
    (str padding sig)))

;; https://www.petfinder.com/developers/api-docs

(defn- url [method & {:keys [age count location offset] :as args}]
  (str "https://api.petfinder.com/"
       method
       "?animal=cat&format=json&key=" (env :petfinder-api-key)
       (if args
         (str "&" (form-encode args)))))

(defn kittens
  ([location]
   (kittens location 0))
  ([location offset]
   (let [resp (-> (http/get (url
                             "pet.find"
                             :location location
                             :offset offset)
                            request-opts))]
     (if (= (:status resp) 200)
       (let [body (-> resp :body :petfinder)]
         {:kittens (-> body :pets :pet)
          :offset (-> body :lastOffset :$t)})
       ;; FIXME: this code will not execute
       ;; because clj-http fails to parse
       ;; XML
       {:error (:body resp)
        :status (:status resp)}))))
