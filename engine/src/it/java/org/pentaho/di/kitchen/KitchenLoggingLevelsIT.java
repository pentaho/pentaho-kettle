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

package org.pentaho.di.kitchen;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.base.CommandExecutorCodes;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.LanguageChoice;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith( Parameterized.class )
public class KitchenLoggingLevelsIT {

  private static final String ERROR_MARKER = "*** KITCHEN ERROR LEVEL MARKER ***";
  private static final String LOGGING_JOB = "PDI_Logging_TransExecutor.kjb";
  private static final String[] LOGGING_FIXTURES = {
    LOGGING_JOB, "PDI_Logging_TransExecutor_Main.ktr", "PDI_Logging_TransExecutor.ktr", "customers-100.txt"
  };

  @Parameterized.Parameters( name = "{0}" )
  public static List<Object[]> levels() {
    return List.of( new Object[] { LogLevel.ERROR }, new Object[] { LogLevel.DETAILED },
      new Object[] { LogLevel.DEBUG }, new Object[] { LogLevel.ROWLEVEL } );
  }

  @Parameterized.Parameter
  public LogLevel level;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test( timeout = 120_000 )
  public void writesOnlyMessagesEnabledAtTheRequestedLevel() throws Exception {
    // The job deletes previous output and writes a new file next to its inputs.
    Path directory = temporaryFolder.getRoot().toPath();
    for ( String fixture : LOGGING_FIXTURES ) {
      try ( InputStream input = getClass().getResourceAsStream( "logging/" + fixture ) ) {
        assertNotNull( "Missing Kitchen logging fixture: " + fixture, input );
        Files.copy( input, directory.resolve( fixture ) );
      }
    }

    Path logfile = directory.resolve( "kitchen.log" );
    runKitchen( directory.resolve( LOGGING_JOB ), logfile );
    assertTrue( "Kitchen did not create its logfile at " + level.getCode(), Files.isRegularFile( logfile ) );
    String log = Files.readString( logfile, Charset.defaultCharset() );
    assertLogMarker( log, ERROR_MARKER, true );

    if ( level == LogLevel.ERROR ) {
      assertTrue( "Unexpected output at Error level:\n" + log, log.lines().allMatch( line ->
        line.contains( ERROR_MARKER )
          || line.endsWith( " - General - Logging plugin type found with ID: CheckpointLogTable" )
          || line.contains( " - Carte - Installing timer to purge stale objects after " ) ) );
      return;
    }

    String firstKitchenLine = log.lines().filter( line -> line.contains( " - Kitchen - " ) ).findFirst().orElse( "" );
    assertTrue( "Wrong level heading:\n" + log,
      firstKitchenLine.endsWith( " - Kitchen - Logging is at level : " + level.getDescription() ) );
    assertLogMarker( log, "Starting entry [write csv_delimiter]", true );
    assertLogMarker( log, "I found 5 different steps to launch.", true );
    assertLogMarker( log, "Kitchen - Allocate new job.", level != LogLevel.DETAILED );
    assertLogMarker( log, "sort by state.0 - Read row:", level == LogLevel.ROWLEVEL );
  }

  private void runKitchen( Path job, Path logfile ) throws Exception {
    KitchenCommandExecutor previousExecutor = Kitchen.getCommandExecutor();
    LanguageChoice language = LanguageChoice.getInstance();
    Locale previousLanguage = language.getDefaultLocale();
    Locale previousLocale = Locale.getDefault();
    Locale previousDisplay = Locale.getDefault( Locale.Category.DISPLAY );
    Locale previousFormat = Locale.getDefault( Locale.Category.FORMAT );
    SecurityManager previousSecurityManager = System.getSecurityManager();
    ExitBlockingSecurityManager securityManager = new ExitBlockingSecurityManager( previousSecurityManager );
    Kitchen.setCommandExecutor( null );
    language.setDefaultLocale( Locale.US );
    System.setSecurityManager( securityManager );
    try {
      Kitchen.main( new String[] { "-file", job.toString(), "-level", level.getCode(),
        "-logfile", logfile.toString(), "-norep" } );
    } catch ( SecurityException ignored ) {
      // Expected: System.exit is blocked so the test JVM stays alive.
    } finally {
      System.setSecurityManager( previousSecurityManager );
      Kitchen.setCommandExecutor( previousExecutor );
      language.setDefaultLocale( previousLanguage );
      Locale.setDefault( previousLocale );
      Locale.setDefault( Locale.Category.DISPLAY, previousDisplay );
      Locale.setDefault( Locale.Category.FORMAT, previousFormat );
    }
    assertEquals( List.of( CommandExecutorCodes.Kitchen.SUCCESS.getCode() ), securityManager.exitStatuses );
  }

  private static void assertLogMarker( String log, String marker, boolean expected ) {
    assertEquals( "Log marker mismatch for [" + marker + "]:\n" + log, expected, log.contains( marker ) );
  }

  private static final class ExitBlockingSecurityManager extends SecurityManager {
    private final SecurityManager delegate;
    private final List<Integer> exitStatuses = new ArrayList<>();

    private ExitBlockingSecurityManager( SecurityManager delegate ) {
      this.delegate = delegate;
    }

    @Override
    public void checkExit( int status ) {
      exitStatuses.add( status );
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
