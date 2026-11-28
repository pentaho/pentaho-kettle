package org.pentaho.di.ui.swtbot.ui.spoon;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.spoon.Spoon;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * Shared access to the in-process Spoon instance started by
 * {@link org.pentaho.di.ui.swtbot.runner.SpoonSwtBotRunner}.
 */
public final class SpoonUi {
  private static final long STARTUP_TIMEOUT_MS = TimeUnit.MINUTES.toMillis( 5 );
  private static final long POLL_INTERVAL_MS = 500;
  private static final long UI_TIMEOUT_MS = 30000;
  private static final long UI_INTERVAL_MS = 250;

  private SpoonUi() {
  }

  /**
   * Blocks until Spoon has created its Display and its main shell is open and visible.
   */
  public static Spoon awaitMainShell() throws InterruptedException {
    long deadline = System.currentTimeMillis() + STARTUP_TIMEOUT_MS;
    while ( System.currentTimeMillis() < deadline ) {
      Spoon spoon = Spoon.getInstance();
      Display display = spoon == null ? null : spoon.getDisplay();
      if ( display != null && !display.isDisposed() && isMainShellVisible( spoon, display ) ) {
        return spoon;
      }
      Thread.sleep( POLL_INTERVAL_MS );
    }
    throw new IllegalStateException( "Spoon main shell did not open within " + STARTUP_TIMEOUT_MS + " ms" );
  }

  /**
   * Closes any dialog a test left open, then force-closes every open transformation/job tab without save prompts, so
   * the next scenario starts from a clean Spoon. All scenarios in a run share one Spoon instance.
   */
  public static void closeAllFiles( Spoon spoon ) {
    Result<Boolean> closeDialogs = () -> {
      for ( Shell shell : spoon.getDisplay().getShells() ) {
        if ( shell != spoon.getShell() && !shell.isDisposed() && shell.isVisible() ) {
          shell.close();
        }
      }
      return true;
    };
    onUiThread( spoon, closeDialogs );
    Result<Boolean> closeAll = () -> spoon.closeAllFiles( true );
    if ( !onUiThread( spoon, closeAll ) ) {
      throw new IllegalStateException( "Spoon did not close all open files" );
    }
  }

  /**
   * Runs {@code result} on the SWT UI thread and returns its value. Declare the lambda as {@code Result<T>} first.
   */
  public static <T> T onUiThread( Spoon spoon, Result<T> result ) {
    return UIThreadRunnable.syncExec( spoon.getDisplay(), result );
  }

  /**
   * Polls {@code condition} (from the test thread) until it is true, failing with "Timed out waiting for
   * &lt;expectation&gt;".
   */
  public static void waitFor( String expectation, BooleanSupplier condition ) {
    new SWTBot().waitUntil( new DefaultCondition() {
      @Override
      public boolean test() {
        return condition.getAsBoolean();
      }

      @Override
      public String getFailureMessage() {
        return "Timed out waiting for " + expectation;
      }
    }, UI_TIMEOUT_MS, UI_INTERVAL_MS );
  }

  /**
   * The Spoon main window, activated so menu and keyboard input go to it.
   */
  public static SWTBotShell mainShell( Spoon spoon ) {
    SWTBotShell shell = new SWTBotShell( spoon.getShell() );
    shell.activate();
    return shell;
  }

  public static int transformationCount( Spoon spoon ) {
    Result<Integer> count = () -> spoon.getLoadedTransformations().length;
    return onUiThread( spoon, count );
  }

  public static int jobCount( Spoon spoon ) {
    Result<Integer> count = () -> spoon.getLoadedJobs().length;
    return onUiThread( spoon, count );
  }

  /**
   * Clicks File &gt; New &gt; Transformation and waits for the new transformation to open.
   *
   * @return the new (active) transformation
   */
  public static TransMeta newTransformationFromMenu( Spoon spoon ) {
    int before = transformationCount( spoon );
    mainShell( spoon ).menu().menu( "File", "New", "Transformation" ).click();
    waitFor( "a new transformation to open", () -> transformationCount( spoon ) == before + 1 );
    Result<TransMeta> active = spoon::getActiveTransformation;
    return onUiThread( spoon, active );
  }

  /**
   * Clicks File &gt; New &gt; Job and waits for the new job to open.
   *
   * @return the new (active) job
   */
  public static JobMeta newJobFromMenu( Spoon spoon ) {
    int before = jobCount( spoon );
    mainShell( spoon ).menu().menu( "File", "New", "Job" ).click();
    waitFor( "a new job to open", () -> jobCount( spoon ) == before + 1 );
    Result<JobMeta> active = spoon::getActiveJob;
    return onUiThread( spoon, active );
  }

  /**
   * Text of every tab in Spoon's editor tab folder, in display order.
   */
  public static List<String> editorTabTexts( Spoon spoon ) {
    Result<List<String>> texts = () -> {
      List<String> result = new ArrayList<>();
      for ( CTabItem item : spoon.getTabSet().getSwtTabset().getItems() ) {
        result.add( item.getText() );
      }
      return result;
    };
    return onUiThread( spoon, texts );
  }

  /**
   * A file or folder bundled under {@code data-integration/samples}, e.g. {@code "transformations/Rounding.ktr"}.
   * The run's working directory is {@code data-integration}. Paths that resolve outside {@code samples} are rejected.
   */
  public static File sampleFile( String relativePath ) {
    try {
      Path samplesRoot = new File( "samples" ).getCanonicalFile().toPath();
      Path sample = samplesRoot.resolve( relativePath ).toFile().getCanonicalFile().toPath();
      if ( !sample.startsWith( samplesRoot ) ) {
        throw new IllegalArgumentException( "sample path must stay under " + samplesRoot + ": " + relativePath );
      }
      File file = sample.toFile();
      if ( !file.exists() ) {
        throw new IllegalStateException( "sample not found at " + file );
      }
      return file;
    } catch ( IOException e ) {
      throw new UncheckedIOException( e );
    }
  }

  private static boolean isMainShellVisible( Spoon spoon, Display display ) {
    Result<Boolean> visible = () -> {
      Shell shell = spoon.getShell();
      return shell != null && !shell.isDisposed() && shell.isVisible();
    };
    return UIThreadRunnable.syncExec( display, visible );
  }
}
