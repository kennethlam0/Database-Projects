INSERT INTO Users
SELECT DISTINCT user_id,first_name,last_name,year_of_birth,month_of_birth,day_of_birth,gender FROM project1.Public_User_Information; 

INSERT INTO Cities(city_name, state_name, country_name)
SELECT DISTINCT current_city,current_state,current_country FROM project1.Public_User_Information
UNION
SELECT DISTINCT hometown_city, hometown_state, hometown_country FROM project1.Public_User_Information
UNION
SELECT DISTINCT event_city, event_state, event_country FROM project1.Public_Event_Information;

INSERT INTO Friends(user1_id, user2_id)
SELECT DISTINCT user1_id, user2_id FROM project1.Public_Are_Friends;

INSERT INTO User_Current_Cities(user_id, current_city_id)
SELECT DISTINCT project1.Public_User_Information.user_id, Cities.city_id FROM project1.Public_User_Information
INNER JOIN Cities ON city_name = current_city 
AND country_name = current_country
AND state_name = current_state;

---potentially dont need above line^^
INSERT INTO User_Hometown_Cities(user_id, hometown_city_id)
SELECT DISTINCT project1.Public_User_Information.user_id, Cities.city_id FROM project1.Public_User_Information
INNER JOIN Cities ON city_name = hometown_city 
AND country_name = hometown_country
AND state_name = hometown_state;

INSERT INTO Programs(institution, concentration, degree)
SELECT DISTINCT institution_name, program_concentration, program_degree FROM project1.Public_User_Information
WHERE institution_name IS NOT NULL;

INSERT INTO Education (user_id, program_id, program_year)
SELECT DISTINCT project1.Public_User_Information.user_id, Programs.program_id, project1.Public_User_Information.program_year FROM project1.Public_User_Information
INNER JOIN Programs ON project1.Public_User_Information.program_concentration = Programs.concentration
AND project1.Public_User_Information.institution_name = Programs.institution
AND project1.Public_User_Information.program_degree = Programs.degree;

INSERT INTO User_Events(event_id, event_creator_id, event_name, event_tagline, event_description, event_host, event_type, event_subtype, event_address, event_city_id, event_start_time, event_end_time)
SELECT DISTINCT event_id, event_creator_id, event_name, event_tagline, event_description, event_host, event_type, event_subtype, event_address, Cities.city_id, event_start_time, event_end_time 
FROM project1.Public_Event_Information
INNER JOIN Cities ON project1.Public_Event_Information.event_city = Cities.city_name
INNER JOIN Cities ON project1.Public_Event_Information.event_state = Cities.state_name
INNER JOIN Cities ON project1.Public_Event_Information.event_country = Cities.country_name;

SET AUTOCOMMIT OFF;

INSERT INTO Albums(album_id, album_owner_id, album_name, album_created_time, album_modified_time, album_link, album_visibility, cover_photo_id)
SELECT DISTINCT album_id, owner_id, album_name, album_created_time, album_modified_time, album_link, album_visibility, cover_photo_id
FROM project1.Public_Photo_Information;

INSERT INTO Photos(photo_id, album_id, photo_caption, photo_created_time, photo_modified_time, photo_link)
SELECT DISTINCT photo_id, album_id, photo_caption, photo_created_time, photo_modified_time, photo_link
FROM project1.Public_Photo_Information;

COMMIT;
SET AUTOCOMMIT ON;

INSERT INTO Tags(tag_photo_id, tag_subject_id, tag_created_time, tag_x, tag_y)
SELECT DISTINCT photo_id, tag_subject_id, tag_created_time, tag_x_coordinate, tag_y_coordinate
FROM project1.Public_Tag_Information;