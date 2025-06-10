# Start Airflow only

```shell
export GIT_REPO_FOLDER='<path-to-the-git-repo>'
export GIT_REPO_FOLDER='/Users/iliyan/work/git/codbex-phoebe'
  
cd "$GIT_REPO_FOLDER/misc/airflow-standalone"

docker compose down -v

docker-compose up

```

## Login

Login at [http://localhost:8080](http://localhost:8080)
