# AGENTS.md

## Repo state
- v1.0 implementation complete: stages 1–6 done. `model` rules + `GameEngine` and 42 JUnit tests (model + persistence); `GameScreen`, `GameLayout`, `BoardRenderer`, `GameUiRenderer`, `SevenSegmentDisplay`; HUD, difficulty selector, custom dialog, scale, full mouse/chord interaction; `Settings`/`HighScores` with `Preferences`, records dialog (`F4`) and reset confirmation; desktop JAR via `lwjgl3:dist`.
- Optional sound/dark theme are not implemented; `Settings` already stores those flags for future work.
- `spec.md` (pt-BR) is the source of truth; implement to it, and update it only if the user asks.
- `progresso.md` tracks stage status and validation history; keep it updated after each stage.

## Target stack
- Java 17+, libGDX 1.12.x, LWJGL3 desktop backend, Gradle layout from `gdx-liftoff`.
- Modules: `core` (logic + rendering) and `lwjgl3` (desktop launcher).
- Tests: JUnit 5, `core` module only; model tests are pure Java and persistence tests use a fake `Preferences`, with no LWJGL/graphics context.

## Commands
- Run: `./gradlew lwjgl3:run`
- Tests: `./gradlew test` (use `--offline` if Maven/GitHub is flaky)
- Single test: `./gradlew core:test --tests "com.exemplo.minesweeper.model.<TestClass>"`
- Distribution JAR: `./gradlew lwjgl3:dist` → `lwjgl3/build/libs/campo-minado.jar`
- If test workers fail with a JDK spawn-helper error after an in-place JDK update, run `./gradlew --stop` and retry.

## Hard constraints from the spec
- `model` package must not import `com.badlogic.gdx`; keep all rules unit-testable.
- Flood fill **must be iterative** (explicit queue/stack), never recursive.
- Mines are placed only on the first reveal click; clicked cell + 8 neighbors are safe (if too many mines, only the clicked cell).
- Timer starts on first reveal (not on flagging), accumulates the frame delta passed from `GameScreen` (originating from `Gdx.graphics.getDeltaTime()`), and stops at 999/win/loss.
- Board storage is a 1D `Cell[]` with `idx = y * cols + x`; row 0 is displayed at the top.
- Custom difficulty values are clamped: cols 9–30, rows 9–24, mines 10..((cols-1)*(rows-1)).
- Mine counter may go negative; `QUESTION` cells do not count as flags.
- Do **not** copy Microsoft sprites/sounds; use original or freely licensed assets only.
