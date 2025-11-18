DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS invoice_items;

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

CREATE TABLE IF NOT EXISTS invoice_items
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)    NOT NULL,
    description VARCHAR(200),
    hsn_code    VARCHAR(6)     NOT NULL,
    quantity    DECIMAL(10, 2) NOT NULL,
    unit        VARCHAR(10)    NOT NULL,
    unit_price  DECIMAL(10, 2) NOT NULL,
    cost_price  DECIMAL(10, 2) NOT NULL DEFAULT 0,
    line_total  DECIMAL(10, 2) NOT NULL,
    type        VARCHAR(10),
    gst         DECIMAL(5, 2)  NOT NULL,
    vendor_name VARCHAR(100)
);