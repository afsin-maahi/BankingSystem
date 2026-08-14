-- ============================================================
-- Banking Management System — full schema
-- Run this whole file against a fresh MySQL database, e.g.:
--   mysql -u root -p -e "CREATE DATABASE banking_db"
--   mysql -u root -p banking_db < schema.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS accounts (
    account_id      INT PRIMARY KEY AUTO_INCREMENT,
    holder_name     VARCHAR(100) NOT NULL,
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      INT PRIMARY KEY AUTO_INCREMENT,
    account_id          INT NOT NULL,
    type                VARCHAR(20) NOT NULL,   -- DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    amount              DECIMAL(15,2) NOT NULL,
    related_account_id  INT NULL,               -- set for TRANSFER_IN / TRANSFER_OUT
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

CREATE TABLE IF NOT EXISTS roles (
    role_id     INT PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(30) NOT NULL UNIQUE   -- ADMIN, TELLER, CUSTOMER
);

INSERT INTO roles (role_name) VALUES ('ADMIN'), ('TELLER'), ('CUSTOMER')
ON DUPLICATE KEY UPDATE role_name = role_name;

CREATE TABLE IF NOT EXISTS users (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(128) NOT NULL,
    salt            VARCHAR(32)  NOT NULL,
    role_id         INT NOT NULL,
    account_id      INT NULL,               -- links a CUSTOMER to their account; NULL for ADMIN/TELLER
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (account_id) REFERENCES accounts(account_id)
);

CREATE TABLE IF NOT EXISTS permissions (
    permission_id   INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO permissions (permission_name) VALUES
    ('VIEW_ALL_ACCOUNTS'), ('CREATE_ACCOUNT'), ('DELETE_ACCOUNT'),
    ('VIEW_OWN_ACCOUNT'), ('DEPOSIT'), ('WITHDRAW'), ('TRANSFER_FUNDS'), ('VIEW_TRANSACTION_HISTORY')
ON DUPLICATE KEY UPDATE permission_name = permission_name;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id         INT NOT NULL,
    permission_id   INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

-- ADMIN: everything
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p WHERE r.role_name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- TELLER: process transactions + view all, no create/delete accounts
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'TELLER'
  AND p.permission_name IN ('VIEW_ALL_ACCOUNTS','DEPOSIT','WITHDRAW','TRANSFER_FUNDS','VIEW_TRANSACTION_HISTORY')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- CUSTOMER: only their own account
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM roles r, permissions p
WHERE r.role_name = 'CUSTOMER'
  AND p.permission_name IN ('VIEW_OWN_ACCOUNT','DEPOSIT','WITHDRAW','TRANSFER_FUNDS','VIEW_TRANSACTION_HISTORY')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ============================================================
-- Seed data so you can log in immediately.
-- Password for every seed user below is:  password123
-- (hash/salt generated with the app's own PasswordUtil — see README)
-- ============================================================
-- NOTE: placeholder hash/salt below — replace by running
-- SeedUsers.java (included) which inserts real hashes for you.
