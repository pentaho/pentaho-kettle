CREATE USER hibuser WITH PASSWORD 'password';
CREATE USER pentaho_user WITH PASSWORD 'password';
CREATE USER pentaho_admin WITH PASSWORD 'password';
CREATE USER jcr_user WITH PASSWORD 'password';
CREATE DATABASE hibernate OWNER hibuser;
CREATE DATABASE quartz OWNER pentaho_user;
CREATE DATABASE jackrabbit OWNER jcr_user;
CREATE DATABASE emailgroups OWNER hibuser;
