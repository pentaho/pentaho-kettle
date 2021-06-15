/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class ClientFilesReader {

  private static final String CHARSET = "UTF-8";

  private final String resourceName;
  private final ArrayList<String> files;

  public static final List<String> getInputFiles( String resourceName ) {
    ClientFilesReader reader = new ClientFilesReader( resourceName );
    try {
      reader.read();
    } catch( IOException exception ) {
      String message = "Failed to read input list from " + resourceName;
      throw new RuntimeException( message, exception );
    }
    return reader.getFiles();
  }

  ClientFilesReader( String resourceName ) {
    this.resourceName = resourceName;
    files = new ArrayList<>();
  }

  InputStream openInputStream() throws IOException {
    ClassLoader classLoader = ClientFilesReader.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream( resourceName );
    if( inputStream == null ) {
      throw new IOException( "Resource not found: " + resourceName );
    }
    return inputStream;
  }

  void read() throws IOException {
    InputStream inputStream = openInputStream();
    try {
      readLines( inputStream );
    } finally {
      inputStream.close();
    }
  }

  List<String> getFiles() {
    return files;
  }

  private void readLines( InputStream inputStream ) throws IOException {
    BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, CHARSET ) );
    String line = reader.readLine();
    while( line != null ) {
      readLine( line );
      line = reader.readLine();
    }
  }

  private void readLine( String line ) {
    String text = line.trim();
    if( text.length() > 0 && !text.startsWith( "#" ) ) {
      files.add( text );
    }
  }

}
