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
