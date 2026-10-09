# Test script and API contract

The files in this directory **are part of the assignment, not of the solution**. They come from the
*Advanced Web Applications* course (Warsaw University of Technology, Computer Science) and were
provided by the course instructor. They are included in the repository **unchanged** and serve as
the basis for the automated tests of the application.

| File | Description |
| --- | --- |
| `zai-api-26z.yaml` | REST API contract (OpenAPI 3.1, version 2.0.0) implemented by the application |
| `zai-tests.mjs` | Instructor's automated tests (Node.js 20+, no extra packages) |

## Usage

Run the tests from the repository root (also used in CI):

```bash
node assignment/zai-tests.mjs --app https://staging.zai-meteo.pl --user <admin> --pass '<password>' --stage E1
```
