#!/bin/sh

host="database"
port=3306
max_attempts=30
attempt_num=1

echo "Waiting for MySQL at $host:$port..."

while ! nc -z $host $port; do
  attempt_num=$((attempt_num+1))
  if [ $attempt_num -gt $max_attempts ]; then
    echo "Max attempts reached, MySQL is still unavailable - exiting"
    exit 1
  fi
  echo "Attempt $attempt_num/$max_attempts: MySQL is unavailable, waiting 2 seconds..."
  sleep 2
done

echo "MySQL is up - executing application"

exec java -jar -Dspring.profiles.active=prod app.jar
