/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.pan;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith( Parameterized.class )
public class PanLoggingLevelsIT {

  // Pan accepts these values through the -level command-line option.
  public enum Level {
    NOTHING( "Nothing" ),
    ERROR( "Error" ),
    MINIMAL( "Minimal" ),
    BASIC( "Basic" ),
    DETAILED( "Detailed" ),
    DEBUG( "Debug" ),
    ROWLEVEL( "Rowlevel" );

    private final String cliCode;

    Level( String cliCode ) {
      this.cliCode = cliCode;
    }

    @Override
    public String toString() {
      return cliCode;
    }
  }

  private static final String LOG_LEVEL_HEADING = "Pan - Logging is at level : ";
  private static final String LOG_OUTPUT_DIR_PROPERTY = "pan.logging.output.dir";
  private static final String ERROR_MARKER = "***PDI ERROR LEVEL MARKER***";

  // Run the test once for each supported log level.
  @Parameterized.Parameters( name = "{0}" )
  public static List<Object[]> levels() {
    return Arrays.asList( Arrays.stream( Level.values() )
      .map( level -> new Object[] { level } )
      .toArray( Object[][]::new ) );
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Parameterized.Parameter
  public Level level;

  @Test
  public void writesOnlyMessagesEnabledAtTheRequestedLevel() throws Exception {
    // The KTR resolves customers-100.txt relative to its own directory.
    Path transformation = fixture( "PDI_Logging_InputOutput.ktr" );
    Path logfile = prepareLogfile();
    runPan( transformation, logfile );

    String log = Files.readString( logfile, StandardCharsets.UTF_8 );
    if ( atLeast( Level.MINIMAL ) ) {
      String description = level == Level.ROWLEVEL ? "Row Level (very detailed)" : level.cliCode;
      assertMarker( log, LOG_LEVEL_HEADING + description, true );
    } else {
      assertMarker( log, LOG_LEVEL_HEADING, false );
    }
    assertMarker( log, "Pan - Start of run.", atLeast( Level.MINIMAL ) );
    assertMarker( log, ERROR_MARKER, atLeast( Level.ERROR ) );
    assertMarker( log, "CSV file input.0 - Finished processing", atLeast( Level.BASIC ) );
    assertMarker( log, "citycount = 4", atLeast( Level.BASIC ) );
    assertMarker( log, "I found 6 different steps to launch.", atLeast( Level.DETAILED ) );
    assertMarker( log, "Pan - Loading transformation from XML file", atLeast( Level.DEBUG ) );
    assertMarker( log, "sort by state.0 - Read row:", level == Level.ROWLEVEL );
  }

  private boolean atLeast( Level threshold ) {
    return level.compareTo( threshold ) >= 0;
  }

  private void assertMarker( String log, String marker, boolean expected ) {
    assertEquals( level + " log marker mismatch for [" + marker + "]\nPan logfile:\n" + log,
      expected, log.contains( marker ) );
  }

  private static Path fixture( String name ) throws URISyntaxException {
    return Path.of( Objects.requireNonNull(
      PanLoggingLevelsIT.class.getResource( "logging/" + name ), "Missing Pan logging fixture: " + name ).toURI() );
  }

  private Path prepareLogfile() throws IOException {
    String outputDirectory = System.getProperty( LOG_OUTPUT_DIR_PROPERTY );
    if ( outputDirectory == null || outputDirectory.trim().isEmpty() ) {
      // Default to temporary output so normal test runs clean up their logs.
      return temporaryFolder.getRoot().toPath().resolve( "pan.log" );
    }

    Path directory = Path.of( outputDirectory );
    if ( !directory.isAbsolute() ) {
      directory = Path.of( System.getProperty( "user.dir" ) ).resolve( directory );
    }
    directory = directory.normalize();
    Files.createDirectories( directory );
    Path logfile = directory.resolve( "PDI_Logging_" + level + ".log" );
    Files.deleteIfExists( logfile );
    return logfile;
  }

  private void runPan( Path transformation, Path logfile ) throws IOException, InterruptedException {
    // Pan exits the JVM, so execute it in a child process.
    Path home = temporaryFolder.newFolder( "home" ).toPath();
    Path tmp = temporaryFolder.newFolder( "tmp" ).toPath();
    Path console = temporaryFolder.newFile( "console.log" ).toPath();
    String executable = System.getProperty( "os.name" ).startsWith( "Windows" ) ? "java.exe" : "java";
    Path java = Path.of( System.getProperty( "java.home" ), "bin", executable );
    ProcessBuilder builder = new ProcessBuilder( java.toString(),
      "-cp", childClasspath(),
      "-Dfile.encoding=UTF-8", "-Duser.language=en", "-Duser.country=US",
      "-DKETTLE_HOME=" + home, "-DPENTAHO_METASTORE_FOLDER=" + home, "-Djava.io.tmpdir=" + tmp,
      Pan.class.getName(), "-file", transformation.toString(), "-level", level.cliCode,
      "-logfile", logfile.toString(), "-norep" );
    builder.environment().put( "KETTLE_HOME", home.toString() );
    builder.directory( home.toFile() ).redirectErrorStream( true ).redirectOutput( console.toFile() );

    // Fail the test instead of waiting indefinitely for Pan.
    Process process = builder.start();
    boolean finished;
    try {
      finished = process.waitFor( 120, TimeUnit.SECONDS );
    } finally {
      if ( process.isAlive() ) {
        process.destroyForcibly();
        process.waitFor( 10, TimeUnit.SECONDS );
      }
    }
    if ( !finished ) {
      fail( "Pan timed out at " + level + diagnostics( console, logfile ) );
    }
    if ( process.exitValue() != 0 ) {
      fail( "Pan exited with " + process.exitValue() + " at " + level + diagnostics( console, logfile ) );
    }
    if ( !Files.isRegularFile( logfile ) ) {
      fail( "Pan did not create its logfile at " + level + diagnostics( console, logfile ) );
    }
  }

  private static String childClasspath() {
    String classpath = System.getProperty( "surefire.test.class.path", System.getProperty( "java.class.path" ) );
    Path parentDirectory = Path.of( System.getProperty( "user.dir" ) ).toAbsolutePath().normalize();
    return Arrays.stream( classpath.split( Pattern.quote( File.pathSeparator ), -1 ) )
      .map( entry -> parentDirectory.resolve( entry ).normalize().toString() )
      .collect( Collectors.joining( File.pathSeparator ) );
  }

  private static String diagnostics( Path console, Path logfile ) throws IOException {
    return "\nPan console:\n" + Files.readString( console, StandardCharsets.UTF_8 )
      + "\nPan logfile:\n" + ( Files.isRegularFile( logfile )
        ? Files.readString( logfile, StandardCharsets.UTF_8 ) : "(not created)" );
  }
}
