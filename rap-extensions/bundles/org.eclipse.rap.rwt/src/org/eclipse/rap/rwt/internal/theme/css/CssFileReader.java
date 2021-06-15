/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.apache.batik.css.parser.ParseException;
import org.eclipse.rap.rwt.internal.theme.ThemeManagerException;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Parser;


public class CssFileReader {

  private static final String CSS_ENCODING = "UTF-8";

  private final List<CSSException> problems;
  private final Parser parser;

  CssFileReader() {
    try {
      parser = new org.eclipse.rap.rwt.apache.batik.css.parser.Parser();
    } catch( Exception e ) {
      throw new RuntimeException( "Failed to instantiate CSS parser", e );
    }
    problems = new ArrayList<>();
  }

  /**
   * Reads a style sheet from a file. The loader is used to load the file and
   * resources referenced in the CSS.
   */
  public static StyleSheet readStyleSheet( String fileName, ResourceLoader loader )
    throws IOException
  {
    ParamCheck.notNull( fileName, "fileName" );
    InputStream inputStream = loader.getResourceAsStream( fileName );
    if( inputStream == null ) {
      throw new IllegalArgumentException( "Could not open resource " + fileName );
    }
    return parseStyleSheet( inputStream, fileName, loader );
  }

  /**
   * Reads a style sheet from an input stream. The fileName is only used for
   * error messages. The loader is used to load resources referenced in the CSS.
   */
  public static StyleSheet readStyleSheet( InputStream inputStream,
                                           String fileName,
                                           ResourceLoader loader ) throws IOException
  {
    ParamCheck.notNull( inputStream, "inputStream" );
    ParamCheck.notNull( fileName, "fileName" );
    return parseStyleSheet( inputStream, fileName, loader );
  }

  private static StyleSheet parseStyleSheet( InputStream inputStream,
                                             String fileName,
                                             ResourceLoader loader )
    throws IOException
  {
    StyleSheet styleSheet;
    try {
      CssFileReader reader = new CssFileReader();
      styleSheet = reader.parse( inputStream, fileName, loader );
    } catch( CSSException e ) {
      throw new ThemeManagerException( "Failed parsing CSS file", e );
    } finally {
      inputStream.close();
    }
    return styleSheet;
  }

  StyleSheet parse( InputStream inputStream, String uri, ResourceLoader loader )
    throws CSSException, IOException
  {
    InputSource source = new InputSource();
    source.setByteStream( inputStream );
    source.setEncoding( CSS_ENCODING );
    source.setURI( uri );
    parser.setConditionFactory( new ConditionFactoryImpl( this ) );
    parser.setSelectorFactory( new SelectorFactoryImpl( this ) );
    DocumentHandlerImpl documentHandler = new DocumentHandlerImpl( this, loader );
    parser.setDocumentHandler( documentHandler );
    parser.setErrorHandler( new ErrorHandlerImpl( this ) );
    // TODO [rst] Batik parser throws ParseException
    try {
      parser.parseStyleSheet( source );
    } catch( ParseException e ) {
      throw new CSSException( e );
    }
    return documentHandler.getStyleSheet();
  }

  CSSException[] getProblems() {
    CSSException[] result = new CSSException[ problems.size() ];
    problems.toArray( result );
    return result;
  }

  void addProblem( CSSException exception ) {
    // TODO [rst] Logging instead of sysout
    System.err.println( exception );
    problems.add( exception );
  }

  private static class ErrorHandlerImpl implements ErrorHandler {

    private final List<CSSException> problems;

    public ErrorHandlerImpl( CssFileReader reader ) {
      problems = reader.problems;
    }

    // TODO [rst] decent logging instead of sysout
    @Override
    public void warning( CSSParseException exception ) throws CSSException {
      String problem = createProblemDescription( "WARNING: ", exception );
      System.err.println( problem );
      problems.add( exception );
    }

    @Override
    public void error( CSSParseException exception ) throws CSSException {
      String problem = createProblemDescription( "ERROR: ", exception );
      System.err.println( problem );
      problems.add( exception );
    }

    @Override
    public void fatalError( CSSParseException exception ) throws CSSException {
      String problem = createProblemDescription( "FATAL ERROR: ", exception );
      System.err.println( problem );
      problems.add( exception );
      throw exception;
    }

    private static String createProblemDescription( String type, CSSParseException exception ) {
      String pattern = "{0}: {1} in {2} at pos [{3}:{4}]";
      Object[] arguments = {
        type,
        exception.getMessage(),
        exception.getURI(),
        String.valueOf( exception.getLineNumber() ),
        String.valueOf( exception.getColumnNumber() )
      };
      return MessageFormat.format( pattern, arguments );
    }
  }

}
