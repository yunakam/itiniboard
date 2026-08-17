ALTER TABLE blocks
    ADD COLUMN block_cost DECIMAL(12, 2) NULL AFTER block_details,
    ADD COLUMN block_duration INT UNSIGNED NULL AFTER block_cost;

ALTER TABLE activities
    MODIFY activity_type VARCHAR(30) NULL;

UPDATE blocks b
    INNER JOIN activities a ON a.block_id = b.block_id
    SET b.block_cost = a.activity_cost,
        b.block_duration = a.activity_duration;

UPDATE blocks b
    INNER JOIN transfers t ON t.block_id = b.block_id
    SET b.block_cost = t.transfer_cost,
        b.block_duration = t.transfer_duration;

ALTER TABLE activities
DROP COLUMN activity_cost,
    DROP COLUMN activity_duration;

ALTER TABLE transfers
DROP COLUMN transfer_cost,
    DROP COLUMN transfer_duration;