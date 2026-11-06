# SWTBot Test Infrastructure Plan for the Kettle Desktop Client

## Decision summary

This plan proposes a desktop UI automation layer for the Pentaho Data Integration client that fits the existing Maven structure of the Kettle repo and supports cross-platform execution on Windows, macOS, and Linux.

Critical launch rule: SWTBot and Spoon must run in the same JVM. SWTBot finds widgets through the SWT `Display` in its own process, so it cannot attach to a Spoon started separately via `spoon.sh`. The harness entry point (`SpoonSwtBotRunner`) runs `Spoon.main` on the JVM main thread, which owns the Display (with `-XstartOnFirstThread` on macOS), and runs the JUnit/SWTBot scenarios on a worker thread. Spoon is not an Eclipse workbench, so tests use plain `SWTBot`/`SWTBotShell` rather than `SWTWorkbenchBot`.

The core approach is:

1. Human-authored workflow descriptions live in markdown.
2. Those descriptions are converted into Gherkin feature files.
3. A generation step turns the Gherkin into SWTBot Java test cases and reusable page-object or widget helpers.
4. The generated tests are executed in a matrix CI or local environment that can target the SWT desktop shell in a stable, cross-platform way.
5. The feature layer remains the source of truth for business workflow intent, and the generated Java layer is maintained through code generation and review.

This preserves readability for product and QA stakeholders while still producing executable UI automation for the SWT desktop client.

---

## Repo fit and current state

The repo is a Maven multi-module Java project, and the SWT desktop client is concentrated under the `ui` module. The project already uses JUnit-based test infrastructure in the Maven build, but there is no existing SWTBot harness or Gherkin-based UI automation layer.

This means the most natural integration point is:

- `ui/` for the SWT client code and the UI test module
- `ui/src/test/java/` for generated and hand-written SWTBot tests
- `ui/src/test/resources/features/` for stored Gherkin feature files
- `docs/` or a dedicated QA folder for authored markdown workflow specs

This keeps the test infrastructure near the actual UI code, while still separating the workflow intent from the generated automation code.

---

## Goals

### Primary goals

- Add workflow-driven UI automation for the Kettle desktop client using SWTBot.
- Allow QA, product, or engineering to describe a workflow in markdown, then have one or more agents turn it into executable scenarios.
- Use Gherkin as the intermediate representation between human-readable workflow specs and SWTBot Java implementations.
- Maintain compatibility with Windows, macOS, and Linux desktop environments.
- Prefer headless CI execution where feasible, without breaking native desktop behavior.

### Non-goals for phase 1

- Full end-to-end replacement of all existing manual desktop validation.
- Building a large abstraction layer for every widget in the UI before proving the workflow model.
- Repository-backed desktop scenarios in the initial pass, including requiring a live repository or repository-connected client setup for the first SWTBot smoke tests.
- Treating generated code as unreviewed output; generation must be reviewable and versioned.

---

## Proposed architecture

### 1. Workflow specs in markdown

Workflow descriptions are authored in markdown and stored in a dedicated folder, for example:

- `docs/qa/workflows/`
- `ui/src/test/resources/workflows/`

Each markdown file should describe:

- feature or story name
- user goal
- preconditions
- steps and expected outcomes
- platform notes or constraints
- any environment or data needs

Example structure:

```markdown
# Create transformation workflow

## Goal
Create a new transformation and save it in a repository.

## Preconditions
- User is signed in
- Repository connection is available

## Steps
1. Open the Spoon home page
2. Click New Transformation
3. Add a step
4. Save the transformation

## Expected results
- Transformation opens with the expected step list
- Save succeeds without errors
```

These files become the human-readable source of truth for the workflow intent.

### 2. Gherkin as the intermediate layer

A generation step converts markdown workflows into Gherkin feature files such as:

```gherkin
Feature: Create transformation
  Scenario: User creates a new transformation and saves it
    Given the user is signed in
    And a repository connection is available
    When the user clicks New Transformation
    And adds a step
    And saves the transformation
    Then the transformation is created successfully
```

Using Gherkin provides a clean middle layer because it is:

- easy to review in PRs
- suitable for QA collaboration
- convertible to automation and test management tooling
- consistent with future syncs to PractiTest or similar systems

### 3. Agent-based generation to SWTBot Java

One or more agents take the `.feature` files and generate or update Java test classes that use SWTBot. The generated layer should include:

- feature-scoped test classes
- reusable page-object or widget abstraction helpers
- stable selectors using names, labels, and test IDs where available
- environment-specific setup and teardown hooks

A recommended pattern is:

- `Feature` -> `Scenario` -> `SWTBot` Java test method
- shared UI actions in helper classes
- generated code verified by a dedicated review gate before merge

### 4. PractiTest sync layer

The Gherkin and workflow metadata can be mapped into external test management formats without forcing the execution layer to depend on that system. The recommended approach is:

- keep Gherkin as the canonical automation source
- export metadata such as scenario name, objective, tags, and acceptance criteria
- push only the normalized scenario metadata to PractiTest later

This avoids coupling the runtime UI automation to an external test management tool while still enabling downstream synchronization.

---

## Recommended repo layout

A practical layout for Kettle would be:

```text
pentaho-kettle/
  docs/
    qa/
      workflows/
      swtbot-test-infrastructure-plan.md
  ui/
    src/
      test/
        java/
          org/pentaho/di/ui/swtbot/
            steps/
            pages/
            features/
            support/
        resources/
          features/
          workflows/
```

This keeps generated Java code inside the existing `ui` module and keeps feature metadata close to the app.

---

## Execution model

### Same-JVM runner (implemented proof of concept)

- `ui/src/swtbot/java/org/pentaho/di/ui/swtbot/` holds the harness. It is outside `src/test` so the normal `ui` build never compiles SWTBot code.
  - `SpoonSwtBotRunner`: starts the JUnit scenarios on a daemon worker thread, then calls `Spoon.main` on the main thread. The worker writes `ui/target/swtbot-reports/status.txt` (`PASSED`/`FAILED` plus failure traces) and calls `System.exit` with the JUnit result. `Spoon.main` always exits 0, so a missing status file means Spoon exited before the scenarios finished, which is a failure.
  - `SpoonUi`: waits until `Spoon.getInstance()` has a visible main shell.
  - `SwtBotReportListener`: writes Surefire-compatible `TEST-<class>.xml` reports to `ui/target/swtbot-reports/` and saves a full-desktop screenshot to `ui/target/swtbot-reports/screenshots/` on each failure. The screenshot is linked from the test case's `system-out` as a Jenkins JUnit attachment marker.
  - `SpoonNewTransformationUiTest`: drives File > New > Transformation through the shell menu and asserts that a new active transformation is loaded.
- `ui/scripts/run-swtbot-tests.sh [test classes...]` does the following:
  - Bootstraps SWTBot 4.2.0 if needed.
  - Compiles the harness with `javac` against an extracted PDI distribution (`PDI_INSTALL_DIR`, default `~/pentaho/builds/pdi-ce-11.1.0.0-SNAPSHOT`).
  - Launches one JVM from `data-integration/` using the same options as `spoon.sh`: the platform `libswt/<os>/swt.jar` first on the classpath, `--add-opens`, and `-XstartOnFirstThread` on macOS.
  - It does not run through Maven surefire, because the `ui` module's test classpath carries the Linux GTK SWT jar and Spoon needs the distribution's plugins and `ui/` resources.
- The script unsets `JAVA_TOOL_OPTIONS`/`JDK_JAVA_OPTIONS`. `-XstartOnFirstThread` is a launcher-only flag and is rejected when injected through those variables.
- Windows needs a PowerShell equivalent of the script (`libswt/win64`, no `-XstartOnFirstThread`). Linux CI wraps the script in `xvfb-run`.

### CI and local execution

The build should support:

- `bash ui/scripts/run-swtbot-tests.sh` (local and CI)
- a later Maven/exec wrapper that invokes the same runner, if needed
- a separate classification for smoke vs regression scenarios

Recommended tags:

- `@smoke`
- `@regression`
- `@platform-windows`
- `@platform-mac`
- `@platform-linux`
- `@headless`

This lets CI run a minimal smoke set on every platform and keep broader suites in nightly or targeted runs.

### Headless execution strategy

Headless execution is ideal, but SWT desktop apps are often tightly coupled to the display system. The plan should assume the following:

- Linux: prefer `Xvfb` or equivalent virtual display for CI. This is usually the most reliable headless route.
- macOS: use native desktop support when possible; full headless execution is less predictable for SWT and may require a virtual display or special environment support.
- Windows: prefer native desktop environment; run in CI with a Windows VM or runner that supports UI automation.

In practice, the test infrastructure should support a layered approach:

1. headless execution when the platform allows it
2. native desktop execution on CI runners when headless is not supported
3. local developer runs in a visible desktop session

The goal is not to force all three OSes into a single exact execution profile; the goal is to support them reliably with a clear fallback matrix.

---

## Implementation phases

### Phase 0: Proof-of-concept

- Create one workflow markdown file for a simple, stable UI action
- Convert it to a Gherkin feature
- Generate a minimal SWTBot Java test class
- Run it locally on at least one OS
- Validate selector stability and test runtime

This establishes the pipeline before expanding to more scenarios.

### Phase 1: Core harness

- Add SWTBot Maven dependencies and standard test support code
- Create a base test harness that starts the desktop app in a consistent way
- Add reusable wait, assertion, and navigation helpers
- Add CI profiles for platform selection and smoke/regression splits

### Phase 2: Workflow-to-feature generation

- Define markdown templates and conventions
- Add agent workflow that converts markdown into Gherkin
- Review and validate generated `.feature` files in repo
- Add a path for humans to correct generated scenarios before Java generation

### Phase 3: Feature-to-SWTBot generation

- Generate Java test classes from Gherkin
- Keep page-object or widget helper classes separate from generated scenario code
- Add review gate so generated code is human-checked before merge

### Phase 4: Cross-platform execution and maintenance

- Validate on Windows, macOS, and Linux runners
- Add CI matrix and platform-specific environment notes
- Add maintenance guidelines for flaky selectors and screen-state resets
- Add regression tracking for the generated tests

---

## Engineering guardrails

These are important for this repo and should be treated as non-negotiable:

- Prefer stable UI selectors over fragile coordinate-based interaction.
- Keep the markdown scenario as the business-facing definition.
- Keep the generated Java code reviewable and not fully opaque.
- Do not generate tests directly from a visual click stream without a scenario model.
- Maintain a clean separation between domain workflow definitions and SWTBot implementation details.
- Treat platform quirks as first-class test configuration, not as a reason to skip coverage.

---

## Risks and mitigations

### Risk: SWTBot is flaky in desktop UI automation
Mitigation: use stable labels, test IDs, and UI-specific waits; avoid brittle timing assumptions.

### Risk: cross-platform differences result in selector drift
Mitigation: encode OS-specific selectors or behaviors in a small compatibility layer, not across every test.

### Risk: product code does not expose stable hooks for automation
Mitigation: allow adding non-user-visible test attributes or identifiers to SWT components when necessary to make the UI reliably discoverable by automation. This should be treated as a controlled testability feature, not as a general UI shortcut: the attribute must be hidden from end users, kept stable, and documented as part of the automation contract.

### Risk: generated tests drift away from the intended workflow
Mitigation: keep markdown + Gherkin as the input source of truth and require review before merge.

### Risk: headless desktop execution is not possible everywhere
Mitigation: support a platform matrix with headless-first where supported and native desktop fallback where not.

---

## Suggested first milestones

1. Decide the initial workflow target: a simple local desktop workflow that does not require a repository, such as creating a new transformation or launching a basic transformation editor flow without repository connectivity.
2. Add the SWTBot and Gherkin foundation under the `ui` module.
3. Create a proof-of-concept markdown-to-Gherkin-to-code pipeline.
4. Run the proof-of-concept in CI on at least one platform.
5. Expand to a small matrix of smoke scenarios, and only then add repository-backed scenarios once the harness is stable.

This is the right scope for the first milestone because it proves the end-to-end automation stack without overcommitting to a huge UI automation effort before the repo has a stable harness.

---

## Recommended next step

The next step is to pick the first target workflow and lock the repo-level structure for the `ui` module, then create the base SWTBot harness and the markdown-to-Gherkin generation pattern. After that, we can expand to more workflows and add CI matrix coverage.

If desired, the next iteration can turn this plan into:

- a concrete directory proposal
- a starter Maven dependency set for SWTBot
- a sample markdown workflow and corresponding Gherkin template
- a starter agent prompt for generating the Java test classes
