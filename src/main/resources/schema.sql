DROP TABLE IF EXISTS customers;

CREATE TABLE IF NOT EXISTS customers
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255),
    phone       VARCHAR(20),
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100) NOT NULL,
    postal_code VARCHAR(6)   NOT NULL,
    country     VARCHAR(20)  NOT NULL DEFAULT 'India',
    type        VARCHAR(20)  NOT NULL,
    gstin       VARCHAR(15)  NOT NULL UNIQUE
);