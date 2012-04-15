# Todos SCHEMA

# --- !Ups
CREATE TABLE Todo (
  id VARCHAR(255) PRIMARY KEY,
  text VARCHAR(100),
  done BOOLEAN,
  disp_order INT UNIQUE
);


# --- !Downs
DROP TABLE Todo;