package org.pentaho.di.ui.swtbot.test.spoon;

import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Scenario: Create a new transformation from the File menu
 * (see ui/src/test/resources/features/new-transformation-basic.feature).
 */
public class SpoonNewTransformationUiTest {
  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    // Given Spoon is running
    spoon = SpoonUi.awaitMainShell();
  }

  @After
  public void tearDown() {
    SpoonUi.closeAllFiles( spoon );
  }

  @Test
  public void opensNewTransformationFromTheFileMenu() throws Exception {
    SWTBotShell mainShell = SpoonUi.mainShell( spoon );
    int transformationsBefore = SpoonUi.transformationCount( spoon );

    // When the user selects File -> New -> Transformation
    mainShell.menu().menu( "File", "New", "Transformation" ).click();

    // Then a new transformation editor tab is opened
    SpoonUi.waitFor( ( transformationsBefore + 1 ) + " loaded transformations after File > New > Transformation",
      () -> SpoonUi.transformationCount( spoon ) == transformationsBefore + 1 );

    Result<TransMeta> activeTransformation = spoon::getActiveTransformation;
    TransMeta active = SpoonUi.onUiThread( spoon, activeTransformation );
    assertNotNull( "The new transformation should be the active editor tab", active );
  }
}
