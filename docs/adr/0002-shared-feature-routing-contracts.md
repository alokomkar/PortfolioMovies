# ADR 0002: Shared Feature Routing Contracts

## Status

Accepted

## Context

The app shell needs to render feature screens without depending on feature implementation internals. Earlier empty feature API modules existed for Movies, TV, and Favorites, but they did not contain meaningful contracts.

## Decision

Keep feature routing contracts in `:core:designsystem`:

- `FeatureTab` identifies top-level tabs.
- `FeatureScreenKey` supports Hilt map keys.
- `FeatureScreenFactory` lets features expose list and detail Composables to the shell.

Remove the empty `:feature:movies:api`, `:feature:tv:api`, and `:feature:favorites:api` modules until there is a real contract worth isolating.

## Consequences

- The module graph is simpler and easier to explain.
- Empty modules no longer add build time, generated sample tests, or documentation noise.
- The shell stays decoupled from feature implementation details through `FeatureScreenFactory`.
- If a feature grows a public contract that is consumed by multiple modules, a dedicated API module can be reintroduced with actual code.

## Follow-Ups

- Add a dependency rule that `:app-ui` cannot depend on `:feature:*:impl`.
- Revisit API modules only when there is a stable cross-module contract to publish.

