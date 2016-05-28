(ns surprise-kittens.models.heart
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "sql/hearts.sql"
  {:connection (env :database-url)})
