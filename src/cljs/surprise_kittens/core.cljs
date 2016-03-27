(ns surprise-kittens.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defui Root
  Object
  (render [this]
    (dom/div #js {:style #js {:textAlign "center"}}
      (dom/h1 nil "Surprise! Kittens!")
      (dom/img #js {:className "rounded shadowed"
                    :src "/kittens/random"}))))

(def root (om/factory Root))

(js/ReactDOM.render (root) (gdom/getElement "app"))
