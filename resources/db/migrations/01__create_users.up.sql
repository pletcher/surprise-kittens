CREATE TABLE users (
       id BIGSERIAL NOT NULL PRIMARY KEY,
       created_at TIMESTAMP NOT NULL DEFAULT now(),
       email VARCHAR(256) NOT NULL,
       password VARCHAR(256) NOT NULL CHECK (length(password) > 8),
       updated_at TIMESTAMP,
       username VARCHAR(64) NOT NULL CHECK (length(username) > 2),
       CONSTRAINT unique_email UNIQUE(email),
       CONSTRAINT unique_username UNIQUE(username)
);
