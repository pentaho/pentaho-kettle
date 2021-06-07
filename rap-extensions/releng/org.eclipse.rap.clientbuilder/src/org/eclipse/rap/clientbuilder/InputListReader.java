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
package org.eclipse.rap.clientbuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class InputListReader {

  private static final String CHARSET = "UTF-8";

  private final File inputFile;
  private final List<File> basePaths;
  private final ArrayList<JSFile> files;

  public static final List<JSFile> getInputFiles( File inputFile, List<File> basePaths ) {
    InputListReader reader = new InputListReader( inputFile, basePaths );
    try {
      reader.read();
    } catch( IOException exception ) {
      String message = "Failed to read input list file " + inputFile.getAbsolutePath();
      throw new RuntimeException( message, exception );
    }
    return reader.getFiles();
  }

  InputListReader( File inputFile, List<File> basePaths ) {
    this.inputFile = inputFile;
    this.basePaths = basePaths;
    files = new ArrayList<>();
  }

  void read() throws IOException {
    files.clear();
    InputStream inputStream = openInputStream();
    try {
      readLines( inputStream );
    } finally {
      inputStream.close();
    }
  }

  InputStream openInputStream() throws IOException {
    return new FileInputStream( inputFile );
  }

  ArrayList<JSFile> getFiles() {
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

  private void readLine( String line ) throws IOException {
    String text = line.trim();
    if( text.length() > 0 && !text.startsWith( "#" ) ) {
      addFile( text );
    }
  }

  private void addFile( String filename ) throws IOException {
    for( File path : basePaths ) {
      File file = new File( path, filename );
      if( file.exists() ) {
        files.add( new JSFile( file ) );
        break;
      }
    }
  }

}
