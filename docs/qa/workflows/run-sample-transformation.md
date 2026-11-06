# Run a bundled sample transformation workflow

## Goal
Open a sample transformation that ships with PDI and run it locally from Spoon with the default run options.

## Preconditions
- Spoon is launched successfully in the test environment for the target OS.
- No transformations or jobs are open.
- No repository connection is required.
- The sample `samples/transformations/Generate Row - basics.ktr` exists in the PDI installation. It has no external dependencies: a "Generate Rows" step (limit 10 rows) feeds a "Dummy (do nothing)" step, with no database connections and no input or output files.

## Steps
1. Select File -> Open..., enter the full path of `samples/transformations/Generate Row - basics.ktr` in the location box, press Enter, and click Open.
2. Select Action -> Run.
3. In the "Run Options" dialog, keep the defaults and click Run.

## Expected results
- The sample opens in its own tab. The label is shortened to "Generate Row - ba…" because Spoon cuts tab labels of 17 or more characters.
- The transformation finishes without errors.
- The "Generate Rows" step writes 10 rows.
- The "Dummy (do nothing)" step reads 10 rows.

## Platform notes
- Same on Windows, macOS, and Linux.
- File -> Open uses the file-open-save-new plugin's SWT dialog; see [file-open-dialog.md](file-open-dialog.md) for its own workflow.

## Tags
- @smoke
- @ui
- @local
- @no-repository
- @samples
