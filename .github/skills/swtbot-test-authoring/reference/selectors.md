# Finding selectors and assertions in Spoon

Never guess a label. Find it in the source, then use the text the user sees with the `&` mnemonic marker removed.

## Main menu bar
- Structure: `assemblies/static/src/main/resources/ui/menubar.xul`.
  - Each `menu`/`menuitem` has an `id`, a `label` (usually a `${Message.Key}`) and a `command` (the Spoon method that runs).
- Label text: `ui/src/main/resources/org/pentaho/di/ui/spoon/messages/messages_en_US.properties`.
  - Example: `Spoon.Menu.Transformation=&Transformation` → use `"Transformation"`.
- Usage: `new SWTBotShell( spoon.getShell() ).menu().menu( "File", "New", "Transformation" ).click();`

Known menu paths:

| Path | Spoon command | Verified |
|------|---------------|----------|
| File → New → Transformation | `spoon.newTransFile()` | yes |
| File → New → Job | `spoon.newJobFile()` | yes |
| Action → Run | `spoon.runFile()` → opens the "Run Options" dialog (see below) | yes |
| File → Open... | `spoon.openFileNew()` → `FileOpenSaveExtensionPoint` → `FileOpenSaveDialog` (SWT, drivable; see below). Not `spoon.openFile()` | yes |
| File → Save | `spoon.saveFile()` → `saveToFile`. A file never saved opens the SWT `FileOpenSaveDialog` titled `Save As`; a file with a name is written without a dialog | yes |

Add a row each time a new path is verified by a passing test. Always read the `command` attribute in `menubar.xul`; similar Spoon methods (e.g. `openFile` vs `openFileNew`) can do very different things.

## Dialogs
| Dialog | How it opens | Shell title | Controls |
|--------|--------------|-------------|----------|
| Run Options (transformation) | Action → Run, because `alwaysShowRunOptions` defaults to true | `Run Options` | Launch button text is `"  &Run"` (leading spaces): match with `allOf( widgetOfType( Button.class ), withRegex( "\\s*&?Run\\s*" ) )`, not `button( "Run" )` |
| Open (file-open-save-new) | File → Open... | `Open` | See "File Open dialog" below |
| Missing Plugins | Opening a file that uses a plugin that is not loaded (e.g. disabled by `.kettle-ignore`) | `Missing Plugins` | `Open the File`, `Close` |
| Save As (file-open-save-new) | File → Save on a file never saved | `Save As` | See "File Open dialog" below; adds a `File name:` box and a `Save` button |
| File has changed! | Action → Run on a new or changed transformation (`TransGraph.start`) | `File has changed!` | Message "Please save your transformation first!", `OK`. Save first; see pitfalls.md |
| Generate rows (`RowGeneratorDialog`) | Double-click the step on the canvas | `Generate rows` | `Step name ` and `Limit ` labels (trailing space) followed by their boxes; fields `TableView`; OK button `"  &OK  "` (padded) |

Keys: `TransExecutionConfigurationDialog.Shell.Title` / `.Button.Launch` in `ui/src/main/resources/org/pentaho/di/ui/trans/dialog/messages/messages_en_US.properties`.

## File Open dialog (`FileOpenSaveDialog`)
- Source: `plugins/file-open-save-new/core/src/main/java/org/pentaho/di/plugins/fileopensave/dialog/FileOpenSaveDialog.java`. Text: `.../fileopensave/dialog/messages/messages_en_US.properties`. It is SWT, not a native chooser. Drive it with `FileOpenSaveDialogUi`.
- Widgets:
  - Location box `txtNav`: the only `Text` with `SWT.BORDER` (the search box `txtSearch` has no border). Enter a full file path and press Enter: the parent folder is selected in the tree and the file in the list.
  - Enter is handled by a `TraverseListener` (`TRAVERSE_RETURN`). Fire an `SWT.Traverse` event (see `FileOpenSaveDialogUi.navigateTo`); SWTBot's `pressShortcut` needs macOS accessibility permission.
  - File list `fileTableViewer`: the only `Table`. Column 0 is the Name **including the extension** (e.g. `Generate Row - basics.ktr`), then Type and Modified.
  - Double-clicking a file with a valid extension opens it and closes the dialog. Double-clicking a folder goes into it.
  - `Open` button: disabled until a file is selected. `Cancel` closes without opening.
  - Not yet automated: folder tree, search, back/forward/up, new folder, delete, refresh, file-type combo, context menu (copy/paste/cut/delete/rename).
- Save As mode (`FileDialogOperation.SAVE_AS`, title `Save As`):
  - The `File name:` box is a bordered `Text` created just **before** its label, so `textWithLabel` cannot find it. `FileOpenSaveDialogUi` takes the `Text` sibling just before the label, and the navigation box is the other bordered `Text`.
  - Navigate to a folder (location box + Enter), set the file name, and click `Save`. Save uses the file-list selection, or the tree selection when the list has none; it is enabled once a folder is selected and the name is not empty.

## Design tab (steps tree)
- Left panel `CTabItem` `Design`. Its control holds one `Text` (the search box, in `TreeToolbar`) and the `coreObjectsTree` `Tree`. Use `SpoonDesignTabUi`.
- Folders are step categories (`Input`, `Flow`, ...). Items are plugin names: `Generate rows` (lowercase r) under `Input`, `Dummy (do nothing)` under `Flow`.
- Typing in the search box rebuilds the tree with the matching steps and expands it. A step can appear in more than one folder. `spoon.clearSearchFilter()` clears it; do it in `@After`.

## Transformation canvas (`TransGraph`)
- Use `TransGraphUi.active( spoon )`. The canvas is the `Canvas` with a `DropTarget` among the `TransGraph`'s children.
- Drag from Design tab: the `Tree`'s `DragSource` (`tree.getData( DND.DRAG_SOURCE_KEY )`) gives `DragAndDropContainer( TYPE_BASE_STEP_TYPE, name, id )`; the canvas `DropTarget` listener calls `spoon.newStep` at `getRealPosition( canvas, event.x, event.y )`. Event x/y are display coordinates. The position is approximate (legacy offsets), so do not assert exact locations.
- Hop: mouse down on the step icon with button 1 + `SWT.SHIFT`, move over the target icon, mouse up there. Generate rows has no error handling, so there is no hop-type popup.
- Double-click (button 1) on a step icon opens its dialog (`editStep`).
- Coordinates: step `getLocation()` + `PropsUI.getInstance().getIconSize() / 2` is the icon centre in model space; `graph.real2screen` converts to canvas pixels (magnification 1.0 by default).
- Run log: `graph.transLogDelegate.getLoggingText()` is the Execution Results "Logging" tab. Step summary line: `<step>.0 - Finished processing (I=, O=, R=, W=, U=, E=)` (`BaseStep.Log.SummaryInfo`).

## Step dialogs and `TableView`
- Step dialog shells are titled with the step type name (e.g. `Generate rows`); labels and keys are in the dialog's package `messages_en_US.properties`.
- Many labels have a trailing space (`"Step name "`, `"Limit "`) and buttons are padded (`"  &OK  "`); match trimmed text or with `withRegex( "\\s*&?OK\\s*" )`.
- `TableView` (fields grids): use `TableViewUi.in( shell.bot() )`. Columns are 1-based (0 is the row number). A new table has one empty row.
  - A click on a cell opens a `Text` or `CCombo` editor as a child of the `Table`. Enter applies it (`character == SWT.CR` for `Text`, `keyCode == SWT.CR` for `CCombo`).
  - Down Arrow in the last row's editor appends a row and edits its first column.
  - `RowGeneratorDialog` columns: 1 Name, 2 Type, 3 Format, 4 Length, 5 Precision, 6 Currency, 7 Decimal, 8 Group, 9 Value, 10 Set empty string.
- Assert the result on the step's meta after OK, e.g. `RowGeneratorMeta.getFieldName()`, `getFieldType()`, `getRowLimit()`. Empty length/precision are stored as `-1`.

## Opening files
- Open files the way the user does: `FileOpenSaveDialogUi.openFromFileMenu( spoon )`, `navigateTo( path )`, wait for `isOpenEnabled()`, then `clickOpen()`.
- Bundled samples: source in `assemblies/samples/src/main/resources/`; in the distribution under `data-integration/samples/`. Use `SpoonUi.sampleFile( "transformations/<name>.ktr" )`; it fails clearly if the file is missing.
- Picking a sample with no external dependencies: grep the `.ktr`/`.kjb` for `<connection>`, `<filename>`, `<file>`, URLs and `<type>` values. Every step/entry type must be loaded, or opening it shows "Missing Plugins". Plugins with a `.kettle-ignore` file in their folder are shipped but disabled for security: `eval-job-entry` (JavaScript `EVAL` entry), `script-values-mod` (Modified JavaScript Value), `janino` (User Defined Java Expression/Class), `exec-sql` (Execute SQL script), `exec-process` (Execute a process). Check with `find <data-integration>/plugins -maxdepth 2 -name .kettle-ignore`. Prefer samples that avoid them, so tests run against the default, secure setup.
  - Known good transformation: `transformations/Generate Row - basics.ktr` (Generate Rows, limit 10 → Dummy).
  - Known good job: `jobs/parallel synchronized jobs/SyncJobs Example1.kjb` (Start, Create file, Delay). Opening only; running it writes files.

## Other UI text
- Dialog and step labels live in `messages/messages_en_US.properties` next to the dialog's Java class (search for the key used in the code, `BaseMessages.getString( PKG, "..." )`).
- XUL-based dialogs and toolbars: search `ui/src/main/resources` and `assemblies/static/src/main/resources/ui` for `*.xul`.
- If a widget has no stable label (icon-only buttons, canvases), prefer calling the same Spoon command the UI calls and asserting on the result. Write that substitution down in the test as a comment.
  - If a stable hook is really needed, propose adding a widget data key to the product code (`widget.setData( "org.eclipse.swtbot.widget.key", "<id>" )`, found with `bot.widgetWithId(...)`). Ask the user before changing product code.

## Editor tabs
- Spoon's editor tabs are SWT `CTabItem`s in `spoon.getTabSet().getSwtTabset()` (a `CTabFolder`), wrapped by `org.pentaho.xul.swt.tab.TabSet`.
- A `Welcome!` tab is always present in addition to file tabs. Do not assume the tab count equals the file count.
- Tab text equals the file name. New files are named `Transformation N` / `Job N` (`Spoon.STRING_TRANSFORMATION` / `STRING_JOB`), using the lowest number not already open.
  - Read the actual name from the returned `TransMeta`/`JobMeta` (`getName()`); do not hard-code `"Transformation 1"`.
  - **Names of 17+ characters are cut** to the first 17 characters plus `\u2026` (`KETTLE_MAX_TAB_LENGTH`, see `SpoonDelegate.getMaxTabLength()`). E.g. "Generate Row - basics" shows as "Generate Row - ba…". Assert or look up the shortened label.
- Switching tabs: `new SWTBot().cTabItem( name ).activate()` fires the SWT selection event that `TabSet.widgetSelected` listens for. Spoon's active file (`getActiveMeta()`) then follows. Verified.
  - `SWTBotCTabItem.isActive()` tells whether that tab is the selected one.

## Spoon state accessors (preferred for `Then` steps)
Call these from the test thread inside `SpoonUi.onUiThread`:

| Purpose | Accessor |
|---------|----------|
| Main shell / display | `spoon.getShell()`, `spoon.getDisplay()` |
| Active transformation | `spoon.getActiveTransformation()` → `TransMeta` or null |
| Open transformations | `spoon.getLoadedTransformations()` → `TransMeta[]` |
| Active job | `spoon.getActiveJob()` → `JobMeta` or null |
| Open jobs | `spoon.getLoadedJobs()` → `JobMeta[]` |
| Active file (trans or job) | `spoon.getActiveMeta()` → `EngineMetaInterface` |
| Close all tabs (cleanup) | `SpoonUi.closeAllFiles( spoon )` |
| Running/last run of the active transformation | `spoon.getActiveTransGraph().getTrans()` → `Trans` or null |
| Connected repository | `spoon.getRepository()` → null when none |
| Global database connections | `spoon.getGlobalManagementBowl().getManager( DatabaseManagementInterface.class )` → `add`, `get( name )`, `remove( name )` |
| Global run configurations | `spoon.getGlobalManagementBowl().getManager( RunConfigurationService.class )` → `getRunConfigurationByType( "Pentaho" )`, `save`, `load( name )`, `delete( name )` |

The managers are what the Database Connection and Run Configuration dialogs call on OK. Use them for "is saved" steps until those dialogs are automated, and note the substitution in the workflow. The View tree's right-click "New..." menu cannot be driven: `ConstUI.displayMenu` shows the menu without attaching it to the tree, so SWTBot's `contextMenu` cannot find it.

Execution results (on the `Trans`; safe to read from the test thread):
- Finished: `trans.isFinished()`. Wait with `SpoonUi.waitFor( "the transformation to finish", ... )`.
- Errors: `trans.getErrors()`.
- Per-step rows: `trans.findRunThread( "<step name>" )` → `StepInterface` with `getLinesWritten()`, `getLinesRead()`, `getLinesInput()`, `getLinesOutput()`, `getLinesRejected()`. These are the I/O/R/W/E numbers in the Step Metrics tab and the "Finished processing" log line.

## `SpoonUi` helper inventory
Check this table before writing a new UI action, and add a row whenever a helper is added.

| Helper | Does |
|--------|------|
| `awaitMainShell()` | Waits for Spoon's main window and returns the `Spoon` instance |
| `closeAllFiles( spoon )` | Closes any dialogs left open, then force-closes all transformation/job tabs (no save prompts) |
| `onUiThread( spoon, Result<T> )` | Runs on the UI thread and returns the value |
| `waitFor( expectation, BooleanSupplier )` | Polls up to 30 s; fails with "Timed out waiting for &lt;expectation&gt;" |
| `mainShell( spoon )` | Activated `SWTBotShell` for the main window (use `.menu()` on it) |
| `transformationCount( spoon )` / `jobCount( spoon )` | Number of open transformations / jobs |
| `newTransformationFromMenu( spoon )` | File → New → Transformation, waits, returns the new `TransMeta` |
| `newJobFromMenu( spoon )` | File → New → Job, waits, returns the new `JobMeta` |
| `editorTabTexts( spoon )` | Texts of all editor tabs in display order (includes `Welcome!`) |
| `sampleFile( relativePath )` | Canonical `File` under `data-integration/samples`; throws if missing |

`SpoonDesignTabUi` (the Design tab):

| Helper | Does |
|--------|------|
| `open( spoon )` | Activates the Design tab |
| `search( text )` / `clearSearch( spoon )` | Types in / clears the search box |
| `selectStep( folder, step )` | Expands the folder, selects the step item, returns the `SWTBotTreeItem` |
| `stepNames( folder )` / `allStepNames()` | Step items listed under one folder / all folders |

`TransGraphUi` (the active transformation canvas):

| Helper | Does |
|--------|------|
| `active( spoon )` | Canvas of the active transformation tab |
| `dropFrom( treeItem, x, y )` | Drags a Design tab item onto the canvas (canvas coordinates); wait for the step |
| `shiftDrag( from, to )` | Shift-drags between two step icons (creates a hop); wait for the hop |
| `doubleClickStep( step )` | Double-clicks a step icon; wait for its dialog |
| `trans()` / `loggingText()` | Running/last `Trans`; text of the Logging tab |

`TableViewUi` (`TableView` grids in step dialogs): `in( bot )`, `rowCount()`, `setCell( row, col, value )` (appends rows as needed), `cell( row, col )`.

`RowGeneratorDialogUi` (Generate rows dialog): `await()`, `setStepName( name )`, `setLimit( limit )`, `fields()` (a `TableViewUi`), `ok()` (waits for the dialog to close), column constants `NAME`, `TYPE`, `FORMAT`, `LENGTH`, `PRECISION`, `DECIMAL`, `VALUE`.

`FileOpenSaveDialogUi` (the File → Open and File → Save As dialog):

| Helper | Does |
|--------|------|
| `openFromFileMenu( spoon )` | File → Open..., waits for the `Open` shell |
| `saveFromFileMenu( spoon )` | File → Save on a never-saved file, waits for the `Save As` shell |
| `navigateTo( path )` | Types into the location box and presses Enter |
| `setFileName( name )` / `clickSave()` | Save As: sets the `File name:` box / waits for Save to be enabled, clicks it, waits for the dialog to close |
| `navigationPath()` / `fileNames()` / `selectedFileName()` | Location box text / names in the file list / selected name |
| `isOpenEnabled()` | Whether the Open button is enabled |
| `doubleClickFile( name )` | Double-clicks a file-list row |
| `clickOpen()` / `clickCancel()` | Clicks and waits until the dialog closes |
| `isOpen()` / `waitUntilClosed()` / `closeIfOpen()` | Dialog state and cleanup |

## SWTBot lookups
- Shell by title: `bot.shell( "Title" )`. Wait for it to appear with `bot.waitUntil( Conditions.shellIsActive( "Title" ) )`.
- Buttons and labels inside a shell: `shell.bot().button( "OK" )`, `shell.bot().label( "..." )`, `shell.bot().text( index )`.
  - `button( text )` matches the mnemonic-stripped label exactly. Pentaho labels are sometimes padded with spaces (e.g. `"  &Run"`); use `withRegex` in that case.
- Tabs: use `bot.cTabItem( text )` (see Editor tabs above). For whole-folder checks, prefer `SpoonUi.editorTabTexts( spoon )`.
