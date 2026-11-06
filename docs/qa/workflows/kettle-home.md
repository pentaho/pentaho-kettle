# KETTLE_HOME settings location workflow

## Goal
Check that the settings Spoon saves for the user go under `KETTLE_HOME`, and not under the user's home folder, when `KETTLE_HOME` is set.

## Preconditions
- `KETTLE_HOME` and the user's home folder (`user.home`) are different, empty folders, and neither contains the other.
- `PENTAHO_METASTORE_FOLDER` is not set.
- Spoon is launched successfully in the test environment for the target OS.
- No repository is connected, so global (local) settings are used.

## Steps
### kettle.properties is created under KETTLE_HOME
1. Start Spoon.

### A new global database connection is saved under KETTLE_HOME
1. Create a global database connection named `swtbot-kettle-home-db` (H2, Native) and save it.

### A new global run configuration is saved under KETTLE_HOME
1. Create a global run configuration named `swtbot-kettle-home-run` (type Pentaho) and save it.

## Expected results
### kettle.properties is created under KETTLE_HOME
- `KETTLE_HOME/.kettle/kettle.properties` exists.
- The user's home folder has no `.kettle` folder.

### A new global database connection is saved under KETTLE_HOME
- A file under `KETTLE_HOME` (`.kettle/shared.xml`) mentions `swtbot-kettle-home-db`.
- No file in the user's home folder mentions it.

### A new global run configuration is saved under KETTLE_HOME
- A file under `KETTLE_HOME` mentions `swtbot-kettle-home-run`.
- No file in the user's home folder mentions it.

## Known issues
- PDI-18465, PDI-19945 (closest existing tickets): run configurations (and anything else in the local metastore) are saved under `user.home/.pentaho/metastore`, not under `KETTLE_HOME`. `MetaStoreConst.getDefaultPentahoMetaStoreLocation()` uses `user.home` unless the `PENTAHO_METASTORE_FOLDER` system property is set; it never uses `KETTLE_HOME`. Observed file: `user.home/.pentaho/metastore/pentaho/Default Run Configuration/swtbot-kettle-home-run.xml`. Workaround: start Spoon with `-DPENTAHO_METASTORE_FOLDER=<folder>`. The automated test for this scenario is `@Ignore`d until the issue is fixed; remove the `@Ignore` to reproduce.

## Platform notes
- Same on Windows, macOS, and Linux.
- The automated test creates and saves the connection and run configuration through Spoon's global managers (`DatabaseManagementInterface`, `RunConfigurationService`) instead of their dialogs, which are not automated yet. The View tree's "New..." pop-up menu cannot be driven by SWTBot (`ConstUI.displayMenu` shows the menu without attaching it to the tree).
- The test runner starts Spoon with a fresh, empty `KETTLE_HOME` and `user.home` under the work folder for every run.

## Tags
- @ui
- @local
- @no-repository
- @kettle-home
- @known-issue (run configuration scenario; PDI-18465, PDI-19945)
