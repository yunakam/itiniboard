CREATE TABLE plans (
                       plan_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       plan_name VARCHAR(100) NOT NULL,
                       plan_start_date DATE NOT NULL,
                       plan_end_date DATE NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT chk_plans_dates
                           CHECK (plan_end_date >= plan_start_date)
);

CREATE TABLE blocks (
                        block_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        block_type ENUM('activity', 'transfer') NOT NULL,
                        block_name VARCHAR(100) NOT NULL,
                        block_place VARCHAR(255) NULL,
                        block_details TEXT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE activities (
                            block_id BIGINT UNSIGNED PRIMARY KEY,
                            activity_type VARCHAR(30) NOT NULL,
                            activity_cost DECIMAL(12, 2) NULL,
                            activity_duration INT UNSIGNED NULL,
                            CONSTRAINT fk_activities_block
                                FOREIGN KEY (block_id)
                                    REFERENCES blocks (block_id)
                                    ON DELETE CASCADE
);

CREATE TABLE transfers (
                           block_id BIGINT UNSIGNED PRIMARY KEY,
                           transfer_departure VARCHAR(255) NOT NULL,
                           transfer_arrival VARCHAR(255) NOT NULL,
                           transfer_method VARCHAR(30) NULL,
                           transfer_cost DECIMAL(12, 2) NULL,
                           transfer_duration INT UNSIGNED NULL,
                           transfer_departure_time TIME NULL,
                           transfer_arrival_time TIME NULL,
                           CONSTRAINT fk_transfers_block
                               FOREIGN KEY (block_id)
                                   REFERENCES blocks (block_id)
                                   ON DELETE CASCADE
);

CREATE TABLE block_positions (
                                 position_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                 position_plan_id BIGINT UNSIGNED NOT NULL,
                                 position_block_id BIGINT UNSIGNED NOT NULL,
                                 position_day_number INT UNSIGNED NOT NULL,
                                 position_order INT UNSIGNED NOT NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                     ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT chk_block_positions_day_number
                                     CHECK (position_day_number >= 1),
                                 CONSTRAINT chk_block_positions_order
                                     CHECK (position_order >= 1),
                                 CONSTRAINT uq_block_positions_plan_block
                                     UNIQUE (position_plan_id, position_block_id),
                                 CONSTRAINT uq_block_positions_plan_day_order
                                     UNIQUE (position_plan_id, position_day_number, position_order),
                                 CONSTRAINT fk_block_positions_plan
                                     FOREIGN KEY (position_plan_id)
                                         REFERENCES plans (plan_id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_block_positions_block
                                     FOREIGN KEY (position_block_id)
                                         REFERENCES blocks (block_id)
                                         ON DELETE CASCADE
);

CREATE TABLE todos (
                       todo_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       todo_block_id BIGINT UNSIGNED NOT NULL,
                       todo_content VARCHAR(500) NOT NULL,
                       todo_deadline DATE NULL,
                       todo_status ENUM('undone', 'done') NOT NULL DEFAULT 'undone',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT fk_todos_block
                           FOREIGN KEY (todo_block_id)
                               REFERENCES blocks (block_id)
                               ON DELETE CASCADE
);