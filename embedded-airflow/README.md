## embedded-airflow

### Build the jar

```shell
GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

cd "$GIT_REPO_FOLDER"

mvn -T 1C clean install -D skipTests -D maven.test.skip=true -D maven.javadoc.skip=true -D license.skip=true

# run java application
# java -jar application/target/codbex-airflow-application-*.jar

# run java application in debug
# java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 -jar application/target/codbex-airflow-application-*.jar
```

### Build the image and run it

```shell
GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

# Build docker image and run it
cd "$GIT_REPO_FOLDER"
docker build -f embedded-airflow/Dockerfile --tag codbex-airflow:dev .

cd "$GIT_REPO_FOLDER/embedded-airflow"

docker compose stop
docker compose down -v

docker compose up
```

### All in one

```shell
GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

cd "$GIT_REPO_FOLDER"

mvn -T 1C clean install -D skipTests -D maven.test.skip=true -D maven.javadoc.skip=true -D license.skip=true

# Build docker image and run it
cd "$GIT_REPO_FOLDER"
docker build -f embedded-airflow/Dockerfile --tag codbex-airflow:dev .

cd "$GIT_REPO_FOLDER/embedded-airflow"

docker compose stop
docker compose down -v

docker compose up
```

### Multi-platform Docker build

```shell
GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

cd "$GIT_REPO_FOLDER"

mvn -T 1C clean install -D skipTests -D maven.test.skip=true -D maven.javadoc.skip=true -D license.skip=true

# Build docker image
cd "$GIT_REPO_FOLDER"

export DOCKER_CLI_EXPERIMENTAL=enabled
docker buildx create --use

# build image for linux/amd64 locally
docker buildx build -f embedded-airflow/Dockerfile --platform linux/amd64 -t codbex-airflow-multi:amd64 --load .

# build image for linux/arm64 locally
docker buildx build -f embedded-airflow/Dockerfile --platform linux/arm64 -t codbex-airflow-multi:arm64 --load .

# build images for both platforms only
docker buildx build -f embedded-airflow/Dockerfile --platform=linux/arm64,linux/amd64 --tag codbex-airflow-multi:latest -o type=image  .

# build multiplatform image and push it
docker buildx build -f embedded-airflow/Dockerfile \
    --platform linux/amd64,linux/arm64 \
    -t ghcr.io/iliyan-velichkov/airflow-ide:latest \
    --push .
    
# pull the image locally

# linux/amd64
docker pull ghcr.io/iliyan-velichkov/airflow-ide:latest --platform linux/amd64

# linux/arm64
docker pull ghcr.io/iliyan-velichkov/airflow-ide:latest --platform linux/arm64
```
