# meteo

Web application for measurement series, built as a project for the *Advanced Web Applications*
course (Warsaw University of Technology). Sensors send air temperature readings for Warsaw, Kraków
and Gdańsk to a REST API; readers browse them as a chart and a table, and the administrator manages
series and sensors.

Stack: Java 25 and Spring Boot 4 (API), PostgreSQL with TimescaleDB, React (frontend), Python
(sensor emulator with real data from Open-Meteo), Docker Compose and Caddy, GitHub Actions.

## Repository layout

| Directory | Contents |
| --- | --- |
| `backend/` | REST API |
| `frontend/` | single-page application (planned) |
| `generator/` | sensor emulator (planned) |
| `deploy/` | Docker Compose files and environment template |
| `docs/` | documentation |
| `assignment/` | API contract and tests provided with the assignment |

## Running

Work in progress.
