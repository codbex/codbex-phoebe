#!/bin/bash

# Start Airflow in the background
echo "Current user: $(whoami)"

# construct airflow connection string
export AIRFLOW__CORE__SQL_ALCHEMY_CONN="postgresql+psycopg2://${AIRFLOW_POSTGRES_USER}:${AIRFLOW_POSTGRES_PASS}@${AIRFLOW_POSTGRES_HOST}/${AIRFLOW_POSTGRES_DB}"
echo AIRFLOW__CORE__SQL_ALCHEMY_CONN: $AIRFLOW__CORE__SQL_ALCHEMY_CONN

echo "Starting Airflow..."
/usr/bin/dumb-init -- /entrypoint standalone &

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./application.jar
