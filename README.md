# Exchange Rate API

A Spring Boot REST API that fetches exchange rates from [exchangerate.host](https://exchangerate.host) (via APILayer) and provides currency lookup and conversion endpoints. Rates are cached in-process for up to 60 seconds to minimise calls to the external provider.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 25 |
| Gradle | bundled via `./gradlew` wrapper |

---

## Configuration

The API key is read from the environment variable `EXCHANGE_RATE_API_KEY`.  
Obtain a free key at <https://apilayer.com/marketplace/exchangerates_data-api>.

```bash
export EXCHANGE_RATE_API_KEY=your_api_key_here
```

On Windows (PowerShell):
```powershell
$env:EXCHANGE_RATE_API_KEY = "your_api_key_here"
```

---

## Running the Application

```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080**.

---

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

---

## API Endpoints

All endpoints are under the base path `/api/v1`.

### 1. Get exchange rate from A to B

```
GET /api/v1/rates/{from}/{to}
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `from`    | path | Source currency code (e.g. `USD`) |
| `to`      | path | Target currency code (e.g. `EUR`) |

**Example**
```
GET /api/v1/rates/USD/EUR
```
```json
{
  "from": "USD",
  "to": "EUR",
  "rate": 0.9234,
  "date": "2024-05-26"
}
```

---

### 2. Get all exchange rates from A

```
GET /api/v1/rates/{from}
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `from`    | path | Base currency code (e.g. `USD`) |

**Example**
```
GET /api/v1/rates/USD
```
```json
{
  "base": "USD",
  "rates": {
    "EUR": 0.9234,
    "GBP": 0.7801,
    "JPY": 156.78
  },
  "date": "2024-05-26"
}
```

---

### 3. Convert value from A to B

```
GET /api/v1/convert/{from}/{to}?amount={amount}
```

| Parameter | Type  | Description |
|-----------|-------|-------------|
| `from`    | path  | Source currency code |
| `to`      | path  | Target currency code |
| `amount`  | query | Positive numeric amount to convert |

**Example**
```
GET /api/v1/convert/USD/EUR?amount=100
```
```json
{
  "from": "USD",
  "to": "EUR",
  "amount": 100.0,
  "converted": 92.34,
  "rate": 0.9234
}
```

---

### 4. Convert value from A to multiple currencies

```
GET /api/v1/convert/{from}?amount={amount}&targets={codes}
```

| Parameter | Type  | Description |
|-----------|-------|-------------|
| `from`    | path  | Source currency code |
| `amount`  | query | Positive numeric amount to convert |
| `targets` | query | Comma-separated list of target currency codes |

**Example**
```
GET /api/v1/convert/USD?amount=100&targets=EUR,GBP,JPY
```
```json
{
  "from": "USD",
  "amount": 100.0,
  "results": [
    { "to": "EUR", "converted": 92.34, "rate": 0.9234 },
    { "to": "GBP", "converted": 78.01, "rate": 0.7801 },
    { "to": "JPY", "converted": 15678.0, "rate": 156.78 }
  ]
}
```

---

## Error Responses

All errors follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

| Status | Meaning |
|--------|---------|
| `400`  | Invalid or missing request parameter |
| `404`  | Unknown currency code |
| `502`  | External rate provider unavailable |
| `500`  | Unexpected server error |

**Example 404**
```json
{
  "type": "https://exchanger.example.com/errors/currency-not-found",
  "title": "Currency Not Found",
  "status": 404,
  "detail": "Currency not found: XYZ"
}
```

---

## Caching

Rates are cached in-process using **Caffeine** with a 60-second TTL (configurable via `application.yaml`).  
All four endpoints share a single cached fetch per base currency per minute, so at most one external API call is made per base currency per 60-second window regardless of how many requests arrive.

---

## Running Tests

```bash
./gradlew test
```

Test reports are generated at `build/reports/tests/test/index.html`.
