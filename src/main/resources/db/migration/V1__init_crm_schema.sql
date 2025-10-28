-- users/roles (optional, useful later)
CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(190) UNIQUE,
    enabled       TINYINT(1) NOT NULL DEFAULT 1,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS role (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_role (
                                         user_id BIGINT NOT NULL,
                                         role_id BIGINT NOT NULL,
                                         PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

-- crm core
CREATE TABLE IF NOT EXISTS customer (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        code   VARCHAR(50) UNIQUE,
    name   VARCHAR(255) NOT NULL,
    email  VARCHAR(190),
    phone  VARCHAR(50),
    status ENUM('LEAD','PROSPECT','ACTIVE','INACTIVE') DEFAULT 'LEAD',
    owner_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX ix_customer_owner (owner_id),
    CONSTRAINT fk_customer_owner FOREIGN KEY (owner_id) REFERENCES app_user(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS contact (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       customer_id BIGINT NOT NULL,
                                       first_name VARCHAR(120),
    last_name  VARCHAR(120),
    email      VARCHAR(190),
    phone      VARCHAR(50),
    position   VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX ix_contact_customer (customer_id),
    CONSTRAINT fk_contact_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS deal (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    customer_id BIGINT NOT NULL,
                                    title   VARCHAR(255) NOT NULL,
    amount  DECIMAL(14,2) DEFAULT 0,
    stage   ENUM('NEW','QUALIFIED','PROPOSAL','WON','LOST') DEFAULT 'NEW',
    close_date DATE,
    owner_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX ix_deal_customer (customer_id),
    INDEX ix_deal_owner (owner_id),
    CONSTRAINT fk_deal_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_deal_owner FOREIGN KEY (owner_id) REFERENCES app_user(id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS activity (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        customer_id BIGINT,
                                        deal_id     BIGINT,
                                        type   ENUM('CALL','EMAIL','MEETING','NOTE') NOT NULL,
    subject VARCHAR(255),
    content TEXT,
    due_at  DATETIME,
    done    TINYINT(1) DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX ix_activity_customer (customer_id),
    INDEX ix_activity_deal (deal_id),
    CONSTRAINT fk_activity_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_deal FOREIGN KEY (deal_id) REFERENCES deal(id) ON DELETE SET NULL,
    CONSTRAINT fk_activity_user FOREIGN KEY (created_by) REFERENCES app_user(id)
    ) ENGINE=InnoDB;
