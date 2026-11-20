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

package org.pentaho.di.docker;

import java.io.IOException;
import java.util.List;

/**
 * Base class of the tests. It starts Carte and the Pentaho Server the first time a test needs them, and keeps them
 * up for the rest of the run. The containers themselves start in the Maven build (fabric8).
 */
abstract class PdiIT {

  private static final String DATABASE_PASSWORD = "password";
  private static final String SERVER_USER = "admin";
  private static final String SERVER_PASSWORD = "password";
  private static final String CARTE_USER = "cluster";
  private static final String CARTE_PASSWORD = "cluster";

  /**
   * Scripts of the Pentaho Server image in data/postgresql, each for one database and its owner.
   */
  private record SchemaScript( String file, String user, String database ) {
  }

  private static final List<SchemaScript> SCHEMA = List.of(
    new SchemaScript( "create_repository_postgresql.sql", "hibuser", "hibernate" ),
    new SchemaScript( "create_quartz_postgresql.sql", "pentaho_user", "quartz" ),
    new SchemaScript( "create_jcr_postgresql.sql", "jcr_user", "jackrabbit" ) );

  private static final Object LOCK = new Object();
  private static CarteRunner carte;
  private static CarteRunner server;

  /**
   * Runs one KTR in one mode.
   */
  static ExecutionResult execute( ExecutionType type, KtrCase ktr ) throws Exception {
    return runner( type ).run( ktr, ktr.load() );
  }

  private static KtrRunner runner( ExecutionType type ) throws Exception {
    synchronized ( LOCK ) {
      return switch ( type ) {
        case PAN -> new PanRunner( ItConfig.pdiContainerId() );
        case CARTE -> carteRunner();
        case SERVER -> serverRunner();
      };
    }
  }

  private static CarteRunner carteRunner() throws Exception {
    if ( carte == null ) {
      DockerCli.execDetached( ItConfig.pdiContainerId(),
        "sh", "-c", "cd " + ItConfig.pdiHome() + " && ./carte.sh 0.0.0.0 8080" );
      CarteRunner runner = new CarteRunner( "http://localhost:" + ItConfig.carteHostPort() + "/kettle/",
        CARTE_USER, CARTE_PASSWORD );
      if ( !runner.awaitReady( ItConfig.startupTimeout() ) ) {
        throw new IllegalStateException( "Carte did not start in " + ItConfig.startupTimeout() );
      }
      carte = runner;
    }
    return carte;
  }

  private static CarteRunner serverRunner() throws Exception {
    if ( server == null ) {
      applySchema();
      DockerCli.execDetached( ItConfig.serverContainerId(),
        "sh", "-c", "cd " + ItConfig.serverHome() + " && ./start-pentaho.sh" );
      CarteRunner runner = new CarteRunner( "http://localhost:" + ItConfig.serverHostPort() + "/pentaho/kettle/",
        SERVER_USER, SERVER_PASSWORD );
      if ( !runner.awaitReady( ItConfig.serverStartupTimeout() ) ) {
        throw new IllegalStateException( "Pentaho Server did not start in " + ItConfig.serverStartupTimeout() );
      }
      server = runner;
    }
    return server;
  }

  /**
   * Waits until the database init of the Postgres image ends. Then it runs the schema scripts of the Server image.
   */
  private static void applySchema() throws Exception {
    String postgres = ItConfig.postgresContainerId();
    awaitDatabases( postgres );
    for ( SchemaScript script : SCHEMA ) {
      DockerCli.Result sql = DockerCli.exec( ItConfig.serverContainerId(),
        "cat", ItConfig.serverHome() + "/data/postgresql/" + script.file() );
      if ( sql.exitCode() != 0 ) {
        throw new IOException( "Cannot read " + script.file() + ": " + sql.text() );
      }
      // The scripts can report "already exists" for objects that the Server created first. The Server start checks the result.
      DockerCli.exec( postgres, sql.output(),
        "env", "PGPASSWORD=" + DATABASE_PASSWORD, "psql", "-q", "-U", script.user(), "-d", script.database() );
    }
  }

  private static void awaitDatabases( String postgres ) throws Exception {
    long deadline = System.nanoTime() + ItConfig.startupTimeout().toNanos();
    while ( System.nanoTime() < deadline ) {
      DockerCli.Result result = DockerCli.exec( postgres,
        "psql", "-U", "postgres", "-d", "testdb", "-tA", "-c", "select count(*) from pg_database where datname = 'quartz'" );
      if ( result.exitCode() == 0 && "1".equals( result.text().trim() ) ) {
        return;
      }
      Thread.sleep( 1000 );
    }
    throw new IllegalStateException( "Postgres did not create the Pentaho databases in " + ItConfig.startupTimeout() );
  }
}
