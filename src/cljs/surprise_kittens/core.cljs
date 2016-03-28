(ns surprise-kittens.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! put! chan]]
            [cognitect.transit :as transit]
            [goog.dom :as gdom]
            [goog.net.XhrIo :as xhr]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

;; (enable-console-print!)

(defonce app-state (atom {:kitten {:link "img/cat-1209743_1920.jpg"}}))
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
    '[:kitten])
  Object
  (componentDidMount [this]
    (GET kitten-url
      #(om/transact! this `[(kitten/change ~%)])))
  (render [this]
    (let [kitten (:kitten (om/props this))
          {:keys [link title]} kitten]
      (dom/div #js {:onClick (fn [e]
                              (GET kitten-url
                                #(om/transact! this `[(kitten/change ~%)])))
                   :style #js {:textAlign "center"}}
       (dom/h1 nil "Surprise! Kittens!")
       (dom/img #js {:className "clickable rounded shadowed"
                     :src link})
       (dom/a #js {:href link :title title}
         (dom/h4 nil title))
       (dom/small nil "Made with <3 for Sofia")))))

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [env key params]
  env)

(defmethod mutate 'kitten/change
  [{:keys [state] :as env} _ params]
  {:value {:keys [:kitten]}
   :action #(swap! state assoc :kitten params)})

(defmulti read om/dispatch)

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
