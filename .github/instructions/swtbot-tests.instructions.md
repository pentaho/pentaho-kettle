---
description: 'Rules for SWTBot UI test code for the Spoon desktop client.'
applyTo: 'ui/src/swtbot/**'
---

- To add a new scenario, follow the `swtbot-test-authoring` skill (workflow markdown → Gherkin → test → run → prove failure).
- Spoon and SWTBot share one JVM. Never launch `spoon.sh` or a separate process. Use plain `SWTBot`/`SWTBotShell`, never `SWTWorkbenchBot`.
- Test code runs off the UI thread. Read or change Spoon/SWT state only through `SpoonUi.onUiThread( spoon, result )`, with the lambda typed explicitly as `Result<T>`.
- No `Thread.sleep`. Wait with `SpoonUi.waitFor( "<expectation>", () -> condition )`.
- Reuse `SpoonUi` helpers (inventory in the skill's `reference/selectors.md`) before writing new UI actions.
- Gherkin → JUnit: `Background` → `@Before` (set up and assert the state); `Scenario` → one `@Test`; `Scenario Outline` → one `@Test` per `Examples` row, each delegating to a shared private method.
- All scenarios share one Spoon instance. Use `@Before` → `SpoonUi.awaitMainShell()` and `@After` → `SpoonUi.closeAllFiles( spoon )`, and close any dialog the test opens.
- Javadoc on each test class must reference its `.feature` file. `// Given/When/Then` comments must quote the Gherkin steps.
- Run with `bash ui/scripts/run-swtbot-tests.sh [fully.qualified.TestClass ...]` (no arguments runs all). It tests the `assemblies/client` zip with sandboxed `KETTLE_HOME`/`user.home`; Maven: `mvn verify -Pswtbot` in `assemblies/client`.
- New files: no copyright header. Imports: `org.pentaho.*`, blank line, others alphabetical, blank line, static imports.
