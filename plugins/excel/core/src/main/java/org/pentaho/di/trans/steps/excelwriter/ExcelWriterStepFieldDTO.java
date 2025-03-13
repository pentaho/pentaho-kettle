/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2025 by Hitachi Vantara : http://www.pentaho.com
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


public class ExcelWriterStepFieldDTO {

  private String type;
  private String name;

  private String format;

  private String styleCell;

  private String title;

  private String titleStyleCell;

  private boolean formula;

  private String hyperlinkField;

  private String commentField;

  private String commentAuthorField;

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat( String format ) {
    this.format = format;
  }

  public String getStyleCell() {
    return styleCell;
  }

  public void setStyleCell( String styleCell ) {
    this.styleCell = styleCell;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getTitleStyleCell() {
    return titleStyleCell;
  }

  public void setTitleStyleCell( String titleStyleCell ) {
    this.titleStyleCell = titleStyleCell;
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

  public String getCommentAuthorField() {
    return commentAuthorField;
  }

  public void setCommentAuthorField( String commentAuthorField ) {
    this.commentAuthorField = commentAuthorField;
  }
}
