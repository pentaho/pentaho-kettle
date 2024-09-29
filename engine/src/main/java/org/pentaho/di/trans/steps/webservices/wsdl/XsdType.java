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


package org.pentaho.di.trans.steps.webservices.wsdl;

import org.pentaho.di.core.row.ValueMetaInterface;

public class XsdType {
  public static final String DATE = "date";
  public static final String TIME = "time";
  public static final String DATE_TIME = "datetime";
  public static final String INTEGER = "int";
  public static final String INTEGER_DESC = "integer";
  public static final String SHORT = "short";
  public static final String BOOLEAN = "boolean";
  public static final String STRING = "string";
  public static final String DOUBLE = "double";
  public static final String FLOAT = "float";
  public static final String BINARY = "base64Binary";
  public static final String DECIMAL = "decimal";

  public static final String[] TYPES = new String[] {
    STRING, INTEGER, INTEGER_DESC, SHORT, BOOLEAN, DATE, TIME, DATE_TIME, DOUBLE, FLOAT, BINARY, DECIMAL, };

  public static int xsdTypeToKettleType( String aXsdType ) {
    int vRet = ValueMetaInterface.TYPE_NONE;
    if ( aXsdType != null ) {
      if ( aXsdType.equalsIgnoreCase( DATE ) ) {
        vRet = ValueMetaInterface.TYPE_DATE;
      } else if ( aXsdType.equalsIgnoreCase( TIME ) ) {
        vRet = ValueMetaInterface.TYPE_DATE;
      } else if ( aXsdType.equalsIgnoreCase( DATE_TIME ) ) {
        vRet = ValueMetaInterface.TYPE_DATE;
      } else if ( aXsdType.equalsIgnoreCase( INTEGER ) || aXsdType.equalsIgnoreCase( INTEGER_DESC ) ) {
        vRet = ValueMetaInterface.TYPE_INTEGER;
      } else if ( aXsdType.equalsIgnoreCase( SHORT ) ) {
        vRet = ValueMetaInterface.TYPE_INTEGER;
      } else if ( aXsdType.equalsIgnoreCase( BOOLEAN ) ) {
        vRet = ValueMetaInterface.TYPE_BOOLEAN;
      } else if ( aXsdType.equalsIgnoreCase( STRING ) ) {
        vRet = ValueMetaInterface.TYPE_STRING;
      } else if ( aXsdType.equalsIgnoreCase( DOUBLE ) ) {
        vRet = ValueMetaInterface.TYPE_NUMBER;
      } else if ( aXsdType.equalsIgnoreCase( BINARY ) ) {
        vRet = ValueMetaInterface.TYPE_BINARY;
      } else if ( aXsdType.equalsIgnoreCase( DECIMAL ) ) {
        vRet = ValueMetaInterface.TYPE_BIGNUMBER;
      } else {
        // When all else fails, map it to a String
        vRet = ValueMetaInterface.TYPE_NONE;
      }
    }
    return vRet;
  }
}
