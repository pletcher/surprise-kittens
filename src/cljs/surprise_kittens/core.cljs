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

(defonce app-state (atom {:kitten {:hearted false
                                   :link "img/cat-1209743_1920.jpg"}}))
(defonce kitten-url "/kittens/random")

(defn GET [url cb]
  (xhr/send url
    (fn [e]
      (this-as this
        (cb (transit/read (om/reader) (.getResponseText this)))))
    "GET" nil
    #js {"Accept" "application/transit+json"}))

(defn medium-image-url [url]
  (let [dot-index (string/last-index-of url ".")]
    (str (subs url 0 dot-index) "m" (subs url dot-index))))

(defn image [kitten]
  (if (:animated kitten)
    (dom/video #js {:autoPlay "autoplay"
                    :className "clickable rounded shadowed"
                    :height (:height kitten)
                    :loop "loop"
                    :muted "muted"}
      (dom/source #js {:src (:webm kitten)})
      (dom/source #js {:src (:mp4 kitten)}))
    (dom/img #js {:className "clickable rounded shadowed"
                  :src (medium-image-url (:link kitten))})))

(defn tweet-text [link]
  (str
    (js/encodeURIComponent "Surprise! Kittens! ")
    (js/encodeURIComponent link)
    (js/encodeURIComponent " via @srprisekittens")))

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

(defui SocialBox
  static om/IQuery
  (query [this]
    [:hearted :id :link])
  Object
  (render [this]
    (let [{:keys [hearted id link] :as props} (om/props this)]
      (dom/div #js {:style #js {:fontSize 36}}
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

(def social-box (om/factory SocialBox))

(defui Root
  static om/IQuery
  (query [this]
    [{:kitten (om/get-query SocialBox)}])
  Object
  (componentDidMount [this]
    (GET kitten-url
      #(om/transact! this `[(kitten/change ~%)])))
  (render [this]
    (let [{:keys [kitten] :as props} (om/props this)
          {:keys [link title]} kitten]
      (dom/div #js {:style #js {:textAlign "center"}}
        (dom/h1 nil "Surprise! Kittens!")
        (dom/div #js {:onClick (fn [e]
                                 (GET kitten-url
                                   #(om/transact! this `[(kitten/change ~%)])))
                      :style #js {:width "100%"}}
          (image kitten))
        (dom/a #js {:href link :title title}
          (dom/h4 nil (or title link)))
        (social-box kitten)
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

(defmethod mutate `kitten/heart
  [{:keys [state] :as env} _ {:keys [id] :as params}]
  {:action (fn []
             (local-storage/set-item! id true)
             (swap! state assoc-in [:kitten :hearted] true))})

(defmethod mutate 'kitten/unheart
  [{:keys [state] :as env} _ {:keys [id] :as params}]
  {:action (fn []
             (local-storage/remove-item! id)
             (swap! state assoc-in [:kitten :hearted] false))})

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(def parser-and-state
  {:parser (om/parser {:mutate mutate
                       :read read})
   :state app-state})

(def reconciler
  (om/reconciler (if goog.DEBUG
                   parser-and-state
                   (assoc parser-and-state :logger nil))))

(om/add-root! reconciler
  Root (gdom/getElement "app"))
