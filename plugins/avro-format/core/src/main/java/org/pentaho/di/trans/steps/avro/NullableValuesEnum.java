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


package org.pentaho.di.trans.steps.avro;


/**
 * Enum with valid list of Nullable values - used for the Nullable combo box
 * <p>
 * Also contains convience methods to get the default value and return a list of values as string to populate combo box
 */
public enum NullableValuesEnum {
  YES( "Yes" ),
  NO( "No" );

  private String value;

  NullableValuesEnum( String value ) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static NullableValuesEnum getDefaultValue() {
    return NullableValuesEnum.YES;
  }

  public static String[] getValuesArr() {
    String[] valueArr = new String[ NullableValuesEnum.values().length ];

    int i = 0;

    for ( NullableValuesEnum nullValueEnum : NullableValuesEnum.values() ) {
      valueArr[ i++ ] = nullValueEnum.getValue();
    }

    return valueArr;
  }
}
