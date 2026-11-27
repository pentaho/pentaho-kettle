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

package org.pentaho.di.www;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.JUnitCore;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Runs Carte in a separate JVM with a test-owned Kettle home, even when KETTLE_HOME is set by the caller.
 */
public class ExecuteFileSchemesIT {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /**
   * Isolates Kettle initialization from the caller's environment and verifies that the worker ran every scenario.
   * Worker output is captured so a startup failure, timeout, or test failure includes its diagnostics.
   */
  @Test
  public void executeApisRespectConfiguredSchemes() throws Exception {
    File home = temporaryFolder.newFolder( "home" );
    File output = temporaryFolder.newFile( "worker.log" );
    String executable = System.getProperty( "os.name" ).startsWith( "Windows" ) ? "java.exe" : "java";
    Path java = Path.of( System.getProperty( "java.home" ), "bin", executable );
    Path fixtures = Path.of( "src", "test", "resources", "org", "pentaho", "di" ).toAbsolutePath();
    ProcessBuilder builder = new ProcessBuilder( java.toString(), "-Dfile.encoding=UTF-8",
      "-DPENTAHO_METASTORE_FOLDER=" + home.getAbsolutePath(),
      "-Dexecute.it.fixtures=" + fixtures,
      "-cp", System.getProperty( "java.class.path" ), JUnitCore.class.getName(),
      ExecuteFileSchemesWorker.class.getName() );
    builder.environment().put( "KETTLE_HOME", home.getAbsolutePath() );
    builder.directory( home );
    builder.redirectErrorStream( true );
    builder.redirectOutput( output );

    Process worker = builder.start();
    try {
      boolean finished = worker.waitFor( 120, TimeUnit.SECONDS );
      if ( !finished ) {
        worker.destroyForcibly();
        worker.waitFor();
      }
      String log = Files.readString( output.toPath(), StandardCharsets.UTF_8 );
      assertTrue( "Carte worker timed out:\n" + log, finished );
      assertEquals( "Carte worker failed:\n" + log, 0, worker.exitValue() );
      assertTrue( "Expected all ten configuration cases to execute:\n" + log, log.contains( "OK (10 tests)" ) );
    } finally {
      if ( worker.isAlive() ) {
        worker.destroyForcibly();
        worker.waitFor();
      }
    }
  }
}
