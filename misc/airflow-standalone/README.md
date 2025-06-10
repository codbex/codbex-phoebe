# Start Airflow only

```shell
export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-phoebe'

cd "$GIT_REPO_FOLDER/misc/airflow-standalone"

docker compose down -v

docker-compose up

```

## Login

User: `airflow`
Pass: `airflow`
