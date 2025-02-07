# codbex-phoebe

Phoebe is a Web IDE which allows you to write [Apache Airflow](https://airflow.apache.org/) application in an efficient
way.

It has the following perspectives:

- Workbench
- Integrated Apache Airflow instance and embedded Apache Airflow Web UI
- Git
- Database Management
- Terminal

It also helps you to easily start your work using the defined Apache Airflow starter template.

<!-- TOC -->

* [codbex-phoebe](#codbex-phoebe)
    * [Run steps](#run-steps)
        * [Build the project jar](#build-the-project-jar)
        * [Start locally using Docker and local sources](#start-locally-using-docker-and-local-sources)
        * [Start locally using Docker and released image](#start-locally-using-docker-and-released-image)
            * [Start PostgreSQL](#start-postgresql)
            * [Start Docker image](#start-docker-image)
        * [Java standalone application](#java-standalone-application)
            * [Prerequisites](#prerequisites)
            * [Start the application](#start-the-application)
        * [Access the application](#access-the-application)
        * [Multi-platform Docker build](#multi-platform-docker-build)

<!-- TOC -->

## Run steps

### Build the project jar

```shell
export GIT_REPO_FOLDER='<set-your-path>'
export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

cd $GIT_REPO_FOLDER

mvn -T 1C clean install \
  -D skipTests -D maven.test.skip=true \
  -D maven.javadoc.skip=true -D license.skip=true
```

### Start locally using Docker and local sources

__Prerequisites:__ [Build the project jar](#build-the-project-jar)

  ```shell
  export GIT_REPO_FOLDER='<set-your-path>'
  export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

  cd "$GIT_REPO_FOLDER/application"
  
  # cleanup
  docker compose down -v
  
  # To force rebuild add --build
  # Needed when you modify something in Dockerfile or in the application
  docker compose up --build
  ```

### Start locally using Docker and released image

#### Start PostgreSQL

The instance which will be used for Airflow DB or used existing DB instance.

```shell
export PHOEBE_AIRFLOW_POSTGRES_USER="postgres"
export PHOEBE_AIRFLOW_POSTGRES_PASS="postgres"
export PHOEBE_AIRFLOW_POSTGRES_DB="postgres"

docker rm -f postgres

docker run --name postgres \
  -e POSTGRES_PASSWORD="$PHOEBE_AIRFLOW_POSTGRES_PASS" \
  -e POSTGRES_USER="$PHOEBE_AIRFLOW_POSTGRES_USER" \
  -e POSTGRES_DB="$PHOEBE_AIRFLOW_POSTGRES_DB" \
  -p 5432:5432 \
  -d postgres:13
```

#### Start Docker image

```shell
export PHOEBE_IMAGE='ghcr.io/cdobex/codbex-phoebe:latest'
export PHOEBE_IMAGE='ghcr.io/iliyan-velichkov/codbex-phoebe:dev'

docker rm -f phoebe

docker pull "$PHOEBE_IMAGE"

docker run --name phoebe  \
    -p 80:80 \
    -e PHOEBE_AIRFLOW_POSTGRES_USER="$PHOEBE_AIRFLOW_POSTGRES_USER" \
    -e PHOEBE_AIRFLOW_POSTGRES_PASS="$PHOEBE_AIRFLOW_POSTGRES_PASS" \
    -e PHOEBE_AIRFLOW_POSTGRES_HOST="host.docker.internal" \
    -e PHOEBE_AIRFLOW_POSTGRES_DB="$PHOEBE_AIRFLOW_POSTGRES_DB" \
    $PHOEBE_IMAGE
```

### Java standalone application

#### Prerequisites

- [Start PostgreSQL](#start-postgresql)

- Start Airflow locally
    ```shell
    export AIRFLOW_WORK_DIR="$HOME/airflow_work"
    export PHOEBE_AIRFLOW_POSTGRES_USER="postgres"
    export PHOEBE_AIRFLOW_POSTGRES_PASS="postgres"
    export PHOEBE_AIRFLOW_POSTGRES_DB="postgres"
    
    docker rm -f airflow
    
    docker run --name airflow  \
    -p 8080:8080 \
    -v "$AIRFLOW_WORK_DIR/dags:/opt/airflow/dags" \
    -v "$AIRFLOW_WORK_DIR/logs:/opt/airflow/logs" \
    -v "$AIRFLOW_WORK_DIR/config:/opt/airflow/config" \
    -v "./application/webserver_config.py:/opt/airflow/webserver_config.py" \
    -e AIRFLOW__CORE__LOAD_EXAMPLES=False \
    -e _AIRFLOW_DB_MIGRATE=true \
    -e AIRFLOW__SCHEDULER__DAG_DIR_LIST_INTERVAL=5 \
    -e AIRFLOW__CORE__EXECUTOR=LocalExecutor \
    -e AIRFLOW__DATABASE__SQL_ALCHEMY_CONN="postgresql+psycopg2://$PHOEBE_AIRFLOW_POSTGRES_USER:$PHOEBE_AIRFLOW_POSTGRES_PASS@host.docker.internal:5432/$PHOEBE_AIRFLOW_POSTGRES_DB" \
    -d apache/airflow:2.10.4 standalone
    ```
- Ensure Airflow is started at [http://localhost:8080](http://localhost:8080)

#### Start the application

- [Build the project jar](#build-the-project-jar)

- Start the application
    ```shell
    export GIT_REPO_FOLDER='<set-your-path>'
    export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'
  
    export PHOEBE_AIRFLOW_WORK_DIR="$AIRFLOW_WORK_DIR"
    java \
        --add-opens=java.base/java.lang=ALL-UNNAMED \
        --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens=java.base/java.nio=ALL-UNNAMED \
        -jar application/target/*-application-*.jar
    ```

- Start the application **in debug** with debug port `8000`
    ```shell
    export GIT_REPO_FOLDER='<set-your-path>'
    export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'
  
    export PHOEBE_AIRFLOW_WORK_DIR="$AIRFLOW_WORK_DIR"
    java \
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
        --add-opens=java.base/java.lang=ALL-UNNAMED \
        --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
        --add-opens=java.base/java.nio=ALL-UNNAMED \
        -jar application/target/*-application-*.jar
    ```

### Access the application

- Open URL [http://localhost](http://localhost) in your browser
- Login with default credentials `admin` / `admin`

### Multi-platform Docker build

__Prerequisites:__ [Build the project jar](#build-the-project-jar)

```shell
export GIT_REPO_FOLDER='<set-your-path>'
export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'
export IMAGE='codbex-phoebe:dev'
export GITHUB_USERNAME='<your-github-user>'
export GITHUB_USERNAME='iliyan-velichkov'

cd "$GIT_REPO_FOLDER/application"

export DOCKER_CLI_EXPERIMENTAL=enabled
docker buildx create --use

# build image for linux/amd64
docker buildx build --platform linux/amd64 -t $IMAGE --load .

# build image for linux/arm64
docker buildx build --platform linux/arm64 -t $IMAGE --load .

# build images for both platforms
docker buildx build --platform=linux/arm64,linux/amd64 -t $IMAGE -o type=image .

# build multiplatform images and push them to GitHub Container Registry
docker login ghcr.io -u "$GITHUB_USERNAME"

docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t "ghcr.io/$GITHUB_USERNAME/$IMAGE" \
    --push .
    
## pull images locally

# linux/amd64
docker pull "ghcr.io/$GITHUB_USERNAME/$IMAGE" --platform linux/amd64

# linux/arm64
docker pull "ghcr.io/$GITHUB_USERNAME/$IMAGE" --platform linux/arm64
```
