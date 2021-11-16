# Import / Export procedures

## First check MySQL service

On Linux:

- `service mysql status`
- `service mysql start`

On Windows:

- `sc query MySQL56`
- `sc start MySQL56`

## Export

- `mysqldump -u user -p[password] [database] > file`
- `mysqldump --user=user --password[=password] [database] > file`

Example (add --routines to export functions and procedures):

`mysqldump --routines --user=root --password cms > C:\prj\dew\cms\mysql\cms_dump.sql`

## Import (after create database: 01_setup.sql)

- `mysql --user=user --password[=password] [database] < file`

Example:

`mysql -u root -p cms < C:\prj\dew\cms\mysql\cms_dump.sql`

## Connect to mysql

`mysql --user=root --password[=password] [database]`

### Some commands:

- `show databases;`
- `select database() from dual;`
- `use cms`
- `show tables;`
- `show full tables in cms where table_type like 'VIEW';`
- `show triggers;`
- `show function status where db='cms';`
- `show procedure status where db='cms';`
- `source C:/prj/dew/cms/mysql/05_data.sql;`