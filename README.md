# Gateway
## Overview
- The Gateway is the single entry point for all client requests to the Mazadak platform, providing routing, security, observability, and resilience capabilities.

- It handles request routing to downstream microservices, JWT validation and decoding, rate limiting using Redis, resilience patterns with Resilience4j, and attaches user context (`X-User-Id` header) to requests.

## API Endpoints
- Gateway routes available at `http://localhost:18090` when running locally
- All microservice endpoints are accessible through the gateway with path-based routing
- Actuator endpoints available for health checks and monitoring

## How to Run
You can run it via [Docker Compose](https://github.com/Mazaadak/mazadak-infrastructure) or [Kubernetes](https://github.com/Mazaadak/mazadak-k8s/)

## Tech Stack
- **Spring Boot 3.5.6** (Java 21)
- **Spring Cloud Gateway** - Routing & API Gateway
- **Resilience4j** - Circuit Breaker, Retry, Fallback
- **Redis** - Rate Limiting
- **JWT** - Authentication & Authorization
- **Netflix Eureka** - Service Discovery
- **Docker & Kubernetes** - Deployment & Containerization
- **Micrometer, OpenTelemetry, Alloy, Loki, Prometheus, Tempo, Grafana** - Observability

## For Further Information
Refer to [Gateway Wiki Page](https://github.com/Mazaadak/.github/wiki/Gateway).

