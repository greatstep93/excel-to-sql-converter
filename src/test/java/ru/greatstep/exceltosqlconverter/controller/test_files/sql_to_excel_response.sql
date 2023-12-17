INSERT INTO db_adapters_sales.clients ( full_name, first_name, last_name, create_date, last_update_date, create_user_id )
VALUES
('Антон Феодосивич Акулов', 'Антон', 'Акулов', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'greatstep')),
('Анастасия Алексеевна Барышева', 'Анастасия', 'Барышева', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'admin')),
('Ольга Никаноровна Белоногова', 'Ольга', 'Белоногова', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'greatstep')),
('Оксана Валерьевна Ярочкина', 'Оксана', 'Ярочкина', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'admin')),
('Егор Степанович Живенков', 'Егор', 'Живенков', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'greatstep')),
('Brian O''Conner', 'Brian', 'O''Conner', current_date, current_timestamp, (SELECT id FROM db_adapters_sales.users WHERE login = 'greatstep'))