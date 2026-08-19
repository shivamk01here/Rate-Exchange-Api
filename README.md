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

### Currency Symbol Lookup

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/currencies/symbols` | List all currency symbols |
| GET | `/api/currencies/symbols/{code}` | Get symbol for a currency code |
| GET | `/api/currencies/symbols/check?symbol=` | Check if a symbol is known |

### Currency Nicknames

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/currency-nicknames` | Give a currency code a friendly nickname |
| GET | `/api/currency-nicknames` | List all nicknames |
| GET | `/api/currency-nicknames/{id}` | Get nickname by ID |
| GET | `/api/currency-nicknames/by-code?code=` | Find nicknames by currency code |
| GET | `/api/currency-nicknames/by-nickname?nickname=` | Find nicknames by nickname |
| PUT | `/api/currency-nicknames/{id}` | Update a nickname |
| DELETE | `/api/currency-nicknames/{id}` | Delete a nickname |
| GET | `/api/currency-nicknames/count` | Get total nickname count |

#### Example: Create a Nickname
```bash
curl -X POST http://localhost:8080/api/currency-nicknames \
  -H "Content-Type: application/json" \
  -d '{"currencyCode": "USD", "nickname": "bucks"}'
```

### Currency Groups

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/currency-groups` | Create a named group of currency pairs |
| GET | `/api/currency-groups` | List all groups |
| GET | `/api/currency-groups/{id}` | Get group by ID |
| GET | `/api/currency-groups/by-name?name=` | Find groups by name |
| GET | `/api/currency-groups/by-pair?from=&to=` | Find groups containing a pair |
| PUT | `/api/currency-groups/{id}` | Update a group |
| DELETE | `/api/currency-groups/{id}` | Delete a group |
| POST | `/api/currency-groups/{id}/pairs` | Add a pair to a group |
| DELETE | `/api/currency-groups/{id}/pairs/{from}/{to}` | Remove a pair from a group |
| GET | `/api/currency-groups/count` | Get total group count |

#### Example: Create a Group and Add Pairs
```bash
# Create
curl -X POST http://localhost:8080/api/currency-groups \
  -H "Content-Type: application/json" \
  -d '{"name": "Travel", "description": "currencies for the trip"}'

# Add a pair
curl -X POST http://localhost:8080/api/currency-groups/1/pairs \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "INR"}'
```

### Recent Currency Pairs

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/recent-pairs/record?from=&to=` | Record a currency pair usage |
| GET | `/api/recent-pairs` | List pairs, newest first |
| GET | `/api/recent-pairs/by-pair?from=&to=` | Get a specific pair |
| GET | `/api/recent-pairs/top?limit=` | Get most recently used pairs |
| GET | `/api/recent-pairs/most-used?limit=` | Get pairs ranked by usage count |
| DELETE | `/api/recent-pairs/by-pair?from=&to=` | Delete a pair |
| DELETE | `/api/recent-pairs/clear` | Clear all tracked pairs |
| GET | `/api/recent-pairs/count` | Get total tracked pair count |

#### Example: Record and Query Pairs
```bash
# Record usage
curl -X POST "http://localhost:8080/api/recent-pairs/record?from=USD&to=INR"

# Most used pairs
curl "http://localhost:8080/api/recent-pairs/most-used?limit=5"
```
Repeatedly recording the same pair bumps its `useCount` and refreshes `lastUsedAt`.

### Trending Pairs

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/trending-pairs?limit=` | Pairs ranked by total converted volume |
| GET | `/api/trending-pairs/by-count?limit=` | Pairs ranked by number of conversions |
| GET | `/api/trending-pairs/recent?hours=&limit=` | Trending pairs within the last N hours |
| GET | `/api/trending-pairs/count` | Number of distinct pairs seen |

#### Example: Query Trending Pairs
```bash
# Top pairs by volume
curl "http://localhost:8080/api/trending-pairs?limit=5"

# Trending within the last 24 hours
curl "http://localhost:8080/api/trending-pairs/recent?hours=24&limit=5"
```
Trending pairs are computed on the fly from the conversion history.

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

### Portfolio Tracker

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/portfolio` | Create a portfolio |
| GET | `/api/portfolio` | List all portfolios |
| GET | `/api/portfolio/{id}` | Get portfolio by ID |
| PUT | `/api/portfolio/{id}` | Update a portfolio |
| DELETE | `/api/portfolio/{id}` | Delete a portfolio |
| POST | `/api/portfolio/{id}/holdings` | Add a currency holding |
| DELETE | `/api/portfolio/{id}/holdings/{currency}` | Remove a currency holding |
| GET | `/api/portfolio/{id}/value` | Get real-time portfolio valuation |
| GET | `/api/portfolio/count` | Get total portfolio count |

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

### Webhooks

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/webhooks` | Create a webhook subscription |
| GET | `/api/webhooks` | List all webhooks |
| GET | `/api/webhooks/{id}` | Get webhook by ID |
| GET | `/api/webhooks/by-event?event=` | Get webhooks by event type |
| DELETE | `/api/webhooks/{id}` | Delete a webhook |
| PATCH | `/api/webhooks/{id}/toggle` | Enable/disable a webhook |
| GET | `/api/webhooks/count` | Get total webhook count |
| GET | `/api/webhooks/stats` | Get webhook delivery metrics |

#### Webhook Event Types
- `RATE_ALERT_TRIGGERED` - Fires when any rate alert triggers
- `RATE_ABOVE_THRESHOLD` - Fires when rate goes above threshold
- `RATE_BELOW_THRESHOLD` - Fires when rate goes below threshold

#### Example: Create a Webhook
```bash
curl -X POST http://localhost:8080/api/webhooks \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/webhook", "events": ["RATE_ALERT_TRIGGERED"], "secret": "my-secret-key", "enabled": true}'
```

#### Webhook Payload
When triggered, the webhook receives a POST with:
```json
{
  "event": "RATE_ALERT_TRIGGERED",
  "alertId": "1",
  "fromCurrency": "USD",
  "toCurrency": "INR",
  "condition": "RATE_ABOVE",
  "threshold": 85.00,
  "currentRate": 86.25,
  "timestamp": "2026-07-19T10:30:00Z"
}
```

The webhook includes an `X-Webhook-Secret` header for signature verification when a secret is configured.

#### Webhook Delivery Logs
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/webhooks/delivery-logs?limit=` | Recent delivery logs |
| GET | `/api/webhooks/delivery-logs/{webhookId}` | Delivery logs for a specific webhook |

### API Keys

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/keys` | Create an API key |
| GET | `/api/keys` | List all API keys |
| GET | `/api/keys/{id}` | Get API key by ID |
| GET | `/api/keys/validate?key=` | Validate an API key |
| DELETE | `/api/keys/{id}` | Delete an API key |
| PATCH | `/api/keys/{id}/toggle` | Enable/disable an API key |
| GET | `/api/keys/count` | Get total API key count |

#### Example: Create an API Key
```bash
curl -X POST http://localhost:8080/api/keys \
  -H "Content-Type: application/json" \
  -d '{"label": "My App Key", "requestsPerMinute": 60, "enabled": true}'
```
The API auto-generates a key prefixed with `ak-` if no key value is provided.

### Scheduled Reports

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/reports` | Create a scheduled report |
| GET | `/api/reports` | List all scheduled reports |
| GET | `/api/reports/{id}` | Get report by ID |
| DELETE | `/api/reports/{id}` | Delete a report |
| PATCH | `/api/reports/{id}/toggle` | Enable/disable a report |
| GET | `/api/reports/count` | Get total report count |

#### Example: Create a Daily Report
```bash
curl -X POST http://localhost:8080/api/reports \
  -H "Content-Type: application/json" \
  -d '{"name": "Daily INR Update", "cronExpression": "0 8 * * * ?", "currencyPairs": [{"from": "USD", "to": "INR"}, {"from": "EUR", "to": "INR"}], "email": "user@example.com", "enabled": true}'
```
Reports are generated at the specified cron schedule and emailed to the configured recipient.

### Conversion Bookmarks

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/bookmarks` | Create a conversion bookmark |
| GET | `/api/bookmarks` | List all bookmarks |
| GET | `/api/bookmarks/{id}` | Get bookmark by ID |
| GET | `/api/bookmarks/by-pair?from=&to=` | Find bookmarks by currency pair |
| GET | `/api/bookmarks/by-name?name=` | Find bookmarks by name |
| PUT | `/api/bookmarks/{id}` | Update a bookmark |
| DELETE | `/api/bookmarks/{id}` | Delete a bookmark |
| POST | `/api/bookmarks/{id}/execute` | Execute a bookmark conversion |
| GET | `/api/bookmarks/count` | Get total bookmark count |

#### Example: Create and Execute a Bookmark
```bash
# Create
curl -X POST http://localhost:8080/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{"name": "USD to INR Check", "fromCurrency": "USD", "toCurrency": "INR", "amount": 100}'

# Execute
curl -X POST http://localhost:8080/api/bookmarks/1/execute
```

### Conversion Notes

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/notes` | Create a note for a currency pair |
| GET | `/api/notes` | List all notes |
| GET | `/api/notes/{id}` | Get note by ID |
| GET | `/api/notes/by-pair?from=&to=` | Find notes by currency pair |
| PUT | `/api/notes/{id}` | Update a note |
| DELETE | `/api/notes/{id}` | Delete a note |
| GET | `/api/notes/count` | Get total note count |

#### Example: Add a Note
```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "INR", "noteText": "Check this rate before the trip"}'
```

### Currency Calculator

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/calculator` | Perform a conversion calculation |
| GET | `/api/calculator` | List all calculation history |
| GET | `/api/calculator/{id}` | Get calculation by ID |
| GET | `/api/calculator/by-pair?from=&to=` | Find calculations by currency pair |
| GET | `/api/calculator/favorites` | Get favorited calculations |
| PATCH | `/api/calculator/{id}/favorite` | Toggle favorite on a calculation |
| POST | `/api/calculator/{id}/reverse` | Reverse a conversion (swap from/to) |
| POST | `/api/calculator/{id}/recalculate` | Recalculate with current rates |
| DELETE | `/api/calculator/{id}` | Delete a calculation entry |
| DELETE | `/api/calculator` | Clear all calculation history |
| GET | `/api/calculator/count` | Get total calculation count |

#### Example: Perform a Calculation
```bash
curl -X POST http://localhost:8080/api/calculator \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency": "USD", "toCurrency": "INR", "amount": 100}'
```

#### Example: Toggle Favorite and Reverse
```bash
# Toggle favorite
curl -X PATCH http://localhost:8080/api/calculator/1/favorite

# Reverse a conversion
curl -X POST http://localhost:8080/api/calculator/1/reverse
```

### Calculator Summary

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/calculator/summary` | Get aggregated calculator statistics |
| GET | `/api/calculator/summary/pairs` | Get currency pair frequency map |
| GET | `/api/calculator/summary/providers` | Get provider usage frequency map |

#### Example: Get Calculator Summary
```bash
curl http://localhost:8080/api/calculator/summary
```
```json
{
  "totalConversions": 15,
  "favoriteCount": 3,
  "totalAmountConverted": "2500.00",
  "averageRate": "83.7500",
  "mostUsedPair": "USD/INR",
  "mostUsedProvider": "EXCHANGE_RATE_API",
  "pairFrequency": { "USD/INR": 8, "EUR/GBP": 4, "GBP/USD": 3 },
  "providerFrequency": { "EXCHANGE_RATE_API": 10, "OPEN_EXCHANGE_RATES": 5 },
  "uniqueCurrencies": ["EUR", "GBP", "INR", "USD"],
  "generatedAt": "2026-07-23T10:30:00Z"
}
```

### Conversion History

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/history` | Record a conversion to history |
| GET | `/api/history?page=&size=` | Get paginated conversion history |
| GET | `/api/history/{id}` | Get history entry by ID |
| GET | `/api/history/by-pair?from=&to=` | Get history by currency pair |
| GET | `/api/history/by-status?status=` | Get history by status |
| GET | `/api/history/count?from=&to=` | Get total or pair-specific count |
| GET | `/api/history/statistics` | Get conversion statistics |
| GET | `/api/history/recent-activity?hours=` | Get recent activity summary |
| DELETE | `/api/history/{id}` | Delete a history entry |
| DELETE | `/api/history` | Clear all history |

#### Example: Get Conversion Statistics
```bash
curl http://localhost:8080/api/history/statistics
```
```json
{
  "totalConversions": 42,
  "averageRate": "83.750000",
  "totalVolume": "12500.00",
  "successRate": "95.2400",
  "conversionsByPair": { "USD/INR": 25, "EUR/GBP": 10, "GBP/USD": 7 }
}
```

#### Example: Get Recent Activity
```bash
curl http://localhost:8080/api/history/recent-activity?hours=24
```
```json
{
  "periodHours": 24,
  "totalConversions": 15,
  "successCount": 14,
  "failureCount": 1,
  "totalVolume": "3200.00"
}
```

### Alert History

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/alerts/history?page=&size=` | Get paginated alert trigger history |
| GET | `/api/alerts/history/{id}` | Get alert history entry by ID |
| GET | `/api/alerts/history/by-alert?alertId=` | Get history entries for an alert |
| GET | `/api/alerts/history/by-pair?from=&to=` | Get history entries by currency pair |
| GET | `/api/alerts/history/count?alertId=` | Get total or alert-specific trigger count |
| GET | `/api/alerts/history/stats` | Get aggregated alert history statistics |
| GET | `/api/alerts/history/recent?hours=` | Get triggers from the last N hours |
| GET | `/api/alerts/history/top-alerts?limit=` | Get alerts ranked by trigger count |
| DELETE | `/api/alerts/history/{id}` | Delete a history entry |
| DELETE | `/api/alerts/history` | Clear all alert history |

#### Example: Get Alert History Statistics
```bash
curl http://localhost:8080/api/alerts/history/stats
```
```json
{
  "totalTriggers": 42,
  "uniqueAlerts": 7,
  "uniqueCurrencyPairs": 4,
  "topPairs": [
    { "key": "USD/INR", "value": 25 },
    { "key": "EUR/GBP", "value": 10 }
  ],
  "triggersLast24h": 5,
  "triggersLast7d": 30,
  "emailSentCount": 42,
  "whatsappSentCount": 30,
  "webhookSentCount": 42,
  "generatedAt": "2026-08-04T10:30:00Z"
}
```

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
| `/api/portfolio` | 20 per minute |
| `/api/portfolio/*/value` | 10 per minute |
| All others | 100 per minute |

### Bypassed Endpoints
- `/api/health`
- `/api/version`
- `/api/rate-limit`
- `/api/portfolio/count`

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

## Portfolio Tracking

Create and manage multi-currency portfolios with real-time valuation. Add holdings in different currencies and get the total value converted to your base currency.

### Example: Create Portfolio and Add Holdings
```bash
# Create a portfolio
curl -X POST http://localhost:8080/api/portfolio \
  -H "Content-Type: application/json" \
  -d '{"name": "Travel Fund", "baseCurrency": "USD", "holdings": {"EUR": 500, "GBP": 300}}'

# Add more holdings
curl -X POST http://localhost:8080/api/portfolio/1/holdings \
  -H "Content-Type: application/json" \
  -d '{"currency": "JPY", "amount": 50000}'

# Get real-time valuation
curl http://localhost:8080/api/portfolio/1/value
```
```json
{
  "portfolioId": "1",
  "portfolioName": "Travel Fund",
  "baseCurrency": "USD",
  "totalValue": "1450.7500",
  "status": "SUCCESS",
  "holdingValues": {
    "EUR": { "amount": 500, "rate": 1.1, "convertedValue": 550, "status": "SUCCESS" },
    "GBP": { "amount": 300, "rate": 1.27, "convertedValue": 381, "status": "SUCCESS" },
    "JPY": { "amount": 50000, "rate": 0.0065, "convertedValue": 519.75, "status": "SUCCESS" }
  }
}
```

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```
