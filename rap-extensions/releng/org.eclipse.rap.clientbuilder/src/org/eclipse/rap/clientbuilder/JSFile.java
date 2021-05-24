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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;


public class JSFile {

  private static final String CHARSET = "UTF-8";
  private static final boolean PRESERVE_ALL_SEMICOLONS = false;
  private static final boolean DISABLE_OPTIMIZATIONS = false;
  private static final boolean VERBOSE = false;
  private static final ErrorReporter REPORTER = new SystemErrorReporter();

  private final File file;
  private final JavaScriptCompressor compressor;
  private final TokenList tokens;

  public JSFile( File file ) throws IOException {
    this.file = file;
    InputStream inputStream = new FileInputStream( file );
    Reader inputReader = new InputStreamReader( inputStream, CHARSET );
    try {
      compressor = new JavaScriptCompressor( inputReader, REPORTER );
    } finally {
      inputReader.close();
    }
    tokens = new TokenList( compressor.getTokens() );
  }

  public File getFile() {
    return file;
  }

  public String getContent() {
    try {
      return readLines( new FileInputStream( file ) );
    } catch( IOException exception ) {
      throw new RuntimeException( exception );
    }
  }

  public TokenList getTokens() {
    return tokens;
  }

  public String compress( DebugFileWriter debugFileWriter ) throws IOException {
    String fileName = file.getName();
    debugFileWriter.beforeCleanup( tokens, fileName );
    QxCodeCleaner codeCleaner = new QxCodeCleaner( tokens );
    codeCleaner.cleanupQxCode();
    debugFileWriter.afterCleanup( tokens, fileName );
    return compress();
  }

  private String compress() throws IOException {
    StringWriter writer = new StringWriter();
    compressor.compress( writer, -1, true, VERBOSE, PRESERVE_ALL_SEMICOLONS, DISABLE_OPTIMIZATIONS );
    writer.flush();
    return writer.getBuffer().toString();
  }

  public static void writeToFile( File outputFile, String compressed )
    throws IOException
  {
    FileOutputStream outputStream = new FileOutputStream( outputFile );
    Writer outputWriter = new OutputStreamWriter( outputStream, CHARSET );
    try {
      outputWriter.write( compressed );
    } finally {
      outputWriter.close();
    }
  }

  private static String readLines( InputStream inputStream ) throws IOException {
    StringBuilder builder = new StringBuilder();
    BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream, CHARSET ) );
    String line = reader.readLine();
    while( line != null ) {
      builder.append( line );
      builder.append( '\n' );
      line = reader.readLine();
    }
    return builder.toString();
  }

  private static final class SystemErrorReporter implements ErrorReporter {

    @Override
    public void warning( String message,
                         String sourceName,
                         int line,
                         String lineSource,
                         int lineOffset )
    {
      System.out.println( getMessage( "WARNING", message ) );
    }

    @Override
    public void error( String message,
                       String sourceName,
                       int line,
                       String lineSource,
                       int lineOffset )
    {
      System.out.println( getMessage( "ERROR", message ) );
    }

    @Override
    public EvaluatorException runtimeError( String message,
                                            String sourceName,
                                            int line,
                                            String lineSource,
                                            int lineOffset )
    {
      error( message, sourceName, line, lineSource, lineOffset );
      return new EvaluatorException( message );
    }

    private static String getMessage( String severity, String message ) {
      StringBuilder result = new StringBuilder();
      result.append( "\n[" );
      result.append( severity );
      result.append( "] " );
      result.append( message );
      return result.toString();
    }

  }

}
