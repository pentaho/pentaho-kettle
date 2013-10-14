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

package org.pentaho.di.core.sql;

public enum ServiceCacheMethod {

  None( "No caching" ), LocalMemory( "Cache in local memory" );

  private String description;

  private ServiceCacheMethod( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static ServiceCacheMethod getMethodByName( String string ) {
    for ( ServiceCacheMethod method : values() ) {
      if ( method.name().equalsIgnoreCase( string ) ) {
        return method;
      }
    }
    return None;
  }

  public static ServiceCacheMethod getMethodByDescription( String description ) {
    for ( ServiceCacheMethod method : values() ) {
      if ( method.getDescription().equalsIgnoreCase( description ) ) {
        return method;
      }
    }
    return None;
  }

  public static String[] getDescriptions() {
    String[] strings = new String[values().length];
    for ( int i = 0; i < values().length; i++ ) {
      strings[i] = values()[i].getDescription();
    }
    return strings;
  }
}
