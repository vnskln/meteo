# Generator

Sensor emulator. It sends measurements to the application through `POST /api/measurements` with the sensor's API key, like a real sensor.

Needs: vanilla Python 3

## Usage

First create a series and a sensor in the application. Then pass the sensor's key with `--api-key` or in the `ZAI_API_KEY` environment variable.

```bash
export ZAI_API_KEY=<sensor key>

# real temperature in Warsaw for the last 24 hours
python generator.py --api-url https://zai-meteo.pl --mode backfill --source open-meteo --latitude 52.23 --longitude 21.01

# 10 past values on a sine wave, 15 minutes apart
python generator.py --api-url https://zai-meteo.pl --mode backfill --source sine --min -5 --max 15 --count 10

# a new value every 5 seconds until Ctrl+C
python generator.py --api-url https://zai-meteo.pl --source random-walk --min -5 --max 15 --interval 5
```

## Parameters

| Parameter | Description | Default |
|---|---|---|
| `--api-url` | application address | required |
| `--api-key` | sensor API key | `ZAI_API_KEY` |
| `--mode` | `backfill` sends past measurements, `live` sends current ones at a fixed interval | `live` |
| `--count` | number of measurements | 96 for backfill, no limit for live |
| `--interval` | seconds between measurements | 900 |
| `--source` | `range`, `random-walk`, `sine` or `open-meteo` | `random-walk` |
| `--min`, `--max` | value range for `range`, `random-walk` and `sine` | -40, 50 |
| `--latitude`, `--longitude` | location for `open-meteo` | none |

In live mode no timestamp is sent, so the server uses its own time. Open-Meteo history is always every 15 minutes, `--interval` doesn't change it.

## Output

```
OK        2026-10-28T08:20:36Z  5.0
REJECTED  422: value 60.1: Value 60.1 is outside [-40.0, 50.0]
REJECTED  401: the API key is wrong or the sensor was deregistered
```

A rejected value is printed and the generator goes on. A rejected key stops it. Exit codes: `0` done, `1` key rejected, `2` wrong parameters.

## Running on the server

`deploy/generator/compose.yaml` runs three live generators, one per city, with the current Open-Meteo temperature every 15 minutes. They send to the public address over HTTPS, like any outside sensor.

The deploy job copies `compose.yaml` and `generator.py` to `/opt/zai/generator-staging` and recreates the containers after every deploy. The `.env` with the sensor keys (see `.env.example`) is created there by hand once, after registering the sensors, and the pipeline never touches it.

## Data source

`--source open-meteo` uses `temperature_2m` from [Open-Meteo](https://open-meteo.com/), licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
