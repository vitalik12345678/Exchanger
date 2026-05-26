# Exchanger

A Spring Boot REST API for currency exchange rate lookups and conversions, backed by [exchangerates_data via APILayer](https://apilayer.com/marketplace/exchangerates_data-api). Rates are cached in-process for 60 seconds to minimise calls to the external provider.

---

## Tech stack

| Layer | Technology |
|---|---|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.6 (Spring Framework 7) |
| Build | Gradle 9 (wrapper included) |
| Cache | Caffeine — 60 s TTL, max 500 entries |
| Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Code style | Checkstyle 10 (Google rules) + Spotless (Eclipse JDT formatter) |
| Coverage | JaCoCo 0.8.13 — 80 % instruction coverage enforced |

---

## Prerequisites

- **Java 25** (`java -version` must report `25`)
- Internet access to `api.apilayer.com`
- An [APILayer exchangerates_data](https://apilayer.com/marketplace/exchangerates_data-api) API key

---

## Configuration

| Property | Env variable | Description |
|---|---|---|
| `exchanger.api.key` | `EXCHANGE_RATE_API_KEY` | APILayer API key (a demo key is bundled for quick starts) |
| `exchanger.api.base-url` | — | External API base URL (default: `https://api.apilayer.com/exchangerates_data`) |

```bash
# Linux / macOS
export EXCHANGE_RATE_API_KEY=your_key_here

# Windows PowerShell
$env:EXCHANGE_RATE_API_KEY = "your_key_here"
```

---

## Running the application

```bash
./gradlew bootRun
```

The server starts on **`http://localhost:8080`**.

---

## Interactive API docs

| Resource | URL |
|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

---

## API endpoints

All endpoints live under `/api/v1`. Currency codes are case-insensitive.

### Get exchange rate from A to B

```
GET /api/v1/rates/{originCurrency}/{targetCurrency}
```

```bash
curl "http://localhost:8080/api/v1/rates/USD/EUR"
```

```json
{
  "originCurrency": "USD",
  "targetCurrency": "EUR",
  "rate": 0.9234,
  "date": "2024-05-26"
}
```

### Get all exchange rates for a base currency

```
GET /api/v1/rates/{originCurrency}
```

```bash
curl "http://localhost:8080/api/v1/rates/USD"
```

```json
{
  "base": "USD",
  "rates": { "EUR": 0.9234, "GBP": 0.7801, "JPY": 156.78 },
  "date": "2024-05-26"
}
```

### Convert an amount from A to B

```
GET /api/v1/convert/{originCurrency}/{targetCurrency}?amount={amount}
```

```bash
curl "http://localhost:8080/api/v1/convert/USD/GBP?amount=250"
```

```json
{
  "originCurrency": "USD",
  "targetCurrency": "GBP",
  "amount": 250,
  "converted": 195.025,
  "rate": 0.7801
}
```

### Convert an amount to multiple currencies

```
GET /api/v1/convert/{originCurrency}?amount={amount}&targets={codes}
```

```bash
curl "http://localhost:8080/api/v1/convert/USD?amount=100&targets=EUR,GBP,JPY"
```

```json
{
  "originCurrency": "USD",
  "amount": 100,
  "results": [
    { "targetCurrency": "EUR", "converted": 92.34,   "rate": 0.9234 },
    { "targetCurrency": "GBP", "converted": 78.01,   "rate": 0.7801 },
    { "targetCurrency": "JPY", "converted": 15678.0, "rate": 156.78 }
  ]
}
```

---

## Error responses

All errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

| Status | Condition |
|---|---|
| `400` | Missing or invalid request parameter |
| `404` | Unknown currency code |
| `502` | External rate provider unavailable |

```json
{
  "type": "https://exchanger.example.com/errors/currency-not-found",
  "title": "Currency Not Found",
  "status": 404,
  "detail": "Currency not found: XYZ"
}
```

---

## Build and code quality

```bash
# Full check: compile → test → checkstyle → format check → coverage gate
./gradlew check

# Auto-format all sources
./gradlew spotlessApply

# Tests + JaCoCo HTML report  →  build/reports/jacoco/test/html/index.html
./gradlew test jacocoTestReport
```

The `check` task enforces:
- **Google Java style** via Checkstyle (warnings reported, violations logged)
- **Consistent formatting** via Spotless with Eclipse JDT formatter
- **≥ 80 % instruction coverage** via JaCoCo (excludes `Application`, `config`, and `model` packages)

---

## Architecture

```
ExchangeRateController     HTTP layer — routing, input validation
        │
ExchangeRateService        Business logic — rate lookup, conversion maths
        │
ExchangeCacheService       @Cacheable wrapper (Caffeine, 60 s TTL)
        │
ExchangeRateHostClient     RestClient adapter for the external API
```

The cache stores a lean `RatesSnapshot(date, rates)` rather than the full API response, so only the data that changes (rates + date) occupies heap.

---

## AOT / native image

Spring Boot 4 ships with Spring AOT processing and GraalVM native image support out of the box.

**This project has not yet been adapted for AOT.** The AOT build is intentionally skipped because it requires a GraalVM JDK and the `native-image` toolchain, which are not assumed to be present in every development environment.

To adopt AOT in the future:

1. Switch to a GraalVM JDK 25 distribution (e.g. via SDKMAN: `sdk install java 25-graalce`).
2. Add the native build plugin to `build.gradle`:
   ```groovy
   id 'org.graalvm.buildtools.native' version '0.10.6'
   ```
3. Compile the native image:
   ```bash
   ./gradlew nativeCompile
   ```
4. Address reflection and proxy hints surfaced by the AOT analysis — common sources in this project are Caffeine cache proxies, Jackson serializers for the response records, and Lombok-generated constructors.
