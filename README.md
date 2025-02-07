# codbex-phoebe

Phoebe Edition is a Web IDE which allows you to write [Apache Airflow](https://airflow.apache.org/) application in an
efficient way.

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
        * [Java standalone application](#java-standalone-application)
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

__Pre-requisites:__ [Build the project jar](#build-the-project-jar)

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

**TODO**

__Pre-requisites:__ [Build the project jar](#build-the-project-jar)

  ```shell
  export GIT_REPO_FOLDER='<set-your-path>'
  export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'
  export IMAGE='codbex-phoebe:dev'

  cd "$GIT_REPO_FOLDER/application"
  
  # cleanup
  docker compose down -v
  docker image rm $IMAGE --force
  
  export PHOEBE_IMAGE=$IMAGE
  docker compose up
  ```

### Java standalone application

> __Note:__ This will start the application but it will not have the embedded Apache Airflow instance.

__Pre-requisites:__ [Build the project jar](#build-the-project-jar)

- Start the application
    ```shell
    export GIT_REPO_FOLDER='<set-your-path>'
    export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'
  
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

__Pre-requisites:__ [Build the project jar](#build-the-project-jar)

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
