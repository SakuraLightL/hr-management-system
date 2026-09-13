CREATE TABLE departments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500)
);
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    provider_id VARCHAR(255),
    provider VARCHAR(50),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_provider_id UNIQUE (provider_id)
);
CREATE TABLE employees (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    position VARCHAR(100),
    department_id BIGINT,
    CONSTRAINT uk_employees_email UNIQUE (email),
    CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id)
);
