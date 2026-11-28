# File Open dialog workflow

## Goal
Find and open transformations and jobs stored on the local file system with the File -> Open dialog (file-open-save-new plugin).

## Preconditions
- Spoon is launched successfully in the test environment for the target OS.
- No transformations or jobs are open.
- No repository connection is required.
- The PDI installation contains the bundled samples:
  - `samples/transformations/Generate Row - basics.ktr` (transformation "Generate Row - basics")
  - `samples/jobs/parallel synchronized jobs/SyncJobs Example1.kjb` (job "SyncJobs Example1"; core entries only)
- Samples must only use plugins that are enabled by default. Some shipped plugins are disabled by a `.kettle-ignore` file in their plugin folder (for security), e.g. the JavaScript `EVAL` job entry (`plugins/eval-job-entry`). Opening a file that uses one shows a "Missing Plugins" dialog.

## Steps
### Open a file by entering its path
1. Select File -> Open...
2. Type the full path of the sample file in the location box above the file list and press Enter.
3. Click Open.

### Open a file from the folder's file list
1. Select File -> Open...
2. Type the full path of the `samples/transformations` folder in the location box and press Enter.
3. Double-click `Generate Row - basics.ktr` in the file list.

### Cancel the dialog
1. Select File -> Open...
2. Type the full path of a sample file in the location box and press Enter.
3. Click Cancel.

## Expected results
### Open a file by entering its path
- The file is selected in the file list and Open becomes enabled.
- The dialog closes and the file opens as the active transformation or job.

### Open a file from the folder's file list
- The file list shows the folder's contents, including `Generate Row - basics.ktr`.
- The dialog closes and "Generate Row - basics" opens as the active transformation.

### Cancel the dialog
- The dialog closes and no transformation or job is opened.

## Platform notes
- Same on Windows, macOS, and Linux. Paths use the platform's separator.
- The dialog is an SWT dialog from `plugins/file-open-save-new`, not the operating system's file chooser.

## Tags
- @smoke
- @ui
- @local
- @no-repository
- @file-open-save
