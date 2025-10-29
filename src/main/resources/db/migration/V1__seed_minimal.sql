-- =========================================================
-- CatConnect CRM - Starter Data (multi-tenant example)
-- Two companies: CAT-A and CAT-B
-- Each company has its own users, roles, and CRM data
-- =========================================================

-- ---------- COMPANY ----------
INSERT INTO company (code, name)
VALUES
    ('CAT-A', 'CatConnect Co. A'),
    ('CAT-B', 'CatConnect Co. B');

-- ---------- APP_USER ----------
INSERT INTO app_user (company_id, username, password_hash, email, enabled)
VALUES
    (1, 'admin_a',  '$2a$10$abcdefg1234567890abcdefg1234567890abcdefg1234567890abc', 'adminA@cat.com', 1),
    (1, 'user_a1',  '$2a$10$abcdefg1234567890abcdefg1234567890abcdefg1234567890abc', 'userA1@cat.com', 1),
    (2, 'admin_b',  '$2a$10$abcdefg1234567890abcdefg1234567890abcdefg1234567890abc', 'adminB@cat.com', 1),
    (2, 'user_b1',  '$2a$10$abcdefg1234567890abcdefg1234567890abcdefg1234567890abc', 'userB1@cat.com', 1);

-- ---------- ROLE ----------
INSERT INTO role (company_id, name)
VALUES
    (1, 'ADMIN'),
    (1, 'SALES'),
    (1, 'SUPPORT'),
    (2, 'ADMIN'),
    (2, 'SALES');

-- ---------- USER_ROLE ----------
-- Company A mappings
INSERT INTO user_role (company_id, user_id, role_id)
VALUES
    (1, 1, 1), -- admin_a → ADMIN
    (1, 2, 2); -- user_a1 → SALES

-- Company B mappings
INSERT INTO user_role (company_id, user_id, role_id)
VALUES
    (2, 3, 4), -- admin_b → ADMIN
    (2, 4, 5); -- user_b1 → SALES

-- ---------- CUSTOMER ----------
INSERT INTO customer (company_id, code, name, email, phone, status, owner_id)
VALUES
    (1, 'CUST-A1', 'Alpha Co.', 'alpha@company.com', '081-111-1111', 'ACTIVE', 2),
    (1, 'CUST-A2', 'Beta Trading', 'beta@company.com', '081-222-2222', 'PROSPECT', 2),
    (2, 'CUST-B1', 'Omega Group', 'omega@group.com', '082-333-3333', 'LEAD', 4);

-- ---------- CONTACT ----------
INSERT INTO contact (company_id, customer_id, first_name, last_name, email, phone, position)
VALUES
    (1, 1, 'Somchai', 'Techawan', 'somchai@alpha.com', '081-555-1111', 'Manager'),
    (1, 2, 'Anucha', 'Boonmee', 'anucha@beta.com', '081-555-2222', 'Sales Rep'),
    (2, 3, 'Suda', 'Thongdee', 'suda@omega.com', '082-555-3333', 'Procurement');

-- ---------- DEAL ----------
INSERT INTO deal (company_id, customer_id, title, amount, stage, close_date, owner_id)
VALUES
    (1, 1, 'Alpha Renewal', 50000.00, 'PROPOSAL', '2025-11-30', 2),
    (1, 2, 'Beta Starter Kit', 12000.00, 'QUALIFIED', '2025-12-15', 2),
    (2, 3, 'Omega Contract', 80000.00, 'NEW', '2025-12-20', 4);

-- ---------- ACTIVITY ----------
INSERT INTO activity (company_id, customer_id, deal_id, type, subject, content, due_at, done, created_by)
VALUES
    (1, 1, 1, 'CALL', 'Follow up Alpha Renewal', 'Discuss proposal details', '2025-11-05 10:00:00', 0, 2),
    (1, 2, 2, 'EMAIL', 'Send quotation to Beta', 'Attached quotation PDF', '2025-11-02 14:00:00', 1, 2),
    (2, 3, 3, 'MEETING', 'Kickoff with Omega', 'Initial project briefing', '2025-11-10 09:00:00', 0, 4);
