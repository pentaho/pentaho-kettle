package org.pentaho.di.ui.swtbot.runner;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Writes Surefire-compatible JUnit XML reports (one TEST-&lt;class&gt;.xml per test class) and captures a screenshot of
 * the desktop whenever a test fails.
 */
public class SwtBotReportListener extends RunListener {
  private final Path reportsDir;
  private final Path screenshotsDir;
  private final Map<Description, TestCaseResult> results = new LinkedHashMap<>();
  private final String timestamp = LocalDateTime.now().truncatedTo( ChronoUnit.SECONDS )
    .format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );

  public SwtBotReportListener( Path reportsDir ) {
    this.reportsDir = reportsDir;
    this.screenshotsDir = reportsDir.resolve( "screenshots" );
  }

  @Override
  public void testStarted( Description description ) {
    resultFor( description ).startNanos = System.nanoTime();
  }

  @Override
  public void testFailure( Failure failure ) {
    TestCaseResult result = resultFor( failure.getDescription() );
    result.failure = failure;
    result.screenshot = captureScreenshot( failure.getDescription() );
  }

  @Override
  public void testAssumptionFailure( Failure failure ) {
    resultFor( failure.getDescription() ).skipped = true;
  }

  @Override
  public void testIgnored( Description description ) {
    resultFor( description ).skipped = true;
  }

  @Override
  public void testFinished( Description description ) {
    TestCaseResult result = resultFor( description );
    if ( result.startNanos > 0 ) {
      result.seconds = ( System.nanoTime() - result.startNanos ) / 1_000_000_000d;
    }
  }

  @Override
  public void testRunFinished( Result runResult ) throws Exception {
    Files.createDirectories( reportsDir );
    Map<String, List<TestCaseResult>> byClass = new LinkedHashMap<>();
    for ( TestCaseResult result : results.values() ) {
      byClass.computeIfAbsent( result.className, k -> new ArrayList<>() ).add( result );
    }
    for ( Map.Entry<String, List<TestCaseResult>> entry : byClass.entrySet() ) {
      writeSuite( entry.getKey(), entry.getValue() );
    }
  }

  private TestCaseResult resultFor( Description description ) {
    return results.computeIfAbsent( description, TestCaseResult::new );
  }

  private Path captureScreenshot( Description description ) {
    try {
      Files.createDirectories( screenshotsDir );
      Path file = screenshotsDir.resolve( safeFileName( description ) + ".png" );
      return SWTUtils.captureScreenshot( file.toString() ) ? file : null;
    } catch ( Exception e ) {
      System.err.println( "Could not capture screenshot for " + description + ": " + e );
      return null;
    }
  }

  private void writeSuite( String className, List<TestCaseResult> cases ) throws Exception {
    int failures = 0;
    int errors = 0;
    int skipped = 0;
    double totalSeconds = 0;
    for ( TestCaseResult result : cases ) {
      if ( result.failure != null ) {
        if ( result.isAssertionFailure() ) {
          failures++;
        } else {
          errors++;
        }
      } else if ( result.skipped ) {
        skipped++;
      }
      totalSeconds += result.seconds;
    }

    Path reportFile = reportsDir.resolve( "TEST-" + className + ".xml" );
    try ( OutputStream out = Files.newOutputStream( reportFile ) ) {
      XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter( out, "UTF-8" );
      xml.writeStartDocument( "UTF-8", "1.0" );
      xml.writeCharacters( "\n" );
      xml.writeStartElement( "testsuite" );
      xml.writeAttribute( "name", className );
      xml.writeAttribute( "tests", String.valueOf( cases.size() ) );
      xml.writeAttribute( "failures", String.valueOf( failures ) );
      xml.writeAttribute( "errors", String.valueOf( errors ) );
      xml.writeAttribute( "skipped", String.valueOf( skipped ) );
      xml.writeAttribute( "time", formatSeconds( totalSeconds ) );
      xml.writeAttribute( "timestamp", timestamp );
      for ( TestCaseResult result : cases ) {
        writeTestCase( xml, result );
      }
      xml.writeCharacters( "\n" );
      xml.writeEndElement();
      xml.writeEndDocument();
      xml.close();
    }
  }

  private void writeTestCase( XMLStreamWriter xml, TestCaseResult result ) throws Exception {
    xml.writeCharacters( "\n  " );
    xml.writeStartElement( "testcase" );
    xml.writeAttribute( "name", result.methodName );
    xml.writeAttribute( "classname", result.className );
    xml.writeAttribute( "time", formatSeconds( result.seconds ) );
    if ( result.failure != null ) {
      Throwable exception = result.failure.getException();
      xml.writeCharacters( "\n    " );
      xml.writeStartElement( result.isAssertionFailure() ? "failure" : "error" );
      if ( exception != null && exception.getMessage() != null ) {
        xml.writeAttribute( "message", exception.getMessage() );
      }
      if ( exception != null ) {
        xml.writeAttribute( "type", exception.getClass().getName() );
      }
      xml.writeCData( result.failure.getTrace() );
      xml.writeEndElement();
      if ( result.screenshot != null ) {
        // Surefire-style attachment marker understood by the Jenkins JUnit attachments plugin.
        xml.writeCharacters( "\n    " );
        xml.writeStartElement( "system-out" );
        xml.writeCharacters( "[[ATTACHMENT|" + result.screenshot.toAbsolutePath() + "]]" );
        xml.writeEndElement();
      }
      xml.writeCharacters( "\n  " );
    } else if ( result.skipped ) {
      xml.writeEmptyElement( "skipped" );
    }
    xml.writeEndElement();
  }

  private static String formatSeconds( double seconds ) {
    return String.format( Locale.ROOT, "%.3f", seconds );
  }

  private static String safeFileName( Description description ) {
    String name = description.getClassName() + "."
      + ( description.getMethodName() == null ? "class" : description.getMethodName() );
    return name.replaceAll( "[^A-Za-z0-9._-]", "_" );
  }

  private static final class TestCaseResult {
    private final String className;
    private final String methodName;
    private long startNanos;
    private double seconds;
    private boolean skipped;
    private Failure failure;
    private Path screenshot;

    private TestCaseResult( Description description ) {
      this.className = description.getClassName();
      this.methodName = description.getMethodName() == null ? "classSetup" : description.getMethodName();
    }

    private boolean isAssertionFailure() {
      return failure != null && failure.getException() instanceof AssertionError;
    }
  }
}
