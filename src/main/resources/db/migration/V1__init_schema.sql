CREATE TABLE IF NOT EXISTS customers
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    email              VARCHAR(255),
    phone              VARCHAR(20),
    street             VARCHAR(255) NOT NULL,
    city               VARCHAR(100) NOT NULL,
    state              VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(6)   NOT NULL,
    country            VARCHAR(20)  NOT NULL DEFAULT 'India',
    type               VARCHAR(20)  NOT NULL,
    gstin              VARCHAR(15)  NOT NULL UNIQUE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            INTEGER      NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS products
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(50)    NOT NULL,
    description        VARCHAR(500),
    hsn_code           VARCHAR(6)     NOT NULL,
    unit               VARCHAR(10)    NOT NULL,
    unit_price         DECIMAL(10, 2) NOT NULL,
    cost_price         DECIMAL(10, 2) NOT NULL,
    type               VARCHAR(10),
    gst_rate           DECIMAL(5, 2)  NOT NULL,
    vendor_name        VARCHAR(100),
    created_date       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            INTEGER        NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS invoices
(
    id                  BIGSERIAL PRIMARY KEY,
    invoice_number      VARCHAR(50),
    invoice_date        DATE,
    billed_to           BIGINT REFERENCES customers (id),
    shipped_to          BIGINT REFERENCES customers (id),
    place               VARCHAR(100),
    transport           VARCHAR(20),
    courier_name        VARCHAR(100),
    vehicle_number      VARCHAR(50),
    e_way_bill_number   VARCHAR(50),
    package_count       INTEGER,
    subtotal            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_percentage DECIMAL(5, 2)  DEFAULT 0,
    total_tax           DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(12, 2) NOT NULL DEFAULT 0,
    created_date        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER        NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS line_items
(
    id              BIGSERIAL PRIMARY KEY,
    invoice_id      BIGINT         NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    line_order      INTEGER        NOT NULL,
    product_id      BIGINT         NOT NULL REFERENCES products (id),
    quantity        INTEGER        NOT NULL,
    unit_price      DECIMAL(12, 2) NOT NULL,
    gst_rate        DECIMAL(5, 2)  NOT NULL,
    tax_amount      DECIMAL(12, 2) NOT NULL,
    total_amount    DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uq_line_item UNIQUE (invoice_id, line_order)
);

-- Optional index to speed up lookups by invoice
CREATE INDEX IF NOT EXISTS idx_line_items_invoice_id ON line_items(invoice_id);
