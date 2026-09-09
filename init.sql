IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'JobPortalDB')
BEGIN
    CREATE DATABASE JobPortalDB;
END
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'jobuser')
BEGIN
    CREATE LOGIN jobuser WITH PASSWORD = '$(JobUserPassword)', DEFAULT_DATABASE = JobPortalDB;
END
GO

USE JobPortalDB;
GO

IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'jobuser')
BEGIN
    CREATE USER jobuser FOR LOGIN jobuser;
    ALTER ROLE db_owner ADD MEMBER jobuser;
END
GO