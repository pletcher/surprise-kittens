(ns surprise-kittens.core-test
  (:require [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]))

(defn with-transaction [f]
  (jdbc/with-db-transaction [tx (env :database-url)]
    (jdbc/db-set-rollback-only! tx)
    (f {:connection tx})))
