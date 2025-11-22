-- V1__init.sql
-- Initial schema for Smart Parking Lot

-- === Enum types ===

CREATE TYPE spot_size AS ENUM ('SMALL', 'MEDIUM', 'LARGE', 'EV', 'BIKE');

CREATE TYPE spot_status AS ENUM ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'OUT_OF_SERVICE');

CREATE TYPE ticket_status AS ENUM ('OPEN', 'CLOSED');

CREATE TYPE payment_method AS ENUM ('CASH', 'CARD');

CREATE TYPE payment_status AS ENUM ('INITIATED', 'SUCCESS', 'FAILED');

CREATE TYPE rate_unit AS ENUM ('MINUTE', 'HOUR', 'FLAT');

-- === Core tables ===

CREATE TABLE lot (
                     id              UUID PRIMARY KEY,
                     name            VARCHAR(100) NOT NULL,
                     address         VARCHAR(255),
                     timezone        VARCHAR(50)  NOT NULL,
                     maintenance_mode BOOLEAN     NOT NULL DEFAULT FALSE,
                     created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                     updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE floor (
                       id          UUID PRIMARY KEY,
                       lot_id      UUID NOT NULL REFERENCES lot(id) ON DELETE CASCADE,
                       label       VARCHAR(50) NOT NULL,
                       ordering    INT NOT NULL,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_floor_lot_label
    ON floor(lot_id, label);

CREATE TABLE spot (
                      id          UUID PRIMARY KEY,
                      floor_id    UUID NOT NULL REFERENCES floor(id) ON DELETE CASCADE,
                      code        VARCHAR(50) NOT NULL,
                      size        spot_size   NOT NULL,
                      status      spot_status NOT NULL,
                      created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                      updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Each floor cannot have duplicate spot codes
CREATE UNIQUE INDEX ux_spot_floor_code
    ON spot(floor_id, code);

-- Index for "first available spot by floor and size"
CREATE INDEX idx_spot_available_by_floor_size
    ON spot(floor_id, size, code)
    WHERE status = 'AVAILABLE';

CREATE TABLE vehicle (
                         id              UUID PRIMARY KEY,
                         license_plate   VARCHAR(50) NOT NULL,
                         size            spot_size   NOT NULL,
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Assume license_plate is globally unique in this system
CREATE UNIQUE INDEX ux_vehicle_plate
    ON vehicle(license_plate);

CREATE TABLE ticket (
                        id          UUID PRIMARY KEY,
                        lot_id      UUID NOT NULL REFERENCES lot(id) ON DELETE RESTRICT,
                        spot_id     UUID NOT NULL REFERENCES spot(id) ON DELETE RESTRICT,
                        vehicle_id  UUID NOT NULL REFERENCES vehicle(id) ON DELETE RESTRICT,
                        entry_at    TIMESTAMPTZ NOT NULL,
                        exit_at     TIMESTAMPTZ,
                        status      ticket_status NOT NULL,
                        created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Invariant: at most one OPEN ticket per spot
CREATE UNIQUE INDEX ux_ticket_open_by_spot
    ON ticket(spot_id)
    WHERE status = 'OPEN';

-- Invariant: at most one OPEN ticket per vehicle per lot
CREATE UNIQUE INDEX ux_ticket_open_by_vehicle_lot
    ON ticket(vehicle_id, lot_id)
    WHERE status = 'OPEN';

-- Index to help look up tickets by vehicle quickly (general queries)
CREATE INDEX idx_ticket_vehicle
    ON ticket(vehicle_id);

CREATE TABLE payment (
                         id            UUID PRIMARY KEY,
                         ticket_id     UUID NOT NULL UNIQUE REFERENCES ticket(id) ON DELETE RESTRICT,
                         amount_minor  BIGINT      NOT NULL,      -- minor units, e.g. paise/cents
                         currency      VARCHAR(3)  NOT NULL,
                         method        payment_method NOT NULL,
                         status        payment_status NOT NULL,
                         paid_at       TIMESTAMPTZ,
                         reference     VARCHAR(100),
                         created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rate_card (
                           id             UUID PRIMARY KEY,
                           name           VARCHAR(100) NOT NULL,
                           currency       VARCHAR(3)   NOT NULL,
                           effective_from TIMESTAMPTZ  NOT NULL,
                           effective_to   TIMESTAMPTZ,
                           lot_id         UUID REFERENCES lot(id) ON DELETE SET NULL,
                           floor_id       UUID REFERENCES floor(id) ON DELETE SET NULL,
                           size           spot_size,
                           created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- One active card per (lot,floor,size) per time window is enforced in services, not via constraint.

CREATE TABLE rate_rule (
                           id             BIGSERIAL PRIMARY KEY,
                           rate_card_id   UUID NOT NULL REFERENCES rate_card(id) ON DELETE CASCADE,
                           start_minute   INT NOT NULL,       -- inclusive
                           end_minute     INT,                -- exclusive, null = open-ended
                           price_per_unit BIGINT NOT NULL,    -- in minor units
                           unit           rate_unit NOT NULL,
                           created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rate_rule_rate_card
    ON rate_rule(rate_card_id, start_minute);

-- Optional check constraints for sanity
ALTER TABLE rate_rule
    ADD CONSTRAINT chk_rate_rule_minutes
        CHECK (start_minute >= 0 AND (end_minute IS NULL OR end_minute > start_minute));

ALTER TABLE payment
    ADD CONSTRAINT chk_payment_amount_positive
        CHECK (amount_minor >= 0);

-- === Seed data ===

-- Fixed UUIDs for seed; you can replace with your own if desired
-- Lot
INSERT INTO lot (id, name, address, timezone, maintenance_mode)
VALUES ('00000000-0000-0000-0000-000000000001', 'Central Lot', 'Main Street 1', 'Asia/Kolkata', FALSE);

-- Floors
INSERT INTO floor (id, lot_id, label, ordering)
VALUES
    ('00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', 'G', 1),
    ('00000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', 'B1', 2);

-- Spots on Ground floor (G)
INSERT INTO spot (id, floor_id, code, size, status)
VALUES
    ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000011', 'G-S1', 'SMALL',  'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000011', 'G-S2', 'SMALL',  'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000011', 'G-M1', 'MEDIUM', 'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000011', 'G-L1', 'LARGE',  'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000011', 'G-EV1','EV',     'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000106', '00000000-0000-0000-0000-000000000011', 'G-B1', 'BIKE',   'AVAILABLE');

-- Spots on Basement floor (B1)
INSERT INTO spot (id, floor_id, code, size, status)
VALUES
    ('00000000-0000-0000-0000-000000000111', '00000000-0000-0000-0000-000000000012', 'B1-S1', 'SMALL',  'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000112', '00000000-0000-0000-0000-000000000012', 'B1-M1', 'MEDIUM', 'AVAILABLE'),
    ('00000000-0000-0000-0000-000000000113', '00000000-0000-0000-0000-000000000012', 'B1-L1', 'LARGE',  'AVAILABLE');

-- Default rate card for the lot (simple banded pricing)
INSERT INTO rate_card (id, name, currency, effective_from, effective_to, lot_id, floor_id, size)
VALUES (
           '00000000-0000-0000-0000-000000000201',
           'Default Lot Rates',
           'INR',
           NOW() - INTERVAL '1 day',
           NULL,
           '00000000-0000-0000-0000-000000000001',
           NULL,
           NULL
       );

-- Rate rules: 0-60 min, 60-240 min, 240+ flat
INSERT INTO rate_rule (rate_card_id, start_minute, end_minute, price_per_unit, unit)
VALUES
    ('00000000-0000-0000-0000-000000000201', 0,   60,  3000, 'HOUR'),  -- e.g. 30 INR/hour
    ('00000000-0000-0000-0000-000000000201', 60, 240,  2000, 'HOUR'),  -- cheaper after 1 hour
    ('00000000-0000-0000-0000-000000000201', 240, NULL, 5000, 'FLAT'); -- max cap