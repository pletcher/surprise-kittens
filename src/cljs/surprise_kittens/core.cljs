(ns surprise-kittens.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! put! chan]]
            [cognitect.transit :as transit]
            [goog.dom :as gdom]
            [goog.net.XhrIo :as xhr]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(defonce app-state (atom {:link "img/cat-1209743_1920.jpg"
                          :request-count 0}))
(defonce kitten-url "/kittens/random")

(defn GET [url cb]
  (xhr/send url
    (fn [e]
      (this-as this
        (cb (transit/read (om/reader) (.getResponseText this)))))
    "GET" nil
    #js {"Accept" "application/transit+json"}))

(defui Root
  static om/IQuery
  (query [this]
    '[:link])
  Object
  (render [this]
    (dom/div #js {:onClick (fn [e]
                             (GET "/kittens/random"
                               #(om/transact! this `[(kitten/change ~%)])))
                  :style #js {:textAlign "center"}}
      (dom/h1 nil "Surprise! Kittens!")
      (dom/img #js {:className "rounded shadowed"
                    :src (:link (om/props this))}))))

(defn mutate [{:keys [state] :as env} key params]
  (if (= 'kitten/change key)
    {:value {:keys [:link]}
     :action #(swap! state assoc :link (:link params))}
    {:value :not-found}))

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(def reconciler
  (om/reconciler {:parser (om/parser {:mutate mutate
                                      :read read})
                  :state app-state}))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
