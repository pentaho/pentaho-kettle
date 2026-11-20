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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * One KTR file that runs as a test case.
 *
 * @param id            name of the case. It is unique in its group.
 * @param file          KTR file in the test resources.
 * @param sshEndpoints  true when the KTR must use the SSH and proxy services of the test network.
 * @param expectFailure true when the KTR must fail. A file named like the KTR with suffix {@code .fail.txt} marks it.
 */
record KtrCase( String id, Path file, boolean sshEndpoints, boolean expectFailure ) {

  byte[] load() throws IOException {
    byte[] xml = Files.readAllBytes( file );
    return sshEndpoints ? SshKtrRewriter.rewrite( xml, SshKtrRewriter.KEY_IN_CONTAINER ) : xml;
  }

  /**
   * Name without a path separator. Carte and file names cannot use the separator.
   */
  String flatId() {
    return id.replace( '/', '_' );
  }

  @Override
  public String toString() {
    return id;
  }

  static List<KtrCase> basic() throws IOException, URISyntaxException {
    Path root = resourceDir( "ktr" );
    try ( Stream<Path> files = Files.walk( root ) ) {
      return files
        .filter( path -> path.getFileName().toString().endsWith( ".ktr" ) )
        .sorted()
        .map( path -> {
          String name = path.getFileName().toString();
          String id = root.relativize( path ).toString().replace( '\\', '/' ).replaceAll( "\\.ktr$", "" );
          boolean expectFailure = Files.exists( path.resolveSibling( name.replaceAll( "\\.ktr$", ".fail.txt" ) ) );
          return new KtrCase( id, path, false, expectFailure );
        } )
        .toList();
    }
  }

  static List<KtrCase> ssh() throws IOException, URISyntaxException {
    Path root = resourceDir( "ssh" );
    try ( Stream<Path> files = Files.list( root ) ) {
      return files
        .filter( path -> {
          String name = path.getFileName().toString();
          return name.startsWith( "Test_SSH_Command-" ) && name.endsWith( ".ktr" );
        } )
        .sorted()
        .map( path -> new KtrCase(
          path.getFileName().toString().replaceAll( "\\.ktr$", "" ), path, true, false ) )
        .toList();
    }
  }

  private static Path resourceDir( String name ) throws URISyntaxException {
    return Path.of( KtrCase.class.getClassLoader().getResource( name ).toURI() );
  }
}
