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


package org.pentaho.di.core;

public enum ObjectLocationSpecificationMethod {
  FILENAME( "filename", "Filename" ),
  REPOSITORY_BY_NAME( "rep_name", "Specify by name in repository" ),
  @Deprecated // this is no longer supported in the UI, only in code with jobs/transformations created prior to 7.0
  REPOSITORY_BY_REFERENCE( "rep_ref", "Specify by reference in repository" );

  private String code;
  private String description;

  private ObjectLocationSpecificationMethod( String code, String description ) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

  public String[] getDescriptions() {
    String[] desc = new String[values().length];
    for ( int i = 0; i < values().length; i++ ) {
      desc[i] = values()[i].getDescription();
    }
    return desc;
  }

  public static ObjectLocationSpecificationMethod getSpecificationMethodByCode( String code ) {
    for ( ObjectLocationSpecificationMethod method : values() ) {
      if ( method.getCode().equals( code ) ) {
        return method;
      }
    }
    return null;
  }

  public static ObjectLocationSpecificationMethod getSpecificationMethodByDescription( String description ) {
    for ( ObjectLocationSpecificationMethod method : values() ) {
      if ( method.getDescription().equals( description ) ) {
        return method;
      }
    }
    return null;
  }
}
