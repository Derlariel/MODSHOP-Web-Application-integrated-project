#!/bin/sh
export DB_PASSWORD=$(cat /run/secrets/db_password)
exec java -jar /app/app.jar