 CREATE TABLE rooms  (
   id BIGINT PRIMARY KEY,
   floorNumber   INT,
   capacity INT,
   balcony SMALLINT );

 CREATE TABLE guests (
   id       BIGINT PRIMARY KEY,
   name VARCHAR,
   dateOfBirth DATE,
   phoneNumber    VARCHAR);

 CREATE TABLE bookings (
   id          BIGINT PRIMARY KEY,
   price INT,
   roomId      BIGINT REFERENCES rooms(id) ON DELETE CASCADE ON UPDATE CASCADE,
   guestId  BIGINT REFERENCES guests(id) ON DELETE CASCADE ON UPDATE CASCADE,
   arrivalDate   DATE,
   departureDate DATE);