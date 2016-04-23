(ns surprise-kittens.models.heart
  (:require [environe.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "surprise_kittens/sql/hearts.sql"
  {:connection (env :database-url)})
