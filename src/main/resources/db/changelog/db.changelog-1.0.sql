-- liquibase formatted sql

-- changeset dimka:1
CREATE TABLE IF NOT EXISTS users(
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
