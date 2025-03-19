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
