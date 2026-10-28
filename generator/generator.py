import argparse
import json
import math
import os
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta, timezone

OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast"


def parse_args(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("--api-url", required=True, help="application address, e.g. https://zai-meteo.pl")
    parser.add_argument("--api-key", default=os.environ.get("ZAI_API_KEY"), help="sensor API key (default: ZAI_API_KEY)")
    parser.add_argument("--mode", choices=["backfill", "live"], default="live")
    parser.add_argument("--count", type=int, help="number of measurements (default: 96 for backfill, no limit for live)")
    parser.add_argument("--interval", type=int, default=900, help="seconds between measurements")
    parser.add_argument("--source", choices=["range", "random-walk", "sine", "open-meteo"], default="random-walk")
    parser.add_argument("--min", type=float, default=-40)
    parser.add_argument("--max", type=float, default=50)
    parser.add_argument("--latitude", type=float)
    parser.add_argument("--longitude", type=float)
    args = parser.parse_args(argv)

    if not args.api_key:
        parser.error("API key is missing: use --api-key or set ZAI_API_KEY")
    if args.source == "open-meteo" and (args.latitude is None or args.longitude is None):
        parser.error("--source open-meteo needs --latitude and --longitude")
    if args.mode == "backfill" and args.count is None:
        args.count = 96
    return args


def range_value(low, high):
    return round(random.uniform(low, high), 1)


def random_walk_value(previous, low, high):
    if previous is None:
        return round((low + high) / 2, 1)
    step = random.uniform(-1, 1) * (high - low) / 20
    return round(min(max(previous + step, low), high), 1)


def sine_value(i, low, high, period=96):
    middle = (low + high) / 2
    amplitude = (high - low) / 2
    return round(middle + amplitude * math.sin(2 * math.pi * i / period), 1)


def next_value(args, i, previous):
    if args.source == "range":
        return range_value(args.min, args.max)
    if args.source == "sine":
        return sine_value(i, args.min, args.max)
    return random_walk_value(previous, args.min, args.max)


def get_json(url):
    with urllib.request.urlopen(url, timeout=10) as response:
        return json.load(response)


def parse_history(data):
    times = data["minutely_15"]["time"]
    values = data["minutely_15"]["temperature_2m"]
    return [(t + ":00Z", v) for t, v in zip(times, values) if v is not None]


def open_meteo_history(latitude, longitude, count):
    query = urllib.parse.urlencode({
        "latitude": latitude,
        "longitude": longitude,
        "minutely_15": "temperature_2m",
        "past_minutely_15": count,
        "forecast_minutely_15": 0,
        "timezone": "GMT",
    })
    return parse_history(get_json(OPEN_METEO_URL + "?" + query))


def open_meteo_current(latitude, longitude):
    query = urllib.parse.urlencode({"latitude": latitude, "longitude": longitude, "current": "temperature_2m"})
    return get_json(OPEN_METEO_URL + "?" + query)["current"]["temperature_2m"]


def send(api_url, api_key, value, timestamp=None):
    body = {"value": value}
    if timestamp is not None:
        body["timestamp"] = timestamp
    request = urllib.request.Request(
        api_url.rstrip("/") + "/api/measurements",
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json", "Accept": "application/json", "X-API-Key": api_key},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.load(response)


def send_and_report(args, value, timestamp=None):
    try:
        measurement = send(args.api_url, args.api_key, value, timestamp)
        print(f"OK        {measurement['timestamp']}  {value}")
    except urllib.error.HTTPError as e:
        if e.code == 401:
            print("REJECTED  401: the API key is wrong or the sensor was deregistered")
            return False
        print(f"REJECTED  {e.code}: value {value}: {json.load(e)['detail']}")
    except urllib.error.URLError as e:
        print(f"ERROR     cannot reach {args.api_url}: {e.reason}")
    return True


def backfill(args):
    if args.source == "open-meteo":
        points = open_meteo_history(args.latitude, args.longitude, args.count)
    else:
        now = datetime.now(timezone.utc).replace(microsecond=0)
        points = []
        value = None
        for i in range(args.count):
            timestamp = now - timedelta(seconds=args.interval * (args.count - 1 - i))
            value = next_value(args, i, value)
            points.append((timestamp.strftime("%Y-%m-%dT%H:%M:%SZ"), value))

    for timestamp, value in points:
        if not send_and_report(args, value, timestamp):
            return 1
    return 0


def live(args):
    i = 0
    value = None
    while args.count is None or i < args.count:
        if i > 0:
            time.sleep(args.interval)
        if args.source == "open-meteo":
            value = open_meteo_current(args.latitude, args.longitude)
        else:
            value = next_value(args, i, value)
        if not send_and_report(args, value):
            return 1
        i += 1
    return 0


def main(argv=None):
    args = parse_args(argv)
    if args.mode == "backfill":
        return backfill(args)
    return live(args)


if __name__ == "__main__":
    sys.exit(main())
