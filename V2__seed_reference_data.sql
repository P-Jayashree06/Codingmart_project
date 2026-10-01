-- V2: reference data. Blood group ids are fixed so compatibility rows are readable.
-- 1=A+ 2=A- 3=B+ 4=B- 5=AB+ 6=AB- 7=O+ 8=O-

INSERT INTO blood_group (id, code) VALUES
    (1, 'A+'), (2, 'A-'), (3, 'B+'), (4, 'B-'),
    (5, 'AB+'), (6, 'AB-'), (7, 'O+'), (8, 'O-');

-- (recipient_group_id, donor_group_id): recipient may receive from donor. 27 rows in total.
INSERT INTO blood_compatibility (recipient_group_id, donor_group_id) VALUES
    -- A+  <- A+, A-, O+, O-
    (1, 1), (1, 2), (1, 7), (1, 8),
    -- A-  <- A-, O-
    (2, 2), (2, 8),
    -- B+  <- B+, B-, O+, O-
    (3, 3), (3, 4), (3, 7), (3, 8),
    -- B-  <- B-, O-
    (4, 4), (4, 8),
    -- AB+ <- all 8
    (5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7), (5, 8),
    -- AB- <- A-, B-, AB-, O-
    (6, 2), (6, 4), (6, 6), (6, 8),
    -- O+  <- O+, O-
    (7, 7), (7, 8),
    -- O-  <- O-
    (8, 8);

-- Initial admin. Password: Admin@123 (BCrypt cost 10). CHANGE IT after first login.
INSERT INTO users (email, password_hash, full_name, role, active)
VALUES ('admin@bloodbank.org',
        '$2a$10$sVIsLf/oSgm7FITl5nT9Xue1s/lrKhR6KVjHv9imW.i1s0nHEdP5W',
        'System Administrator', 'ADMIN', TRUE);
