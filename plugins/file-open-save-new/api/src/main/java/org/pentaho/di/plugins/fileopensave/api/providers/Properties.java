/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.api.providers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bmorrise on 3/1/19.
 */
public class Properties {
  private Map<String, Object> properties = new HashMap<>();

  public void put( String name, Object value ) {
    properties.put( name, value );
  }

  public String getString( String name ) {
    String value = getByType( name, String.class );
    if ( value == null ) {
      return "";
    }
    return value;
  }

  public Boolean getBoolean( String name ) {
    Boolean value = getByType( name, Boolean.class );
    if ( value == null ) {
      return false;
    }
    return value;
  }

  private <T> T getByType( String name, Class<T> clazz ) {
    if ( properties.get( name ) != null && properties.get( name ).getClass().isAssignableFrom( clazz ) ) {
      return (T) properties.get( name );
    }
    return null;
  }

  public static Properties create( String... values ) {
    Properties properties = new Properties();
    for ( int i = 0; i < values.length; i += 2 ) {
      properties.put( values[ i ], values[ i + 1 ] );
    }
    return properties;
  }
}
