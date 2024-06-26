CREATE VIEW View_User_Information(user_id,first_name, last_name,year_of_birth,month_of_birth,day_of_birth,gender,current_city,current_state,current_country,hometown_city, hometown_state,hometown_country,institution_name,program_year,program_concentration, program_degree) AS 
SELECT Users.user_id, Users.first_name, Users.last_name, Users.year_of_birth, Users.month_of_birth, Users.day_of_birth, Users.gender, C.city_name, C.state_name, C.country_name,
H.city_name, H.state_name, H.country_name, Programs.institution, Education.program_year, Programs.concentration, Programs.degree 
FROM Users 
LEFT JOIN User_Current_Cities ON User_Current_Cities.user_id = Users.user_id 
LEFT JOIN User_Hometown_Cities ON User_Hometown_Cities.user_id = Users.user_id
LEFT JOIN Cities C ON User_Current_Cities.current_city_id = C.city_id
LEFT JOIN Cities H ON User_Hometown_Cities.hometown_city_id = H.city_id
LEFT JOIN Education ON Education.user_id = Users.user_id
LEFT JOIN Programs ON Programs.program_id = Education.program_id;

CREATE VIEW View_Are_Friends(user1_id,user2_id) AS 
SELECT user1_id,user2_id FROM Friends;

CREATE VIEW View_Photo_Information(album_id,owner_id,cover_photo_id,album_name,album_created_time,album_modified_time,album_link,album_visibility,photo_id,photo_caption,photo_created_time,photo_modified_time,photo_link) AS
SELECT Albums.album_id, Albums.album_owner_id, Albums.cover_photo_id, Albums.album_name, Albums.album_created_time, Albums.album_modified_time, Albums.album_link, Albums.album_visibility, Photos.photo_id, Photos.photo_caption, Photos.photo_created_time, Photos.photo_modified_time, Photos.photo_link
FROM Albums 
JOIN Photos ON Albums.album_id = Photos.album_id;

CREATE VIEW View_Event_Information(event_id,even_creator_id,event_name, event_tagline, event_description, event_host, event_type, event_subtype, event_address, event_city, event_state, event_country, event_start_time, event_end_time) AS
SELECT User_Events.event_id, User_Events.event_creator_id, User_Events.event_name, User_Events.event_tagline, User_Events.event_description, User_Events.event_host, User_Events.event_type, User_Events.event_subtype, User_Events.event_address, Cities.city_name, Cities.state_name, Cities.country_name, User_Events.event_start_time, User_Events.event_end_time
FROM User_Events
JOIN Cities ON Cities.city_id = User_Events.event_city_id;

CREATE VIEW View_Tag_Information(photo_id, tag_subject_id, tag_created_time, tag_x_coordinate, tag_y_coordinate) AS
SELECT Tags.tag_photo_id, Tags.tag_subject_id, Tags.tag_created_time, Tags.tag_x, Tags.tag_y FROM Tags;