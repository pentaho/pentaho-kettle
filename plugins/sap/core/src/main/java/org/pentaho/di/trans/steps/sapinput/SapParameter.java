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


package org.pentaho.di.trans.steps.sapinput;

public class SapParameter {
  private String fieldName;
  private SapType sapType;
  private String tableName;
  private String parameterName;
  private int targetType;

  /**
   * @param fieldName
   * @param sapType
   * @param tableName
   * @param parameterName
   */
  public SapParameter( String fieldName, SapType sapType, String tableName, String parameterName, int targetType ) {
    this.fieldName = fieldName;
    this.sapType = sapType;
    this.tableName = tableName;
    this.parameterName = parameterName;
    this.targetType = targetType;
  }

  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          the fieldName to set
   */
  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return the sapType
   */
  public SapType getSapType() {
    return sapType;
  }

  /**
   * @param sapType
   *          the sapType to set
   */
  public void setSapType( SapType sapType ) {
    this.sapType = sapType;
  }

  /**
   * @return the tableName
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          the tableName to set
   */
  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  /**
   * @return the parameterName
   */
  public String getParameterName() {
    return parameterName;
  }

  /**
   * @param parameterName
   *          the parameterName to set
   */
  public void setParameterName( String parameterName ) {
    this.parameterName = parameterName;
  }

  /**
   * @return the targetType
   */
  public int getTargetType() {
    return targetType;
  }

  /**
   * @param targetType
   *          the targetType to set
   */
  public void setTargetType( int targetType ) {
    this.targetType = targetType;
  }

}
