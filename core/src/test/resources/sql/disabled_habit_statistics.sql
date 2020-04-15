INSERT INTO users (id,
                   date_of_registration,
                   email,
                   email_notification,
                   first_name,
                   last_name,
                   last_visit,
                   role,
                   user_status,
                   refresh_token_key)
VALUES (1, current_date, 'foo@bar.com', 1, 'foo', 'bar', current_date, 1, 1, 'quux');
INSERT INTO users (id,
                   date_of_registration,
                   email,
                   email_notification,
                   first_name,
                   last_name,
                   last_visit,
                   role,
                   user_status,
                   refresh_token_key)
VALUES (2, current_date, 'baz@bar.com', 1, 'foo', 'barbar', current_date, 1, 1, 'quuxbaz');

INSERT INTO habit_dictionary (id, image)
VALUES (1, 'foo');
INSERT INTO habit_dictionary (id, image)
VALUES (2, 'quux');

INSERT INTO languages (id, code)
VALUES (1, 'en');

INSERT INTO habit_dictionary_translation (id, name, description, habit_item, language_id, habit_dictionary_id)
VALUES (1, 'spam', 'eggs', 'foo', 1, 1);
INSERT INTO habit_dictionary_translation (id, name, description, habit_item, language_id, habit_dictionary_id)
VALUES (2, 'barbaz', 'barbar', 'eggs', 1, 2);

INSERT INTO habits (id, user_id, habit_dictionary_id, status, create_date)
VALUES (1, 1, 1, false, current_date); -- This habit is disabled
INSERT INTO habits (id, user_id, habit_dictionary_id, status, create_date)
VALUES (2, 1, 2, true, current_date);
INSERT INTO habits (id, user_id, habit_dictionary_id, status, create_date)
VALUES (3, 2, 1, false, current_date); -- This habit is disabled

INSERT INTO habit_statistics (id, rate, date, amount_of_items, habit_id)
VALUES (1, 'quux', current_date, 42, 1);
INSERT INTO habit_statistics (id, rate, date, amount_of_items, habit_id)
VALUES (2, 'quux', current_date, 8, 2);
INSERT INTO habit_statistics (id, rate, date, amount_of_items, habit_id)
VALUES (3, 'quux', current_date, 3, 3);
