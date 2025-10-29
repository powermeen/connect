-- =========================================================
-- CatConnect CRM - Initial schema (multi-tenant, row-level)
-- - Every business table has company_id
-- - All FKs are tenant-safe using composite (company_id, id)
-- - Indexes start with company_id for fast pruning
-- =========================================================

-- =============== TENANT (COMPANY) ========================
CREATE TABLE IF NOT EXISTS company
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    code       VARCHAR(64)  NOT NULL UNIQUE, -- e.g., CAT-A, CAT-B
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =============== USERS / ROLES ===========================
CREATE TABLE IF NOT EXISTS app_user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id    BIGINT       NOT NULL,
    username      VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(190),
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    -- tenant-scope uniqueness & FK support
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company (id),
    UNIQUE KEY ux_user_company_id (company_id, id),
    UNIQUE KEY ux_user_company_username (company_id, username),
    UNIQUE KEY ux_user_company_email (company_id, email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS role
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_role_company FOREIGN KEY (company_id) REFERENCES company (id),
    UNIQUE KEY ux_role_company_id (company_id, id),
    UNIQUE KEY ux_role_company_name (company_id, name) -- role names unique per company
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- tenant-scoped user <-> role mapping
CREATE TABLE IF NOT EXISTS user_role
(
    company_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
    PRIMARY KEY (company_id, user_id, role_id),

    CONSTRAINT fk_user_role_user
        FOREIGN KEY (company_id, user_id)
            REFERENCES app_user (company_id, id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (company_id, role_id)
            REFERENCES role (company_id, id)
            ON DELETE CASCADE,

    INDEX ix_user_role_user (company_id, user_id),
    INDEX ix_user_role_role (company_id, role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- =============== CRM CORE ================================

-- CUSTOMER (owned by a user of the same company)
CREATE TABLE IF NOT EXISTS customer
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT       NOT NULL,
    code       VARCHAR(50), -- unique per company
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(190),
    phone      VARCHAR(50),
    status     ENUM ('LEAD','PROSPECT','ACTIVE','INACTIVE') DEFAULT 'LEAD',
    owner_id   BIGINT,      -- app_user.id within same company
    created_at TIMESTAMP    NOT NULL                        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NULL                            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_customer_company FOREIGN KEY (company_id) REFERENCES company (id),
    -- tenant-safe FK to app_user
    CONSTRAINT fk_customer_owner FOREIGN KEY (company_id, owner_id)
        REFERENCES app_user (company_id, id),

    UNIQUE KEY ux_customer_company_id (company_id, id),
    UNIQUE KEY ux_customer_company_code (company_id, code),
    INDEX ix_customer_company_owner (company_id, owner_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- CONTACT (belongs to a customer in same company)
CREATE TABLE IF NOT EXISTS contact
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id  BIGINT    NOT NULL,
    customer_id BIGINT    NOT NULL,
    first_name  VARCHAR(120),
    last_name   VARCHAR(120),
    email       VARCHAR(190),
    phone       VARCHAR(50),
    position    VARCHAR(120),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_contact_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_contact_customer FOREIGN KEY (company_id, customer_id)
        REFERENCES customer (company_id, id) ON DELETE CASCADE,

    UNIQUE KEY ux_contact_company_id (company_id, id),
    INDEX ix_contact_company_customer (company_id, customer_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- DEAL (belongs to a customer; owned by a user; same company)
CREATE TABLE IF NOT EXISTS deal
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id  BIGINT       NOT NULL,
    customer_id BIGINT       NOT NULL,
    title       VARCHAR(255) NOT NULL,
    amount      DECIMAL(14, 2)                                   DEFAULT 0,
    stage       ENUM ('NEW','QUALIFIED','PROPOSAL','WON','LOST') DEFAULT 'NEW',
    close_date  DATE,
    owner_id    BIGINT,
    created_at  TIMESTAMP    NOT NULL                            DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NULL                                DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_deal_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_deal_customer FOREIGN KEY (company_id, customer_id)
        REFERENCES customer (company_id, id) ON DELETE CASCADE,
    CONSTRAINT fk_deal_owner FOREIGN KEY (company_id, owner_id)
        REFERENCES app_user (company_id, id),

    UNIQUE KEY ux_deal_company_id (company_id, id),
    INDEX ix_deal_company_customer (company_id, customer_id),
    INDEX ix_deal_company_owner (company_id, owner_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- ACTIVITY (may reference customer and/or deal; created by user; same company)
CREATE TABLE IF NOT EXISTS activity
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id  BIGINT                                 NOT NULL,
    customer_id BIGINT                                 NULL,
    deal_id     BIGINT                                 NULL,
    type        ENUM ('CALL','EMAIL','MEETING','NOTE') NOT NULL,
    subject     VARCHAR(255),
    content     TEXT,
    due_at      DATETIME,
    done        TINYINT(1)                                      DEFAULT 0,
    created_by  BIGINT,
    created_at  TIMESTAMP                              NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_activity_company FOREIGN KEY (company_id) REFERENCES company (id),

    -- CHANGED: SET NULL -> CASCADE (company_id stays NOT NULL)
    CONSTRAINT fk_activity_customer FOREIGN KEY (company_id, customer_id)
        REFERENCES customer (company_id, id) ON DELETE CASCADE,

    -- CHANGED: SET NULL -> CASCADE
    CONSTRAINT fk_activity_deal FOREIGN KEY (company_id, deal_id)
        REFERENCES deal (company_id, id) ON DELETE CASCADE,

    CONSTRAINT fk_activity_user FOREIGN KEY (company_id, created_by)
        REFERENCES app_user (company_id, id),

    UNIQUE KEY ux_activity_company_id (company_id, id),
    INDEX ix_activity_company_customer (company_id, customer_id),
    INDEX ix_activity_company_deal (company_id, deal_id),
    INDEX ix_activity_company_user (company_id, created_by),
    INDEX ix_activity_company_due (company_id, due_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

