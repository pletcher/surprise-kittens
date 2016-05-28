-- name: create-user<!
-- Creates a user
INSERT INTO users (email, password, username)
       VALUES (:email, :password, :username)

-- name: delete-user!
-- Deletes a user by id
DELETE FROM users WHERE id = :id

-- name: update-user-email!
-- Updates a user's email address
UPDATE users SET email = :email WHERE id = :id

-- name: update-user-password!
-- Updates a user's password
UPDATE users SET password = :password WHERE id = :id

-- name: find-user-by-email
-- Finds a user by `email`
SELECT * FROM users WHERE email = :email LIMIT 1

-- name: find-user-by-id
-- Finds a user by `id`
SELECT * FROM users WHERE id = :id LIMIT 1

-- name: find-user-by-username
-- Finds a user by `username`
SELECT * FROM users WHERE username = :username LIMIT 1
