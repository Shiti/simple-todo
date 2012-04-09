-- Todos SCHEMA

CREATE TABLE Todo (
  id VARCHAR(255) PRIMARY KEY ,
  text VARCHAR(100),
  done BOOLEAN,
  disp_order INT UNIQUE      --had to change the attribute name as its a reserved keyword
);

