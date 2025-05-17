#!/bin/sh
export DB_PASSWORD=$(cat /run/secrets/db_password)
exec su appuser -c "java -jar -Dspring.profiles.active=prod /app/app.jar"