(ns surprise-kittens.util
  (:require [cognitect.transit :as transit]
            [goog.net.XhrIo :as xhr]
            [om.next :as om]))

(defn request [url method body headers cb]
  (xhr/send url
    (fn [e]
      (this-as this
        ;; this is going to break _real_ fast
        (let [b (transit/read (om/reader) (.getResponseText this))
              k (first (keys b))
              ks (get-in b [k :keys])
              r (get-in b [k :result])]
          (cb {(first ks) r}) ks)))
    method body headers))

(defn transit-get [url cb]
  (request url "GET" nil #js {"Accept" "application/transit+json"} cb))

(defn transit-post [url body cb]
  (request url "POST"
    (transit/write (om/writer) body)
    #js {"Accept" "application/transit+json"
         "Content-Type" "application/transit+json"} cb))
