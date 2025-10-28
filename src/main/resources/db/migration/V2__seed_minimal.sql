INSERT INTO role(name) VALUES ('ADMIN'), ('SALES');

INSERT INTO app_user (username, password_hash, email)
VALUES ('admin', '{noop}admin', 'admin@catconnect.local');

INSERT INTO user_role(user_id, role_id)
SELECT u.id, r.id FROM app_user u, role r WHERE u.username='admin' AND r.name='ADMIN';

INSERT INTO customer(code, name, status) VALUES ('CUST-001', 'CatConnect Test', 'LEAD');

INSERT INTO contact(customer_id, first_name, last_name, email)
SELECT id, 'Meow', 'Owner', 'owner@example.com' FROM customer WHERE code='CUST-001';
