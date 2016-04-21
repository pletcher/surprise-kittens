-- name: upsert-heart<!
-- Creates a heart belonging to a user and an image
INSERT INTO hearts (user_id, image_id)
       VALUES :user_id, :image_id
       ON CONFLICT DO UPDATE SET deleted_at = null

-- name: delete-heart!
-- Deletes a heart belonging to a user and an image
UPDATE hearts SET deleted_at = now()
       WHERE user_id = :user_id AND image_id = :image_id
