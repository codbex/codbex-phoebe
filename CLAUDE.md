# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Phoebe is a **Web IDE for Apache Airflow workflow development**. It is a Spring Boot
application built on top of the **Eclipse Dirigible** platform (via the
`com.codbex.platform:codbex-platform-parent` parent POM). At runtime the Java app and an
Apache Airflow instance run side by side (in the same Docker container in production), and
Phoebe embeds Airflow's web UI by reverse-proxying to it.

## Build & test commands

Build profiles (`quick-build`, `unit-tests`, `integration-tests`, `tests`, `format`) are
**inherited from the parent platform POM**, not defined in this repo.

```shell
mvn -T 1C clean install -P quick-build   # build the executable jar (skips tests/checks)
mvn clean install -P unit-tests          # unit tests
mvn clean install -P integration-tests   # integration tests (Selenium-based UI tests)
mvn clean install -P tests               # all tests
mvn verify -P format                     # format the code (run before committing)
```

Run a single test class: `mvn test -P unit-tests -Dtest=TextResponseBodyRewriterTest`
(use `-pl integration-tests` and `-Dit.test=...` for integration tests).

The build produces `application/target/*executable*.jar`. Run it standalone with the
`--add-opens` flags shown in the README (required for Dirigible). Requires a running Airflow
instance (see README "Start Airflow") and Java 21.

## Architecture

Multi-module Maven build. Modules: `application`, `branding`, `components`,
`integration-tests`.

### `application/` — the runnable Spring Boot app
- `PhoebeApplication` — entry point. Scans both `com.codbex.phoebe` and
  `org.eclipse.dirigible.components`; Dirigible supplies most of the IDE, persistence, and
  security infrastructure. DataSource/JPA auto-configuration is deliberately excluded
  (Dirigible manages datasources).
- `proxy/` — **the core of Phoebe.** A Spring Cloud Gateway (MVC) route in
  `AirflowProxyConfig` forwards all methods on `/services/airflow/**` to the Airflow URL
  (`PHOEBE_AIRFLOW_URL`). Because Airflow's UI assumes it is served at the root, two
  rewriters fix up responses so the UI works behind the `/services/airflow` prefix:
  `RelativeLocationHeaderRewriter` (redirect/location headers) and
  `TextResponseBodyRewriter` (rewrites paths inside HTML/JS/CSS bodies). The route is locked
  to the Dirigible `DEVELOPER` role by `AirflowProxySecurityConfigurator`. When changing the
  proxy base path, also update
  `components/ui/perspective-airflow/.../index.html` which hardcodes `/services/airflow/`.
- `cfg/AppConfig` — typed enum over Dirigible's `Configuration` (env vars). Two keys:
  `PHOEBE_AIRFLOW_URL` (default `http://localhost:8080`) and `PHOEBE_AIRFLOW_WORK_DIR`
  (default `/opt/airflow`). Use this enum to read/set config, not raw env access.
- `Dockerfile` + `start.sh` — image is based on `apache/airflow:3.0.2`, adds Java 21
  (Corretto), node/esbuild/typescript (for Dirigible's TS compilation), and `ttyd` (terminal
  perspective). `start.sh` launches Airflow `standalone` and the Java jar together;
  PostgreSQL is used when the `PHOEBE_AIRFLOW_POSTGRES_*` env vars are all set, otherwise
  Airflow falls back to SQLite.

### `components/` — Eclipse Dirigible content modules
These are not Java logic; they package IDE artifacts as classpath resources under
`src/main/resources/META-INF/dirigible/`. Dirigible discovers them via `.extension` files
that register against extension points (e.g. `platform-perspectives`).
- `ui/perspective-airflow` — the embedded Airflow perspective (iframes the proxied UI).
- `ui/view-welcome`, `ui/menu-help` — welcome view and help menu.
- `templates/template-airflow-starter` — the "Apache Airflow Starter" project template
  (`template.js` defines generated DAG/config files); used by the IDE to scaffold new
  Airflow projects.

### `branding/` — Dirigible branding overrides (product name, logos, etc.)

### `integration-tests/` — UI integration tests
Extend `PhoebeIntegrationTest` (which extends Dirigible's `UserInterfaceIntegrationTest`,
a Selenium/browser harness). Tests drive the real IDE; e.g. `AirflowPerspectiveIT` points
`AppConfig.AIRFLOW_URL` at a stub host and asserts the proxied page renders.

## Conventions

- Every source file carries the EPL-2.0 license header (see existing files); keep it.
- Spring profiles: to activate Dirigible features, `SPRING_PROFILES_ACTIVE` must include
  `common` and `app-default` (e.g. `common,snowflake,app-default`).
- Admin login defaults to `admin`/`admin` (`DIRIGIBLE_BASIC_USERNAME`/`_PASSWORD`,
  Base64-encoded).
