package org.pentaho.di.ui.swtbot.test.plugins.fileopensave.dialog;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.swtbot.ui.plugins.fileopensave.dialog.FileOpenSaveDialogUi;
import org.pentaho.di.ui.swtbot.ui.spoon.SpoonUi;

import java.io.File;
import java.util.List;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Open files with the File Open dialog
 * (see ui/src/test/resources/features/file-open-dialog.feature).
 */
public class SpoonFileOpenDialogUiTest {
  private static final String TRANSFORMATION_SAMPLE = "transformations/Generate Row - basics.ktr";
  private static final String TRANSFORMATION_FILE_NAME = "Generate Row - basics.ktr";
  private static final String TRANSFORMATION_NAME = "Generate Row - basics";

  private Spoon spoon;
  private FileOpenSaveDialogUi dialog;
  /** Tabs open before the file, e.g. Welcome, which Spoon only shows when a browser (WebKit on Linux) is available. */
  private int tabsBeforeOpening;

  @Before
  public void setUp() throws Exception {
    // Given Spoon is started successfully in the automated test environment
    spoon = SpoonUi.awaitMainShell();
    // And no transformations or jobs are open
    SpoonUi.closeAllFiles( spoon );
    assertNoFilesOpen();
    tabsBeforeOpening = SpoonUi.editorTabTexts( spoon ).size();
    // And the Open dialog is shown from the File menu
    dialog = FileOpenSaveDialogUi.openFromFileMenu( spoon );
    assertTrue( "the Open dialog should be shown", dialog.isOpen() );
  }

  @After
  public void tearDown() {
    if ( dialog != null ) {
      dialog.closeIfOpen();
    }
    SpoonUi.closeAllFiles( spoon );
  }

  /**
   * Scenario Outline: Open a sample &lt;file type&gt; by entering its path. Examples: transformation.
   */
  @Test
  public void opensTransformationByEnteringItsPath() {
    assertOpensSampleByEnteringItsPath( FileType.TRANSFORMATION, TRANSFORMATION_SAMPLE, TRANSFORMATION_FILE_NAME,
      TRANSFORMATION_NAME );
  }

  /**
   * Scenario Outline: Open a sample &lt;file type&gt; by entering its path. Examples: job.
   */
  @Test
  public void opensJobByEnteringItsPath() {
    assertOpensSampleByEnteringItsPath( FileType.JOB, "jobs/parallel synchronized jobs/SyncJobs Example1.kjb",
      "SyncJobs Example1.kjb", "SyncJobs Example1" );
  }

  /**
   * Scenario: Open a transformation by double-clicking it in the file list.
   */
  @Test
  public void opensTransformationByDoubleClickingItInTheFileList() {
    // Given the user has entered the path of the sample folder "transformations"
    dialog.navigateTo( SpoonUi.sampleFile( "transformations" ).getPath() );

    // And "Generate Row - basics.ktr" is listed in the file list
    SpoonUi.waitFor( "'" + TRANSFORMATION_FILE_NAME + "' to be listed in the file list",
      () -> dialog.fileNames().contains( TRANSFORMATION_FILE_NAME ) );

    // When the user double-clicks "Generate Row - basics.ktr" in the file list
    dialog.doubleClickFile( TRANSFORMATION_FILE_NAME );

    // Then the Open dialog closes
    dialog.waitUntilClosed();

    // And "Generate Row - basics" is open as the active transformation
    assertActiveFile( FileType.TRANSFORMATION, TRANSFORMATION_NAME );
  }

  /**
   * Scenario: Cancelling the Open dialog opens nothing.
   */
  @Test
  public void cancellingTheOpenDialogOpensNothing() {
    // Given the user has entered the path of the sample "transformations/Generate Row - basics.ktr"
    dialog.navigateTo( SpoonUi.sampleFile( TRANSFORMATION_SAMPLE ).getPath() );
    SpoonUi.waitFor( "'" + TRANSFORMATION_FILE_NAME + "' to be selected in the file list",
      () -> TRANSFORMATION_FILE_NAME.equals( dialog.selectedFileName() ) );

    // When the user clicks Cancel
    dialog.clickCancel();

    // Then the Open dialog closes
    assertTrue( "the Open dialog should be closed", !dialog.isOpen() );

    // And no transformations or jobs are open
    assertNoFilesOpen();
  }

  private void assertOpensSampleByEnteringItsPath( FileType fileType, String sample, String fileName, String name ) {
    // Given the user has entered the path of the sample "<sample file>"
    File sampleFile = SpoonUi.sampleFile( sample );
    dialog.navigateTo( sampleFile.getPath() );

    // And "<file name>" is selected in the file list
    SpoonUi.waitFor( "'" + fileName + "' to be selected in the file list",
      () -> fileName.equals( dialog.selectedFileName() ) );
    assertTrue( "Open should be enabled once a file is selected", dialog.isOpenEnabled() );

    // When the user clicks Open
    dialog.clickOpen();

    // Then the Open dialog closes
    assertTrue( "the Open dialog should be closed", !dialog.isOpen() );

    // And "<name>" is open as the active <file type>
    assertActiveFile( fileType, name );
  }

  private void assertActiveFile( FileType fileType, String name ) {
    SpoonUi.waitFor( "'" + name + "' to open", () -> name.equals( fileType.activeFileName( spoon ) ) );
    List<String> tabTexts = SpoonUi.editorTabTexts( spoon );
    assertEquals( "open " + fileType + "s", 1, fileType.openCount( spoon ) );
    assertEquals( "editor tabs " + tabTexts, tabsBeforeOpening + 1, tabTexts.size() );
  }

  private void assertNoFilesOpen() {
    assertEquals( "open transformations", 0, SpoonUi.transformationCount( spoon ) );
    assertEquals( "open jobs", 0, SpoonUi.jobCount( spoon ) );
  }

  private enum FileType {
    TRANSFORMATION {
      @Override
      String activeFileName( Spoon spoon ) {
        Result<EngineMetaInterface> active = spoon::getActiveTransformation;
        return nameOf( SpoonUi.onUiThread( spoon, active ) );
      }

      @Override
      int openCount( Spoon spoon ) {
        return SpoonUi.transformationCount( spoon );
      }
    },
    JOB {
      @Override
      String activeFileName( Spoon spoon ) {
        Result<EngineMetaInterface> active = spoon::getActiveJob;
        return nameOf( SpoonUi.onUiThread( spoon, active ) );
      }

      @Override
      int openCount( Spoon spoon ) {
        return SpoonUi.jobCount( spoon );
      }
    };

    abstract String activeFileName( Spoon spoon );

    abstract int openCount( Spoon spoon );

    private static String nameOf( EngineMetaInterface meta ) {
      return meta == null ? null : meta.getName();
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
}
