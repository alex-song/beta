--initialize users
INSERT INTO User (id, name, password) VALUES (1, 'admin', 'admin');
INSERT INTO User (id, name, password) VALUES (2, 'user', 'user');
--initialize user roles
INSERT INTO User_Role (user_id, roles) VALUES (1, 'ADMIN');
INSERT INTO User_Role (user_id, roles) VALUES (2, 'USER');
