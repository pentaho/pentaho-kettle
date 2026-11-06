package org.pentaho.di.ui.swtbot;

import org.pentaho.di.ui.spoon.Spoon;

import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Runs SWTBot scenarios and Spoon in the same JVM.
 * <p>
 * SWT requires the Display to live on the JVM main thread (mandatory on macOS with -XstartOnFirstThread), so Spoon
 * is started on the main thread and the JUnit/SWTBot scenarios run on a worker thread that drives the UI.
 * <p>
 * Spoon.main() ends with System.exit(0), so the outcome is reported through a status file: if Spoon exits before the
 * scenarios finish, the status file is missing and the calling script treats the run as failed.
 */
public final class SpoonSwtBotRunner {
  public static final String STATUS_FILE_PROPERTY = "swtbot.status.file";

  private SpoonSwtBotRunner() {
  }

  public static void main( String[] args ) throws Exception {
    if ( args.length == 0 ) {
      System.err.println( "Usage: SpoonSwtBotRunner <test class> [<test class> ...]" );
      System.exit( 2 );
    }

    Class<?>[] testClasses = new Class<?>[ args.length ];
    for ( int i = 0; i < args.length; i++ ) {
      testClasses[ i ] = Class.forName( args[ i ] );
    }

    Path statusFile = Paths.get( System.getProperty( STATUS_FILE_PROPERTY, "swtbot-status.txt" ) );
    Files.deleteIfExists( statusFile );

    PrintStream console = System.out;
    Thread testThread = new Thread( () -> runScenarios( testClasses, statusFile, console ), "swtbot-scenarios" );
    testThread.setDaemon( true );
    testThread.start();

    Spoon.main( new String[ 0 ] );
  }

  private static void runScenarios( Class<?>[] testClasses, Path statusFile, PrintStream console ) {
    int exitCode;
    try {
      JUnitCore junit = new JUnitCore();
      junit.addListener( new TextListener( console ) );
      junit.addListener( new SwtBotReportListener( statusFile.toAbsolutePath().getParent() ) );
      Result result = junit.run( testClasses );
      writeStatus( statusFile, result );
      exitCode = result.wasSuccessful() ? 0 : 1;
    } catch ( Throwable t ) {
      t.printStackTrace( console );
      exitCode = 1;
    }
    System.exit( exitCode );
  }

  private static void writeStatus( Path statusFile, Result result ) throws Exception {
    StringWriter status = new StringWriter();
    status.append( result.wasSuccessful() ? "PASSED" : "FAILED" ).append( '\n' );
    status.append( "tests=" ).append( String.valueOf( result.getRunCount() ) )
      .append( " failures=" ).append( String.valueOf( result.getFailureCount() ) )
      .append( " ignored=" ).append( String.valueOf( result.getIgnoreCount() ) ).append( '\n' );
    for ( Failure failure : result.getFailures() ) {
      status.append( '\n' ).append( failure.getTestHeader() ).append( '\n' ).append( failure.getTrace() );
    }
    Path parent = statusFile.toAbsolutePath().getParent();
    if ( parent != null ) {
      Files.createDirectories( parent );
    }
    Files.write( statusFile, status.toString().getBytes( StandardCharsets.UTF_8 ) );
  }
}
