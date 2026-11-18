#!/bin/sh
# Creates one database and one dedicated user per service (README §5 "Database per service").
# Runs once, on the first start with an empty data volume (postgres image entrypoint).
# All names and passwords come from the container environment (.env), never from this file.
set -eu

# Nobody may connect to a database unless explicitly granted.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<'SQL'
REVOKE CONNECT ON DATABASE postgres FROM PUBLIC;
REVOKE CONNECT ON DATABASE template1 FROM PUBLIC;
SQL

create_service_database() {
  db_name="$1"; db_user="$2"; db_password="$3"
  echo "Creating database ${db_name} owned by ${db_user}"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
       -v db="$db_name" -v usr="$db_user" -v pwd="$db_password" <<'SQL'
CREATE ROLE :"usr" LOGIN PASSWORD :'pwd';
CREATE DATABASE :"db" OWNER :"usr";
REVOKE ALL ON DATABASE :"db" FROM PUBLIC;
GRANT CONNECT, TEMPORARY ON DATABASE :"db" TO :"usr";
SQL
}

create_service_database "$IDENTITY_DB_NAME"     "$IDENTITY_DB_USER"     "$IDENTITY_DB_PASSWORD"
create_service_database "$USER_DB_NAME"         "$USER_DB_USER"         "$USER_DB_PASSWORD"
create_service_database "$DOCUMENT_DB_NAME"     "$DOCUMENT_DB_USER"     "$DOCUMENT_DB_PASSWORD"
create_service_database "$REQUEST_DB_NAME"      "$REQUEST_DB_USER"      "$REQUEST_DB_PASSWORD"
create_service_database "$ASSISTANT_DB_NAME"    "$ASSISTANT_DB_USER"    "$ASSISTANT_DB_PASSWORD"
create_service_database "$NOTIFICATION_DB_NAME" "$NOTIFICATION_DB_USER" "$NOTIFICATION_DB_PASSWORD"
create_service_database "$AUDIT_DB_NAME"        "$AUDIT_DB_USER"        "$AUDIT_DB_PASSWORD"
create_service_database "$REPORTING_DB_NAME"    "$REPORTING_DB_USER"    "$REPORTING_DB_PASSWORD"
create_service_database "$AI_DB_NAME"           "$AI_DB_USER"           "$AI_DB_PASSWORD"

# pgvector is needed only by the AI bounded context.
echo "Enabling the vector extension in ${AI_DB_NAME}"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$AI_DB_NAME" \
     -c "CREATE EXTENSION IF NOT EXISTS vector;"
