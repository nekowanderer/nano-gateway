SELECT 'CREATE DATABASE nano_postgresql'
WHERE NOT EXISTS (SELECT * FROM pg_database WHERE datname = 'nano_postgresql');
\gexec

GRANT ALL PRIVILEGES ON DATABASE nano_postgresql TO admin;

SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT * FROM pg_database WHERE datname = 'keycloak');
\gexec

GRANT ALL PRIVILEGES ON DATABASE keycloak TO admin;
