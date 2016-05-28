(ns surprise-kittens.core
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [surprise-kittens.lib.local-storage :as local-storage]
            [surprise-kittens.parser :as parser]
            [surprise-kittens.util :as util]))

(if goog.DEBUG
  (enable-console-print!))

(defonce app-state (atom {}))

(defonce kitten-url "/kittens/random")

(defn medium-image-url [url]
  (when url
    (let [dot-index (string/last-index-of url ".")]
      (str (subs url 0 dot-index) "m" (subs url dot-index)))))

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
  static om/Ident
  (ident [_ {:keys [id] :or {id :temp}}]
    [:kitten/by-id id])
  static om/IQuery
  (query [this]
    [:hearted :id :link :loading :title])
  Object
  (componentDidMount [this]
    (om/transact! this `[(kitten/change)]))
  (render [this]
    (let [{:keys [hearted id link loading title] :as kitten} (om/props this)]
      (if loading
        (dom/div nil
          (dom/h2 nil "Loading..."))
        (dom/div #js {:style #js {:fontSize 36}}
          (dom/div #js {:onClick (fn [e]
                                   (om/transact! this `[(kitten/change)]))
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
                               (om/transact! this `[(kitten/heart {:id ~id})])))})))))))

(def kitten-factory (om/factory Kitten))

(defn sign-up [component props]
  (om/transact! component `[(user/sign-up ~(dissoc props :logged-in))]))

(defn set-field [component field e]
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
                              :onChange #(set-field this :email %)
                              :type "email"
                              :value (or email "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "username")
              (dom/input #js {:className "right rounded"
                              :maxLength 20
                              :onChange #(set-field this :username %)
                              :type "text"
                              :value (or username "")}))
            (dom/div #js {:className "clearfix"}
              (dom/label #js {:className "left"} "password")
              (dom/input #js {:className "right rounded"
                              :onChange #(set-field this :password %)
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
  (render [this]
    (let [{:keys [kitten current-user] :as props} (om/props this)]
      (dom/div #js {:style #js {:textAlign "center"}}
        (dom/h1 nil "Surprise! Kittens!")
        (kitten-factory kitten)
        (comment (authentication-form current-user))
        (dom/div #js {:className "py4"}
          (dom/small nil "Made with <3 for S."))))))

(defn send-query
  [{:keys [remote]} cb]
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
