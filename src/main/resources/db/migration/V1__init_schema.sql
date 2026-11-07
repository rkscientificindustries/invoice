CREATE TABLE IF NOT EXISTS customers
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    email              VARCHAR(255),
    phone              VARCHAR(20),
    street             VARCHAR(255) NOT NULL,
    city               VARCHAR(255) NOT NULL,
    state              VARCHAR(100) NOT NULL,
    postal_code        VARCHAR(6)   NOT NULL,
    country            VARCHAR(20)  NOT NULL DEFAULT 'India',
    type               VARCHAR(20)  NOT NULL,
    gstin              VARCHAR(15)  NOT NULL UNIQUE,
    created_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT chk_customers_type
        CHECK (type IN ('BUSINESS', 'INDIVIDUAL')),
    CONSTRAINT chk_customers_state
        CHECK (state IN (
            'ANDHRA_PRADESH', 'ARUNACHAL_PRADESH', 'ASSAM', 'BIHAR', 'CHHATTISGARH', 'GOA', 'GUJARAT', 'HARYANA',
            'HIMACHAL_PRADESH', 'JHARKHAND', 'KARNATAKA', 'KERALA', 'MADHYA_PRADESH', 'MAHARASHTRA', 'MANIPUR',
            'MEGHALAYA', 'MIZORAM', 'NAGALAND', 'ODISHA', 'PUNJAB', 'RAJASTHAN', 'SIKKIM', 'TAMIL_NADU',
            'TELANGANA', 'TRIPURA', 'UTTAR_PRADESH', 'UTTARAKHAND', 'WEST_BENGAL', 'ANDAMAN_AND_NICOBAR_ISLANDS',
            'CHANDIGARH', 'DNHDD', 'DELHI', 'JAMMU_AND_KASHMIR', 'LADAKH', 'LAKSHADWEEP', 'PUDUCHERRY'
        )),
    CONSTRAINT chk_customers_gstin
        CHECK (gstin ~ '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'),
    CONSTRAINT chk_customers_postal_code
        CHECK (postal_code ~ '^[1-9][0-9]{5}$')
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
    type               VARCHAR(10)    NOT NULL,
    gst_rate           DECIMAL(5, 2)  NOT NULL,
    vendor_name        VARCHAR(100),
    created_date       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            INTEGER        NOT NULL DEFAULT 0,
    CONSTRAINT chk_products_hsn_code
        CHECK (hsn_code ~ '^([0-9]{4}|[0-9]{6})$'),
    CONSTRAINT chk_products_unit
        CHECK (unit IN ('PAIR', 'PCS', 'GM', 'KG', 'ML', 'L')),
    CONSTRAINT chk_products_type
        CHECK (type IN ('BO', 'RM', 'MA')),
    CONSTRAINT chk_products_unit_price
        CHECK (unit_price >= 0),
    CONSTRAINT chk_products_cost_price
        CHECK (cost_price >= 0),
    CONSTRAINT chk_products_gst_rate
        CHECK (gst_rate >= 0)
);

CREATE TABLE IF NOT EXISTS invoices
(
    id                   BIGSERIAL PRIMARY KEY,
    invoice_number       BIGSERIAL,
    invoice_date         DATE,
    billed_to            BIGINT REFERENCES customers (id),
    shipped_to           BIGINT REFERENCES customers (id),
    place                VARCHAR(100),
    transport            VARCHAR(20),
    courier_name         VARCHAR(100),
    vehicle_number       VARCHAR(50),
    e_way_bill_number    VARCHAR(50),
    package_count        INTEGER,
    subtotal             DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_percentage  DECIMAL(5, 2)           DEFAULT 0,
    total_tax            DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount         DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status               VARCHAR(20)    NOT NULL,
    created_date         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version              INTEGER        NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS line_items
(
    id           BIGSERIAL PRIMARY KEY,
    invoice_id   BIGINT         NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    line_order   INTEGER        NOT NULL,
    product_id   BIGINT         NOT NULL REFERENCES products (id),
    quantity     INTEGER        NOT NULL,
    unit_price   DECIMAL(12, 2) NOT NULL,
    gst_rate     DECIMAL(5, 2)  NOT NULL,
    tax_amount   DECIMAL(12, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uq_line_item UNIQUE (invoice_id, line_order)
);

-- Optional index to speed up lookups by invoice
CREATE INDEX IF NOT EXISTS idx_line_items_invoice_id ON line_items (invoice_id);
