#!/bin/bash

# construct airflow connection string which depends on env variables
if [[ -n "$PHOEBE_AIRFLOW_POSTGRES_USER" && -n "$PHOEBE_AIRFLOW_POSTGRES_PASS" && -n "$PHOEBE_AIRFLOW_POSTGRES_HOST" && -n "$PHOEBE_AIRFLOW_POSTGRES_DB" ]]; then
  echo "Setting AIRFLOW__DATABASE__SQL_ALCHEMY_CONN to PostgreSQL"
  export AIRFLOW__DATABASE__SQL_ALCHEMY_CONN="postgresql+psycopg2://${PHOEBE_AIRFLOW_POSTGRES_USER}:${PHOEBE_AIRFLOW_POSTGRES_PASS}@${PHOEBE_AIRFLOW_POSTGRES_HOST}/${PHOEBE_AIRFLOW_POSTGRES_DB}"
else
  echo "Skipping AIRFLOW__DATABASE__SQL_ALCHEMY_CONN since the required env variables are not provided"
fi

echo "Airflow version:"
/entrypoint airflow version

echo "Starting Airflow..."
/entrypoint airflow standalone &

# Explicitly create the target dir
mkdir -p /opt/airflow/codbex/target

# Start the Java application
cd /opt/airflow/codbex
echo "Starting java application..."
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --add-opens=java.base/java.nio=ALL-UNNAMED \
     -jar ./codbex-phoebe.jar
