# Import / Export procedures

## First check Oracle service

On Windows:

- `sc query OracleOraDb11g_home1TNSListener`
- `sc query OracleServiceORCL`
- `sc start OracleOraDb11g_home1TNSListener`
- `sc start OracleServiceORCL`

## Export

`exp CMS/PASS123 FIlE=CMS.dmp OWNER=CMS STATISTICS=NONE`

## Import

`imp system/password@ORCL file=CMS.dmp log=imp_CMS.log fromuser=CMS touser=CMS`

## Connect to Oracle Instance

`sqlplus / AS SYSDBA`

### Some commands:

- `select * from all_users;`
- `connect CMS;`
- `use CMS`
- `select table_name from all_tables;`
- `desc ACTIVITIES;`
- `@/path/script.sql` or `start /path/script.sql`
