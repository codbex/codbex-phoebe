#!/bin/bash

# Start Airflow in the background
echo "Current user: $(whoami)"
#echo "Env: $(env)"

#echo "Airflow DB migrate..."
#airflow db migrate
#
#echo "Creating admin user..."
#airflow users create \
#          --username ${_AIRFLOW_WWW_USER_USERNAME:-admin1234} \
#          --password ${_AIRFLOW_WWW_USER_USERNAME:-admin1234} \
#          --firstname FIRST_NAME \
#          --lastname LAST_NAME \
#          --role Admin \
#          --email admin@example.org

#airflow standalone &
#airflow standalone


## Initialize the database
#echo "Init DB..."
#/usr/bin/dumb-init -- /entrypoint db init

# Create an admin user if not exists
#echo "Creating default user..."
#/usr/bin/dumb-init -- /entrypoint users create \
#    --username admin \
#    --password admin \
#    --firstname Admin \
#    --lastname User \
#    --role Admin \
#    --email admin@example.com || echo "Admin user already exists"

#export AIRFLOW__WEBSERVER__AUTHENTICATE=False
#export AIRFLOW__WEBSERVER__AUTHORIZE=False
#export AIRFLOW__WEBSERVER__RBAC=False
#export AIRFLOW__WEBSERVER__AUTH_BACKEND=airflow.api.auth.backend.default
#
#echo "Starting Airflow Webserver..."
#/usr/bin/dumb-init -- /entrypoint webserver --port 8080 &
#
## Start Airflow Scheduler
#echo "Starting Airflow Scheduler..."
#/usr/bin/dumb-init -- /entrypoint scheduler

echo "Starting Airflow..."
/usr/bin/dumb-init -- /entrypoint standalone &

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./application.jar
