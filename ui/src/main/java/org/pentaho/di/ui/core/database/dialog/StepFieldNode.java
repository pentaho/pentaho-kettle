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


package org.pentaho.di.ui.core.database.dialog;

import org.pentaho.ui.xul.XulEventSourceAdapter;

public class StepFieldNode extends XulEventSourceAdapter {

  private String fieldName;
  private String type;
  private String length;
  private String precision;
  private String origin;
  private String storageType;
  private String conversionMask;
  private String decimalSymbol;
  private String groupingSymbol;
  private String trimType;
  private String comments;

  public String getFieldName() {
    return this.fieldName;
  }

  public void setFieldName( String aFieldName ) {
    this.fieldName = aFieldName;
  }

  public String getType() {
    return this.type;
  }

  public void setType( String aType ) {
    this.type = aType;
  }

  public String getLength() {
    return this.length;
  }

  public void setLength( String aLength ) {
    this.length = aLength;
  }

  public String getPrecision() {
    return this.precision;
  }

  public void setPrecision( String aPrecision ) {
    this.precision = aPrecision;
  }

  public String getOrigin() {
    return this.origin;
  }

  public void setOrigin( String aOrigin ) {
    this.origin = aOrigin;
  }

  public String getStorageType() {
    return this.storageType;
  }

  public void setStorageType( String aStorageType ) {
    this.storageType = aStorageType;
  }

  public String getConversionMask() {
    return this.conversionMask;
  }

  public void setConversionMask( String aConversionMask ) {
    this.conversionMask = aConversionMask;
  }

  public String getDecimalSymbol() {
    return this.decimalSymbol;
  }

  public void setDecimalSymbol( String aDecimalSymbol ) {
    this.decimalSymbol = aDecimalSymbol;
  }

  public String getGroupingSymbol() {
    return this.groupingSymbol;
  }

  public void setGroupingSymbol( String aGroupingSymbol ) {
    this.groupingSymbol = aGroupingSymbol;
  }

  public String getTrimType() {
    return this.trimType;
  }

  public void setTrimType( String aTrimType ) {
    this.trimType = aTrimType;
  }

  public String getComments() {
    return this.comments;
  }

  public void setComments( String aComments ) {
    this.comments = aComments;
  }
}
