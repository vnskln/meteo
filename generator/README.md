# Generator

Sensor emulator. It sends measurements to the application through `POST /api/measurements` with
the sensor's API key, exactly like a real sensor would. It never writes to the database directly,
so everything it sends goes through the same validation as any other sensor.

It is one file, `generator.py`, and uses only the Python standard library, so there is nothing to
install. I run it with Python 3.14, locally and in the `python:3.14.10-slim` image on the server.

What it can do:

- send past measurements with timestamps (backfill), useful for preparing sample data,
- send current measurements at a fixed interval (live), until stopped or until `--count` is reached,
- generate values in four ways: random in a range, random walk, sine wave, or real temperature from
  Open-Meteo,
- print a readable line for every measurement, including the reason when the server rejects one.

## Before you start

The generator needs a sensor key, and a sensor belongs to a series. Both are created by the
administrator through the API (and later in the admin panel):

1. `POST /api/series` creates a series with a name and a `[minValue, maxValue]` range,
2. `POST /api/sensors` with the series id registers a sensor and returns its `apiKey`, only once.

The root [`README.md`](../README.md) has the curl commands for this.

The key is never stored in the code. Pass it with `--api-key` or, better, in the `ZAI_API_KEY`
environment variable, so it doesn't end up in the shell history or the process list.

## Usage

```bash
export ZAI_API_KEY=<sensor key>

# real temperature in Warsaw for the last 24 hours (96 values, every 15 minutes)
python generator.py --api-url https://zai-meteo.pl --mode backfill --source open-meteo --latitude 52.23 --longitude 21.01

# 10 past values on a sine wave, 15 minutes apart, ending now
python generator.py --api-url https://zai-meteo.pl --mode backfill --source sine --min -5 --max 15 --count 10

# a new value every 5 seconds until Ctrl+C
python generator.py --api-url https://zai-meteo.pl --source random-walk --min -5 --max 15 --interval 5

# current temperature in Kraków every 15 minutes, the way it runs on the server
python generator.py --api-url https://zai-meteo.pl --source open-meteo --latitude 50.06 --longitude 19.94
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
| `--latitude`, `--longitude` | location for `open-meteo` | none, required for that source |

The sources:

- `range`: every value is random between `--min` and `--max`,
- `random-walk`: starts in the middle of the range and moves up or down by at most 1/20 of the range
  each time, so it looks like a slowly changing reading,
- `sine`: a sine wave between `--min` and `--max` with a period of 96 measurements (one day at the
  default 15-minute interval),
- `open-meteo`: `temperature_2m` from Open-Meteo for the given location; backfill takes the
  history, live takes the current value.

In backfill mode the timestamps end at the current time and go back by `--interval`. Open-Meteo
history always comes every 15 minutes, so `--interval` doesn't change it. In live mode no
timestamp is sent and the server uses its own time.

`--min` and `--max` only shape the generated values. The range that counts is the one set on the
series, so a value outside it is rejected by the server. That's an easy way to see the validation
working, for example `--source range --min 40 --max 60` on a series with a maximum of 50.

## Output

```
OK        2026-10-28T08:20:36Z  5.0
REJECTED  422: value 60.1: Value 60.1 is outside [-40.0, 50.0]
REJECTED  401: the API key is wrong or the sensor was deregistered
ERROR     cannot reach https://zai-meteo.pl: <reason>
```

A rejected value is printed with the server's reason and the generator goes on with the next one.
A connection error is printed and skipped in the same way. A rejected key (401) stops it, because
nothing more can be sent. Exit codes: `0` done, `1` key rejected, `2` wrong parameters.

## Running on the server

`deploy/generator/compose.yaml` runs three live generators, one per city (Warsaw, Kraków,
Gdańsk), each sending the current Open-Meteo temperature every 15 minutes. They send to the public
HTTPS address, like any sensor outside the server would.

There is one copy per environment: `/opt/zai/generator-staging` and `/opt/zai/generator-prod`.
After every deploy the pipeline copies `compose.yaml` and `generator.py` there and recreates the
containers. The `.env` with `API_URL` and the three sensor keys (see `deploy/generator/.env.example`)
was created by hand once, after registering the sensors, and the pipeline never touches it.

That's also where the sample data comes from. On production the three series have been filled by
these generators since 30 October 2026, so they hold real readings and grow by four values per hour
each. On staging I additionally ran a three-day backfill once, with
`docker compose run --rm <city> python -u /app/generator.py ... --mode backfill --count 288`.

## Data source

`--source open-meteo` uses `temperature_2m` from [Open-Meteo](https://open-meteo.com/), licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
