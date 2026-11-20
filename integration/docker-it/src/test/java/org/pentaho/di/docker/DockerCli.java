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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Runs commands in the containers that fabric8 started. It uses the docker CLI of the host.
 */
final class DockerCli {

  /**
   * @param exitCode exit code of the command.
   * @param output   stdout and stderr, merged.
   */
  record Result( int exitCode, byte[] output ) {

    String text() {
      return new String( output, StandardCharsets.UTF_8 );
    }
  }

  private DockerCli() {
  }

  static Result exec( String container, String... command ) throws IOException, InterruptedException {
    return exec( container, null, command );
  }

  /**
   * @param stdin bytes for the command input, or null for no input.
   */
  static Result exec( String container, byte[] stdin, String... command ) throws IOException, InterruptedException {
    List<String> args = new ArrayList<>( List.of( "docker", "exec" ) );
    if ( stdin != null ) {
      args.add( "-i" );
    }
    args.add( container );
    args.addAll( List.of( command ) );
    Process process = new ProcessBuilder( args ).redirectErrorStream( true ).start();
    CompletableFuture<Void> writer = CompletableFuture.runAsync( () -> writeAndClose( process, stdin ) );
    byte[] output = process.getInputStream().readAllBytes();
    int exitCode = process.waitFor();
    writer.join();
    return new Result( exitCode, output );
  }

  /**
   * Starts a command in the background of the container. It returns at once.
   */
  static void execDetached( String container, String... command ) throws IOException, InterruptedException {
    List<String> args = new ArrayList<>( List.of( "docker", "exec", "-d", container ) );
    args.addAll( List.of( command ) );
    Process process = new ProcessBuilder( args ).redirectErrorStream( true ).start();
    process.getInputStream().readAllBytes();
    if ( process.waitFor() != 0 ) {
      throw new IOException( "docker exec -d failed for " + container );
    }
  }

  /**
   * Writes a file in the container. The parent folder is created when it is missing.
   */
  static void writeFile( String container, String path, byte[] content ) throws IOException, InterruptedException {
    String parent = path.substring( 0, path.lastIndexOf( '/' ) );
    Result result = exec( container, content, "sh", "-c", "mkdir -p " + parent + " && cat > " + path );
    if ( result.exitCode() != 0 ) {
      throw new IOException( "Cannot write " + path + " in " + container + ": " + result.text() );
    }
  }

  private static void writeAndClose( Process process, byte[] stdin ) {
    try ( OutputStream out = process.getOutputStream() ) {
      if ( stdin != null ) {
        out.write( stdin );
      }
    } catch ( IOException ignored ) {
      // The command ended before it read all input. The exit code shows the error.
    }
  }
}
