(ns surprise-kittens.parser
  (:require [om.next.server :as om]
            [surprise-kittens.kittens :refer [kitten]]
            [surprise-kittens.models.user :as user]))

(defmulti mutate om/dispatch)

(defmethod mutate 'kitten/change
  [_ _ _]
  {:action #(kitten)
   :value {:keys [:kitten]}})

(defmethod mutate 'user/log-in
  [env k params]
  env)

(defmethod mutate 'user/sign-up
  [env k params]
  {:action #(dissoc (user/create-user params) :password)
   :value {:keys [:current-user]}})

(defmulti readf om/dispatch)

(defmethod readf :default
  [env k params]
  env)

(def parser (om/parser {:mutate mutate
                        :read readf}))
