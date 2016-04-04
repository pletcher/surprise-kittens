(ns surprise-kittens.core
  (:require [clojure.string :as string]
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

(defn medium-image-url [url]
  (let [dot-index (string/last-index-of url ".")]
    (str (subs url 0 dot-index) "m" (subs url dot-index))))

(defn image [kitten]
  (if-let [webm (:webm kitten)]
    (dom/video #js {:autoPlay "autoplay"
                    :className "clickable rounded shadowed"
                    :height (:height kitten)
                    :loop "loop"
                    :muted "muted"}
      (dom/source #js {:src webm})
      (dom/source #js {:src (:mp4 kitten)}))
    (dom/img #js {:className "clickable rounded shadowed"
                  :src (medium-image-url (:link kitten))})))

(defn tweet-url [link]
  (str "https://twitter.com/intent/tweet?text="
    (js/encodeURIComponent "Surprise! Kittens! ")
    (js/encodeURIComponent link)
    (js/encodeURIComponent " from http://surprisekittens.com")
    "&via=sprisekittens"))

(defn tweet-button [link]
  (dom/a #js {:className "twitter-share-button"
              :href (tweet-url link)
              :style #js {:textDecoration "none"}
              :target "_blank"
              :title "Tweet this kitten!"}
    (dom/span nil
      (dom/span #js {:className "fa fa-twitter-square"})
      (dom/span nil " Tweet this kitten!"))))

(defui SocialBox
  static om/IQuery
  (query [this]
    '[:link])
  Object
  (render [this]
    (let [link (:link (om/props this))]
      (dom/div nil
        (dom/h3 nil (tweet-button link))))))

(def social-box (om/factory SocialBox))

(defui Root
  static om/IQuery
  (query [this]
    '[:kitten])
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
        (dom/small nil "Made with <3 for S.")))))

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
