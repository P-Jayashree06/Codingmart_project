-- DEMO DATA. NOT a Flyway migration (lives in db/demo, outside db/migration).
-- Run once on a freshly migrated database:
--   docker exec -i bloodbank-mysql mysql -ubloodbank -pbloodbank bloodbank < src/main/resources/db/demo/sample_data.sql
-- All demo accounts use password: Demo@123   (admin keeps Admin@123)
-- Dates are relative to today so expiry mix stays realistic whenever you load it.
-- Stock is deliberately low (5 AVAILABLE units) so low-stock and near-expiry alerts can be demonstrated.

SET @pw := '$2a$10$XNOdv1ypqlfrRlCxs3DHKu6vi33sKhMR8SB2i49rTwhYCKHByySz6';

-- Users: 1 staff, 5 donors, 2 hospitals
INSERT INTO users (id, email, password_hash, full_name, role, active) VALUES
    (106, 'staff@bloodbank.org',            @pw, 'Anitha Krishnan',               'STAFF',    TRUE),
    (101, 'aarav.sharma@example.com',       @pw, 'Aarav Sharma',                  'DONOR',    TRUE),
    (102, 'priya.nair@example.com',         @pw, 'Priya Nair',                    'DONOR',    TRUE),
    (103, 'karthik.raman@example.com',      @pw, 'Karthik Raman',                 'DONOR',    TRUE),
    (104, 'meera.iyer@example.com',         @pw, 'Meera Iyer',                    'DONOR',    TRUE),
    (105, 'rahul.verma@example.com',        @pw, 'Rahul Verma',                   'DONOR',    TRUE),
    (111, 'chennai.general@example.org',    @pw, 'Chennai General Hospital',      'HOSPITAL', TRUE),
    (112, 'sunrise.clinic@example.org',     @pw, 'Sunrise Multispeciality Hospital', 'HOSPITAL', TRUE);

-- Donors (groups: A+, A+, O+, O-, B+). last_donation_date = their recent donation.
INSERT INTO donor (id, user_id, blood_group_id, dob, gender, weight_kg, phone, city, last_donation_date, deferred_until) VALUES
    (1, 101, 1, '1992-04-12', 'MALE',   72.50, '+919800000001', 'Chennai',    DATE_SUB(CURDATE(), INTERVAL 3 DAY),  NULL),
    (2, 102, 1, '1995-09-03', 'FEMALE', 58.00, '+919800000002', 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 12 DAY), NULL),
    (3, 103, 7, '1988-01-25', 'MALE',   80.20, '+919800000003', 'Madurai',    DATE_SUB(CURDATE(), INTERVAL 20 DAY), NULL),
    (4, 104, 8, '1990-11-30', 'FEMALE', 61.40, '+919800000004', 'Chennai',    DATE_SUB(CURDATE(), INTERVAL 30 DAY), NULL),
    (5, 105, 3, '1985-07-19', 'MALE',   75.00, '+919800000005', 'Trichy',     DATE_SUB(CURDATE(), INTERVAL 38 DAY), NULL);

-- Hospitals: one approved, one awaiting approval
INSERT INTO hospital (id, user_id, name, license_no, phone, city, address, approval_status, decision_reason, decided_by, decided_at) VALUES
    (1, 111, 'Chennai General Hospital',         'TN-HOSP-0001', '+914400000001', 'Chennai', '1 Hospital Road, Chennai',   'APPROVED', NULL, 1, UTC_TIMESTAMP(6)),
    (2, 112, 'Sunrise Multispeciality Hospital', 'TN-HOSP-0002', '+914400000002', 'Madurai', '22 Sunrise Avenue, Madurai', 'PENDING',  NULL, NULL, NULL);

-- 10 donations, all PASSED, 450 ml. Odd ids are old donations, even ids are recent ones.
INSERT INTO donation (id, donor_id, donation_date, volume_ml, screening_status, recorded_by) VALUES
    (1,  1, DATE_SUB(CURDATE(), INTERVAL 130 DAY), 450, 'PASSED', 106),
    (2,  1, DATE_SUB(CURDATE(), INTERVAL 3   DAY), 450, 'PASSED', 106),
    (3,  2, DATE_SUB(CURDATE(), INTERVAL 140 DAY), 450, 'PASSED', 106),
    (4,  2, DATE_SUB(CURDATE(), INTERVAL 12  DAY), 450, 'PASSED', 106),
    (5,  3, DATE_SUB(CURDATE(), INTERVAL 135 DAY), 450, 'PASSED', 106),
    (6,  3, DATE_SUB(CURDATE(), INTERVAL 20  DAY), 450, 'PASSED', 106),
    (7,  4, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 450, 'PASSED', 106),
    (8,  4, DATE_SUB(CURDATE(), INTERVAL 30  DAY), 450, 'PASSED', 106),
    (9,  5, DATE_SUB(CURDATE(), INTERVAL 128 DAY), 450, 'PASSED', 106),
    (10, 5, DATE_SUB(CURDATE(), INTERVAL 38  DAY), 450, 'PASSED', 106);

-- 10 units (one per donation), shelf life 42 days.
-- AVAILABLE expiry in days from today: id2=+39, id4=+30, id6=+22, id8=+12, id10=+4 (near expiry).
-- Old units: 1 ISSUED, 3 EXPIRED, 5 EXPIRED, 7 DISCARDED, 9 ISSUED.
INSERT INTO blood_inventory (id, unit_number, blood_group_id, donation_id, collection_date, expiry_date, status, discard_reason, discarded_by)
SELECT d.id,
       CONCAT('BU-', DATE_FORMAT(d.donation_date, '%Y%m%d'), '-', LPAD(d.id, 4, '0')),
       dn.blood_group_id,
       d.id,
       d.donation_date,
       DATE_ADD(d.donation_date, INTERVAL 42 DAY),
       CASE d.id WHEN 1 THEN 'ISSUED' WHEN 3 THEN 'EXPIRED' WHEN 5 THEN 'EXPIRED'
                 WHEN 7 THEN 'DISCARDED' WHEN 9 THEN 'ISSUED' ELSE 'AVAILABLE' END,
       CASE d.id WHEN 7 THEN 'Haemolysis observed during quality check' ELSE NULL END,
       CASE d.id WHEN 7 THEN 106 ELSE NULL END
FROM donation d
JOIN donor dn ON dn.id = d.donor_id;

-- Requests: two historical FULFILLED ones (matching the two ISSUED units) and one PENDING.
INSERT INTO blood_request (id, request_no, hospital_id, blood_group_id, units_requested, priority, status, required_by, patient_note, decided_by, decided_at, created_at) VALUES
    (1, CONCAT('REQ-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 127 DAY), '%Y%m%d'), '-0001'),
        1, 1, 1, 'URGENT', 'FULFILLED', DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 125 DAY), 'Surgery, ward 4',
        106, DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 126 DAY), DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 127 DAY)),
    (2, CONCAT('REQ-', DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 125 DAY), '%Y%m%d'), '-0001'),
        1, 3, 1, 'NORMAL', 'FULFILLED', DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 122 DAY), 'Planned procedure',
        106, DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 124 DAY), DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 125 DAY)),
    (3, CONCAT('REQ-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-0001'),
        1, 7, 1, 'NORMAL', 'PENDING', DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 2 DAY), 'Anaemia treatment',
        NULL, NULL, UTC_TIMESTAMP(6));

INSERT INTO blood_issue (request_id, inventory_id, issued_by, issued_at) VALUES
    (1, 1, 106, DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 126 DAY)),
    (2, 9, 106, DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 124 DAY));

-- Keep daily counters consistent with the demo request number used for today.
INSERT INTO daily_sequence (seq_type, seq_date, current_value) VALUES ('REQUEST', CURDATE(), 1);
