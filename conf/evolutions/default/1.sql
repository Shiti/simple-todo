-- Todos SCHEMA

CREATE TABLE Todo (
  id VARCHAR(255) PRIMARY KEY ,
  text VARCHAR(100),
  done VARCHAR(5),
  disp_order INT UNIQUE      --had to change the attribute name as its a reserved keyword
);

