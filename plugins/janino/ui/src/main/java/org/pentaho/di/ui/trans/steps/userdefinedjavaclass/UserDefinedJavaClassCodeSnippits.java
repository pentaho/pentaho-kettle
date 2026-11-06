/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClass;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDefinedJavaClassCodeSnippits {
  private static Class<?> PKG = UserDefinedJavaClass.class;
  private static final UserDefinedJavaClassCodeSnippits snippitsHelper;

  private final List<Snippit> snippits = new ArrayList<Snippit>();
  private final Map<String, Snippit> snippitsMap = new HashMap<String, Snippit>();
  private final LogChannel log = new LogChannel( "UserDefinedJavaClassCodeSnippits" );

  static {
    snippitsHelper = new UserDefinedJavaClassCodeSnippits();
    try {
      snippitsHelper.addSnippits( "codeSnippits.xml" );
    } catch ( KettleXMLException ex ) {
      throw new IllegalStateException( "Could not initialize from codeSnippets.xml" );
    }
  }

  public static UserDefinedJavaClassCodeSnippits getSnippitsHelper() throws KettleXMLException {
    return snippitsHelper;
  }

  private UserDefinedJavaClassCodeSnippits() {
  }

  public void addSnippits( String strFileName ) throws KettleXMLException {
    Document doc =
      XMLHandler.loadXMLFile(
        UserDefinedJavaClassCodeSnippits.class.getResourceAsStream( strFileName ), null, false, false );
    buildSnippitList( doc );
  }

  public enum Category {
    COMMON( BaseMessages.getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.COMMON" ) ), STATUS(
      BaseMessages.getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.STATUS" ) ), LOGGING(
      BaseMessages.getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.LOGGING" ) ), LISTENERS(
      BaseMessages.getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.LISTENERS" ) ), ROW(
      BaseMessages.getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.ROW" ) ), OTHER( BaseMessages
      .getString( PKG, "UserDefinedJavaClassCodeSnippits.categories.OTHER" ) );

    private String description;

    private Category( String description ) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    @Override
    public String toString() {
      return description;
    }
  }

  public static class Snippit {
    private Snippit( Category category, String name, String sample, String code ) {
      this.category = category;
      this.name = name;
      this.sample = sample;
      this.code = code;
    }

    public final Category category;
    public final String name;
    public final String sample;
    public final String code;
  }

  public List<Snippit> getSnippits() {
    return Collections.unmodifiableList( snippits );
  }

  public String getDefaultCode() {
    return getCode( "Implement processRow" );
  }

  public String getCode( String snippitName ) {
    Snippit snippit = snippitsMap.get( snippitName );
    return ( snippit == null ) ? "" : snippit.code;
  }

  public String getSample( String snippitName ) {
    Snippit snippit = snippitsMap.get( snippitName );
    return ( snippit == null ) ? "" : snippit.sample;
  }

  private void buildSnippitList( Document doc ) {
    List<Node> nodes = XMLHandler.getNodes( XMLHandler.getSubNode( doc, "codeSnippits" ), "codeSnippit" );
    for ( Node node : nodes ) {
      Snippit snippit =
        new Snippit( Category.valueOf( XMLHandler.getTagValue( node, "category" ) ), XMLHandler.getTagValue(
          node, "name" ), XMLHandler.getTagValue( node, "sample" ), XMLHandler.getTagValue( node, "code" ) );
      snippits.add( snippit );
      Snippit oldSnippit = snippitsMap.put( snippit.name, snippit );
      if ( oldSnippit != null ) {
        log.logError( "Multiple code snippits for name: " + snippit.name );
      }
    }
  }
}
