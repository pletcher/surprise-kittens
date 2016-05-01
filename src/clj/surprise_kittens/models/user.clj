(ns surprise-kittens.models.user
  (:require [crypto.password.pbkdf2 :as pbkdf2]
            [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "surprise_kittens/sql/users.sql"
  {:connection (env :database-url)})

(defn email? [s]
  "Hacky check for whether s looks like an email address"
  (re-matches #".+\@.+\..+" s))

(defn find-user
  ([identifier]
   (if (email? identifier)
     (first (find-user-by-email {:email identifier}))
     (first (find-user-by-username {:username identifier}))))
  ([identifier opts]
   (if (email? identifier)
     (first (find-user-by-email {:email identifier} opts))
     (first (find-user-by-username {:username identifier} opts)))))

(defn authenticate-user
  ([identifier password]
   (if-let [user (find-user identifier)]
     (if (pbkdf2/check password (:password user))
       user)))
  ([identifier password opts]
   (if-let [user (find-user identifier opts)]
     (if (pbkdf2/check password (:password user))
       user))))

(defn create-user
  ([m]
   (create-user<!
     (merge m {:password (pbkdf2/encrypt (:password m))})))
  ([m opts]
   (create-user<!
     (merge m {:password (pbkdf2/encrypt (:password m))})
     opts)))
