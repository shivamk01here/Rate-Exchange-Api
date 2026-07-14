# Exchange Rate Service

Multi-provider exchange rate API built with Spring Boot 2.7 & WebFlux, inspired by PSP failover patterns.

## Endpoints

### Exchange Rates

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/rates` | Get exchange rate for a currency pair |
| POST | `/api/rates/batch` | Batch multi-currency conversion |
| POST | `/api/rates/compare` | Compare rates across providers |
| POST | `/api/rates/pipe` | Pipe-delimited rate request |
| GET | `/api/rates/convert?from=&to=&amount=` | Quick conversion via query parameters |
| GET | `/api/rates/result?data=` | Handle redirect from pipe-format request |

### Currencies

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/currencies` | List supported currencies |
| GET | `/api/currencies/search?q=` | Search currencies by code or name |
| GET | `/api/currencies/{code}` | Get specific currency info |

### Alerts

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/alerts` | Create a rate alert |
| GET | `/api/alerts` | List all alerts |
| GET | `/api/alerts/{id}` | Get alert by ID |
| DELETE | `/api/alerts/{id}` | Delete an alert |
| PATCH | `/api/alerts/{id}/toggle` | Enable/disable an alert |

### Audit & Export

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/audit/history?limit=` | Recent conversion history |
| GET | `/api/audit/history/page` | Paginated history with filters |
| GET | `/api/audit/history/pair?from=&to=` | History by currency pair |
| GET | `/api/audit/export/csv` | Export history as CSV |
| GET | `/api/audit/export/json` | Export history as JSON |
| GET | `/api/audit/stats` | Conversion statistics |

### Favorites

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/favorites` | Create a favorite pair |
| GET | `/api/favorites` | List all favorites |
| GET | `/api/favorites/{id}` | Get favorite by ID |
| PUT | `/api/favorites/{id}` | Update a favorite |
| DELETE | `/api/favorites/{id}` | Delete a favorite |
| GET | `/api/favorites/rates` | Fetch current rates for all favorites |

### Rate Trends

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/trends/snapshots?from=&to=` | Get all rate snapshots for a pair |
| GET | `/api/trends/snapshots/latest?from=&to=` | Get latest snapshot for a pair |
| GET | `/api/trends?from=&to=&limit=` | Get recent trend data points |
| GET | `/api/trends/summary?from=&to=` | Get full trend summary with stats |
| GET | `/api/trends/stats` | Get trend storage statistics |
| DELETE | `/api/trends/clear` | Clear all trend data |
| DELETE | `/api/trends/clear/{from}/{to}` | Clear trend data for a pair |

### Cache

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/cache/stats` | Cache statistics |
| DELETE | `/api/cache/{from}/{to}` | Evict specific cache entry |
| DELETE | `/api/cache/clear` | Clear entire cache |

### Health & System

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Full health check |
| GET | `/api/health/providers` | Provider metrics |
| GET | `/api/version` | Service version info |
| GET | `/api/rate-limit/status` | Rate limit config status |
| GET | `/api/rate-limit/clear` | Clear all rate limit entries |

### Notifications

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/sms/send` | Send SMS via default provider |
| POST | `/api/sms/send/{provider}` | Send SMS via specified provider |
| POST | `/api/whatsapp/send` | Send WhatsApp via default provider |
| POST | `/api/whatsapp/send/{provider}` | Send WhatsApp via specified provider |

## Rate Trend Tracking

The API automatically tracks exchange rate changes over time and provides trend analysis for any currency pair.

### Trend Directions
- **RISING** - rate increased above the stability threshold
- **FALLING** - rate decreased below the stability threshold
- **STABLE** - rate change is within the threshold (default: 0.5%)

### Example: Get Trend Summary
```bash
curl "http://localhost:8080/api/trends/summary?from=USD&to=INR"
```
```json
{
  "fromCurrency": "USD",
  "toCurrency": "INR",
  "totalSnapshots": 15,
  "latestRate": 84.25,
  "oldestRate": 83.10,
  "highestRate": 84.50,
  "lowestRate": 83.00,
  "averageRate": 83.75,
  "overallPercentChange": 1.38,
  "overallDirection": "RISING"
}
```

## Rate Limiting

The API uses per-client sliding window rate limiting to protect against abuse.

### Default Limits
| Endpoint | Requests/Window |
|----------|----------------|
| `/api/rates` | 30 per minute |
| `/api/rates/batch` | 10 per minute |
| `/api/rates/compare` | 20 per minute |
| `/api/rates/convert` | 30 per minute |
| All others | 100 per minute |

### Bypassed Endpoints
- `/api/health`
- `/api/version`
- `/api/rate-limit`

### Response Headers
Every response includes rate limit headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1720000000
```

### 429 Response
When rate limit is exceeded, the API returns HTTP 429 with a JSON body:
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "path": "/api/rates"
}
```

## Configuration

Rate limiting is configured in `application.yml`:
```yaml
rate-limit:
  enabled: true
  default-requests-per-window: 100
  window-size: 1m
  bypass-paths:
    - /api/health
  endpoints:
    /api/rates:
      max-requests: 30
      window: 1m
```

Rate trend tracking is configured per profile:
```yaml
rate-trend:
  enabled: true
  max-snapshots-per-pair: 500
  display-limit: 10
  stability-threshold-percent: 0.5
  cleanup-interval-minutes: 60
```

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```
