FROM apache/airflow:2.10.4

USER root

RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y wget

# Define the correct Java package based on platform
ARG TARGETPLATFORM
RUN if [ "$TARGETPLATFORM" = "linux/amd64" ]; then \
        wget https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.deb && \
        dpkg -i amazon-corretto-21-x64-linux-jdk.deb || apt-get install -f -y && \
        rm amazon-corretto-21-x64-linux-jdk.deb; \
    elif [ "$TARGETPLATFORM" = "linux/arm64" ]; then \
        wget https://corretto.aws/downloads/latest/amazon-corretto-21-aarch64-linux-jdk.deb && \
        dpkg -i amazon-corretto-21-aarch64-linux-jdk.deb || apt-get install -f -y && \
        rm amazon-corretto-21-aarch64-linux-jdk.deb; \
    else \
        echo "Unsupported platform: $TARGETPLATFORM" && exit 1; \
    fi

# Set JAVA_HOME environment variable for Amazon Corretto
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH=$JAVA_HOME/bin:$PATH

RUN mkdir -p /opt/airflow/codbex && \
    chown -R airflow /opt/airflow/codbex

# Switch back to the airflow user for running the application
USER airflow

# Copy your Spring Boot application JAR into the container
COPY ./target/codbex-phoebe-[0-9]*.jar /opt/airflow/codbex/codbex-phoebe.jar

# Expose 80 for the Java app only
EXPOSE 80

COPY ./start.sh /opt/airflow/codbex/start.sh

COPY ./webserver_config.py /opt/airflow
# COPY application/docker/start.sh /opt/airflow/codbex/start.sh
#
# COPY application/docker/webserver_config.py /opt/airflow

# Phoebe env variables
ENV PHOEBE_AIRFLOW_POSTGRES_USER="postgres"
ENV PHOEBE_AIRFLOW_POSTGRES_PASS="postgres"
ENV PHOEBE_AIRFLOW_POSTGRES_HOST="postgres"
ENV PHOEBE_AIRFLOW_POSTGRES_DB="postgres"
ENV PHOEBE_AIRFLOW_WORK_DIR="/opt/airflow"

# Airflow env variables
ENV AIRFLOW__CORE__EXECUTOR="LocalExecutor"
ENV AIRFLOW__CORE__LOAD_EXAMPLES="False"
ENV _AIRFLOW_DB_MIGRATE="true"
ENV AIRFLOW__SCHEDULER__DAG_DIR_LIST_INTERVAL="5"

ENTRYPOINT ["/opt/airflow/codbex/start.sh"]
