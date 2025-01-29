## embedded-airflow

### Build the jar
```shell
GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-airflow'

cd "$GIT_REPO_FOLDER"

mvn -T 1C clean install -D skipTests -D maven.test.skip=true -D maven.javadoc.skip=true -D license.skip=true

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
