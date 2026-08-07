ALTER TABLE todos
    ADD COLUMN is_completed BOOLEAN NOT NULL DEFAULT FALSE
    AFTER todo_deadline;

UPDATE todos
SET is_completed = (todo_status = 'done');

ALTER TABLE todos
DROP COLUMN todo_status;