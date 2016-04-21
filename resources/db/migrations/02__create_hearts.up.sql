CREATE TABLE hearts (
       id BIGSERIAL NOT NULL PRIMARY KEY,
       image_id VARCHAR(255) NOT NULL,
       user_id BIGSERIAL NOT NULL,
       UNIQUE(image_id, user_id)
);
