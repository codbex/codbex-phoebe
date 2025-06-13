# Phoebe by codbex

[![Build Status](https://github.com/codbex/codbex-phoebe/actions/workflows/build.yaml/badge.svg)](https://github.com/codbex/codbex-phoebe/actions/workflows/build.yaml)
[![Eclipse License](https://img.shields.io/badge/License-EPL%202.0-brightgreen.svg)](https://github.com/codbex/codbex-phoebe/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.codbex.phoebe/codbex-phoebe-application.svg)](https://central.sonatype.com/namespace/com.codbex.phoebe)

Web IDE for [Apache Airflow](https://airflow.apache.org/) workflows development.

<!-- TOC -->

* [Phoebe by codbex](#phoebe-by-codbex)
    * [Description](#description)
    * [Run steps](#run-steps)
        * [Start using Docker and released image](#start-using-docker-and-released-image)
            * [Start PostgreSQL](#start-postgresql)
            * [Start Docker image](#start-docker-image)
        * [Build the project jar](#build-the-project-jar)
        * [Start using Docker Compose and local sources](#start-using-docker-compose-and-local-sources)
        * [Java standalone application](#java-standalone-application)
            * [Prerequisites](#prerequisites)
            * [Start the application](#start-the-application)
        * [Multi-platform Docker build](#multi-platform-docker-build)
            * [Spring profiles](#spring-profiles)
        * [Run unit tests](#run-unit-tests)
        * [Run integration tests](#run-integration-tests)
        * [Run all tests](#run-all-tests)
        * [Format the code](#format-the-code)
    * [Configurations](#configurations)
    * [Access the application](#access-the-application)

<!-- TOC -->

## Description

Phoebe is a Web IDE which allows you to write [Apache Airflow](https://airflow.apache.org/) application in an efficient
way.

It has the following perspectives:

- Workbench
  ![workbench](misc/images/workbench.png)
- Integrated Apache Airflow instance and embedded Apache Airflow Web UI
  ![airflow-ui](misc/images/airflow-ui.png)
- Git
  ![git-perspective](misc/images/git-perspective.png)
- Database Management
  ![db-perspective](misc/images/db-perspective.png)
- Terminal
  ![terminal](misc/images/terminal.png)

It also helps you to easily start your work using the defined Apache Airflow starter template.

## Run steps

__Prerequisites:__

- Export the following variables before executing the steps
  ```shell
  export GIT_REPO_FOLDER='<path-to-the-git-repo>'
  
  export PHOEBE_CONTAINER_NAME='phoebe'
  export DEV_IMAGE='codbex-phoebe:dev'
  export PHOEBE_IMAGE='ghcr.io/codbex/codbex-phoebe:latest'
  
  export PHOEBE_AIRFLOW_POSTGRES_USER="postgres"
  export PHOEBE_AIRFLOW_POSTGRES_PASS="postgres"
  export PHOEBE_AIRFLOW_POSTGRES_DB="postgres"
  
  export AIRFLOW_WORK_DIR="$HOME/airflow_work"
  export PHOEBE_AIRFLOW_WORK_DIR="$AIRFLOW_WORK_DIR"

  export GITHUB_USERNAME='<your-github-user>'

  ```

### Start using Docker and released image

#### Start PostgreSQL

The instance which will be used for Airflow DB or used existing DB instance.

```shell
docker rm -f postgres

docker run --name postgres \
  -e POSTGRES_PASSWORD="$PHOEBE_AIRFLOW_POSTGRES_PASS" \
  -e POSTGRES_USER="$PHOEBE_AIRFLOW_POSTGRES_USER" \
  -e POSTGRES_DB="$PHOEBE_AIRFLOW_POSTGRES_DB" \
  -p 5432:5432 \
  -d postgres:16
  
```

#### Start Docker image

```shell
docker rm -f "$PHOEBE_CONTAINER_NAME"

docker pull "$PHOEBE_IMAGE"

docker run --name "$PHOEBE_CONTAINER_NAME"  \
    -p 80:80 \
    -e PHOEBE_AIRFLOW_POSTGRES_USER="$PHOEBE_AIRFLOW_POSTGRES_USER" \
    -e PHOEBE_AIRFLOW_POSTGRES_PASS="$PHOEBE_AIRFLOW_POSTGRES_PASS" \
    -e PHOEBE_AIRFLOW_POSTGRES_HOST="host.docker.internal" \
    -e PHOEBE_AIRFLOW_POSTGRES_DB="$PHOEBE_AIRFLOW_POSTGRES_DB" \
    $PHOEBE_IMAGE
    
```

---

### Build the project jar

```shell
cd $GIT_REPO_FOLDER

mvn -T 1C clean install -P quick-build

```

---

### Start using Docker Compose and local sources

__Prerequisites:__ [Build the project jar](#build-the-project-jar)

  ```shell
  cd "$GIT_REPO_FOLDER/application"
  
  # cleanup
  docker rm -f "$PHOEBE_CONTAINER_NAME"
  docker compose down -v
  
  # To force rebuild add --build
  # Needed when you modify something in Dockerfile or in the application
  docker compose up --build
  
  ```

--- 

### Java standalone application

#### Prerequisites

- [Start PostgreSQL](#start-postgresql)

- Start Airflow locally
    ```shell
    cd "$GIT_REPO_FOLDER"
    
    docker rm -f airflow
    
    docker run --name airflow  \
       -p 8080:8080 \
       -v "$AIRFLOW_WORK_DIR/dags:/opt/airflow/dags" \
       -v "$AIRFLOW_WORK_DIR/logs:/opt/airflow/logs" \
       -v "$AIRFLOW_WORK_DIR/config:/opt/airflow/config" \
       -e AIRFLOW__CORE__LOAD_EXAMPLES=False \
       -e _AIRFLOW_DB_MIGRATE=true \
       -e AIRFLOW__SCHEDULER__DAG_DIR_LIST_INTERVAL=5 \
       -e AIRFLOW__CORE__EXECUTOR=LocalExecutor \
       -e AIRFLOW__CORE__SIMPLE_AUTH_MANAGER_ALL_ADMINS=True \
       -e AIRFLOW__DATABASE__SQL_ALCHEMY_CONN="postgresql+psycopg2://$PHOEBE_AIRFLOW_POSTGRES_USER:$PHOEBE_AIRFLOW_POSTGRES_PASS@host.docker.internal:5432/$PHOEBE_AIRFLOW_POSTGRES_DB" \
       -d apache/airflow:3.0.1 standalone
    ```

- Ensure Airflow is started at [http://localhost:8080](http://localhost:8080)

#### Start the application

- [Build the project jar](#build-the-project-jar)

- Start the application
    ```shell
    cd "$GIT_REPO_FOLDER"
  
    java \
        --add-opens=java.base/java.lang=ALL-UNNAMED \
        --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens=java.base/java.nio=ALL-UNNAMED \
        -jar application/target/*executable*.jar
  
    ```

- Start the application **in debug** with debug port `8000`
    ```shell
    cd "$GIT_REPO_FOLDER"
  
    java \
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
        --add-opens=java.base/java.lang=ALL-UNNAMED \
        --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens=java.base/java.nio=ALL-UNNAMED \
        -jar application/target/*executable*.jar
  
    ```

---

### Multi-platform Docker build

__Prerequisites:__ [Build the project jar](#build-the-project-jar)

```shell
cd "$GIT_REPO_FOLDER/application"

export DOCKER_CLI_EXPERIMENTAL=enabled
docker buildx create --use

# build image for linux/amd64
docker buildx build --platform linux/amd64 -t $DEV_IMAGE --load .

# build image for linux/arm64
docker buildx build --platform linux/arm64 -t $DEV_IMAGE --load .

# build images for both platforms
docker buildx build --platform=linux/arm64,linux/amd64 -t $DEV_IMAGE -o type=image .

# build multiplatform images and push them to GitHub Container Registry
docker login ghcr.io -u "$GITHUB_USERNAME"

docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t "ghcr.io/$GITHUB_USERNAME/$DEV_IMAGE" \
    --push .
    
## pull images locally

# linux/amd64
docker pull "ghcr.io/$GITHUB_USERNAME/$DEV_IMAGE" --platform linux/amd64

# linux/arm64
docker pull "ghcr.io/$GITHUB_USERNAME/$DEV_IMAGE" --platform linux/arm64

```

#### Spring profiles

- Eclipse Dirigible profiles
  To activate Eclipse Dirigible, you have to add profiles `common` and `app-default` explicitly.<br>
  Example for profile `snowflake`: `SPRING_PROFILES_ACTIVE=common,snowflake,app-default`

---

### Run unit tests

```shell
cd "$GIT_REPO_FOLDER"
mvn clean install -P unit-tests

```

---

### Run integration tests

```shell
cd "$GIT_REPO_FOLDER"
mvn clean install -P integration-tests

```

---

### Run all tests

```shell
cd "$GIT_REPO_FOLDER"
mvn clean install -P tests

```

---

### Format the code

```shell
cd "$GIT_REPO_FOLDER"
mvn verify -P format

```

---

## Configurations

The following configurations are available:

| Name                         | Description                                              | Default value           |
|------------------------------|----------------------------------------------------------|-------------------------|
| PHOEBE_AIRFLOW_URL           | The URL of the Airflow URL                               | `http://localhost:8080` |
| PHOEBE_AIRFLOW_WORK_DIR      | Airflow working directory                                | `/opt/airflow`          |
| PHOEBE_AIRFLOW_POSTGRES_USER | Docker config for Airflow PostgreSQL user                | `postgres`              |
| PHOEBE_AIRFLOW_POSTGRES_PASS | Docker config for Airflow PostgreSQL password            | `postgres`              |
| PHOEBE_AIRFLOW_POSTGRES_DB   | Docker config for Airflow PostgreSQL DB name             | `postgres`              |
| PHOEBE_AIRFLOW_POSTGRES_HOST | Docker config for Airflow PostgreSQL host                | `postgres`              |
| DIRIGIBLE_BASIC_USERNAME     | Phoebe admin username. The value must be Base64 encoded. | `YWRtaW4=`              |
| DIRIGIBLE_BASIC_PASSWORD     | Phoebe admin password. The value must be Base64 encoded. | `YWRtaW4=`              |

Depending on the use case these configurations could be set in different ways.

- For java standalone application they could be set as environment variables.
    ```shell
    export PHOEBE_AIRFLOW_URL='http://localhost:8080'
    java -jar ...
    ```
- For docker run
    ```shell
    docker run --name "$PHOEBE_CONTAINER_NAME"  \
        -p 80:80 \
        -e PHOEBE_AIRFLOW_POSTGRES_USER="$PHOEBE_AIRFLOW_POSTGRES_USER" \
        -e PHOEBE_AIRFLOW_POSTGRES_PASS="$PHOEBE_AIRFLOW_POSTGRES_PASS" \
        -e PHOEBE_AIRFLOW_POSTGRES_HOST="host.docker.internal" \
        -e PHOEBE_AIRFLOW_POSTGRES_DB="$PHOEBE_AIRFLOW_POSTGRES_DB" \
        $PHOEBE_IMAGE
  
    ```
- When using docker compose they could be set in the `docker-compose.yml` file.
    ```yaml
    services:
      phoebe:
        environment:
          PHOEBE_AIRFLOW_POSTGRES_USER: postgres
          PHOEBE_AIRFLOW_POSTGRES_PASS: postgres
          PHOEBE_AIRFLOW_POSTGRES_HOST: host.docker.internal
          PHOEBE_AIRFLOW_POSTGRES_DB: postgres
    ```

---

## Access the application

- Open URL [http://localhost](http://localhost) in your browser
- Login with default credentials `admin` / `admin`

--- 
