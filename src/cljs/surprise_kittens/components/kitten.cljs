(ns surprise-kittens.components.kitten
  (:require [clojure.string :as string]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]))

(defn medium-image-url [url]
  (when url
    (let [dot-index (string/last-index-of url ".")]
      (str (subs url 0 dot-index) "m" (subs url dot-index)))))

(defn image [props]
  (if (:animated props)
    (dom/video #js {:autoPlay "autoplay"
                    :className "clickable rounded shadowed"
                    :height (:height props)
                    :loop "loop"
                    :muted "muted"
                    :src (:mp4 props)}
      (dom/source #js {:src (:webm props)})
      (dom/source #js {:src (:mp4 props)}))
    (dom/img #js {:className "clickable rounded shadowed"
                  :src (medium-image-url (:link props))})))

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
    (let [{:keys [hearted id link loading title] :as props} (om/props this)]
      (if loading
        (dom/div nil
          (dom/h2 nil "Loading..."))
        (dom/div #js {:style #js {:fontSize 36}}
          (dom/div #js {:onClick #(om/transact! this `[(kitten/change)])
                        :style #js {:width "100%"}}
            (image props))
          (dom/a #js {:href link :title title}
            (dom/h4 nil (or title link)))
          (dom/span #js {:className "px1"}
            (dom/span nil (tweet-button link)))
          (dom/span #js {:className "px1"}
            (dom/span #js
              {:className
               (str "clickable "
                 (if hearted
                   "fa fa-heart"
                   "fa fa-heart-o"))
               :onClick
               #(if hearted
                  (om/transact! this `[(kitten/unheart {:id ~id})])
                  (om/transact! this `[(kitten/heart {:id ~id})]))})))))))

(def kitten (om/factory Kitten))
