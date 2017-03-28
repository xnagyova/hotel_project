CREATE TABLE rooms (
  id     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  floorNumber INT,
  capacity INT,
  balcony SMALLINT

);

CREATE TABLE guests (
  id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(50),
  dateOfBirth DATE,
  phoneNumber    VARCHAR(20)
);

CREATE TABLE bookings (
  id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  price       INT,
  roomId      BIGINT REFERENCES rooms(id) ON DELETE CASCADE,
  guestId  BIGINT REFERENCES guests (id) ON DELETE CASCADE,
  arrivalDate   DATE,
  departureDate DATE
);