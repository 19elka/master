INSERT INTO users (username, password, name, roles)
SELECT 'admin',
       '$2a$10$VpWIm6QdfIBdSEn5BF2.POd6duxt3x3qWwPUpjDeaxBaYHJBNfz5K',
       'Admin User',
       '["ADMIN"]'::jsonb WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, name, roles)
SELECT 'user',
       '$2a$10$jtANSJ3.LBpc.Tw539Rov.VGxHEOv0vYBXS9BJWG3aG3UXKr7Fo8q',
       'Regular User',
       '["USER"]'::jsonb WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');