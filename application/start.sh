#!/bin/bash

# Start Airflow in the background
echo "Current user: $(whoami)"

# construct airflow connection string which depends on env variables
export AIRFLOW__DATABASE__SQL_ALCHEMY_CONN="postgresql+psycopg2://${PHOEBE_AIRFLOW_POSTGRES_USER}:${PHOEBE_AIRFLOW_POSTGRES_USER}@${PHOEBE_AIRFLOW_POSTGRES_HOST}/${PHOEBE_AIRFLOW_POSTGRES_DB}"

echo "Airflow version:"
/entrypoint airflow version

echo "Starting Airflow..."
/entrypoint airflow standalone &

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./codbex-phoebe.jar
