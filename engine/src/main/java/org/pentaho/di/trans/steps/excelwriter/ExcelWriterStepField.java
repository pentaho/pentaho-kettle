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

package org.pentaho.di.trans.steps.excelwriter;

import org.pentaho.di.core.row.value.ValueMetaFactory;

public class ExcelWriterStepField implements Cloneable {
  private String name;
  private int type;
  private String format;
  private String title;
  private boolean formula;
  private String hyperlinkField;
  private String commentField;
  private String commentAuthorField;
  private String titleStyleCell;
  private String styleCell;

  public String getCommentAuthorField() {
    return commentAuthorField;
  }

  public void setCommentAuthorField( String commentAuthorField ) {
    this.commentAuthorField = commentAuthorField;
  }

  public ExcelWriterStepField( String name, int type, String format ) {
    this.name = name;
    this.type = type;
    this.format = format;
  }

  public ExcelWriterStepField() {
  }

  public int compare( Object obj ) {
    ExcelWriterStepField field = (ExcelWriterStepField) obj;

    return name.compareTo( field.getName() );
  }

  @Override
  public boolean equals( Object obj ) {
    ExcelWriterStepField field = (ExcelWriterStepField) obj;

    return name.equals( field.getName() );
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Deprecated
  public boolean equal( Object obj ) {
    return equals( obj );
  }

  @Override
  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public String getName() {
    return name;
  }

  public void setName( String fieldname ) {
    this.name = fieldname;
  }

  public int getType() {
    return type;
  }

  public String getTypeDesc() {
    return ValueMetaFactory.getValueMetaName( type );
  }

  public void setType( int type ) {
    this.type = type;
  }

  public void setType( String typeDesc ) {
    this.type = ValueMetaFactory.getIdForValueMeta( typeDesc );
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public boolean isFormula() {
    return formula;
  }

  public void setFormula( boolean formula ) {
    this.formula = formula;
  }

  public String getHyperlinkField() {
    return hyperlinkField;
  }

  public void setHyperlinkField( String hyperlinkField ) {
    this.hyperlinkField = hyperlinkField;
  }

  public String getCommentField() {
    return commentField;
  }

  public void setCommentField( String commentField ) {
    this.commentField = commentField;
  }

  public String getTitleStyleCell() {
    return titleStyleCell;
  }

  public void setTitleStyleCell( String formatCell ) {
    this.titleStyleCell = formatCell;
  }

  public String getStyleCell() {
    return styleCell;
  }

  public void setStyleCell( String styleCell ) {
    this.styleCell = styleCell;
  }

  @Override
  public String toString() {
    return name + ":" + getTypeDesc();
  }
}
