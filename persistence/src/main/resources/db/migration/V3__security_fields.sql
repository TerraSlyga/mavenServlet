ALTER TABLE users ADD COLUMN enabled BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE users ADD COLUMN confirmation_code VARCHAR(64);

UPDATE users SET password = '$2a$10$jsRmxco.ZOqeZUQDUFs.wOiDpwkMI1BGRx1LkGyZ3w1UOadpiLu0u' WHERE username = 'john_doe';
UPDATE users SET password = '$2a$10$UwkSrXiJ8hXesOAH0pZABOyHoqT0L/h6hJXKDhuCEy5dPwZRlJDey' WHERE username = 'jane_smith';
UPDATE users SET password = '$2a$10$DNivn/gvb70ZKFnKICDyM.UwcpSgswveW9.g1i5HgK7amRJJFa.v6' WHERE username = 'taras_shevchenko';

INSERT INTO users (username, password, role, enabled)
VALUES ('admin', '$2a$10$IopMD5FGD6C04c4tO.1blO5iSVnF9O67uXSfS0zi099zIb/YJUp5O', 'ROLE_ADMIN', TRUE);
