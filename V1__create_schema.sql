-- V1: full schema. MySQL 8, InnoDB, utf8mb4. Enums are stored as VARCHAR (EnumType.STRING).
-- Timestamps are DATETIME(6) holding UTC values (Hibernate jdbc.time_zone=UTC). Dates are DATE.

CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'STAFF', 'DONOR', 'HOSPITAL'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE blood_group (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    code VARCHAR(3)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_blood_group_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- One row per (recipient, donor) pair: the recipient group may receive red cells from the donor group.
CREATE TABLE blood_compatibility (
    recipient_group_id BIGINT NOT NULL,
    donor_group_id     BIGINT NOT NULL,
    PRIMARY KEY (recipient_group_id, donor_group_id),
    CONSTRAINT fk_compat_recipient FOREIGN KEY (recipient_group_id) REFERENCES blood_group (id),
    CONSTRAINT fk_compat_donor     FOREIGN KEY (donor_group_id)     REFERENCES blood_group (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE donor (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    user_id            BIGINT        NOT NULL,
    blood_group_id     BIGINT        NOT NULL,
    dob                DATE          NOT NULL,
    gender             VARCHAR(10)   NOT NULL,
    weight_kg          DECIMAL(5, 2) NOT NULL,
    phone              VARCHAR(20)   NOT NULL,
    city               VARCHAR(100)  NOT NULL,
    last_donation_date DATE          NULL,
    deferred_until     DATE          NULL,
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_donor_user UNIQUE (user_id),
    CONSTRAINT fk_donor_user  FOREIGN KEY (user_id)        REFERENCES users (id),
    CONSTRAINT fk_donor_group FOREIGN KEY (blood_group_id) REFERENCES blood_group (id),
    CONSTRAINT ck_donor_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT ck_donor_weight CHECK (weight_kg > 0),
    INDEX idx_donor_group (blood_group_id),
    INDEX idx_donor_city (city)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE donation (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    donor_id         BIGINT       NOT NULL,
    donation_date    DATE         NOT NULL,
    volume_ml        INT          NOT NULL,
    screening_status VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    failure_reason   VARCHAR(500) NULL,
    recorded_by      BIGINT       NOT NULL,
    notes            VARCHAR(1000) NULL,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_donation_donor    FOREIGN KEY (donor_id)    REFERENCES donor (id),
    CONSTRAINT fk_donation_recorder FOREIGN KEY (recorded_by) REFERENCES users (id),
    CONSTRAINT ck_donation_volume CHECK (volume_ml BETWEEN 300 AND 500),
    CONSTRAINT ck_donation_screening CHECK (screening_status IN ('PENDING', 'PASSED', 'FAILED')),
    INDEX idx_donation_donor_date (donor_id, donation_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- One row per blood bag. Stock is always COUNT(*) over this table, never a counter.
CREATE TABLE blood_inventory (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    unit_number     VARCHAR(30)  NOT NULL,
    blood_group_id  BIGINT       NOT NULL,
    donation_id     BIGINT       NOT NULL,
    collection_date DATE         NOT NULL,
    expiry_date     DATE         NOT NULL,
    status          VARCHAR(10)  NOT NULL DEFAULT 'AVAILABLE',
    discard_reason  VARCHAR(500) NULL,
    discarded_by    BIGINT       NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_unit_number UNIQUE (unit_number),
    CONSTRAINT uk_inventory_donation    UNIQUE (donation_id),
    CONSTRAINT fk_inventory_group     FOREIGN KEY (blood_group_id) REFERENCES blood_group (id),
    CONSTRAINT fk_inventory_donation  FOREIGN KEY (donation_id)    REFERENCES donation (id),
    CONSTRAINT fk_inventory_discarder FOREIGN KEY (discarded_by)   REFERENCES users (id),
    CONSTRAINT ck_inventory_dates  CHECK (expiry_date > collection_date),
    CONSTRAINT ck_inventory_status CHECK (status IN ('AVAILABLE', 'ISSUED', 'EXPIRED', 'DISCARDED')),
    INDEX idx_inventory_fefo (blood_group_id, status, expiry_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE hospital (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(200) NOT NULL,
    license_no      VARCHAR(60)  NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    city            VARCHAR(100) NOT NULL,
    address         VARCHAR(300) NULL,
    approval_status VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    decision_reason VARCHAR(500) NULL,
    decided_by      BIGINT       NULL,
    decided_at      DATETIME(6)  NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_hospital_user    UNIQUE (user_id),
    CONSTRAINT uk_hospital_license UNIQUE (license_no),
    CONSTRAINT fk_hospital_user    FOREIGN KEY (user_id)    REFERENCES users (id),
    CONSTRAINT fk_hospital_decider FOREIGN KEY (decided_by) REFERENCES users (id),
    CONSTRAINT ck_hospital_approval CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    INDEX idx_hospital_approval (approval_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE blood_request (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    request_no       VARCHAR(30)  NOT NULL,
    hospital_id      BIGINT       NOT NULL,
    blood_group_id   BIGINT       NOT NULL,
    units_requested  INT          NOT NULL,
    priority         VARCHAR(10)  NOT NULL,
    status           VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    required_by      DATETIME(6)  NOT NULL,
    patient_note     VARCHAR(500) NULL,
    rejection_reason VARCHAR(500) NULL,
    idempotency_key  VARCHAR(64)  NULL,
    decided_by       BIGINT       NULL,
    decided_at       DATETIME(6)  NULL,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_request_no UNIQUE (request_no),
    CONSTRAINT uk_request_idempotency UNIQUE (hospital_id, idempotency_key),
    CONSTRAINT fk_request_hospital FOREIGN KEY (hospital_id)    REFERENCES hospital (id),
    CONSTRAINT fk_request_group    FOREIGN KEY (blood_group_id) REFERENCES blood_group (id),
    CONSTRAINT fk_request_decider  FOREIGN KEY (decided_by)     REFERENCES users (id),
    CONSTRAINT ck_request_units    CHECK (units_requested BETWEEN 1 AND 10),
    CONSTRAINT ck_request_priority CHECK (priority IN ('EMERGENCY', 'URGENT', 'NORMAL')),
    CONSTRAINT ck_request_status   CHECK (status IN ('PENDING', 'APPROVED', 'FULFILLED', 'REJECTED', 'CANCELLED')),
    INDEX idx_request_queue (status, priority, required_by),
    INDEX idx_request_hospital (hospital_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE blood_issue (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    request_id   BIGINT      NOT NULL,
    inventory_id BIGINT      NOT NULL,
    issued_by    BIGINT      NOT NULL,
    issued_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_issue_inventory UNIQUE (inventory_id),
    CONSTRAINT fk_issue_request   FOREIGN KEY (request_id)   REFERENCES blood_request (id),
    CONSTRAINT fk_issue_inventory FOREIGN KEY (inventory_id) REFERENCES blood_inventory (id),
    CONSTRAINT fk_issue_issuer    FOREIGN KEY (issued_by)    REFERENCES users (id),
    INDEX idx_issue_request (request_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE notification (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    VARCHAR(1000) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    ref_type   VARCHAR(50)  NULL,
    ref_id     BIGINT       NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_notification_inbox (user_id, is_read, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Append-only: the application never issues UPDATE or DELETE here (see handoff notes).
-- actor_id is NULL for system actions (scheduled jobs).
CREATE TABLE audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor_id    BIGINT       NULL,
    action      VARCHAR(60)  NOT NULL,
    entity_type VARCHAR(60)  NOT NULL,
    entity_id   BIGINT       NULL,
    details     TEXT         NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users (id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE outbox_event (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSON         NOT NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at DATETIME(6)  NULL,
    PRIMARY KEY (id),
    INDEX idx_outbox_processed (processed_at, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Per-day counters for unit numbers (seq_type 'UNIT') and request numbers (seq_type 'REQUEST').
-- Atomic increment:
--   INSERT INTO daily_sequence (seq_type, seq_date, current_value) VALUES (?, ?, LAST_INSERT_ID(1))
--   ON DUPLICATE KEY UPDATE current_value = LAST_INSERT_ID(current_value + 1);
--   SELECT LAST_INSERT_ID();
CREATE TABLE daily_sequence (
    seq_type   VARCHAR(20) NOT NULL,
    seq_date   DATE        NOT NULL,
    current_value INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (seq_type, seq_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
