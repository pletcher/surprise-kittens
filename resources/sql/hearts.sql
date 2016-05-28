-- name: upsert-heart<!
-- Creates a heart belonging to a user and an image
INSERT INTO hearts (user_id, image_id)
       VALUES :user_id, :image_id
       ON CONFLICT DO UPDATE SET deleted_at = null

-- name: delete-heart-by-user-and-image!
-- Deletes a heart belonging to a user and an image
UPDATE hearts SET deleted_at = now()
       WHERE user_id = :user_id AND image_id = :image_id

-- name: delete-heart!
-- Deletes a heart by id
UPDATE hearts SET deleted_at = now() WHERE id = :id

-- name: hearts
-- Returns the image_ids for hearts where user_id = :user_id
SELECT id, image_id FROM hearts WHERE user_id = :user_id
