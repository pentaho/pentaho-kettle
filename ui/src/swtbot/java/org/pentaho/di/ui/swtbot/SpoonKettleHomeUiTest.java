package org.pentaho.di.ui.swtbot;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationService;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.ui.spoon.Spoon;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Feature: Spoon keeps the user's settings under KETTLE_HOME
 * (see ui/src/test/resources/features/kettle-home.feature).
 * <p>
 * run-swtbot-tests.sh starts Spoon with KETTLE_HOME and user.home set to two different, empty folders, so a file
 * written to the wrong one is visible. The "is saved" steps call the same manager that the Database Connection and
 * Run Configuration dialogs call when OK is clicked; those dialogs are not automated yet.
 */
public class SpoonKettleHomeUiTest {
  private static final String DATABASE_NAME = "swtbot-kettle-home-db";
  private static final String RUN_CONFIGURATION_NAME = "swtbot-kettle-home-run";
  /** Skip native libraries and other large files when searching for a name. */
  private static final long MAX_SEARCHED_FILE_SIZE = 1024L * 1024L;

  private Spoon spoon;
  private Path kettleHome;
  private Path userHome;

  @Before
  public void kettleHomeAndUserHomeAreDifferentFolders() throws InterruptedException {
    // Given KETTLE_HOME and the user's home folder are different, empty folders
    String kettleHomeSetting = System.getProperty( "KETTLE_HOME" );
    assertNotNull( "KETTLE_HOME is not set; run the tests with run-swtbot-tests.sh", kettleHomeSetting );
    kettleHome = Paths.get( kettleHomeSetting ).toAbsolutePath().normalize();
    userHome = Paths.get( System.getProperty( "user.home" ) ).toAbsolutePath().normalize();
    assertNotEquals( "KETTLE_HOME and user.home must be different folders", kettleHome, userHome );
    assertFalse( "KETTLE_HOME must not be inside user.home", kettleHome.startsWith( userHome ) );
    assertFalse( "user.home must not be inside KETTLE_HOME", userHome.startsWith( kettleHome ) );

    // And Spoon has been started with no repository connected
    spoon = SpoonUi.awaitMainShell();
    Result<Boolean> connected = () -> spoon.getRepository() != null;
    assertFalse( "a repository is connected", SpoonUi.onUiThread( spoon, connected ) );
  }

  @After
  public void removeSavedSettings() {
    Result<Boolean> remove = () -> {
      try {
        DatabaseManagementInterface databases = globalDatabases();
        if ( databases.get( DATABASE_NAME ) != null ) {
          databases.remove( DATABASE_NAME );
        }
        globalRunConfigurations().delete( RUN_CONFIGURATION_NAME );
      } catch ( KettleException e ) {
        throw new IllegalStateException( e );
      }
      return true;
    };
    SpoonUi.onUiThread( spoon, remove );
  }

  @Test
  public void kettlePropertiesIsCreatedUnderKettleHome() {
    // Then "kettle.properties" exists in the ".kettle" folder under KETTLE_HOME
    Path kettleProperties = kettleHome.resolve( ".kettle" ).resolve( "kettle.properties" );
    assertTrue( kettleProperties + " does not exist", Files.isRegularFile( kettleProperties ) );
    // And the user's home folder has no ".kettle" folder
    assertFalse( userHome.resolve( ".kettle" ) + " exists", Files.exists( userHome.resolve( ".kettle" ) ) );
  }

  @Test
  public void aNewGlobalDatabaseConnectionIsSavedUnderKettleHome() {
    // When a global database connection named "swtbot-kettle-home-db" is saved
    Result<Boolean> save = () -> {
      try {
        globalDatabases().add( new DatabaseMeta( DATABASE_NAME, "H2", "Native", null, "swtbot", null, null, null ) );
      } catch ( KettleException e ) {
        throw new IllegalStateException( e );
      }
      return true;
    };
    SpoonUi.onUiThread( spoon, save );

    assertSavedUnderKettleHomeOnly( DATABASE_NAME );
  }

  /**
   * Known issue PDI-18465 / PDI-19945 (@known-issue in the feature): run configurations, like everything else in the
   * metastore, are saved under user.home/.pentaho/metastore because MetaStoreConst.getDefaultPentahoMetaStoreLocation()
   * uses user.home and only the PENTAHO_METASTORE_FOLDER system property overrides it; KETTLE_HOME is never used.
   * Remove @Ignore to reproduce.
   */
  @Ignore( "Known issue PDI-18465 / PDI-19945: the metastore ignores KETTLE_HOME (uses user.home/.pentaho)" )
  @Test
  public void aNewGlobalRunConfigurationIsSavedUnderKettleHome() {
    // When a global run configuration named "swtbot-kettle-home-run" is saved
    Result<Boolean> save = () -> {
      RunConfigurationService runConfigurations = globalRunConfigurations();
      RunConfiguration runConfiguration = runConfigurations.getRunConfigurationByType( "Pentaho" );
      runConfiguration.setName( RUN_CONFIGURATION_NAME );
      return runConfigurations.save( runConfiguration );
    };
    assertTrue( "the run configuration was not saved", SpoonUi.onUiThread( spoon, save ) );

    assertSavedUnderKettleHomeOnly( RUN_CONFIGURATION_NAME );
  }

  private void assertSavedUnderKettleHomeOnly( String name ) {
    SpoonUi.waitFor( "'" + name + "' to be written under KETTLE_HOME (" + kettleHome + ") or user.home (" + userHome
        + ")", () -> !filesMentioning( kettleHome, name ).isEmpty() || !filesMentioning( userHome, name ).isEmpty() );
    List<String> inUserHome = filesMentioning( userHome, name );
    // Then a file under KETTLE_HOME mentions <name>
    assertFalse( "no file under KETTLE_HOME (" + kettleHome + ") mentions '" + name + "'; it was written to user.home ("
      + userHome + "): " + inUserHome, filesMentioning( kettleHome, name ).isEmpty() );
    // And no file in the user's home folder mentions <name>
    assertEquals( "files in user.home (" + userHome + ") that mention '" + name + "'", List.of(), inUserHome );
  }

  private DatabaseManagementInterface globalDatabases() throws KettleException {
    return spoon.getGlobalManagementBowl().getManager( DatabaseManagementInterface.class );
  }

  private RunConfigurationService globalRunConfigurations() {
    try {
      return spoon.getGlobalManagementBowl().getManager( RunConfigurationService.class );
    } catch ( KettleException e ) {
      throw new IllegalStateException( e );
    }
  }

  /** Files under root whose path or content contains text, relative to root. */
  private static List<String> filesMentioning( Path root, String text ) {
    if ( !Files.isDirectory( root ) ) {
      return List.of();
    }
    try ( Stream<Path> files = Files.walk( root ) ) {
      return files.filter( Files::isRegularFile )
        .filter( file -> file.toString().contains( text ) || contentContains( file, text ) )
        .map( file -> root.relativize( file ).toString() )
        .sorted()
        .collect( Collectors.toList() );
    } catch ( IOException e ) {
      throw new UncheckedIOException( e );
    }
  }

  private static boolean contentContains( Path file, String text ) {
    try {
      return Files.size( file ) <= MAX_SEARCHED_FILE_SIZE
        && new String( Files.readAllBytes( file ), StandardCharsets.UTF_8 ).contains( text );
    } catch ( IOException e ) {
      return false;
    }
  }
}
