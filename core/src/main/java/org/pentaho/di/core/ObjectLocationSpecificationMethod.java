/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

public enum ObjectLocationSpecificationMethod {
  FILENAME( "filename", "Filename" ), REPOSITORY_BY_NAME( "rep_name", "Specify by name in repository" ),
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
