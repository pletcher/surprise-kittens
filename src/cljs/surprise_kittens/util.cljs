(ns surprise-kittens.util
  (:require [cognitect.transit :as transit]
            [goog.net.XhrIo :as xhr]
            [om.next :as om]))

(defn request [url method body headers cb]
  (xhr/send url
    (fn [e]
      (this-as this
        (cb (transit/read (om/reader) (.getResponseText this)) body)))
    method body headers))

(defn transit-get [url cb]
  (request url "GET" nil #js {"Accept" "application/transit+json"} cb))

(defn transit-post [url body cb]
  (request url "POST"
    (transit/write (om/writer) body)
    #js {"Accept" "application/transit+json"
         "Content-Type" "application/transit+json"} cb))
