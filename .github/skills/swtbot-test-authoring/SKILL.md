---
name: swtbot-test-authoring
description: 'Add or update SWTBot UI tests for the Spoon (PDI) desktop client from markdown workflows via Gherkin. Use when asked to create a Spoon UI test, automate a desktop workflow, write an SWTBot test, convert a QA workflow or .feature into a test, or extend the SWTBot harness under ui/src/swtbot. Covers workflow markdown, Gherkin feature, SWTBot Java test, running run-swtbot-tests.sh, and proving the test can fail.'
---

# SWTBot test authoring for Spoon

Turn a desktop workflow into an executable SWTBot test using this chain:

**workflow markdown → Gherkin feature → SWTBot Java test → passing run → proven failure → revert**

The markdown workflow and the `.feature` file are the source of truth. The Java test implements them and must stay traceable to them.

## Before starting

Read these first. They are short and prevent the common mistakes:
- [reference/pitfalls.md](./reference/pitfalls.md): harness rules (same JVM, threading, shared Spoon instance).
- [reference/selectors.md](./reference/selectors.md): where to find real labels and state accessors.

Existing exemplar: `ui/src/swtbot/java/org/pentaho/di/ui/swtbot/SpoonNewTransformationUiTest.java`.

## Locations

| Artifact | Path |
|----------|------|
| Workflow markdown | `docs/qa/workflows/<slug>.md` |
| Gherkin feature | `ui/src/test/resources/features/<slug>.feature` |
| SWTBot test | `ui/src/swtbot/java/org/pentaho/di/ui/swtbot/Spoon<Scenario>UiTest.java` |
| Shared helpers | `ui/src/swtbot/java/org/pentaho/di/ui/swtbot/SpoonUi.java` |
| Runner script | `ui/scripts/run-swtbot-tests.sh` |
| User documentation | `ui/src/swtbot/README.md` (test class → feature table, run options, known issues) |
| Reports | `ui/target/swtbot-reports/` (`status.txt`, `TEST-*.xml`, `screenshots/`); under `assemblies/client/target/swtbot/` when run through Maven |

Use the same `<slug>` (kebab-case) for the workflow and the feature.

## Procedure

### 1. Workflow markdown
- If the user provides a workflow file, use it. Otherwise draft one from [templates/workflow.md](./templates/workflow.md) and confirm it with the user before continuing.
- Every expected result must be observable: something visible in the UI or readable from Spoon's state.
- Prefer repository-free workflows unless the user explicitly asks for repository scenarios.
- Workflows that use bundled samples or other files must use ones with no external dependencies (databases, network, files outside the distribution). See "Opening files" in [reference/selectors.md](./reference/selectors.md) for how to check a sample.

### 2. Gherkin feature
- Create or update the feature from [templates/scenario.feature](./templates/scenario.feature).
- Map steps one-to-one: workflow preconditions become `Given`, steps become `When`, expected results become `Then`/`And`.
- Gherkin best practices:
  - **One feature per file, one behavior per scenario.** If a workflow tests two behaviors (e.g. "tabs open" and "switching tabs"), write two scenarios in the same feature, not one long scenario.
  - **A single `When` per scenario.** If you need `When … Then … When … Then`, split it into separate scenarios.
  - **`Background`** holds the `Given` steps every scenario shares. Keep it short and in the domain's language.
  - **`Scenario Outline` + `Examples`** when the same behavior is checked with different data (e.g. transformation→job and job→transformation). Do not copy and paste near-identical scenarios.
  - **Declarative steps, not imperative ones:** "the user creates 3 transformations from the File -> New menu", not "click File, click New, click Transformation". Name UI elements only when the menu path itself is what is being tested.
  - Do not put internal details (generated names, widget classes) in steps; the test resolves those.
  - Put a short `As a / I want / So that` description under `Feature:`. Put tags shared by all scenarios on the `Feature:` and per-scenario tags (`@smoke`, `@regression`) on each scenario.
- Copy the workflow's tags (`@smoke`, `@ui`, `@local`, `@no-repository`, `@platform-*`).

### 3. Resolve selectors and assertions (never guess)
For each `When` step, find the exact UI label or widget, using [reference/selectors.md](./reference/selectors.md).
- Read the Spoon command behind the menu item and follow it to the first dialog. Note the dialog's shell title and button labels from its `messages_en_US.properties`, and check it is not a native OS dialog (see [reference/pitfalls.md](./reference/pitfalls.md)).
For each `Then` step, choose an assertion:
- First choice: Spoon state, e.g. `getActiveTransformation()`, `getLoadedTransformations()`, `getActiveJob()`.
- Second choice: widget state found by SWTBot (shell title, tab text, enabled/visible).
- If a `Then` step cannot be asserted yet, say so explicitly in the test (`// NOT ASSERTED: <reason>`) and report it to the user. Never drop a step silently.

### 4. SWTBot Java test
- Start from [templates/SpoonScenarioUiTest.java.template](./templates/SpoonScenarioUiTest.java.template).
- One test class per feature. Map the Gherkin to JUnit as follows:
  - `Background` → `@Before`. Set up the state and assert it (e.g. `closeAllFiles`, then assert 0 open files), so a dirty start fails clearly instead of producing confusing results later.
  - `Scenario` → one `@Test` method. Its Javadoc names the scenario.
  - `Scenario Outline` → one `@Test` method per `Examples` row. Each delegates to a shared private method that takes the row values (an enum works well for the row parameters; see `SpoonEditorTabsUiTest.FileType`).
- Use `// Given`, `// When` and `// Then` comments that quote the Gherkin step text, keeping `<placeholders>` for outlines.
- The class Javadoc must reference the `.feature` path.
- Use `@Before` → `SpoonUi.awaitMainShell()` and `@After` → `SpoonUi.closeAllFiles( spoon )`. Any dialog a test opens must be closed by that test.
- Check the `SpoonUi` helper inventory in [reference/selectors.md](./reference/selectors.md) before writing any UI action. Move an action into `SpoonUi` once two or more tests need it; do not duplicate it.
- When asserting on a collection, include the actual values in the message (e.g. `"expected a tab labeled 'X' but tabs were " + tabTexts`).
- File rules:
  - No copyright header (it is added automatically later).
  - Import order: `org.pentaho.*`, blank line, all other imports alphabetically, blank line, `static` imports.

### 5. Run until it passes
```bash
bash ui/scripts/run-swtbot-tests.sh org.pentaho.di.ui.swtbot.<TestClass>
```
- Run it synchronously with a generous timeout (about 3 minutes). Spoon takes about 10 seconds to start.
- With no arguments the script runs every class under `ui/src/swtbot/java` that has `@Test`. `SWTBOT_TESTS="a.B,c.D"` also selects classes.
- Spoon under test, in order of precedence:
  - `PDI_ZIP=<zip>`;
  - `PDI_INSTALL_DIR=<folder containing data-integration>`;
  - otherwise `assemblies/client/target/pdi-ce-*.zip`, extracted to `ui/target/swtbot-pdi` (and re-extracted only when the zip changes).
- Through Maven, after the client zip is built: `mvn verify -Pswtbot` from the root or `assemblies/client`. Add `-Dswtbot.tests=<classes>` for a subset. To rerun against the existing zip without rebuilding: `mvn -o -Pswtbot -pl assemblies/client exec:exec@swtbot-ui-tests`.
- Each run starts Spoon with a fresh, empty `KETTLE_HOME` and `user.home` under `<work dir>/swtbot-home/` (see "Settings folders" in [reference/pitfalls.md](./reference/pitfalls.md)). The user's real `~/.kettle` and `~/.pentaho` are never used.
- SWTBot comes from the local Maven repository (`org.eclipse.swtbot:org.eclipse.swtbot.swt.finder:4.2.0`). If it is missing, the script runs `ui/scripts/bootstrap-swtbot-repository.sh` to download and install it. JUnit and Hamcrest are fetched with `mvn dependency:get`.
- On Linux without `DISPLAY`, the script runs Spoon under `xvfb-run -a`. To check Linux from a Mac, use the container in `ui/scripts/swtbot-linux.Dockerfile` (the build and run commands are in its header).
- On failure, read `ui/target/swtbot-reports/status.txt` and view the screenshot in `ui/target/swtbot-reports/screenshots/` before changing code.
- If the result says "Spoon exited before the SWTBot scenarios finished", Spoon crashed or closed. Read the console output; this is not a test failure.

### 6. Prove the test can fail, then revert
- Break **every** scenario (each Examples row counts), not just one. Target the behavior under test:
  - Comment out the action itself (e.g. the tab click), so the `Then` must fail.
  - Or change an expected value (a wrong label, a wrong count).
- Mark each break with a `// FAIL-PROOF` comment or a `"FAIL-PROOF"` string so the revert can be checked with grep.
- Rerun only that class and confirm:
  - exit code 1
  - every scenario fails with a readable assertion message (not a timeout or NPE, unless that is what's expected)
  - one screenshot per failure
- Revert, confirm `grep -c FAIL-PROOF <test file>` prints `0`, rerun, and confirm it passes again.

### 7. Finish
- Re-open each new or changed Java file and check imports and headers, including unused imports.
- Run the full set of SWTBot tests together (no arguments) to catch state leaking from one test into another:
  ```bash
  bash ui/scripts/run-swtbot-tests.sh
  ```
- A scenario that reproduces a known product bug: keep it in the feature with a `@known-issue` tag, put `@Ignore( "Known issue <JIRA-KEY>: <summary>" )` on the test (Javadoc says how to reproduce), tag the scenario with the Jira key (e.g. `@PDI-18465`), and record it under "Known issues" in the workflow markdown. Prove it fails before ignoring it.
- Update `ui/src/swtbot/README.md`: add the test class and feature to its table, and any `@Ignore`d known issue to "Known issues".
- Report to the user:
  - files created or changed
  - the mapping from Gherkin steps to assertions, including any `NOT ASSERTED` steps
  - pass/fail evidence
- If this run taught something new (a new pitfall, a selector location, a useful helper), update this skill's reference files.
