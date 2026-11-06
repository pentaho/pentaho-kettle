# Spoon UI tests (SWTBot)

End-to-end UI tests for the Spoon desktop client. They start the real Spoon from a built PDI distribution and drive it with [SWTBot](https://www.eclipse.org/swtbot/) in the same JVM.

These tests are not part of the normal Maven build. Run them with the script below, or with the `swtbot` Maven profile.

## Layout

| What | Where |
|------|-------|
| QA workflows (source of truth) | [docs/qa/workflows/](../../../docs/qa/workflows) |
| Gherkin features | [ui/src/test/resources/features/](../test/resources/features) |
| SWTBot tests and helpers | [ui/src/swtbot/java/org/pentaho/di/ui/swtbot/](java/org/pentaho/di/ui/swtbot) |
| Runner script | [ui/scripts/run-swtbot-tests.sh](../../scripts/run-swtbot-tests.sh) |
| Linux container | [ui/scripts/swtbot-linux.Dockerfile](../../scripts/swtbot-linux.Dockerfile) |
| SWTBot install script | [ui/scripts/bootstrap-swtbot-repository.sh](../../scripts/bootstrap-swtbot-repository.sh) |

Each test class implements one `.feature` file, which implements one workflow with the same name.

| Test class | Feature |
|------------|---------|
| `SpoonNewTransformationUiTest` | `new-transformation-basic.feature` |
| `SpoonEditorTabsUiTest` | `editor-tabs.feature` |
| `SpoonFileOpenDialogUiTest` | `file-open-dialog.feature` |
| `SpoonRunSampleTransformationUiTest` | `run-sample-transformation.feature` |
| `SpoonKettleHomeUiTest` | `kettle-home.feature` |

## Prerequisites

- JDK 21 (`JAVA_HOME` or `java`/`javac` on the `PATH`), Maven, `unzip`, `bash`.
- A built desktop client: `assemblies/client/target/pdi-ce-*.zip` (see "Building it" in the [repository README](../../../README.md)).
- SWTBot in the local Maven repository as `org.eclipse.swtbot:org.eclipse.swtbot.swt.finder:4.2.0`. If it is missing, the script runs `bootstrap-swtbot-repository.sh`, which downloads it from eclipse.org and installs it. JUnit and Hamcrest are fetched with Maven.
- macOS: an unlocked desktop session. Linux: an X display, or `xvfb-run` (used automatically when `DISPLAY` is not set). Linux SWT is x86_64 only.
- Windows is not supported yet.

Do not set `JAVA_TOOL_OPTIONS=-XstartOnFirstThread`. The script adds that option on macOS itself; in `JAVA_TOOL_OPTIONS` it breaks every JVM, including Maven.

## Running

From the repository root:

```bash
# All tests
bash ui/scripts/run-swtbot-tests.sh

# Some test classes
bash ui/scripts/run-swtbot-tests.sh org.pentaho.di.ui.swtbot.SpoonFileOpenDialogUiTest
```

With Maven, after the client zip is built:

```bash
# Build and test
mvn verify -Pswtbot

# Test the zip that is already built, without rebuilding
mvn -o -Pswtbot -pl assemblies/client exec:exec@swtbot-ui-tests

# Some test classes
mvn -o -Pswtbot -pl assemblies/client exec:exec@swtbot-ui-tests -Dswtbot.tests=org.pentaho.di.ui.swtbot.SpoonKettleHomeUiTest
```

Spoon takes about 10 seconds to start; the whole suite takes about a minute.

### Options

| Variable | Default | Purpose |
|----------|---------|---------|
| `PDI_ZIP` | `assemblies/client/target/pdi-ce-*.zip` | Client zip to test. It is extracted to `<work dir>/swtbot-pdi` and only re-extracted when the zip changes. |
| `PDI_INSTALL_DIR` | | Already-extracted distribution (the folder that contains `data-integration`). Used when `PDI_ZIP` is not set. |
| `SWTBOT_TESTS` | all classes with `@Test` | Test classes, comma- or space-separated. Command-line arguments take precedence. |
| `SWTBOT_WORK_DIR` | `ui/target` (`assemblies/client/target/swtbot` with Maven) | Where everything is written. |
| `M2_REPO` | `~/.m2/repository` | Maven repository with SWTBot, JUnit and Hamcrest. |

Product changes are only tested once they are in the zip: rebuild the changed module and then `assemblies/client`.

### Linux, headless, in Docker

The image has JDK 21, Xvfb and GTK2. It must run as `linux/amd64` (emulated on Apple silicon) because PDI ships Linux SWT for x86_64 only.

```bash
docker build --platform linux/amd64 -t pdi-swtbot-linux -f ui/scripts/swtbot-linux.Dockerfile ui/scripts

docker run --rm --platform linux/amd64 \
  -v "$PWD":"$PWD" -v "$HOME/.m2/repository":/root/.m2/repository -w "$PWD" \
  -e SWTBOT_WORK_DIR="$PWD/ui/target/swtbot-linux" \
  pdi-swtbot-linux bash ui/scripts/run-swtbot-tests.sh
```

On a Linux host, add `--user "$(id -u):$(id -g)" -e HOME=/tmp` so the files written to the repository are not owned by root.

## Results

In `<work dir>/swtbot-reports/`:

- `status.txt`: `PASSED` or `FAILED`, the counts, and every failure with its stack trace.
- `TEST-<class>.xml`: JUnit XML reports.
- `screenshots/<class>.<test>.png`: the screen when a test failed.

The script exits with 0 when all tests pass and 1 otherwise. "Spoon exited before the SWTBot scenarios finished" means Spoon crashed or closed; read the console output.

## Test environment

- Every run starts Spoon with new, empty, separate `KETTLE_HOME` and `user.home` folders in `<work dir>/swtbot-home/`. Your own `~/.kettle` and `~/.pentaho` are never read or changed.
- No repository is connected.
- All test classes share one Spoon. Tests close the files and dialogs they open.
- Plugins disabled by a `.kettle-ignore` file (for example the JavaScript job entry) are not loaded, as in a default installation. Samples that use them open with a "Missing Plugins" dialog, so tests avoid them.

## Known issues

- `SpoonKettleHomeUiTest.aNewGlobalRunConfigurationIsSavedUnderKettleHome` is `@Ignore`d. Run configurations (the metastore) are saved under `user.home/.pentaho/metastore` instead of `KETTLE_HOME` (PDI-18465, PDI-19945). Remove `@Ignore` to reproduce.

## Writing a test

Follow the `swtbot-test-authoring` skill in [.github/skills/swtbot-test-authoring](../../../.github/skills/swtbot-test-authoring/SKILL.md): workflow markdown, then Gherkin, then the SWTBot test, then a passing run, then a deliberately broken run to prove the test can fail. Its [reference](../../../.github/skills/swtbot-test-authoring/reference) folder lists the helpers in `SpoonUi`, where to find labels, and harness pitfalls.

Rules in short:

- Spoon and the tests share one JVM; never start `spoon.sh`.
- Test code runs off the UI thread; read or change Spoon state with `SpoonUi.onUiThread`.
- Wait with `SpoonUi.waitFor`, never `Thread.sleep`.
- Do not depend on the Welcome tab: it is only shown when Spoon has a browser (it is missing in the Linux container).
