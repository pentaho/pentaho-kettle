/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.starmodeler;


public enum AttributeType {
  TECHNICAL_KEY,
  SMART_TECHNICAL_KEY,
  VERSION_FIELD,
  DATE_START,
  NATURAL_KEY,
  DATE_END,
  ATTRIBUTE,
  ATTRIBUTE_OVERWRITE,
  ATTRIBUTE_HISTORICAL,
  FACT,
  DEGENERATE_DIMENSION,
  OTHER;

  public static AttributeType getAttributeType( String typeString ) {
    try {
      return valueOf(typeString );
    } catch ( Exception e ) {
      return OTHER;
    }
  }

  public boolean isAttribute() {
    return this == ATTRIBUTE || this == ATTRIBUTE_OVERWRITE || this == ATTRIBUTE_HISTORICAL;
  }
}
