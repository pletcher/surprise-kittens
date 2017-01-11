(ns surprise-kittens.components.authentication-form
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]))

(def fields [:email :password :username])

(defn authenticate [c e]
  (let [st (om/get-state c)]
    (do
      (doto e (.preventDefault) (.stopPropagation))
      (om/set-state! c {})
      (om/transact! c `[(user/authenticate ~st) :current-user]))))

(defn set-field [c field e]
  (om/update-state! c assoc field (.. e -target -value)))

(defui AuthenticationForm
  static om/IQuery
  (query [this]
    [:logged-in])
  Object
  (render [this]
    (let [{:keys [logged-in] :as props} (om/props this)
          {:keys [email password username]} (om/get-state this)]
      (when-not logged-in
        (dom/div #js {:className "mx-auto py2" :style #js {:maxWidth 240}}
          (dom/form nil
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "email")
              (dom/input #js {:className "right rounded"
                              :id "authentication-form-email"
                              :maxLength 254
                              :onChange #(set-field this :email %)
                              :type "email"
                              :value (or email "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "username")
              (dom/input #js {:className "right rounded"
                              :id "authentication-form-username"
                              :maxLength 20
                              :onChange #(set-field this :username %)
                              :type "text"
                              :value (or username "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "password")
              (dom/input #js {:className "right rounded"
                              :id "authentication-form-password"
                              :onChange #(set-field this :password %)
                              :type "password"
                              :value (or password "")}))
            (dom/div #js {:className "clearfix"}
              (dom/a #js {:className "btn mt2 rounded"
                          :onClick #(authenticate this %)} "Submit"))))))))

(def authentication-form (om/factory AuthenticationForm))
