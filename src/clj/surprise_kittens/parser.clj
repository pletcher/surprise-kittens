(ns surprise-kittens.parser
  (:require [om.next.server :as om]
            [surprise-kittens.kittens :refer [kitten]]
            [surprise-kittens.models.user :as user]))

(defmulti mutate om/dispatch)

(defmethod mutate 'kitten/change
  [env k params]
  {:action #(kitten)
   :value {:keys [:kitten]}})

(defmethod mutate 'user/log-in
  [env k params]
  env)

(defmethod mutate 'user/authenticate
  [env k params]
  {:action #(dissoc (user/create-user params) :password)
   :value {:keys [:current-user]}})

(defmethod mutate :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

(defmulti readf om/dispatch)

(defmethod readf :default
  [{:keys [state] :as env} k params]
  (let [st @state]
    (if-let [[_ value] (find st k)]
      {:value value}
      {:value :not-found})))

(def parser (om/parser {:mutate mutate
                        :read readf}))
