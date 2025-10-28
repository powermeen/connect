DROP USER IF EXISTS 'catconnect_user'@'%';
CREATE USER 'catconnect_user'@'%' IDENTIFIED BY 'StrongPass#123';
GRANT ALL PRIVILEGES ON catconnect_db.* TO 'catconnect_user'@'%';
SHOW GRANTS FOR 'catconnect_user'@'%';