# Order Tracking System

A "dummy" order tracking system built with Scala, ZIO, Kafka and Postgres.

## Architecture

TODO: add architecture diagram

## Roadmap

<details>
  <summary>Users</summary>

- [x] Users initial operations
  - [x] Create user
  - [x] Get user by id
  - [x] Get user by email
</details>

<details>
  <summary>Auth</summary>

- [x] Auth
  - [x] Login
  - [x] Protect routes
</details>

<details>
  <summary>Misc</summary>

- [x] Misc
  - [x] Prometheus/Grafana
  - [x] Swagger/OpenAPI
  - [x] DB migrations
</details>

- [ ] HTMX UI - Users
  - [ ] User Register
  - [ ] User Login
  - [ ] User Profile
- [ ] Testing
  - [ ] Unit tests
  - [ ] Integration tests
- [ ] Order generator service
  - [ ] Add Kafka to Docker Compose
  - [ ] Create Kafka topics
  - [ ] Order generator
  - [ ] Publish orders to Kafka
  - [ ] Custom metrics for observability (publish success rate, publish error rate)
- [ ] Order validator service
  - [ ] Consume orders from Kafka
  - [ ] Validate order
  - [ ] Publish to Kafka
  - [ ] Custom metrics for observability (consume success rate, consume error rate, validate success rate, validate error rate, publish success rate, publish error rate)
- [ ] Order ingestor service
  - [ ] Consume orders from Kafka
  - [ ] Store order in Postgres
  - [ ] Custom metrics for observability (consume success rate, consume error rate, store success rate, store error rate)
- [ ] HTMX UI - Orders
  - [ ] Order list
  - [ ] Order detail
  - [ ] Order search

