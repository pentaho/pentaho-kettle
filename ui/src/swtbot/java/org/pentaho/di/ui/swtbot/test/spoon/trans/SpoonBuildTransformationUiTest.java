package org.pentaho.di.ui.swtbot.test.spoon.trans;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.swtbot.ui.core.widget.TableViewUi;
import org.pentaho.di.ui.swtbot.ui.plugins.fileopensave.dialog.FileOpenSaveDialogUi;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonDesignTabUi;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;
import org.pentaho.di.ui.swtbot.ui.spoon.trans.TransGraphUi;
import org.pentaho.di.ui.swtbot.ui.trans.steps.rowgenerator.RowGeneratorDialogUi;

import java.io.File;
import java.util.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Build and run a Generate Rows transformation on the canvas
 * (see ui/src/test/resources/features/build-generate-rows-transformation.feature).
 */
public class SpoonBuildTransformationUiTest {
  /** Design tab names come from the plugin names; "Generate rows" has a lowercase r. */
  private static final String GENERATE_ROWS = "Generate rows";
  private static final String GENERATE_ROWS_RENAMED = "Generate Rows";
  private static final String DUMMY = "Dummy (do nothing)";
  private static final String INPUT_FOLDER = "Input";
  private static final String FLOW_FOLDER = "Flow";
  private static final String SEARCH_TEXT = "dummy";
  /** Drop points in canvas coordinates: Dummy to the right of Generate rows. */
  private static final int DROP_Y = 120;
  private static final int GENERATE_ROWS_DROP_X = 120;
  private static final int DUMMY_DROP_X = 360;
  private static final String LIMIT = "10";
  /** Name, Type, Format, Length, Precision, Decimal, Value: the fields of the "Generate Row - basics" sample. */
  private static final String[][] FIELDS = {
    { "FieldAsNumber", "Number", "", "", "", ".", "1.2345" },
    { "FieldAsString", "String", "", "20", "", "", "Test" },
    { "FieldAsDate", "Date", "yyyy-MM-dd", "", "", "", "2006-07-27" },
    { "FieldAsBoolean", "Boolean", "", "", "", "", "True" },
    { "FieldAsInteger", "Integer", "", "", "", "", "12345" },
    { "FieldAsBigNumber", "BigNumber", "", "10", "5", ".", "1.2345" },
  };
  private static final int[] FIELD_COLUMNS = {
    RowGeneratorDialogUi.NAME, RowGeneratorDialogUi.TYPE, RowGeneratorDialogUi.FORMAT, RowGeneratorDialogUi.LENGTH,
    RowGeneratorDialogUi.PRECISION, RowGeneratorDialogUi.DECIMAL, RowGeneratorDialogUi.VALUE };
  private static final String RUN_OPTIONS_TITLE = "Run Options";
  /** The Run Options launch button is labeled "  &amp;Run" (leading spaces), so a mnemonic match does not work. */
  private static final String RUN_BUTTON_REGEX = "\\s*&?Run\\s*";
  private static final String DUMMY_LOG_LINE =
    DUMMY + ".0 - Finished processing (I=0, O=0, R=10, W=10, U=0, E=0)";
  /** Saved in the run's sandbox home folder, which the runner recreates for every run. */
  private static final String SAVED_FILE_NAME = "build-generate-rows.ktr";

  private Spoon spoon;
  private TransMeta transMeta;
  private TransGraphUi graph;
  private SpoonDesignTabUi design;

  @Before
  public void setUp() throws Exception {
    // Given Spoon is started successfully in the automated test environment
    spoon = SpoonUi.awaitMainShell();
    // And no transformations or jobs are open
    SpoonUi.closeAllFiles( spoon );
    assertEquals( "open transformations before scenario", 0, SpoonUi.transformationCount( spoon ) );
    assertEquals( "open jobs before scenario", 0, SpoonUi.jobCount( spoon ) );
    // And a new transformation is open
    transMeta = SpoonUi.newTransformationFromMenu( spoon );
    graph = TransGraphUi.active( spoon );
    design = SpoonDesignTabUi.open( spoon );
  }

  @After
  public void tearDown() {
    SpoonDesignTabUi.clearSearch( spoon );
    SpoonUi.closeAllFiles( spoon );
  }

  /**
   * Scenario: Drag a step from a Design tab folder onto the canvas.
   */
  @Test
  public void dragsStepFromDesignFolderOntoCanvas() {
    // When the user drags "Generate rows" from the "Input" folder of the Design tab onto the canvas
    addGenerateRows();

    // Then the transformation has 1 step
    assertEquals( "steps in the transformation", 1, stepCount() );
    // And the transformation has a step named "Generate rows" drawn on the canvas
    assertTrue( "'" + GENERATE_ROWS + "' is drawn on the canvas", step( GENERATE_ROWS ).isDrawn() );
  }

  /**
   * Scenario: Filter the Design tab with the search box.
   */
  @Test
  public void filtersDesignTabWithSearchBox() {
    // When the user types "dummy" in the Design tab search box
    design.search( SEARCH_TEXT );

    // Then the Design tab lists "Dummy (do nothing)" under "Flow"
    SpoonUi.waitFor( "'" + DUMMY + "' under '" + FLOW_FOLDER + "'",
      () -> design.stepNames( FLOW_FOLDER ).contains( DUMMY ) );
    // And the Design tab does not list "Generate rows"
    List<String> listed = design.allStepNames();
    assertFalse( "Design tab steps after searching '" + SEARCH_TEXT + "': " + listed, listed.contains( GENERATE_ROWS ) );
  }

  /**
   * Scenario: Drag a search result onto the canvas.
   */
  @Test
  public void dragsSearchResultOntoCanvas() {
    // Given the transformation has a "Generate rows" step
    addGenerateRows();
    // And the user has typed "dummy" in the Design tab search box
    design.search( SEARCH_TEXT );

    // When the user drags "Dummy (do nothing)" from the search results onto the canvas
    dragOntoCanvas( FLOW_FOLDER, DUMMY, DUMMY_DROP_X );

    // Then the transformation has 2 steps
    assertEquals( "steps in the transformation", 2, stepCount() );
    // And the transformation has a step named "Dummy (do nothing)" drawn on the canvas
    assertTrue( "'" + DUMMY + "' is drawn on the canvas", step( DUMMY ).isDrawn() );
    // And the transformation has no hops
    assertEquals( "hops in the transformation", 0, hopCount() );
  }

  /**
   * Scenario: Connect two steps with a hop.
   */
  @Test
  public void connectsStepsWithHop() {
    // Given the transformation has a "Generate rows" step and a "Dummy (do nothing)" step
    addGenerateRows();
    addDummy();

    // When the user Shift-drags from "Generate rows" to "Dummy (do nothing)"
    graph.shiftDrag( GENERATE_ROWS, DUMMY );

    // Then the transformation has 1 hop
    SpoonUi.waitFor( "a hop to be created", () -> hopCount() == 1 );
    // And the hop goes from "Generate rows" to "Dummy (do nothing)" and is enabled
    assertHop( GENERATE_ROWS, DUMMY );
  }

  /**
   * Scenario: Configure the Generate Rows step in its dialog.
   */
  @Test
  public void configuresGenerateRowsStepInDialog() {
    // Given "Generate rows" is connected to "Dummy (do nothing)"
    addGenerateRows();
    addDummy();
    connect();

    // When the user double-clicks "Generate rows", renames it to "Generate Rows", sets Limit to 10, adds these
    // fields, and clicks OK
    configureGenerateRows();

    // Then the step is named "Generate Rows" and its row limit is 10
    RowGeneratorMeta meta = (RowGeneratorMeta) step( GENERATE_ROWS_RENAMED ).getStepMetaInterface();
    assertEquals( "row limit", LIMIT, meta.getRowLimit() );
    // And the step has the 6 fields from the table, in order
    assertFields( meta );
    // And the hop goes from "Generate Rows" to "Dummy (do nothing)"
    assertHop( GENERATE_ROWS_RENAMED, DUMMY );
  }

  /**
   * Scenario: Run the transformation built on the canvas.
   */
  @Test
  public void runsTransformationBuiltOnCanvas() {
    // Given the "Generate Rows" step is configured with the 6 fields and connected to "Dummy (do nothing)"
    addGenerateRows();
    addDummy();
    connect();
    configureGenerateRows();
    // And the user has saved the transformation as "build-generate-rows.ktr" in the home folder
    File home = new File( System.getProperty( "user.home" ) );
    File saved = new File( home, SAVED_FILE_NAME );
    assertFalse( "'" + saved + "' exists before saving", saved.exists() );
    FileOpenSaveDialogUi.saveFromFileMenu( spoon ).navigateTo( home.getAbsolutePath() ).setFileName( SAVED_FILE_NAME )
      .clickSave();
    Result<Boolean> unsaved = transMeta::hasChanged;
    SpoonUi.waitFor( "'" + saved + "' to be saved", () -> saved.isFile() && !SpoonUi.onUiThread( spoon, unsaved ) );

    // When the user runs the transformation with the default run options
    SpoonUi.mainShell( spoon ).menu().menu( "Action", "Run" ).click();
    SWTBotShell runOptions = new SWTBot().shell( RUN_OPTIONS_TITLE );
    runOptions.activate();
    Matcher<Button> runButton = allOf( widgetOfType( Button.class ), withRegex( RUN_BUTTON_REGEX ) );
    new SWTBotButton( runOptions.bot().widget( runButton ) ).click();

    // Then the transformation finishes without errors
    SpoonUi.waitFor( "the transformation to finish", () -> {
      Trans trans = graph.trans();
      return trans != null && trans.isFinished();
    } );
    Trans trans = graph.trans();
    assertEquals( "errors reported by the transformation", 0, trans.getErrors() );
    // And the "Generate Rows" step writes 10 rows
    assertEquals( "rows written by '" + GENERATE_ROWS_RENAMED + "'", 10,
      runningStep( trans, GENERATE_ROWS_RENAMED ).getLinesWritten() );
    // And the "Dummy (do nothing)" step reads 10 rows
    assertEquals( "rows read by '" + DUMMY + "'", 10, runningStep( trans, DUMMY ).getLinesRead() );
    // And the Logging tab shows "Dummy (do nothing).0 - Finished processing (I=0, O=0, R=10, W=10, U=0, E=0)"
    SpoonUi.waitFor( "the Logging tab to show '" + DUMMY_LOG_LINE + "'",
      () -> graph.loggingText().contains( DUMMY_LOG_LINE ) );
  }

  private void addGenerateRows() {
    dragOntoCanvas( INPUT_FOLDER, GENERATE_ROWS, GENERATE_ROWS_DROP_X );
  }

  /** Finds "Dummy (do nothing)" with the search box and drags it onto the canvas. */
  private void addDummy() {
    design.search( SEARCH_TEXT );
    dragOntoCanvas( FLOW_FOLDER, DUMMY, DUMMY_DROP_X );
  }

  private void connect() {
    graph.shiftDrag( GENERATE_ROWS, DUMMY );
    SpoonUi.waitFor( "a hop to be created", () -> hopCount() == 1 );
  }

  private void dragOntoCanvas( String folder, String step, int x ) {
    int before = stepCount();
    SWTBotTreeItem item = design.selectStep( folder, step );
    graph.dropFrom( item, x, DROP_Y );
    SpoonUi.waitFor( "'" + step + "' to be added to the canvas", () -> stepCount() == before + 1 );
  }

  /** Opens "Generate rows" and enters the sample's name, limit and fields. */
  private void configureGenerateRows() {
    graph.doubleClickStep( GENERATE_ROWS );
    RowGeneratorDialogUi dialog = RowGeneratorDialogUi.await();
    dialog.setStepName( GENERATE_ROWS_RENAMED );
    dialog.setLimit( LIMIT );
    TableViewUi fields = dialog.fields();
    for ( int row = 0; row < FIELDS.length; row++ ) {
      for ( int i = 0; i < FIELD_COLUMNS.length; i++ ) {
        if ( !FIELDS[row][i].isEmpty() ) {
          fields.setCell( row, FIELD_COLUMNS[i], FIELDS[row][i] );
        }
      }
    }
    dialog.ok();
    SpoonUi.waitFor( "the step to be renamed '" + GENERATE_ROWS_RENAMED + "'",
      () -> findStep( GENERATE_ROWS_RENAMED ) != null );
  }

  private void assertFields( RowGeneratorMeta meta ) {
    String[] names = new String[FIELDS.length];
    String[] types = new String[FIELDS.length];
    String[] formats = new String[FIELDS.length];
    int[] lengths = new int[FIELDS.length];
    int[] precisions = new int[FIELDS.length];
    String[] decimals = new String[FIELDS.length];
    String[] values = new String[FIELDS.length];
    for ( int row = 0; row < FIELDS.length; row++ ) {
      names[row] = FIELDS[row][0];
      types[row] = FIELDS[row][1];
      formats[row] = FIELDS[row][2];
      lengths[row] = FIELDS[row][3].isEmpty() ? -1 : Integer.parseInt( FIELDS[row][3] );
      precisions[row] = FIELDS[row][4].isEmpty() ? -1 : Integer.parseInt( FIELDS[row][4] );
      decimals[row] = FIELDS[row][5];
      values[row] = FIELDS[row][6];
    }
    assertArrayEquals( "field names", names, meta.getFieldName() );
    assertArrayEquals( "field types", types, meta.getFieldType() );
    assertArrayEquals( "field formats", formats, meta.getFieldFormat() );
    assertArrayEquals( "field lengths", lengths, meta.getFieldLength() );
    assertArrayEquals( "field precisions", precisions, meta.getFieldPrecision() );
    assertArrayEquals( "field decimals", decimals, meta.getDecimal() );
    assertArrayEquals( "field values", values, meta.getValue() );
  }

  private void assertHop( String from, String to ) {
    Result<TransHopMeta> hop = () -> transMeta.nrTransHops() == 1 ? transMeta.getTransHop( 0 ) : null;
    TransHopMeta only = SpoonUi.onUiThread( spoon, hop );
    assertNotNull( "expected exactly 1 hop", only );
    assertEquals( "hop from", from, only.getFromStep().getName() );
    assertEquals( "hop to", to, only.getToStep().getName() );
    assertTrue( "hop is enabled", only.isEnabled() );
  }

  private int stepCount() {
    Result<Integer> count = transMeta::nrSteps;
    return SpoonUi.onUiThread( spoon, count );
  }

  private int hopCount() {
    Result<Integer> count = transMeta::nrTransHops;
    return SpoonUi.onUiThread( spoon, count );
  }

  private StepMeta findStep( String name ) {
    Result<StepMeta> step = () -> transMeta.findStep( name );
    return SpoonUi.onUiThread( spoon, step );
  }

  private StepMeta step( String name ) {
    StepMeta step = findStep( name );
    assertNotNull( "no step named '" + name + "'", step );
    return step;
  }

  private static StepInterface runningStep( Trans trans, String stepName ) {
    StepInterface step = trans.findRunThread( stepName );
    assertNotNull( "step '" + stepName + "' did not run", step );
    return step;
  }
}
