#!/bin/bash

# Start Airflow in the background
echo "Current user: $(whoami)"

echo "Starting Airflow..."
/usr/bin/dumb-init -- /entrypoint standalone &

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./application.jar
