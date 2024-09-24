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

public enum SapType {
  Single( "SINGLE", "Single" ), Structure( "STRUCTURE", "Structure" ), Table( "TABLE", "Table" );

  private String code;
  private String description;

  private SapType( String code, String description ) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public static SapType findTypeForCode( String code ) {
    for ( SapType type : values() ) {
      if ( type.getCode().equalsIgnoreCase( code ) ) {
        return type;
      }
    }
    return null;
  }

  public static SapType findTypeForDescription( String description ) {
    for ( SapType type : values() ) {
      if ( type.getDescription().equalsIgnoreCase( description ) ) {
        return type;
      }
    }
    return null;
  }

  public static String[] getDescriptions() {
    String[] descriptions = new String[values().length];
    for ( int i = 0; i < values().length; i++ ) {
      descriptions[i] = values()[i].getDescription();
    }
    return descriptions;
  }
}
