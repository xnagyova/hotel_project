CREATE TABLE room (
  id     INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  floorNumber INT,
  capacity INT,
  balcony SMALLINT
);

CREATE TABLE guest (
  id       INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(50),
  dateOfBirth DATE,
  phoneNumber    VARCHAR(20)
);

CREATE TABLE booking (
  id          INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  price       INT,
  roomId      INT REFERENCES room(id) ON DELETE CASCADE,
  guestId  INT REFERENCES guest (id) ON DELETE CASCADE,
  arrivaltDate   DATE,
  departureDate DATE
);