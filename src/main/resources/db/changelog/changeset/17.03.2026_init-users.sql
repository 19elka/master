INSERT INTO users (username, password, roles, name)
SELECT 'admin',
       '$2a$10$VpWIm6QdfIBdSEn5BF2.POd6duxt3x3qWwPUpjDeaxBaYHJBNfz5K',
       'ADMIN',
       'Admin User' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, roles, name)
SELECT 'user',
       '$2a$10$jtANSJ3.LBpc.Tw539Rov.VGxHEOv0vYBXS9BJWG3aG3UXKr7Fo8q',
       'USER',
       'Regular User' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');

INSERT INTO user_roles (user_id, roles)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';

INSERT INTO user_roles (user_id, roles)
SELECT id, 'USER' FROM users WHERE username = 'user';