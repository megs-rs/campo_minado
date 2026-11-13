# AGENTS.md

## Repo state
- Implementation in progress: stages 1–3 complete (`model` board rules + `GameEngine`, 31 JUnit tests). Stages 4–6 pending (rendering, HUD, persistence, distribution).
- `spec.md` (pt-BR) is the source of truth; implement to it, and update it only if the user asks.
- `progresso.md` tracks stage status and validation history; keep it updated after each stage.

## Target stack
- Java 17+, libGDX 1.12.x, LWJGL3 desktop backend, Gradle layout from `gdx-liftoff`.
- Modules: `core` (logic + rendering) and `lwjgl3` (desktop launcher).
- Tests: JUnit 5, `core` module only, no graphics dependencies.

## Commands
- Run: `./gradlew lwjgl3:run`
- Tests: `./gradlew test` (use `--offline` if Maven/GitHub is flaky)
- Single test: `./gradlew core:test --tests "com.exemplo.minesweeper.model.<TestClass>"`
- Distribution JAR: `./gradlew lwjgl3:dist` (implementado na etapa 6)

## Hard constraints from the spec
- `model` package must not import `com.badlogic.gdx`; keep all rules unit-testable.
- Flood fill **must be iterative** (explicit queue/stack), never recursive.
- Mines are placed only on the first reveal click; clicked cell + 8 neighbors are safe (if too many mines, only the clicked cell).
- Timer starts on first reveal (not on flagging), accumulates `Gdx.graphics.getDeltaTime()`, stops at 999/win/loss.
- Board storage is a 1D `Cell[]` with `idx = y * cols + x`; row 0 is displayed at the top.
- Custom difficulty values are clamped: cols 9–30, rows 9–24, mines 10..((cols-1)*(rows-1)).
- Mine counter may go negative; `QUESTION` cells do not count as flags.
- Do **not** copy Microsoft sprites/sounds; use original or freely licensed assets only.
