# ADR 0003: Hilt Multibindings For Feature Registration

## Status

Accepted

## Context

The shell needs to discover feature screens without hard-coding construction logic. Each feature implementation should be able to contribute its screen factory independently.

## Decision

Use Hilt multibindings to register feature screen factories:

- Each feature module provides a `FeatureScreenFactory` implementation.
- Each factory is bound into a `Map<FeatureTab, FeatureScreenFactory>`.
- `MainActivity` receives the map through Hilt injection and resolves the current tab or detail route from it.

## Consequences

- Feature registration is declarative and close to the feature implementation.
- The shell does not construct feature screens directly.
- Adding a new tab is mostly additive: add a new `FeatureTab`, feature factory, and Hilt binding.
- Missing bindings can be handled gracefully by the shell with a fallback message.

## Follow-Ups

- Add a UI or integration test that verifies every `FeatureTab` has a registered factory in the application graph.
- Consider typed navigation if the app grows beyond tab/detail flows.

