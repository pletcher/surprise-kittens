(ns surprise-kittens.parser
  (:require [om.next :as om]
            [surprise-kittens.lib.local-storage :as local-storage]))

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [env key params]
  env)

(defmethod mutate 'kitten/change
  [{:keys [ast state] :as env} _ _]
  {:action #(swap! state assoc-in [:kitten :loading] true)
   :remote ast
   :value {:keys [:kitten]}})

(defmethod mutate 'kitten/heart
  [{:keys [state] :as env} _ {:keys [id] :as params}]
  {:action (fn []
             (local-storage/set-item! id true)
             (swap! state assoc-in [:kitten :hearted] true))})

(defmethod mutate 'kitten/unheart
  [{:keys [state] :as env} _ {:keys [id] :as params}]
  {:action (fn []
             (local-storage/remove-item! id)
             (swap! state assoc-in [:kitten :hearted] false))})

(defmethod mutate 'authentication-form/set-field
  [{:keys [state] :as env} k {:keys [field value]}]
  {:action #(swap! state assoc-in [:current-user field] value)
   :value {:keys [:current-user]}})

(defmethod mutate 'user/authenticate
  [{:keys [ast state] :as env} k params]
  {:action #(swap! state assoc :current-user {:logged-in true})
   :remote ast
   :value {:keys [:current-user]}})

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} k params]
  (let [st @state]
    (if-let [[_ value] (find st k)]
      {:value value}
      {:value :not-found})))

(defmethod read :authentication-form
  [{:keys [state]} k _]
  {:value (get @state k {})})

(defmethod read :current-user
  [{:keys [state] :as env} k _]
  (let [st @state]
    {:value (if-let [user (get-in st ['user/authenticate :result])]
              (assoc user :logged-in true)
              (get st k {:logged-in false}))}))

(defmethod read :kitten
  [{:keys [query state] :as env} k _]
  (let [st @state]
    {:value (get-in st ['kitten/change :result]
              (get st k
                {:id :temp
                 :hearted false
                 :link "img/cat-1209743_1920.jpg"
                 :loading false}))}))
