/*******************************************************************************
 * Copyright (c) 2010, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


final class DebugFileWriter {

  private final File directoryForDebugFiles;

  public DebugFileWriter( File parentDirectory ) {
    directoryForDebugFiles = parentDirectory;
  }

  public void beforeCleanup( TokenList tokens, String fileName ) {
    if( directoryForDebugFiles != null ) {
      String code = getCodeForDebugFile( tokens );
      createDebugFile( "orig", fileName, code );
    }
  }

  public void afterCleanup( TokenList tokens, String fileName ) {
    if( directoryForDebugFiles != null ) {
      String code = getCodeForDebugFile( tokens );
      createDebugFile( "clean", fileName, code );
    }
  }

  private void createDebugFile( String dirName, String fileName, String code ) {
    File subDir = new File( directoryForDebugFiles, dirName );
    subDir.mkdirs();
    File file = new File( subDir, fileName );
    try {
      writeToFile( code, file );
    } catch( IOException e ) {
      System.err.println( "Failed to write to file " + file.getAbsolutePath() );
      e.printStackTrace();
    }
  }

  private String getCodeForDebugFile( TokenList tokens ) {
    String code = null;
    if( directoryForDebugFiles != null ) {
      code = JavaScriptPrinter.printTokens( tokens );
    }
    return code;
  }

  private static void writeToFile( String Code, File file ) throws IOException {
    FileWriter fileWriter = new FileWriter( file );
    try {
      fileWriter.write( Code );
    } finally {
      fileWriter.close();
    }
  }

}
