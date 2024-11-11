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
