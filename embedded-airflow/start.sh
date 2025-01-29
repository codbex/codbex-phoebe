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

echo "Starting Airflow..."
/usr/bin/dumb-init -- /entrypoint standalone &

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./application.jar
