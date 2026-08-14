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
- JDK 17+ 
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

