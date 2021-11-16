-- DROP USER CMS CASCADE; 

-- DROP TABLESPACE CMS_DATA INCLUDING CONTENTS AND DATAFILES;
-- DROP TABLESPACE CMS_TEMP INCLUDING CONTENTS AND DATAFILES;

-- SELECT * FROM v$version;

CREATE TABLESPACE CMS_DATA DATAFILE '/u02/app/oracle/oradata/ORCL/CMS_DATA.dbf' SIZE 20M REUSE AUTOEXTEND ON NEXT 512K MAXSIZE 8192M;
CREATE TEMPORARY TABLESPACE CMS_TEMP TEMPFILE '/u02/app/oracle/oradata/ORCL/CMS_TEMP.dbf' SIZE 5M AUTOEXTEND ON;

-- SELECT * FROM V$DATAFILE
-- alter system set DEFERRED_SEGMENT_CREATION=FALSE scope=both; // To include empty tables in export

-- To create user CMS without prefix C## in Oracle 12:
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- USER SQL
CREATE USER CMS IDENTIFIED BY PASS123 DEFAULT TABLESPACE "CMS_DATA" TEMPORARY TABLESPACE "CMS_TEMP";
-- ROLE
GRANT "RESOURCE" TO CMS WITH ADMIN OPTION;
GRANT "CONNECT" TO CMS WITH ADMIN OPTION;
ALTER USER CMS DEFAULT ROLE "RESOURCE","CONNECT";
-- SYSTEM PRIVILEGES
GRANT CREATE ANY VIEW TO CMS WITH ADMIN OPTION;
GRANT DROP ANY VIEW TO CMS WITH ADMIN OPTION;

-- To prevent ora-01950 in Oracle 12:
GRANT UNLIMITED TABLESPACE TO CMS;

-- PURGE RECYCLEBIN;