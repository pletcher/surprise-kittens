(ns surprise-kittens.core
  (:require [clojure.string :as string]
            [cognitect.transit :as transit]
            [goog.dom :as gdom]
            [goog.net.XhrIo :as xhr]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [surprise-kittens.lib.local-storage :as local-storage]))

(if goog.DEBUG
  (enable-console-print!))

(defonce app-state
  (atom {:authentication-form {}
         :current-user {:logged-in false}
         :kitten {:hearted false
                  :link "img/cat-1209743_1920.jpg"}}))
(defonce kitten-url "/kittens/random")

(defn request [url method body headers cb]
  (xhr/send url
    (fn [e]
      (this-as this
        (cb (transit/read (om/reader) (.getResponseText this)))))
    method body headers))

(defn GET [url cb]
  (request url "GET" nil #js {"Accept" "application/transit+json"} cb))

(defn POST [url body cb]
  (request url "POST"
    (transit/write (om/writer) body)
    #js {"Accept" "application/transit+json"
         "Content-Type" "application/transit+json"} cb))

(defn medium-image-url [url]
  (let [dot-index (string/last-index-of url ".")]
    (str (subs url 0 dot-index) "m" (subs url dot-index))))

(defn image [kitten]
  (if (:animated kitten)
    (dom/video #js {:autoPlay "autoplay"
                    :className "clickable rounded shadowed"
                    :height (:height kitten)
                    :loop "loop"
                    :muted "muted"
                    :src (:mp4 kitten)}
      (dom/source #js {:src (:webm kitten)})
      (dom/source #js {:src (:mp4 kitten)}))
    (dom/img #js {:className "clickable rounded shadowed"
                  :src (medium-image-url (:link kitten))})))

(defn tweet-text [link]
  (str
    (js/encodeURIComponent "Surprise! Kittens! ")
    (js/encodeURIComponent link)
    (js/encodeURIComponent " via @sprisekittens")))

(defn tweet-deep-url [link]
  (str "twitter://post?message=" (tweet-text link)))

(defn tweet-url [link]
  (str "https://twitter.com/intent/tweet?text=" (tweet-text link)))

(defn tweet-button [link]
  (dom/a #js {:className "twitter-share-button"
              :href (tweet-deep-url link)
              :style #js {:textDecoration "none"}
              :target "_blank"
              :title "Tweet this kitten!"}
    (dom/span nil
      (dom/span #js {:className "fa fa-twitter-square"}))))

(defui Kitten
  static om/IQuery
  (query [this]
    [:hearted :id :link :title])
  Object
  (render [this]
    (let [{:keys [hearted id link title] :as kitten} (om/props this)]
      (dom/div #js {:style #js {:fontSize 36}}
        (dom/div #js {:onClick (fn [e]
                                 (GET kitten-url
                                   #(om/transact! this `[(kitten/change ~%)])))
                      :style #js {:width "100%"}}
          (image kitten))
        (dom/a #js {:href link :title title}
          (dom/h4 nil (or title link)))
        (dom/span #js {:className "px1"}
          (dom/span nil (tweet-button link)))
        (dom/span #js {:className "px1"}
          (dom/span #js {:className
                         (str "clickable "
                           (if hearted
                             "fa fa-heart"
                             "fa fa-heart-o"))
                         :onClick
                         (fn []
                           (if hearted
                             (om/transact! this `[(kitten/unheart {:id ~id})])
                             (om/transact! this `[(kitten/heart {:id ~id})])))}))))))

(def kitten-factory (om/factory Kitten))

(defn sign-up [component props]
  (om/transact! component `[(user/sign-up ~(dissoc props :logged-in))]))

(defn change-form [component field e]
  (om/transact! component `[(authentication-form/set-field
                              {:field ~field
                               :value ~(.. e -target -value)})]))

(defui AuthenticationForm
  static om/IQuery
  (query [this]
    [:email :logged-in :password :username])
  Object
  (render [this]
    (let [{:keys [email logged-in password username] :as props} (om/props this)]
      (when-not logged-in
        (dom/div #js {:className "mx-auto py2" :style #js {:maxWidth 240}}
          (dom/form nil
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "email")
              (dom/input #js {:className "right rounded"
                              :maxLength 254
                              :onChange #(change-form this :email %)
                              :type "email"
                              :value (or email "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "username")
              (dom/input #js {:className "right rounded"
                              :maxLength 20
                              :onChange #(change-form this :username %)
                              :type "text"
                              :value (or username "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "password")
              (dom/input #js {:className "right rounded"
                              :onChange #(change-form this :password %)
                              :type "password"
                              :value (or password "")}))
            (dom/div #js {:className "clearfix"}
              (dom/a #js {:className "btn mt2 rounded"
                          :onClick #(sign-up this props)} "Submit"))))))))

(def authentication-form (om/factory AuthenticationForm))

(defui Root
  static om/IQuery
  (query [this]
    [{:current-user (om/get-query AuthenticationForm)}
     {:kitten (om/get-query Kitten)}])
  Object
  (componentDidMount [this]
    (GET kitten-url
      #(om/transact! this `[(kitten/change ~%)])))
  (render [this]
    (let [{:keys [kitten current-user] :as props} (om/props this)]
      (dom/div #js {:style #js {:textAlign "center"}}
        (dom/h1 nil "Surprise! Kittens!")
        (kitten-factory kitten)
        (authentication-form current-user)
        (dom/div #js {:className "py4"}
          (dom/small nil "Made with <3 for S."))))))

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [env key params]
  env)

(defmethod mutate 'kitten/change
  [{:keys [state] :as env} _ {:keys [id] :as params}]
  {:value {:keys [:kitten]}
   :action #(swap! state assoc :kitten
              (assoc params :hearted (local-storage/get-item id)))})

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

(defmethod mutate 'user/sign-up
  [{:keys [ast state] :as env} k params]
  {:action #(swap! state assoc :current-user {:logged-in true})
   :remote ast
   :value {:keys [:current-user]}})

(defmulti read om/dispatch)

(defmethod read :current-user
  [{:keys [ast state] :as env} k params]
  {:value (get @state k {:logged-in false})})

(defmethod read :default
  [{:keys [state] :as env} k params]
  (let [st @state]
    (if-let [[_ value] (find st k)]
      {:value value}
      {:value :not-found})))

(defn send-query
  [{:keys [remote]} cb]
  (POST "/query" remote cb))

(def reconciler-cfg
  (merge
    {:parser (om/parser {:mutate mutate
                        :read read})
     :send send-query
     :state app-state}
    (when-not goog.DEBUG
      {:logger nil})))

(def reconciler
  (om/reconciler reconciler-cfg))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
