DROP TABLE IF EXISTS booking;
  DROP TABLE IF EXISTS guest;
  DROP TABLE IF EXISTS room;

 CREATE TABLE room (
   id     SERIAL PRIMARY KEY,
   floorNumber   INT,
   capacity INT,
   balcony SMALLINT );

 CREATE TABLE guest (
   id       SERIAL PRIMARY KEY,
   name VARCHAR,
   dateOfBirth DATE,
   phoneNumber    VARCHAR);

 CREATE TABLE booking (
   id          SERIAL PRIMARY KEY,
   roomId      INT REFERENCES room(id) ON DELETE CASCADE ON UPDATE CASCADE,
   guestId  INT REFERENCES guest(id) ON DELETE CASCADE ON UPDATE CASCADE,
   arrivalDate   DATE,
   departureDate DATE);