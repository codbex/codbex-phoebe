# Use the official Airflow image as the base image
FROM apache/airflow:2.10.4

USER root

RUN apt-get update && \
    apt-get install -y git && \
    apt-get install -y wget

# RUN wget https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.deb
RUN dpkg -i amazon-corretto-21-x64-linux-jdk.deb || apt-get install -f -y

RUN wget https://corretto.aws/downloads/latest/amazon-corretto-21-aarch64-linux-jdk.deb
RUN dpkg -i amazon-corretto-21-aarch64-linux-jdk.deb || apt-get install -f -y

# RUN rm amazon-corretto-21-x64-linux-jdk.deb
RUN rm amazon-corretto-21-aarch64-linux-jdk.deb

# Set JAVA_HOME environment variable for Amazon Corretto
ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH=$JAVA_HOME/bin:$PATH

RUN mkdir -p /opt/airflow/codbex && \
    chown -R airflow /opt/airflow/codbex
# RUN mkdir -p /opt/airflow/codbex/target && \
#     chown -R airflow /opt/airflow/codbex/target

# Switch back to the airflow user for running the application
USER airflow

# Copy your Spring Boot application JAR into the container
COPY application/target/*.jar /opt/airflow/codbex/application.jar

# Expose 80 for the java app and 8080 for the Airflow
EXPOSE 80 8080

COPY embedded-airflow/start.sh /opt/airflow/codbex/start.sh

# RUN chmod +x /opt/airflow/codbex/start.sh
ENTRYPOINT ["/opt/airflow/codbex/start.sh"]
