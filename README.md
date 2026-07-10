# Exchange Rate Service

Multi-provider exchange rate API built with Spring Boot 2.7 & WebFlux, inspired by PSP failover patterns.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/rates/convert` | Convert currency |
| POST | `/api/rates/batch` | Batch multi-currency conversion |
| GET | `/api/currencies` | List supported currencies |
| GET | `/api/audit/history` | Conversion history |
| GET | `/api/audit/history/page` | Paginated history |
| GET | `/api/audit/export/csv` | Export history as CSV |
| GET | `/api/audit/stats` | Conversion stats |
| GET | `/api/cache` | Cache management |
| GET | `/api/health` | System health |
| GET | `/api/rate-limit/status` | Rate limit config status |
| GET | `/api/rate-limit/clear` | Clear all rate limit entries |

## Rate Limiting

The API uses per-client sliding window rate limiting to protect against abuse.

### Default Limits
| Endpoint | Requests/Window |
|----------|----------------|
| `/api/rates` | 30 per minute |
| `/api/rates/batch` | 10 per minute |
| `/api/rates/compare` | 20 per minute |
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

### Configuration
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

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```
