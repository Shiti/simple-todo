# Todos SCHEMA

# --- !Ups
CREATE TABLE Users(
  userId VARCHAR(50) PRIMARY KEY,
  password VARCHAR(50) NOT NULL
  );

CREATE TABLE Todo(
  id VARCHAR(255),
  text VARCHAR(100),
  done BOOLEAN,
  disp_order INT UNIQUE,
  userId VARCHAR(50),
  PRIMARY KEY(id),
  FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE
  );


# --- !Downs
DROP TABLE Todo;
DROP TABLE Users;

