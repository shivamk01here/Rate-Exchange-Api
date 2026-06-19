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

## Build & Run

```bash
mvn clean package
mvn spring-boot:run
```
