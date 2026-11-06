# SWTBot harness pitfalls

## Same JVM, two threads
- SWTBot finds widgets through the SWT `Display` in its own process. Spoon and the tests MUST run in the same JVM.
  - Never launch `spoon.sh` or `Spoon.bat`, or start a separate process and try to attach to it. The error "Could not find a display" means SWTBot is not in Spoon's JVM.
- `SpoonSwtBotRunner` runs `Spoon.main` on the main thread (the UI thread) and runs the JUnit tests on a worker thread. Test code is therefore never on the UI thread.
- To read or change Spoon or SWT state, use `SpoonUi.onUiThread( spoon, result )` (a wrapper around `UIThreadRunnable.syncExec`). Calling widget or Spoon UI methods directly from the test thread throws `SWTException: Invalid thread access`.
- SWTBot widget actions (`click()`, `setText()`, `menu(...)`) already move work to the UI thread. Do not wrap them in `syncExec`.
- Use plain `SWTBot`, `SWTBotShell` and similar. Spoon is not an Eclipse workbench, so `SWTWorkbenchBot` does not work.

## `syncExec` overload ambiguity
`UIThreadRunnable.syncExec` has overloads for `Result<T>`, `ArrayResult<T>` and `VoidResult`, so an untyped lambda may not compile or may pick the wrong overload. Always declare the lambda's type first:
```java
Result<Integer> count = () -> spoon.getLoadedTransformations().length;
int n = UIThreadRunnable.syncExec( spoon.getDisplay(), count );
```

## Waiting
- Never use `Thread.sleep`. Use `SpoonUi.waitFor( "<what is expected>", () -> condition )` (30 s timeout, 250 ms poll). The expectation text becomes the "Timed out waiting for …" failure message.
- The default SWTBot widget lookup timeout is 5000 ms. Dialogs that load slowly may need a longer explicit `waitUntil`.

## One Spoon instance per run
- Every test class in a run shares one Spoon. State leaks between tests unless each test cleans up:
  - `@After` → `SpoonUi.closeAllFiles( spoon )` force-closes transformation and job tabs without save prompts.
  - Close any dialog the test opened, including when the test fails (use `try`/`finally`, or close it in `@After`).
- Assert relative changes (`count == before + 1`), not absolute values. This keeps tests independent of execution order.
  - Exception: when a `Background` declares a starting state (e.g. "no transformations or jobs are open"), set it in `@Before` and assert it. Absolute assertions are then safe.
- Generated file names depend on what is already open (`Transformation N` takes the lowest free N). Read the name from the returned `TransMeta`/`JobMeta`; do not hard-code it.
- After `closeAllFiles`, the `Welcome!` tab is still open when Spoon has a browser (macOS, Windows, Linux with WebKit), and is absent otherwise. Do not assert that the tab folder is empty or that Welcome is present.

## Native dialogs
- SWTBot only sees SWT widgets in Spoon's own shells. Native OS dialogs (`org.eclipse.swt.widgets.FileDialog`, `DirectoryDialog`, `ColorDialog`, `FontDialog`, and `MessageBox` on some platforms) cannot be found or clicked, and a test that opens one hangs until it times out.
- Before automating a menu item, follow its `menubar.xul` `command` to the dialog it opens. Do not assume: File → Open is **not** native; it is the SWT `FileOpenSaveDialog` (see selectors.md). Only if the dialog really is native, call what the command does after the dialog returns and note the substitution in the workflow and the test.

## Keyboard
- SWTBot keyboard methods (`pressShortcut`, `typeText`) post OS events through `Display.post`. On macOS that needs accessibility permission for the terminal/java, so keys can be silently lost.
- Prefer `setText()` for text, and fire the event the widget listens for to "press" a key, e.g. an `SWT.Traverse` event with `detail = SWT.TRAVERSE_RETURN` for Enter (see `FileOpenSaveDialogUi.navigateTo`).

## Unexpected dialogs
- If a wait times out right after an action, look at the failure screenshot for a dialog such as "Missing Plugins" or an error box. `SpoonUi.closeAllFiles` closes leftover dialogs so they do not break later tests, but the test must still be fixed.

## Spoon lifecycle and exit codes
- `Spoon.main` always calls `System.exit(0)`. The runner reports the real result through `ui/target/swtbot-reports/status.txt` and its own exit code.
  - If `status.txt` is missing, Spoon exited before the tests finished.
- Startup can show dialogs (repository login, tips) depending on the settings in `KETTLE_HOME/.kettle`. The script starts every run with an empty one; if a test hangs at startup, check the screenshot or the screen for a modal dialog.
- `System.exit` can interrupt Spoon while it writes `.spoonrc`, leaving it empty. This is why tests must never run against the user's real `~/.kettle` (an earlier unsandboxed run truncated one).

## Settings folders
- The script sets `KETTLE_HOME` (environment and `-DKETTLE_HOME`) and `-Duser.home` to two different, empty folders under `<work dir>/swtbot-home/`, recreated for every run, and unsets `PENTAHO_METASTORE_FOLDER`.
- Where Spoon writes:
  - `KETTLE_HOME/.kettle/`: `kettle.properties`, `shared.xml` (global database connections, via `Const.getKettleDirectory()`), `.spoonrc`.
  - `user.home/.pentaho/metastore/`: everything in the local metastore, e.g. run configurations. `MetaStoreConst.getDefaultPentahoMetaStoreLocation()` uses `user.home` unless the `PENTAHO_METASTORE_FOLDER` system property is set; it ignores `KETTLE_HOME`. This is a known issue (PDI-18465, PDI-19945; `SpoonKettleHomeUiTest`, `@known-issue`).
- A test that checks for a saved file should search both folders, so a file written to the wrong one is reported instead of timing out.

## Platform
- macOS: `-XstartOnFirstThread` must be passed to `java` on the command line. Putting it in `JAVA_TOOL_OPTIONS` fails with "Unrecognized option", which is why the script unsets that variable.
  - Never export it yourself; the script adds it. If `JAVA_TOOL_OPTIONS=-XstartOnFirstThread` is set in the shell, `mvn` itself fails ("Picked up JAVA_TOOL_OPTIONS … Unrecognized option") before the `-Pswtbot` profile or the script runs. Fix: `unset JAVA_TOOL_OPTIONS`.
- Linux CI: the script uses `xvfb-run -a` automatically when `DISPLAY` is not set.
  - Linux SWT is x86_64 only, so on Apple silicon or other arm64 hosts run in a `linux/amd64` container. `ui/scripts/swtbot-linux.Dockerfile` has the image (JDK 21, Xvfb, GTK2) and the `docker run` command. Expect about 1 minute of test time for the suite under emulation, plus the first zip extraction.
  - The image has no WebKit, so Spoon has no browser and shows **no Welcome tab**. Never assume the Welcome tab is there: count tabs relative to the state after `closeAllFiles` (see `SpoonFileOpenDialogUiTest.tabsBeforeOpening`).
  - If Docker's `apt-get update` fails with "At least one invalid signature was encountered", the Docker disk is probably full (`docker system df`).
- Windows: a PowerShell equivalent of the script is still to be written.
- Menu labels are the same on all platforms. On macOS, SWTBot still drives the Spoon shell's menu bar even though macOS draws it in the global menu bar.

## Build
- `ui/src/swtbot/java` is intentionally outside `src/test`, so the normal Maven build does not compile SWTBot code.
- The script compiles these classes with `javac` against the PDI distribution under test (by default the extracted `assemblies/client` zip). Product changes in `ui/` or plugins are not picked up until that zip is rebuilt (`mvn install` of the changed module, then `assemblies/client`).
- The `-Pswtbot` profile in `assemblies/client/pom.xml` runs the script in the `integration-test` phase against the zip that module just built, with its work folder at `assemblies/client/target/swtbot`.
- Not every shipped plugin is loaded: a `.kettle-ignore` file in a plugin folder disables it (for security). Files that use these plugins open with a "Missing Plugins" dialog. See "Opening files" in selectors.md.
