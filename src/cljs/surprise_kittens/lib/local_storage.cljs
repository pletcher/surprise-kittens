(ns surprise-kittens.lib.local-storage)

(defn set-item!
  [k v]
  (.setItem (.-localStorage js/window) k v))

(defn get-item
  [k]
  (.getItem (.-localStorage js/window) k))

(defn remove-item!
  [k]
  (.removeItem (.-localStorage js/window) k))
