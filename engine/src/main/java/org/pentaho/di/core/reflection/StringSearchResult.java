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


package org.pentaho.di.core.reflection;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;

public class StringSearchResult {
  private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!

  private String string;
  private Object parentObject;
  private String fieldName;
  private Object grandParentObject;

  /**
   * @param string
   * @param parentObject
   */
  public StringSearchResult( String string, Object parentObject, Object grandParentObject, String fieldName ) {
    super();

    this.string = string;
    this.parentObject = parentObject;
    this.grandParentObject = grandParentObject;
    this.fieldName = fieldName;
  }

  public Object getParentObject() {
    return parentObject;
  }

  public void setParentObject( Object parentObject ) {
    this.parentObject = parentObject;
  }

  public String getString() {
    return string;
  }

  public void setString( String string ) {
    this.string = string;
  }

  public static final RowMetaInterface getResultRowMeta() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString(
      BaseMessages.getString( PKG, "SearchResult.TransOrJob" ) ) );
    rowMeta.addValueMeta( new ValueMetaString(
      BaseMessages.getString( PKG, "SearchResult.StepDatabaseNotice" ) ) );
    rowMeta.addValueMeta( new ValueMetaString(
      BaseMessages.getString( PKG, "SearchResult.String" ) ) );
    rowMeta.addValueMeta( new ValueMetaString(
      BaseMessages.getString( PKG, "SearchResult.FieldName" ) ) );
    return rowMeta;
  }

  public Object[] toRow() {
    return new Object[] { grandParentObject.toString(), parentObject.toString(), string, fieldName, };
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append( parentObject.toString() ).append( " : " ).append( string );
    sb.append( " (" ).append( fieldName ).append( ")" );
    return sb.toString();
  }

  /**
   * @return Returns the fieldName.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          The fieldName to set.
   */
  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return the grandParentObject
   */
  public Object getGrandParentObject() {
    return grandParentObject;
  }

  /**
   * @param grandParentObject
   *          the grandParentObject to set
   */
  public void setGrandParentObject( Object grandParentObject ) {
    this.grandParentObject = grandParentObject;
  }
}
