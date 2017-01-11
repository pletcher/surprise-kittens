(ns surprise-kittens.core
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [surprise-kittens.components.authentication-form
             :refer [AuthenticationForm authentication-form]]
            [surprise-kittens.components.kitten
             :refer [Kitten kitten]]
            [surprise-kittens.lib.local-storage :as local-storage]
            [surprise-kittens.parser :as parser]
            [surprise-kittens.util :as util]))

(when goog.DEBUG
  (enable-console-print!))

(defonce app-state (atom {}))

(defui Root
  static om/IQuery
  (query [this]
    [{:current-user (om/get-query AuthenticationForm)}
     {:kitten (om/get-query Kitten)}])
  Object
  (render [this]
    (let [{:keys [current-user] :as props} (om/props this)]
      (dom/div #js {:style #js {:textAlign "center"}}
        (dom/h1 nil "Surprise! Kittens!")
        (kitten (:kitten props))
        (authentication-form current-user)
        (dom/div #js {:className "py4"}
          (dom/small nil "Made with <3 for S."))))))

(defn send-query
  [{:keys [remote] :as env} cb]
  (util/transit-post "/query" remote cb))

(def reconciler-cfg
  (merge
    {:id-key :id
     :parser (om/parser
               {:mutate parser/mutate
                :read parser/read})
     :send send-query
     :state app-state}
    (when-not goog.DEBUG
      {:logger nil})))

(def reconciler
  (om/reconciler reconciler-cfg))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
