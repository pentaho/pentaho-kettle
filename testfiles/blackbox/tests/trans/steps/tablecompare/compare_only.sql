DROP TABLE IF EXISTS reference;
CREATE TABLE reference (
  key1 int,
  key2 int,
  value varchar(20),
  PRIMARY KEY (key1)
);

DROP TABLE IF EXISTS compare;
CREATE TABLE compare (
  key1 int,
  key2 int,
  value varchar(20),
  PRIMARY KEY (key1)
);
INSERT INTO compare VALUES ('1', '2', 'a');
INSERT INTO compare VALUES ('2', '1', 'b');
INSERT INTO compare VALUES ('3', '3', 'c');
INSERT INTO compare VALUES ('4', '4', 'd');