(ns surprise-kittens.models.user
  (:require [crypto.password.pbkdf2 :as pbkdf2]
            [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "surprise_kittens/sql/users.sql"
  {:connection (env :database-url)})

(defn email? [s]
  "Hacky check for whether s looks like an email address"
  (re-matches #".+\@.+\..+" s))

(defn find-user [identifier]
  (if (email? identifier)
    (first (find-user-by-email {:email identifier}))
    (first (find-user-by-username {:username identifier}))))

(defn authenticate-user [identifier password]
  (if-let [user (find-user identifier)]
    (if (pbkdf2/check password (:password user))
      user)))

(defn create-user [email username password]
  (create-user<! {:email email
                  :password (pbkdf2/encrypt password)
                  :username username}))
