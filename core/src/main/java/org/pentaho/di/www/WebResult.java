/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.www;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class WebResult {
  private static Class<?> PKG = WebResult.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "webresult";

  public static final String STRING_OK = "OK";
  public static final String STRING_ERROR = "ERROR";

  public static final WebResult OK = new WebResult( STRING_OK );

  private String result;
  private String message;
  private String id;

  public WebResult( String result ) {
    this( result, null, null );
  }

  public WebResult( String result, String message ) {
    this( result, message, null );
  }

  public WebResult( String result, String message, String id ) {
    this.result = result;
    this.message = message;
    this.id = id;
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();

    xml.append( "<" + XML_TAG + ">" ).append( Const.CR );

    xml.append( "  " ).append( XMLHandler.addTagValue( "result", result ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "message", message ) );
    xml.append( "  " ).append( XMLHandler.addTagValue( "id", id ) );

    xml.append( "</" + XML_TAG + ">" ).append( Const.CR );

    return xml.toString();
  }

  @Override
  public String toString() {
    return getXML();
  }

  public WebResult( Node webResultNode ) {
    result = XMLHandler.getTagValue( webResultNode, "result" );
    message = XMLHandler.getTagValue( webResultNode, "message" );
    id = XMLHandler.getTagValue( webResultNode, "id" );
  }

  public String getResult() {
    return result;
  }

  public void setResult( String result ) {
    this.result = result;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public static WebResult fromXMLString( String xml ) throws KettleXMLException {
    try {
      Document doc = XMLHandler.loadXMLString( xml );
      Node node = XMLHandler.getSubNode( doc, XML_TAG );

      return new WebResult( node );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "WebResult.Error.UnableCreateResult" ), e );
    }
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }
}
