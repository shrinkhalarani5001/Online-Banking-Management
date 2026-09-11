# Online Banking Management System

Java Full Stack project using Java 21, Spring Boot, Thymeleaf, MySQL, JPA and Maven.

## Features
- Customer registration and login
- Automatic bank account creation
- Customer dashboard
- Balance checking
- Deposit
- Withdrawal
- Money transfer between accounts
- Transaction history
- Customer profile
- Admin dashboard
- Customer activate/block
- Account activate/block
- View all transactions
- BCrypt password hashing

## Requirements
- JDK 21
- IntelliJ IDEA
- MySQL 8+
- Maven (or use IntelliJ Maven wrapper/import)

## Database setup
Create the database:

```sql
CREATE DATABASE online_banking;
```

Default configuration is in:
`src/main/resources/application.properties`

If your MySQL password is not empty, change:
`spring.datasource.password=`

## Run
Open the project in IntelliJ and run:

`OnlineBankingApplication.java`

Then open:

`http://localhost:8080`

## Admin login
Username: `admin`
Password: `Admin@2026`

The admin user is created automatically on first startup.

## Customer
Click `Create an account`, register, then login.

A savings account is automatically created for the customer.

## GitHub
Do not commit real production passwords, database credentials, API keys or secrets. For a demo project, use environment variables or a local application.properties file when deploying for real users.
