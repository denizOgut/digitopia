CREATE DATABASE user_db;
CREATE DATABASE organization_db;
CREATE DATABASE invitation_db;


GRANT ALL PRIVILEGES ON DATABASE user_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE organization_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE invitation_db TO postgres;