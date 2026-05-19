# ADR 0004: Room Favorites Persistence

## Status

Accepted

## Context

Favorites should survive app restarts and should be queryable from multiple features. The data model is small, local-first, and does not require server synchronization.

## Decision

Use Room for local favorites persistence:

- `FavoritesDao` owns database reads and writes.
- `FavoritesRepository` maps database entities into shared domain models.
- Feature ViewModels interact with the repository instead of direct DAO access.

## Consequences

- Favorites are available offline.
- Repository tests can validate mapping and favorite/unfavorite behavior with fakes.
- Room gives compile-time SQL validation and observable query support.
- There is currently no remote sync or conflict resolution because favorites are device-local.

## Follow-Ups

- Add database migration tests before shipping schema changes.
- Consider caching popular movie/TV lists in Room if offline list browsing becomes a goal.

