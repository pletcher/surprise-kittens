(ns surprise-kittens.models.user-test
  (:require [clojure.test :refer :all]
            [surprise-kittens.core-test :refer [*opts* with-transaction]]
            [surprise-kittens.models.user :as user]))

(use-fixtures :each with-transaction)

(defn- create-user [opts]
  (user/create-user {:email "email@email.com"
                     :password "password"
                     :username "username"}
    opts))

(deftest test-create-user
  (let [created-user (create-user *opts*)]
    (is (= (:email created-user) "email@email.com"))
    (is (= (:username created-user) "username"))
    (is (not= (:password created-user) "password"))))

(deftest test-email?
  (is (user/email? "email@email.com"))
  (is (nil? (user/email? "email"))))

(deftest test-find-user-by-email
  (let [created (create-user *opts*)
        u (user/find-user "email@email.com" *opts*)]
    (is (not= nil u))
    (is (= (:username u) (:username created)))))

(deftest test-find-user-by-username
  (let [created (create-user *opts*)
        u (user/find-user "username" *opts*)]
    (is (not= nil u))
    (is (= (:email u) (:email created)))))

(deftest test-authenticate-user
  (let [created (create-user *opts*)
        u (user/authenticate-user "username" "password" *opts*)
        nu (user/authenticate-user "username" "wrong" *opts*)]
    (is (not= nil u))
    (is (nil? nu))))

(deftest test-delete-user
  (let [created (create-user *opts*)
        id (:id created)]
    (user/delete-user! {:id id} *opts*)
    (let [u (first (user/find-user-by-id {:id id} *opts*))]
      (is (nil? u)))))
