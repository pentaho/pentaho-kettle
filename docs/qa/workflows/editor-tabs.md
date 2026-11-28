# Editor tabs for multiple open files workflow

## Goal
Work with several new transformations and jobs at once: each opens in its own editor tab, and selecting a tab switches the active file.

## Preconditions
- Spoon is launched successfully in the test environment for the target OS.
- No transformations or jobs are open.
- No repository connection is required.

## Steps
### Creating several files
1. Select File -> New -> Transformation three times.
2. Select File -> New -> Job twice.

### Switching between tabs
1. Create a transformation and a job (in either order).
2. Click the tab of the file that was created first (the one that is not active).

## Expected results
### Creating several files
- Each new transformation and job gets its own editor tab.
- Each tab is labeled with the name of its file (for example "Transformation 2", "Job 1").

### Switching between tabs
- The clicked tab becomes the selected tab.
- The file shown in that tab becomes Spoon's active file. Switching works from a transformation to a job and from a job to a transformation.

## Platform notes
- Same on Windows, macOS, and Linux.

## Tags
- @smoke
- @ui
- @local
- @no-repository
