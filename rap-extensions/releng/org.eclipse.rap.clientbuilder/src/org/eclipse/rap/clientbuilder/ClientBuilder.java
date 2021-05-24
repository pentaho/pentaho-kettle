/*******************************************************************************
 * Copyright (c) 2010, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import static org.eclipse.rap.clientbuilder.InputListReader.getInputFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Usage: JSCompressor [--input-path <path>] --input-list <file> --output-file <file>
 *
 *   --input-list <file>
 *       file that includes the names of all input files, one per line
 *
 *   --input-path <path>
 *       path to the directory that contains the input files
 *
 *   --output-file <file>
 *       compressed javascript file
 *
 *   --no-compress
 *       concatenate only, do not compress
 */
public class ClientBuilder {

  private static final String OPT_INPUT_PATH = "--input-path";
  private static final String OPT_INPUT_LIST = "--input-list";
  private static final String OPT_OUTPUT_FILE = "--output-file";
  private static final String OPT_NO_COMPRESS = "--no-compress";

  private static final boolean CREATE_DEBUG_FILES
    = "true".equals( System.getProperty( "jscompressor.debug" ) );
  private final DebugFileWriter debugFileWriter;

  public static void main( String[] args ) {
    List<File> inputPaths = new ArrayList<>();
    File inputListFile = null;
    File outputFile = null;
    boolean compress = true;
    String last = null;
    for( int i = 0; i < args.length; i++ ) {
      String arg = args[ i ];
      if( OPT_NO_COMPRESS.equals( arg ) ) {
        compress = false;
      } else if( OPT_INPUT_PATH.equals( last ) ) {
        inputPaths.add( new File( arg ) );
      } else if( OPT_INPUT_LIST.equals( last ) ) {
        inputListFile = new File( arg );
      } else if( OPT_OUTPUT_FILE.equals( last ) ) {
        outputFile = new File( arg );
      } else if( !isValidOption( arg ) ) {
        System.err.println( "Illegal parameter: " + arg );
      }
      last = arg;
    }
    if( inputListFile == null ) {
      System.err.println( "Input list file missing, use parameter " + OPT_INPUT_LIST );
    } else if( outputFile == null ) {
      System.err.println( "Output file missing, use parameter " + OPT_OUTPUT_FILE );
    } else if( !inputListFile.exists() ) {
      System.err.println( "Input list file not found: " + inputListFile.getAbsolutePath() );
    } else {
      List<JSFile> inputFiles = getInputFiles( inputListFile, inputPaths );
      ClientBuilder builder = new ClientBuilder();
      builder.build( inputFiles, outputFile, compress );
    }
  }

  public ClientBuilder() {
    debugFileWriter = createDebugFileWriter();
  }

  public void build( List<JSFile> inputFiles, File outputFile, boolean compress ) {
    try {
      long start = System.currentTimeMillis();
      String compressed = build( inputFiles, compress );
      long time = System.currentTimeMillis() - start;
      JSFile.writeToFile( outputFile, compressed );
      int count = inputFiles.size();
      System.out.println( "Compressed " + count + " files in " + time + " ms" );
      System.out.println( "Result size: " + compressed.length() + " bytes" );
    } catch( IOException e ) {
      throw new RuntimeException( "Failed to compress Javascript files", e );
    }
  }

  private String build( List<JSFile> inputFiles, boolean compress  ) throws IOException {
    StringReplacer stringReplacer = new StringReplacer();
    for( JSFile inputFile : inputFiles ) {
      stringReplacer.discoverStrings( inputFile.getTokens() );
    }
    stringReplacer.optimize();
    StringBuilder buffer = new StringBuilder();
    buffer.append( "(function(_){" );
    for( JSFile inputFile : inputFiles ) {
      if( compress ) {
        stringReplacer.replaceStrings( inputFile.getTokens() );
      }
      String result = compress ? inputFile.compress( debugFileWriter ) : inputFile.getContent();
      buffer.append( result );
      buffer.append( "\n" );
      System.out.println( inputFile.getFile().getAbsolutePath()
                          + "\t"
                          + result.length() );
    }
    buffer.append( "})(");
    String[] strings = stringReplacer.getStrings();
    String stringArrayCode = createStringArray( strings );
    System.out.println( "Replaced " + strings.length + " strings" );
    buffer.append( stringArrayCode );
    buffer.append( ");" );
    return buffer.toString();
  }

  private static boolean isValidOption( String arg ) {
    return    OPT_INPUT_PATH.equals( arg )
           || OPT_INPUT_LIST.equals( arg )
           || OPT_OUTPUT_FILE.equals( arg )
           || OPT_NO_COMPRESS.equals( arg );
  }

  private static DebugFileWriter createDebugFileWriter() {
    File debugDir = null;
    if( CREATE_DEBUG_FILES ) {
      try {
        debugDir = createTempDir();
        System.out.println( "Creating debug files in " + debugDir );
      } catch( IOException exception ) {
        exception.printStackTrace();
      }
    }
    return new DebugFileWriter( debugDir );
  }

  private static File createTempDir() throws IOException {
    File tmpDir = File.createTempFile( "jscompressor-", "" );
    tmpDir.delete();
    tmpDir.mkdir();
    return tmpDir;
  }

  private static String createStringArray( String[] strings ) {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "[" );
    for( int i = 0; i < strings.length; i++ ) {
      String string = strings[ i ];
      buffer.append( "\"" + JavaScriptPrinter.escapeString( string ) + "\"," );
    }
    if( buffer.charAt( buffer.length() - 1 ) == ',' ) {
      buffer.setLength( buffer.length() - 1 );
    }
    buffer.append( "]" );
    return buffer.toString();
  }

}
