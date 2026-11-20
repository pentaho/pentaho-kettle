# Build and run a Generate Rows transformation workflow

## Goal
Build a small transformation on the canvas from the Design tab without opening a file, then run it. It re-creates the bundled sample `samples/transformations/Generate Row - basics.ktr` by hand: a "Generate Rows" step feeds 10 rows of constant fields to a "Dummy (do nothing)" step.

## Preconditions
- Spoon is launched successfully in the test environment for the target OS.
- No transformations or jobs are open.
- The Design tab search box is empty.
- No repository connection is required.

## Steps
1. Select File -> New -> Transformation.
2. In the left panel, click the "Design" tab, expand the "Input" folder, and drag "Generate rows" onto the canvas.
3. Type `dummy` in the Design tab search box. The tree is filtered to the matching steps.
4. Drag "Dummy (do nothing)" from the search results onto the canvas, to the right of "Generate rows".
5. Hold Shift and drag from "Generate rows" to "Dummy (do nothing)" to create a hop.
6. Double-click "Generate rows" to open its dialog and set:
   - Step name: `Generate Rows`
   - Limit: `10`
   - Fields (one row each; leave other columns empty):

     | Name | Type | Format | Length | Precision | Decimal | Value |
     |------|------|--------|--------|-----------|---------|-------|
     | FieldAsNumber | Number | | | | . | 1.2345 |
     | FieldAsString | String | | 20 | | | Test |
     | FieldAsDate | Date | yyyy-MM-dd | | | | 2006-07-27 |
     | FieldAsBoolean | Boolean | | | | | True |
     | FieldAsInteger | Integer | | | | | 12345 |
     | FieldAsBigNumber | BigNumber | | 10 | 5 | . | 1.2345 |

   Click OK.
7. Select File -> Save. The transformation has never been saved, so the "Save As" dialog opens. Go to the home folder, enter `build-generate-rows.ktr` as the File name, and click Save. Spoon runs only saved transformations: running a new or changed one shows "Please save your transformation first!".
8. Select Action -> Run. In the "Run Options" dialog, keep the defaults and click Run.

## Expected results
- Step 2: the transformation has one step, "Generate rows", drawn on the canvas.
- Step 3: the Design tree lists "Dummy (do nothing)" under "Flow" and no longer lists "Generate rows".
- Step 4: the transformation has two steps, "Generate rows" and "Dummy (do nothing)", and no hops yet.
- Step 5: the transformation has one enabled hop, from "Generate rows" to "Dummy (do nothing)".
- Step 6: the step is renamed to "Generate Rows", its row limit is 10, and it has the six fields from the table, in order. The hop still connects "Generate Rows" to "Dummy (do nothing)".
- Step 7: the file exists and the transformation has no unsaved changes.
- Step 8: the transformation finishes without errors. "Generate Rows" writes 10 rows and "Dummy (do nothing)" reads 10 rows. The Execution Results "Logging" tab shows `Dummy (do nothing).0 - Finished processing (I=0, O=0, R=10, W=10, U=0, E=0)`.

## Platform notes
- Same on Windows, macOS, and Linux.
- Step names in the Design tree come from the plugin names: "Generate rows" uses a lowercase "r".
- Dropping a second step may show a "How to create a new hop?" help tip. It does not block the next step.
- Automation substitutions:
  - Drag and drop: the test calls Spoon's own drag listeners on the Design tree and its own drop listeners on the canvas, in the order SWT calls them. It skips only the operating-system drag session, which needs OS-level mouse input. On macOS that input requires the Accessibility permission.
  - Hop creation and double-click: the test sends Spoon's canvas the mouse events that a Shift-drag and a double-click produce.
  - Table cells: the test clicks a cell to open its editor, sets the value, and presses Enter. To add a row, it presses Down Arrow in the last row.
  - Save As: the test types the home folder into the location box and presses Enter, as in the Open dialog.

## Tags
- @smoke
- @ui
- @local
- @no-repository
- @canvas
