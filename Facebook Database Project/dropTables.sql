DROP TRIGGER Order_Friend_Pairs;
DROP TRIGGER city_trigger;
DROP TRIGGER program_trigger;
ALTER TABLE Albums DROP CONSTRAINT alt_photo_id;
ALTER TABLE Photos DROP CONSTRAINT alt_album_id;
DROP TABLE Participants;
DROP TABLE User_Events;
DROP TABLE Education;
DROP TABLE Programs;
DROP TABLE Messages;
DROP TABLE User_Hometown_Cities;
DROP TABLE User_Current_Cities;
DROP TABLE Cities;
DROP TABLE Tags;
DROP TABLE Photos;
DROP TABLE Albums;
DROP TABLE Friends;
DROP TABLE Users;
DROP SEQUENCE city_seq;
DROP SEQUENCE program_seq;
