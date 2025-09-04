-- Remove FK user_roles -> users se existir
SET @fk := (SELECT CONSTRAINT_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'user_roles'
              AND COLUMN_NAME = 'user_id'
              AND REFERENCED_TABLE_NAME = 'users'
            LIMIT 1);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE user_roles DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Corrige tipos
ALTER TABLE users      MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE user_roles MODIFY COLUMN user_id BIGINT NOT NULL;

-- Recria FK
ALTER TABLE user_roles
  ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;