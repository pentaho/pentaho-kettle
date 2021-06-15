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

import org.eclipse.rap.rwt.internal.theme.StyleSheetBuilder;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;


public class DocumentHandlerImpl implements DocumentHandler {

  private String uri;
  private final CssFileReader reader;
  private final ResourceLoader loader;
  private final StyleSheetBuilder styleSheetBuilder;
  private PropertyResolver propertyResolver;

  public DocumentHandlerImpl( CssFileReader reader, ResourceLoader loader ) {
    this.reader = reader;
    this.loader = loader;
    styleSheetBuilder = new StyleSheetBuilder();
  }

  @Override
  public void startDocument( InputSource source ) throws CSSException {
    uri = source.getURI();
    log( "=== startDocument " + uri + "===" );
  }

  @Override
  public void endDocument( InputSource source ) throws CSSException {
    log( "___ endDocument ___" );
  }

  @Override
  public void startSelector( SelectorList selectors ) throws CSSException {
    log( "startSelector " + toString( selectors ) );
    propertyResolver = new PropertyResolver();
  }

  @Override
  public void endSelector( SelectorList selectors ) throws CSSException {
    log( "endSelector " + toString( selectors ) );
    StyleRule styleRule = new StyleRule( selectors, propertyResolver.getResolvedProperties() );
    styleSheetBuilder.addStyleRule( styleRule );
    propertyResolver = null;
  }

  @Override
  public void property( String name, LexicalUnit value, boolean important ) throws CSSException {
    log( "  property "
         + name
         + " := "
         + PropertyResolver.toString( value )
         + ( important? " !" : "" ) );
    if( important ) {
      reader.addProblem( new CSSException( "Important rules not supported - ignored" ) );
    }
    if( propertyResolver != null ) {
      try {
        propertyResolver.resolveProperty( name, value, loader );
      } catch( IllegalArgumentException exception ) {
        reader.addProblem( new CSSException( "Failed to read property "
                                             + name
                                             + ": "
                                             + exception.getMessage() ) );
      }
    }
  }

  // -- ignored --
  @Override
  public void comment( String text ) throws CSSException {
    log( "    /*" + text + "*/" );
  }

  // -- unsupported --
  @Override
  public void importStyle( String uri, SACMediaList media, String defaultNamespaceURI )
    throws CSSException
  {
    log( "importStyle " + uri + ", " + media + ", " + defaultNamespaceURI );
    reader.addProblem( new CSSException( "import rules not supported - ignored" ) );
  }

  @Override
  public void namespaceDeclaration( String prefix, String uri ) throws CSSException {
    log( "namespaceDeclaration " + prefix + ", " + uri );
    reader.addProblem( new CSSException( "unsupported namespace declaration '"
                                         + prefix
                                         + ":"
                                         + uri
                                         + "' - ignored" ) );
  }

  @Override
  public void ignorableAtRule( String atRule ) throws CSSException {
    log( "ignorableAtRule " + atRule );
    reader.addProblem( new CSSException( "unsupported at rule '" + atRule + "' - ignored" ) );
  }

  @Override
  public void startPage( String name, String pseudo_page ) throws CSSException {
    log( "startPage " + name + ", " + pseudo_page );
    reader.addProblem( new CSSException( "page rules not supported - ignored" ) );
  }

  @Override
  public void endPage( String name, String pseudo_page ) throws CSSException {
    log( "endPage " + name + ", " + pseudo_page );
  }

  @Override
  public void startMedia( SACMediaList media ) throws CSSException {
    log( "startMedia " + media );
    reader.addProblem( new CSSException( "media rules not supported - ignored" ) );
  }

  @Override
  public void endMedia( SACMediaList media ) throws CSSException {
    log( "endMedia " + media );
  }

  @Override
  public void startFontFace() throws CSSException {
    log( "startFontFace" );
    reader.addProblem( new CSSException( "font face rules not supported - ignored" ) );
  }

  @Override
  public void endFontFace() throws CSSException {
    log( "end FontFace" );
  }

  public StyleSheet getStyleSheet() {
    return styleSheetBuilder.getStyleSheet();
  }

  @SuppressWarnings( "unused" )
  private void log( String message ) {
//    System.out.println( message );
  }

  private static String toString( SelectorList patterns ) {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "[" );
    int length = patterns.getLength();
    for( int i = 0; i < length; i++ ) {
      buffer.append( " " );
      Selector selector = patterns.item( i );
      buffer.append( selector.toString() );
    }
    buffer.append( " ]" );
    return buffer.toString();
  }

}
