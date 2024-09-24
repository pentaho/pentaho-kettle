/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.w3c.dom.Node;

/*
 * Created on 5-apr-2004
 *
 */

public class TransDependency implements XMLInterface, Cloneable {
  private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "dependency";

  private DatabaseMeta db;
  private String tablename;
  private String fieldname;

  private ObjectId id;

  public TransDependency( DatabaseMeta db, String tablename, String fieldname ) {
    this.db = db;
    this.tablename = tablename;
    this.fieldname = fieldname;
  }

  public TransDependency() {
    this( null, null, null );
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    xml.append( "        " ).append( XMLHandler.addTagValue( "connection", db == null ? "" : db.getName() ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "table", tablename ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "field", fieldname ) );
    xml.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return xml.toString();
  }

  public TransDependency( Node depnode, List<DatabaseMeta> databases ) throws KettleXMLException {
    try {
      String depcon = XMLHandler.getTagValue( depnode, "connection" );
      db = DatabaseMeta.findDatabase( databases, depcon );
      tablename = XMLHandler.getTagValue( depnode, "table" );
      fieldname = XMLHandler.getTagValue( depnode, "field" );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "TransDependency.Exception.UnableToLoadTransformation" ), e );
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  public ObjectId getObjectId() {
    return id;
  }

  public void setDatabase( DatabaseMeta db ) {
    this.db = db;
  }

  public DatabaseMeta getDatabase() {
    return db;
  }

  public void setTablename( String tablename ) {
    this.tablename = tablename;
  }

  public String getTablename() {
    return tablename;
  }

  public void setFieldname( String fieldname ) {
    this.fieldname = fieldname;
  }

  public String getFieldname() {
    return fieldname;
  }
}
