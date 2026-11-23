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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.LanguageChoice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

  @BeforeClass
  public static void useEnglishLogMessages() {
    Locale.setDefault( Locale.US );
    LanguageChoice.getInstance().setDefaultLocale( Locale.US );
  }

  @Test( timeout = 120_000 )
  public void writesOnlyMessagesEnabledAtTheRequestedLevel() throws Exception {
    // The KTR resolves customers-100.txt relative to its own directory.
    Path transformation = fixture( "PDI_Logging_InputOutput.ktr" );
    Path logfile = prepareLogfile();
    runPan( transformation, logfile );

    String log = Files.readString( logfile, Charset.defaultCharset() );
    if ( atLeast( Level.MINIMAL ) ) {
      String description = LogLevel.valueOf( level.name() ).getDescription();
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

  private void runPan( Path transformation, Path logfile ) throws Exception {
    PanCommandExecutor previousExecutor = Pan.getCommandExecutor();
    Pan.setCommandExecutor( null );
    SecurityManager previousSecurityManager = System.getSecurityManager();
    ExitBlockingSecurityManager securityManager = new ExitBlockingSecurityManager( previousSecurityManager );
    System.setSecurityManager( securityManager );
    try {
      Pan.main( new String[] { "-file", transformation.toString(), "-level", level.cliCode,
        "-logfile", logfile.toString(), "-norep" } );
    } catch ( SecurityException ignored ) {
      // Expected: System.exit is blocked so the test JVM stays alive; Pan.main catches the first
      // SecurityException and calls exitJVM( UNEXPECTED_ERROR ), so assert against the initial exit code.
    } finally {
      System.setSecurityManager( previousSecurityManager );
      Pan.setCommandExecutor( previousExecutor );
    }
    assertEquals( Integer.valueOf( CommandExecutorCodes.Pan.SUCCESS.getCode() ), securityManager.firstExitStatus );
    assertTrue( "Pan did not create its logfile at " + level, Files.isRegularFile( logfile ) );
  }

  private static final class ExitBlockingSecurityManager extends SecurityManager {
    private final SecurityManager delegate;
    private Integer firstExitStatus;

    private ExitBlockingSecurityManager( SecurityManager delegate ) {
      this.delegate = delegate;
    }

    @Override
    public void checkExit( int status ) {
      if ( firstExitStatus == null ) {
        firstExitStatus = status;
      }
      throw new SecurityException( "System exit not allowed" );
    }

    @Override
    public void checkPermission( Permission perm ) {
      if ( perm.getName().startsWith( "exitVM" ) ) {
        throw new SecurityException( "System exit not allowed" );
      }
      if ( delegate != null ) {
        delegate.checkPermission( perm );
      }
    }

    @Override
    public void checkPermission( Permission perm, Object context ) {
      if ( delegate != null ) {
        delegate.checkPermission( perm, context );
      }
    }
  }
}
