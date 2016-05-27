(ns surprise-kittens.core-test
  (:require [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]))

(declare ^:dynamic *opts*)

(defn with-transaction [f]
  (jdbc/with-db-transaction [tx (env :database-url)]
    (jdbc/db-set-rollback-only! tx)
    (binding [*opts* {:connection tx}] (f))))
