# New transformation workflow

## Goal
Create a new local transformation from the Spoon desktop UI without needing a repository connection.

## Preconditions
- Spoon is launched successfully in the test environment for the target OS.
- The application is running without repository connectivity requirements.
- The default desktop shell is available and can accept menu interaction.

## Steps
1. Launch Spoon in the automated test environment.
2. Select File -> New -> Transformation from the application menu.
3. Wait for the new transformation editor tab to open.

## Expected results
- A new transformation tab opens successfully.
- The transformation editor is visible and ready for editing.
- No repository connection error is shown.

## Platform notes
- This flow should run on Windows, macOS, and Linux.
- This is the first smoke scenario and should remain repository-free.

## Tags
- @smoke
- @ui
- @local
- @no-repository
