/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
