package org.pentaho.di.ui.swtbot.test.spoon.trans;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.swtbot.ui.plugins.fileopensave.dialog.FileOpenSaveDialogUi;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import java.util.List;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Run a bundled sample transformation
 * (see ui/src/test/resources/features/run-sample-transformation.feature).
 */
public class SpoonRunSampleTransformationUiTest {
  private static final String SAMPLE_NAME = "Generate Row - basics";
  private static final String SAMPLE_PATH = "transformations/" + SAMPLE_NAME + ".ktr";
  /** Spoon shortens tab labels of 17+ characters to 17 characters plus an ellipsis (KETTLE_MAX_TAB_LENGTH). */
  private static final String SAMPLE_TAB_LABEL = "Generate Row - ba\u2026";
  private static final String RUN_OPTIONS_TITLE = "Run Options";
  /** The Run Options launch button is labeled "  &amp;Run" (leading spaces), so a mnemonic match does not work. */
  private static final String RUN_BUTTON_REGEX = "\\s*&?Run\\s*";

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    // Given Spoon is started successfully in the automated test environment
    spoon = SpoonUi.awaitMainShell();
    // And no transformations or jobs are open
    SpoonUi.closeAllFiles( spoon );
    assertEquals( "open transformations before scenario", 0, SpoonUi.transformationCount( spoon ) );
    assertEquals( "open jobs before scenario", 0, SpoonUi.jobCount( spoon ) );
  }

  @After
  public void tearDown() {
    SpoonUi.closeAllFiles( spoon );
  }

  /**
   * Scenario: Run the "Generate Row - basics" sample with the default run options.
   */
  @Test
  public void runsGenerateRowBasicsSampleWithDefaultRunOptions() {
    // Given the sample transformation "Generate Row - basics" is open in its own tab
    TransMeta sample = openSample();
    assertEquals( "active transformation after opening the sample", SAMPLE_NAME, sample.getName() );
    List<String> tabTexts = SpoonUi.editorTabTexts( spoon );
    assertTrue( "expected a tab labeled '" + SAMPLE_TAB_LABEL + "' but tabs were " + tabTexts,
      tabTexts.contains( SAMPLE_TAB_LABEL ) );

    // When the user runs the transformation with the default run options
    SpoonUi.mainShell( spoon ).menu().menu( "Action", "Run" ).click();
    SWTBotShell runOptions = new SWTBot().shell( RUN_OPTIONS_TITLE );
    runOptions.activate();
    Matcher<Button> runButton = allOf( widgetOfType( Button.class ), withRegex( RUN_BUTTON_REGEX ) );
    new SWTBotButton( runOptions.bot().widget( runButton ) ).click();

    // Then the transformation finishes without errors
    SpoonUi.waitFor( "the transformation to finish", () -> {
      Trans trans = activeTrans();
      return trans != null && trans.isFinished();
    } );
    Trans trans = activeTrans();
    assertEquals( "errors reported by the transformation", 0, trans.getErrors() );

    // And the "Generate Rows" step writes 10 rows
    assertEquals( "rows written by 'Generate Rows'", 10, step( trans, "Generate Rows" ).getLinesWritten() );

    // And the "Dummy (do nothing)" step reads 10 rows
    assertEquals( "rows read by 'Dummy (do nothing)'", 10, step( trans, "Dummy (do nothing)" ).getLinesRead() );
  }

  /**
   * Opens the sample with File &gt; Open: enter its path in the Open dialog and click Open.
   */
  private TransMeta openSample() {
    FileOpenSaveDialogUi dialog = FileOpenSaveDialogUi.openFromFileMenu( spoon );
    dialog.navigateTo( SpoonUi.sampleFile( SAMPLE_PATH ).getPath() );
    SpoonUi.waitFor( "the sample to be selected in the Open dialog", dialog::isOpenEnabled );
    dialog.clickOpen();
    SpoonUi.waitFor( "the sample to open", () -> SpoonUi.transformationCount( spoon ) == 1 );
    Result<TransMeta> active = spoon::getActiveTransformation;
    TransMeta opened = SpoonUi.onUiThread( spoon, active );
    assertNotNull( "no active transformation after opening " + SAMPLE_PATH, opened );
    return opened;
  }

  private Trans activeTrans() {
    Result<Trans> trans = () -> {
      TransGraph graph = spoon.getActiveTransGraph();
      return graph == null ? null : graph.getTrans();
    };
    return SpoonUi.onUiThread( spoon, trans );
  }

  private static StepInterface step( Trans trans, String stepName ) {
    StepInterface step = trans.findRunThread( stepName );
    assertNotNull( "step '" + stepName + "' did not run", step );
    return step;
  }
}
