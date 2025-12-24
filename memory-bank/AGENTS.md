# Repository Guidelines

## Project Structure & Module Organization
- `shared/`: Kotlin Multiplatform core (commonMain, platform sources, parsers, generators, utilities); edits here affect every target.
- `backend/`: Ktor JVM server hosting APIs, plugins, and business services; keep routes and service logic here.
- `web-app/`: Compose for Web dashboard sources under `wasmJsMain`; UI changes and client-side state live here.
- Top-level tooling: `docker-compose.yml`, `settings.gradle.kts`, and root Gradle files coordinate orchestration and module inclusion.

## Build, Test, and Development Commands
- `./gradlew clean build`: compiles shared + backend + web modules and runs the default test suites.
- `./gradlew :backend:run`: spins up the Ktor backend for local API verification; useful when validating translation flows.
- `./gradlew :web-app:browserDevelopmentRun`: launches the Compose Web UI for manual interaction during styling or workflow tweaks.
- `docker compose up --build`: boots PostgreSQL/Redis dependencies referenced in design docs; stop with `docker compose down`.

## Coding Style & Naming Conventions
- Follow idiomatic Kotlin: 4-space indentation, camelCase for functions/properties, PascalCase for classes/objects, and explicit visibility modifiers when exposing cross-module APIs.
- Shared module packages mirror feature slices (`models`, `parser`, `generator`, `util`) as shown in `tech-stack.md`.
- Prefer descriptive names over abbreviations; e.g., `TranslationMemoryRepository` instead of `TMRepo`.
- Keep formatting consistent with `ktlint`/`detekt` defaults if configured; run `./gradlew ktlintFormat` before commits when available.

## Testing Guidelines
- Tests live alongside sources (`commonTest`, `jvmTest`, `wasmJsTest`); name them `*Test` to keep Gradle discovery happy.
- Use `kotlin.test` or platform-specific expectations; assert logic in shared modules first, then verify platform adapters.
- Run `./gradlew test` after touching parser, generator, or API logic to catch regressions before pushing.

## Commit & Pull Request Guidelines
- Write commit messages in imperative tense (e.g., “Add translation memory cache”). Include scope when relevant (`backend`, `shared`).
- PRs should explain the change, list testing steps (commands run), and link related issues or design docs.
- Attach screenshots or GIFs when UI/dashboard behavior changes, and mention database/infra impacts when relevant.

## Mandatory Reading & Update Rules
**Always**.
- **Always** read `architecture.md` (including the full database structure) before writing any code.
- **Always** read `lan-sys-design-document.md` completely before writing any code.
- **Always** update `architecture.md` after delivering each major feature or milestone so documentation stays current.
- Follow the tech stack’s best practices (networking, state management, etc.) for every other guideline that is not marked **Always**.
