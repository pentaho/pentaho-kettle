package org.pentaho.di.ui.swtbot.test.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Editor tabs for multiple open transformations and jobs
 * (see ui/src/test/resources/features/editor-tabs.feature).
 */
public class SpoonEditorTabsUiTest {
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
   * Scenario: Each new transformation and job opens in its own tab.
   */
  @Test
  public void eachNewTransformationAndJobOpensInItsOwnTab() {
    // When the user creates 3 transformations and 2 jobs from the File -> New menu
    List<String> fileNames = new ArrayList<>();
    for ( int i = 0; i < 3; i++ ) {
      fileNames.add( SpoonUi.newTransformationFromMenu( spoon ).getName() );
    }
    for ( int i = 0; i < 2; i++ ) {
      fileNames.add( SpoonUi.newJobFromMenu( spoon ).getName() );
    }

    // Then 3 transformations and 2 jobs are open
    assertEquals( "open transformations", 3, SpoonUi.transformationCount( spoon ) );
    assertEquals( "open jobs", 2, SpoonUi.jobCount( spoon ) );

    // And each new transformation and job has its own tab labeled with its name
    assertEquals( "every new file should have a distinct name: " + fileNames, fileNames.size(),
      new HashSet<>( fileNames ).size() );
    List<String> tabTexts = SpoonUi.editorTabTexts( spoon );
    for ( String fileName : fileNames ) {
      assertTrue( "expected a tab labeled '" + fileName + "' but tabs were " + tabTexts,
        tabTexts.contains( fileName ) );
    }
  }

  /**
   * Scenario Outline: Selecting a tab makes its file the active one. Examples: transformation, then job.
   */
  @Test
  public void selectingTransformationTabMakesItActive() {
    assertSelectingTabOfFirstFileMakesItActive( FileType.TRANSFORMATION, FileType.JOB );
  }

  /**
   * Scenario Outline: Selecting a tab makes its file the active one. Examples: job, then transformation.
   */
  @Test
  public void selectingJobTabMakesItActive() {
    assertSelectingTabOfFirstFileMakesItActive( FileType.JOB, FileType.TRANSFORMATION );
  }

  private void assertSelectingTabOfFirstFileMakesItActive( FileType firstFile, FileType secondFile ) {
    // Given a new <first file> and then a new <second file> are open
    String firstName = firstFile.createFromMenu( spoon );
    String secondName = secondFile.createFromMenu( spoon );
    assertEquals( "the most recently created file should be active", secondName, activeFileName() );

    // When the user selects the tab of the <first file>
    SWTBotCTabItem firstTab = new SWTBot().cTabItem( firstName );
    firstTab.activate();

    // Then the tab of the <first file> is the selected tab
    assertTrue( "tab '" + firstName + "' should be selected", firstTab.isActive() );

    // And the <first file> is the active file
    SpoonUi.waitFor( "'" + firstName + "' to become the active file", () -> firstName.equals( activeFileName() ) );
    assertNotNull( "the active file should be a " + firstFile, firstFile.activeFile( spoon ) );
  }

  private String activeFileName() {
    Result<String> name = () -> {
      EngineMetaInterface active = spoon.getActiveMeta();
      return active == null ? null : active.getName();
    };
    return SpoonUi.onUiThread( spoon, name );
  }

  private enum FileType {
    TRANSFORMATION {
      @Override
      String createFromMenu( Spoon spoon ) {
        return SpoonUi.newTransformationFromMenu( spoon ).getName();
      }

      @Override
      Object activeFile( Spoon spoon ) {
        Result<Object> active = spoon::getActiveTransformation;
        return SpoonUi.onUiThread( spoon, active );
      }
    },
    JOB {
      @Override
      String createFromMenu( Spoon spoon ) {
        return SpoonUi.newJobFromMenu( spoon ).getName();
      }

      @Override
      Object activeFile( Spoon spoon ) {
        Result<Object> active = spoon::getActiveJob;
        return SpoonUi.onUiThread( spoon, active );
      }
    };

    abstract String createFromMenu( Spoon spoon );

    abstract Object activeFile( Spoon spoon );

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
}
