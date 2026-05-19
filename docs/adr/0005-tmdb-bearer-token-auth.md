# ADR 0005: TMDB Bearer Token Network Auth

## Status

Accepted

## Context

TMDB v3 requests can be authorized with a bearer access token in the `Authorization` header. Passing secrets as query parameters is noisier, easier to log accidentally, and does not match the documented curl flow used for this project.

## Decision

Read `TMDB_ACCESS_TOKEN` from `local.properties` and expose it to `:core:network` through generated `BuildConfig`. Add the token to requests with an OkHttp interceptor:

```text
Authorization: Bearer <TMDB_ACCESS_TOKEN>
```

Do not pass `api_key` query parameters from Retrofit service methods.

## Consequences

- Auth behavior is centralized in the network layer.
- Retrofit API declarations stay focused on endpoints and path parameters.
- CI can provide the token through a GitHub Actions secret named `TMDB_ACCESS_TOKEN`.
- The access token is still a build-time secret, so release-grade apps should use a backend or stronger secret-management strategy.

## Follow-Ups

- Add release signing and secret-management documentation if this project is prepared for distribution.
- Consider an authenticated backend proxy for production use cases where API tokens must never ship in the app binary.

