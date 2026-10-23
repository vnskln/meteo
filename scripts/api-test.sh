#!/usr/bin/env bash
# Smoke test of all backend controllers against a running instance.
# Creates its own series and sensor and deletes them at the end.
#
# Usage:
#   ADMIN_USERNAME=admin ADMIN_PASSWORD=... ./scripts/api-test.sh [base-url]
# base-url defaults to http://localhost:8080

BASE_URL="${1:-http://localhost:8080}"
: "${ADMIN_USERNAME:?ADMIN_USERNAME is not set}"
: "${ADMIN_PASSWORD:?ADMIN_PASSWORD is not set}"

JSON="Content-Type: application/json"
PASSED=0
FAILED=0

# request METHOD PATH [curl options...]
# Sets STATUS, LOCATION and BODY.
request() {
    local method=$1 path=$2
    shift 2
    local out
    out=$(curl -s -X "$method" "$BASE_URL$path" -w $'\n%header{location}\n%{http_code}' "$@")
    STATUS=${out##*$'\n'}
    out=${out%$'\n'*}
    LOCATION=${out##*$'\n'}
    BODY=${out%$'\n'*}
}

# expect NAME EXPECTED_STATUS
expect() {
    if [ "$STATUS" = "$2" ]; then
        echo "PASS  $1"
        PASSED=$((PASSED + 1))
    else
        echo "FAIL  $1: expected $2, got $STATUS"
        echo "      $BODY"
        FAILED=$((FAILED + 1))
    fi
}

# check NAME CONDITION_RESULT - for checks other than the status code
check() {
    if [ "$2" = "true" ]; then
        echo "PASS  $1"
        PASSED=$((PASSED + 1))
    else
        echo "FAIL  $1"
        echo "      $BODY"
        FAILED=$((FAILED + 1))
    fi
}

contains() {
    case "$1" in *"$2"*) echo true ;; *) echo false ;; esac
}

# json_field NAME - value of a field from BODY (flat JSON object, no jq needed)
json_field() {
    echo "$BODY" | sed -nE "s/.*\"$1\":\"?([^\",}]*).*/\1/p"
}

# count - number of objects in a JSON array in BODY
count() {
    echo "$BODY" | grep -o '"id":' | wc -l | tr -d ' '
}

# first_timestamp / last_timestamp - timestamp of the first / last object in BODY
first_timestamp() {
    echo "$BODY" | grep -o '"timestamp":"[^"]*"' | head -1 | cut -d'"' -f4
}

last_timestamp() {
    echo "$BODY" | grep -o '"timestamp":"[^"]*"' | tail -1 | cut -d'"' -f4
}

echo "Testing $BASE_URL"

echo
echo "--- health"
request GET /api/health
expect "GET /api/health" 200
check "health body is {\"status\":\"ok\"}" "$(contains "$BODY" '"status":"ok"')"

echo
echo "--- auth"
request POST /api/auth/login -H "$JSON" -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"wrong-password\"}"
expect "login with wrong password" 401

request POST /api/auth/login -H "$JSON" -d "{\"username\":\"$ADMIN_USERNAME\"}"
expect "login without password" 400

request POST /api/auth/login -H "$JSON" -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}"
expect "login" 200
TOKEN=$(json_field accessToken)
check "login returns accessToken" "$([ -n "$TOKEN" ] && echo true || echo false)"
AUTH="Authorization: Bearer $TOKEN"

request POST /api/auth/login -H "$JSON" -d "{\"username\":\"$ADMIN_USERNAME\",\"password\":\"$ADMIN_PASSWORD\"}"
SECOND_TOKEN=$(json_field accessToken)

request PUT /api/auth/password -H "$JSON" -d '{"currentPassword":"x","newPassword":"long-enough"}'
expect "change password without token" 401

request PUT /api/auth/password -H "$AUTH" -H "$JSON" -d '{"currentPassword":"wrong-password","newPassword":"long-enough"}'
expect "change password with wrong current password" 403

request PUT /api/auth/password -H "$AUTH" -H "$JSON" -d "{\"currentPassword\":\"$ADMIN_PASSWORD\",\"newPassword\":\"short\"}"
expect "change password to a too short one" 400

request POST /api/auth/logout -H "Authorization: Bearer $SECOND_TOKEN"
expect "logout" 204

request GET /api/sensors -H "Authorization: Bearer $SECOND_TOKEN"
expect "logged out token no longer works" 401

request GET /api/sensors -H "$AUTH"
expect "other token still works after logout" 200

echo
echo "--- series"
SERIES='{"name":"API test series","minValue":0,"maxValue":10,"color":"#ff0000","unit":"C"}'

request GET /api/series
expect "GET /api/series without token" 200

request POST /api/series -H "$JSON" -d "$SERIES"
expect "create series without token" 401

request POST /api/series -H "$AUTH" -H "$JSON" -d '{"name":"x","minValue":10,"maxValue":10,"color":"#ff0000"}'
expect "create series with min = max" 422

request POST /api/series -H "$AUTH" -H "$JSON" -d '{"name":"x","minValue":0,"maxValue":10,"color":"red"}'
expect "create series with bad color" 400

request POST /api/series -H "$AUTH" -H "$JSON" -d '{"name":'
expect "create series with broken JSON" 400

request POST /api/series -H "$AUTH" -H "Content-Type: text/plain" -d 'hello'
expect "create series as text/plain" 415

request POST /api/series -H "$AUTH" -H "$JSON" -d "$SERIES"
expect "create series" 201
SERIES_ID=$(json_field id)
check "create series sets Location" "$(contains "$LOCATION" "/api/series/$SERIES_ID")"

request GET "/api/series/$SERIES_ID"
expect "GET /api/series/{id}" 200

request GET /api/series -H "Accept: application/xml"
expect "GET /api/series as XML" 406

request PUT "/api/series/$SERIES_ID" -H "$AUTH" -H "$JSON" \
    -d '{"name":"API test series (edited)","minValue":0,"maxValue":10,"color":"#00ff00","unit":"C"}'
expect "update series" 200

request PUT "/api/series/$SERIES_ID" -H "$JSON" -d "$SERIES"
expect "update series without token" 401

request GET /api/series/999999999
expect "GET missing series" 404

echo
echo "--- sensors"
request GET /api/sensors
expect "GET /api/sensors without token" 401

request POST /api/sensors -H "$AUTH" -H "$JSON" -d '{"name":"API test sensor","seriesId":999999999}'
expect "create sensor for missing series" 422

request POST /api/sensors -H "$AUTH" -H "$JSON" -d '{"seriesId":1}'
expect "create sensor without name" 400

request POST /api/sensors -H "$AUTH" -H "$JSON" -d "{\"name\":\"API test sensor\",\"seriesId\":$SERIES_ID}"
expect "create sensor" 201
SENSOR_ID=$(json_field id)
API_KEY=$(json_field apiKey)
check "create sensor returns apiKey" "$([ -n "$API_KEY" ] && echo true || echo false)"
check "create sensor sets Location" "$(contains "$LOCATION" "/api/sensors/$SENSOR_ID")"

request GET /api/sensors -H "$AUTH"
expect "GET /api/sensors" 200
check "sensor list has no key or hash" "$( [ "$(contains "$BODY" apiKey)" = false ] && [ "$(contains "$BODY" ash)" = false ] && echo true || echo false)"

request GET "/api/sensors/$SENSOR_ID" -H "$AUTH"
expect "GET /api/sensors/{id}" 200

request GET /api/sensors/999999999 -H "$AUTH"
expect "GET missing sensor" 404

echo
echo "--- measurements"
KEY="X-API-Key: $API_KEY"
PAST=$(date -u -d '-1 hour' +%Y-%m-%dT%H:%M:%SZ)
FUTURE=$(date -u -d '+10 minutes' +%Y-%m-%dT%H:%M:%SZ)

request POST /api/measurements -H "$JSON" -d '{"value":5}'
expect "measurement without key" 401

request POST /api/measurements -H "$AUTH" -H "$JSON" -d '{"value":5}'
expect "measurement with admin token instead of key" 401

request POST /api/measurements -H "X-API-Key: wrong-key" -H "$JSON" -d '{"value":5}'
expect "measurement with wrong key" 401

request POST /api/measurements -H "$KEY" -H "$JSON" -d "{\"value\":5,\"timestamp\":\"$PAST\"}"
expect "measurement with timestamp" 201
MEASUREMENT_ID=$(json_field id)
check "measurement sets Location" "$(contains "$LOCATION" "/api/measurements/$MEASUREMENT_ID")"
check "measurement belongs to the sensor's series" "$(contains "$BODY" "\"seriesId\":$SERIES_ID")"

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":6}'
expect "measurement without timestamp" 201

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":0}'
expect "measurement equal to min" 201

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":10}'
expect "measurement equal to max" 201

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":11}'
expect "measurement above max" 422

request POST /api/measurements -H "$KEY" -H "$JSON" -d "{\"value\":5,\"timestamp\":\"$FUTURE\"}"
expect "measurement 10 minutes in the future" 422

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":"abc"}'
expect "measurement with text value" 400

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{}'
expect "measurement without value" 400

request POST /api/measurements -H "$KEY" -H "Content-Type: text/plain" -d 'hello'
expect "measurement as text/plain" 415

# The series now has 4 measurements: 5 (at PAST), 6, 0 and 10
request GET "/api/measurements/$MEASUREMENT_ID"
expect "GET /api/measurements/{id}" 200
check "GET /api/measurements/{id} returns the saved value" "$(contains "$BODY" '"value":5')"

request GET /api/measurements/999999999
expect "GET missing measurement" 404

request GET "/api/measurements?series=$SERIES_ID"
expect "GET /api/measurements?series=" 200
check "series filter returns 4 measurements" "$([ "$(count)" = 4 ] && echo true || echo false)"
check "default sort is ascending (oldest first)" "$([ "$(first_timestamp)" = "$PAST" ] && echo true || echo false)"

request GET "/api/measurements?series=$SERIES_ID&sort=-timestamp"
expect "GET with sort=-timestamp" 200
check "sort=-timestamp is descending (oldest last)" "$([ "$(last_timestamp)" = "$PAST" ] && echo true || echo false)"

request GET "/api/measurements?series=$SERIES_ID&from=$PAST&to=$PAST"
expect "GET with from = to" 200
check "from/to range is closed" "$([ "$(count)" = 1 ] && echo true || echo false)"

request GET "/api/measurements?series=$SERIES_ID&limit=1"
expect "GET with limit=1" 200
check "limit=1 returns 1 measurement" "$([ "$(count)" = 1 ] && echo true || echo false)"

request GET "/api/measurements?from=not-a-date"
expect "GET with from=not-a-date" 400

request GET "/api/measurements?sort=value"
expect "GET with unknown sort" 400

request GET "/api/measurements?limit=0"
expect "GET with limit=0" 400

request PUT "/api/measurements/$MEASUREMENT_ID" -H "$AUTH" -H "$JSON" -d '{"value":7}'
expect "PUT measurement is not allowed" 405

request DELETE "/api/measurements/$MEASUREMENT_ID" -H "$AUTH"
expect "DELETE measurement is not allowed" 405

request GET "/api/measurements/$MEASUREMENT_ID"
check "measurement is unchanged after PUT and DELETE" "$(contains "$BODY" '"value":5')"

echo
echo "--- series range vs measurements"
request PUT "/api/series/$SERIES_ID" -H "$AUTH" -H "$JSON" \
    -d '{"name":"API test series","minValue":0.5,"maxValue":10,"color":"#ff0000","unit":"C"}'
expect "update series with min above a measurement" 409

request PUT "/api/series/$SERIES_ID" -H "$AUTH" -H "$JSON" \
    -d '{"name":"API test series","minValue":0,"maxValue":9.5,"color":"#ff0000","unit":"C"}'
expect "update series with max below a measurement" 409

request PUT "/api/series/$SERIES_ID" -H "$AUTH" -H "$JSON" -d "$SERIES"
expect "update series with measurements on both bounds" 200

echo
echo "--- cleanup"
request DELETE "/api/sensors/$SENSOR_ID" -H "$AUTH"
expect "revoke sensor" 204

request GET "/api/sensors/$SENSOR_ID" -H "$AUTH"
expect "revoked sensor is gone" 404

request POST /api/measurements -H "$KEY" -H "$JSON" -d '{"value":5}'
expect "revoked key no longer works" 401

request DELETE "/api/series/$SERIES_ID"
expect "delete series without token" 401

request DELETE "/api/series/$SERIES_ID" -H "$AUTH"
expect "delete series" 204

request GET "/api/series/$SERIES_ID"
expect "deleted series is gone" 404

request POST /api/auth/logout -H "$AUTH"
expect "final logout" 204

echo
echo "Passed: $PASSED, failed: $FAILED"
[ "$FAILED" -eq 0 ]
