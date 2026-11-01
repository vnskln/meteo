# meteo

[![Deploy Pipeline](https://github.com/vnskln/meteo/actions/workflows/pipeline.yaml/badge.svg?branch=main)](https://github.com/vnskln/meteo/actions/workflows/pipeline.yaml)

Web application for measurement series, built as a project for the *Advanced Web Applications*
course (Warsaw University of Technology). Sensors send air temperature readings for Warsaw, Kraków
and Gdańsk to a REST API; readers browse them as a chart and a table, and the administrator manages
series and sensors.

The sensors are emulated by a small Python script that reads the real temperature from
[Open-Meteo](https://open-meteo.com/) and sends it to the API every 15 minutes, the same way a
physical device would.

Stack: Java 25 and Spring Boot 4.1 (API), PostgreSQL 17 with TimescaleDB, React (frontend, in
progress), Python (sensor emulator), Docker Compose and Caddy, GitHub Actions.

## Where it runs

| Environment | Address | Deployed |
| --- | --- | --- |
| production | https://zai-meteo.pl | after manual approval in the pipeline |
| staging | https://staging.zai-meteo.pl | automatically after every push to `main` |

The API is under `/api`, for example https://zai-meteo.pl/api/series or
https://zai-meteo.pl/api/health. The root page is still a placeholder, the frontend comes in the
second stage.

## Status

The project is handed in two stages. The first one (deployed API) is done, now I'm working on
the frontend.

Stage E1, deployed API:

- [x] REST API that follows the course contract (`assignment/zai-api-26z.yaml`): series, sensors,
  measurements, admin login, logout and password change, errors as Problem Details
- [x] sensors authenticate with their own API key in the `X-API-Key` header; the key is shown once
  at registration, only its SHA-256 hash is stored, and a deregistered sensor's key stops working
  while its measurements stay
- [x] values outside the series range and timestamps from the future are rejected (422) and logged
- [x] PostgreSQL database, schema created by Flyway migrations (`backend/src/main/resources/db/migration`)
- [x] public deployment over HTTPS, HTTP redirects to HTTPS, security headers (CSP, HSTS, `nosniff`) set in Caddy
- [x] sensor emulator (`generator/`) with past (backfill) and live mode
- [x] sample data: three series (Warsaw, Cracow, Gdansk), filled through the API by three generator
  instances running on the server since 30 October 2026
- [x] the instructor's test script (`assignment/zai-tests.mjs`) passes all contract, validation
  and security checks on staging and production

Stage E2, full application:

- [ ] React single-page app: chart and table, time range and series filters, clicking a table
  row highlights the point on the chart
- [ ] admin panel for series and sensors
- [ ] responsive layout, print view, accessibility (Lighthouse)
- [ ] documentation and demo recording

Bonus parts:

- [x] CI/CD in GitHub Actions: tests block the deployment, staging is deployed automatically,
  production with the same image after approval (badge at the top)
- [x] TimescaleDB: the `measurements` table is a hypertable partitioned by time
  (`V2__measurements_hypertable.sql`)
- [x] real data in the generator: current and past temperature from Open-Meteo
- [ ] live chart updates with server-sent events, planned together with the frontend

## Repository layout

| Path | Contents |
| --- | --- |
| `backend/` | Spring Boot API: one package per resource (`series`, `sensor`, `measurement`, `auth`), error handling in `error`, migrations in `src/main/resources/db/migration`, integration tests in `src/test` (they start PostgreSQL with Testcontainers) |
| `generator/` | sensor emulator, see [`generator/README.md`](generator/README.md) |
| `frontend/` | single-page application (not there yet) |
| `deploy/` | Docker Compose files: `compose.yaml` is shared, `compose.local.yaml`, `compose.staging.yaml` and `compose.prod.yaml` add what differs per environment, `compose.e2e.yaml` is used in CI; `.env.example` lists the variables |
| `deploy/edge/` | Caddy, the only entry point on the server: HTTPS certificates, security headers, proxy to staging and production |
| `deploy/generator/` | three live generators (one per city) run next to each environment |
| `.github/workflows/pipeline.yaml` | the whole CI/CD pipeline |
| `docs/` | database diagram (`erd-v1.png`) and AI usage declaration |
| `assignment/` | API contract and test script from the course, unchanged (see [`assignment/README.md`](assignment/README.md)) |
| `scripts/api-test.sh` | quick curl check of all endpoints against a running instance |

## How it's deployed

Everything runs on one small VPS. Caddy listens on ports 80 and 443 and forwards `/api` to the
staging or production backend. Each environment is a separate Compose project with its own
database, and neither the database nor the backend is published to the internet directly.

The pipeline runs on every push to `main`:

1. `backend` builds the API and runs the JUnit tests,
2. `e2e` starts the whole stack with Caddy, fills it with generator data and runs the
   instructor's tests over HTTPS; `image` builds the Docker image and pushes it to GHCR, tagged with
   the commit SHA,
3. `deploy-staging` deploys that image, restarts the generators, runs the instructor's tests against
   staging and deletes the test series they leave behind,
4. `deploy-edge` (Caddy config) and `deploy-prod` wait for my approval; production gets exactly the
   image that was tested on staging.

## Running locally

Needs Docker with Compose v2. JDK 25 is only needed to run the backend outside Docker (the Maven
Wrapper is in the repo), Python 3 for the generator and Node.js 20+ for the instructor's tests.

```bash
cd deploy
cp .env.example .env    # fill in POSTGRES_PASSWORD and ADMIN_PASSWORD, leave TAG empty
docker compose -f compose.yaml -f compose.local.yaml up --build
```

The API is then on http://localhost:8080 and the database on `localhost:5432`. The admin account
is created on the first start from `ADMIN_USERNAME` and `ADMIN_PASSWORD`.

To get some data, log in, create a series and a sensor, and run the generator with the sensor key:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"<ADMIN_PASSWORD>"}' | jq -r .accessToken)

curl -s -X POST http://localhost:8080/api/series -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Warsaw","minValue":-40,"maxValue":50,"color":"#d62728","unit":"°C"}'

curl -s -X POST http://localhost:8080/api/sensors -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"name":"Warsaw sensor","seriesId":1}'
# the response contains apiKey, it is shown only once

python generator/generator.py --api-url http://localhost:8080 --api-key <apiKey> \
  --mode backfill --source open-meteo --latitude 52.23 --longitude 21.01
```

Tests:

```bash
cd backend && ./mvnw verify    # Docker has to be running, Testcontainers starts PostgreSQL
node assignment/zai-tests.mjs --app http://localhost:8080 --user admin --pass '<ADMIN_PASSWORD>' --stage E1
```

`--stage E1` runs only the contract checks. With `--stage E2` the script also checks HTTPS and
security headers, and those fail locally because there is no Caddy in front of the backend (the
`e2e` job in CI adds it with `compose.e2e.yaml`).

## AI usage

I used Claude (Anthropic) in Claude Code during the project: to explain and create things I didn't know well
(GitHub Actions, Caddy), to review and give feedback about the API and the architecture, and to draft descriptions like this README. What exactly was done with its help, and
when, is listed in [`docs/ai-declaration.md`](docs/ai-declaration.md), which I keep up to date as
the project goes on. I've reviewed everything that ended up in the repository and I'm responsible
for all of it.

## Data source

Temperature readings come from [Open-Meteo](https://open-meteo.com/) (`temperature_2m`), licensed
under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
