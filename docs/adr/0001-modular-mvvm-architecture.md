# ADR 0001: Modular MVVM Architecture

## Status

Accepted

## Context

Portfolio Movies needs to keep feature code isolated enough to evolve independently while still being small enough for contributors to understand quickly. The app has a shell, shared core services, and three user-facing feature areas: Movies, TV, and Favorites.

## Decision

Use a multi-module MVVM structure:

- `:app` owns the application entry point and final APK packaging.
- `:app-ui` owns `MainActivity`, tab navigation, and feature routing.
- `:feature:*:impl` modules own feature UI, ViewModels, and feature-specific mapping logic.
- `:core:*` modules own shared models, networking, persistence, design-system contracts, and testing utilities.

ViewModels expose UI state to Compose screens and coordinate calls to Retrofit-backed network services and Room-backed persistence.

## Consequences

- Feature changes have a smaller blast radius.
- Shared code has explicit ownership in `core` modules.
- Hilt can still aggregate the full dependency graph through the final app module.
- There is more Gradle wiring than in a single-module app, but the boundary clarity is worth it for a showcase project.

## Follow-Ups

- Add architecture boundary checks to prevent accidental feature-to-feature dependencies.
- Consider Gradle convention plugins if module count or build-script duplication grows.

