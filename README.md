# Banking Management System (Java, Swing, JDBC, MySQL)

A GUI banking app with real authentication and data-driven RBAC — three roles
(ADMIN, TELLER, CUSTOMER), each restricted to only the actions their role
permits, enforced at both the UI and the service layer.

## Features
- Login with salted, hashed passwords (no plaintext ever stored)
- Role-Based Access Control: permissions live in a `role_permissions` table,
  not hardcoded if/else — add a role or tighten a permission without touching code
- Create accounts, deposit, withdraw, transfer funds (atomic — both legs of a
  transfer commit together or neither does)
- Full transaction history per account (audit trail)
- Every sensitive action is double-checked: hidden in the UI *and* blocked at
  the service layer if bypassed

## Requirements
- JDK 17+ (you likely have this already — resume mentions VS Code/Maven)
- MySQL Server running locally
- MySQL Connector/J (JDBC driver) — download the jar from
  `https://dev.mysql.com/downloads/connector/j/` (choose "Platform Independent")

## Setup

**1. Create the database and load the schema**
```bash
mysql -u root -p -e "CREATE DATABASE banking_db"
mysql -u root -p banking_db < schema.sql
```

**2. Configure credentials**
Edit `src/DBConnection.java` — set your MySQL username/password.

**3. Compile**
```bash
cd src
javac -cp .:mysql-connector-j-9.x.x.jar -d out $(find . -name "*.java")
```
(On Windows, use `;` instead of `:` in the classpath.)

**4. Seed test users** (creates admin / teller1 / customer1, all with password `password123`)
```bash
java -cp out:mysql-connector-j-9.x.x.jar SeedUsers
```

**5. Run the app**
```bash
java -cp out:mysql-connector-j-9.x.x.jar Main
```

Log in as any of the three seeded users to see the role-gated menu change.

## Project structure
```
src/
  DBConnection.java       MySQL connection config
  PasswordUtil.java       Salted SHA-256 hashing
  AppUser.java             Logged-in user model
  AuthService.java         Register / login
  AccessControl.java       RBAC permission checks
  Account.java              Account model
  Transaction.java          Transaction model
  AccountService.java       Core banking ops (deposit/withdraw/transfer/history)
  SeedUsers.java             One-time test data setup
  Main.java                   Entry point
  ui/
    LoginFrame.java
    DashboardFrame.java     Role-gated menu
    CreateAccountDialog.java
    DepositWithdrawDialog.java
    TransferFundsDialog.java
    AccountListDialog.java
    TransactionHistoryDialog.java
```

## Resume bullets (once you've run it end-to-end)
- Built a GUI-based banking application in Java (Swing) with JDBC/MySQL backend,
  implementing salted-hash authentication, deposits, withdrawals, transfers, and
  transaction history.
- Designed a data-driven Role-Based Access Control (RBAC) model with least-privilege
  enforcement — Admin, Teller, and Customer roles each mapped to a distinct
  permission set stored in the database rather than hardcoded in application logic.
- Implemented atomic fund transfers using JDBC transactions (commit/rollback) to
  guarantee consistency between the debit and credit legs.
- Designed and normalized a relational MySQL schema (accounts, transactions, users,
  roles, permissions) with CRUD operations via JDBC PreparedStatement.

## Honest scope (know this before an interviewer asks)
- Password hashing is SHA-256 + salt, not BCrypt/Argon2 — fine for an academic
  project, and you should be ready to name BCrypt as the production-grade upgrade.
- No session tokens, no account lockout after failed logins, no MFA — reasonable
  "future work" answers if asked what you'd add with more time.
- This was rebuilt from scratch after the original (built in 2nd sem) was lost —
  if asked directly, just say that; it's a normal thing to happen, not something
  to hide or overstate.
