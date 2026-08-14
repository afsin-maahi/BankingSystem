# Banking Management System

A desktop-based Banking Management System built using **Java Swing, JDBC, and MySQL**. The application provides authentication, role-based access control, account management, fund transfers, and transaction history through a graphical user interface.

## Features

* User login and authentication
* Salted and hashed password storage
* Role-Based Access Control (RBAC)
* Three user roles:

  * **Admin**
  * **Teller**
  * **Customer**
* Create and manage bank accounts
* Deposit funds
* Withdraw funds
* Transfer funds between accounts
* View account information
* View transaction history
* Database-backed permissions
* Atomic fund transfers using JDBC transactions
* Service-layer authorization checks in addition to UI restrictions

## Technologies Used

* **Java 25**
* **Java Swing** — GUI
* **JDBC** — Java–MySQL connectivity
* **MySQL** — relational database
* **MySQL Connector/J** — JDBC driver

## Role-Based Access Control

The application uses a database-driven RBAC model.

| Role     | Access                                                                                          |
| -------- | ----------------------------------------------------------------------------------------------- |
| Admin    | Create/delete accounts, deposits, withdrawals, transfers, view accounts and transaction history |
| Teller   | Deposits, withdrawals, transfers, view accounts and transaction history                         |
| Customer | View own account, deposits, withdrawals, transfers and own transaction history                  |

Permissions are stored in the database using the `roles`, `permissions`, and `role_permissions` tables instead of relying only on hardcoded UI conditions.

## Database

The project uses a MySQL database named:

```text
banking_db
```

The database contains tables for:

* Users
* Roles
* Permissions
* Role permissions
* Accounts
* Transactions

The database structure is provided in:

```text
schema.sql
```

## Project Structure

```text
BankingManagementSystem/
│
├── README.md
├── schema.sql
│
└── src/
    ├── DBConnection.java
    ├── PasswordUtil.java
    ├── AppUser.java
    ├── AuthService.java
    ├── AccessControl.java
    ├── Account.java
    ├── AccountService.java
    ├── Transaction.java
    ├── SeedUsers.java
    ├── Main.java
    │
    └── ui/
        ├── LoginFrame.java
        ├── DashboardFrame.java
        ├── CreateAccountDialog.java
        ├── DepositWithdrawDialog.java
        ├── TransferFundsDialog.java
        ├── AccountListDialog.java
        └── TransactionHistoryDialog.java
```

## Requirements

Before running the application, install:

* JDK 17 or higher
* MySQL Server
* MySQL Workbench (recommended)
* MySQL Connector/J

The project was tested with **JDK 25** and **MySQL Connector/J 26.7.0**.

## Setup and Run — Windows

### 1. Create the Database

Open MySQL Workbench and create the database:

```sql
CREATE DATABASE banking_db;
```

Then select it:

```sql
USE banking_db;
```

Open the project's `schema.sql` file in MySQL Workbench and execute it.

Verify the tables:

```sql
USE banking_db;
SHOW TABLES;
```

You should have tables such as:

```text
accounts
permissions
role_permissions
roles
transactions
users
```

### 2. Configure MySQL Credentials

Open:

```text
src/DBConnection.java
```

Update the MySQL username and password for your local MySQL installation.

Example:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/banking_db";

private static final String USER = "root";

private static final String PASSWORD = "your_password";
```

**Do not commit your actual database password to GitHub.**

### 3. Download MySQL Connector/J

Download MySQL Connector/J from the official MySQL website.

Place the downloaded `.jar` file inside:

```text
src/
```

For example:

```text
src/
└── mysql-connector-j-26.7.0.jar
```

The JAR is required locally to compile and run the application.

### 4. Compile the Project

Open a terminal in the project directory and run:

```cmd
cd src
mkdir out
javac -cp ".;mysql-connector-j-26.7.0.jar" -d out *.java ui\*.java
```

If compilation succeeds, no error message will be displayed.

### 5. Create Test Users

Run:

```cmd
java -cp "out;mysql-connector-j-26.7.0.jar" SeedUsers
```

This creates the initial test users.

The seeded password is:

```text
password123
```

### 6. Start the Application

Run:

```cmd
java -cp "out;mysql-connector-j-26.7.0.jar" Main
```

The Banking Management System login window should appear.

## Test Accounts

After running `SeedUsers`:

| Username  | Role     | Password    |
| --------- | -------- | ----------- |
| admin     | Admin    | password123 |
| teller1   | Teller   | password123 |
| customer1 | Customer | password123 |

These accounts are provided only for local testing.

## Security

The project demonstrates several security concepts:

* Passwords are not stored as plaintext.
* Passwords are salted and hashed.
* Permissions are stored in the database.
* Authorization is checked at the service layer.
* Database operations use prepared statements.
* Fund transfers use database transactions to maintain consistency.

## Future Improvements

Possible improvements for a production-level system include:

* BCrypt or Argon2 password hashing
* Multi-factor authentication
* Account lockout after repeated failed login attempts
* Session/token management
* Better input validation
* Logging and monitoring
* Connection pooling
* REST API backend
* Automated unit and integration tests

