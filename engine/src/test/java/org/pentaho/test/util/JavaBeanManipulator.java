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

package org.pentaho.test.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.steps.loadsave.getter.FieldGetter;
import org.pentaho.di.trans.steps.loadsave.getter.Getter;
import org.pentaho.di.trans.steps.loadsave.getter.MethodGetter;
import org.pentaho.di.trans.steps.loadsave.setter.FieldSetter;
import org.pentaho.di.trans.steps.loadsave.setter.MethodSetter;
import org.pentaho.di.trans.steps.loadsave.setter.Setter;

public class JavaBeanManipulator<T> {
  private final Class<? extends T> clazz;
  private final Map<String, String> getterMap;
  private final Map<String, Getter<?>> getterMethodMap;
  private final Map<String, String> setterMap;
  private final Map<String, Setter<?>> setterMethodMap;

  public JavaBeanManipulator( Class<? extends T> clazz, List<String> attributes, Map<String, String> getterMap,
      Map<String, String> setterMap ) {
    this.clazz = clazz;
    this.getterMap = new HashMap<String, String>( getterMap );
    this.setterMap = new HashMap<String, String>( setterMap );
    this.getterMethodMap = new HashMap<String, Getter<?>>();
    this.setterMethodMap = new HashMap<String, Setter<?>>();
    populateGetters( attributes );
    populateSetters( attributes );
  }

  private String getPrefixedName( String prefix, String name ) {
    String[] underScoreSplit = name.split( "_" );
    name = "";
    for ( String part : underScoreSplit ) {
      if ( part.length() > 0 ) {
        name += part.substring( 0, 1 ).toUpperCase();
        if ( part.length() > 1 ) {
          name += part.substring( 1 );
        }
      }
    }
    return prefix + name;
  }

  @SuppressWarnings( "rawtypes" )
  private void populateGetters( List<String> attributes ) {
    for ( String attribute : attributes ) {
      String getterMethodName = getterMap.get( attribute );
      try {
        Getter<?> getter;
        if ( getterMethodName != null ) {
          getter = new MethodGetter( clazz.getMethod( getterMethodName ) );
        } else {
          try {
            getter = new MethodGetter( clazz.getMethod( getPrefixedName( "get", attribute ) ) );
          } catch ( NoSuchMethodException e ) {
            try {
              getter = new MethodGetter( clazz.getMethod( getPrefixedName( "is", attribute ) ) );
            } catch ( NoSuchMethodException e2 ) {
              getter = new FieldGetter( clazz.getField( attribute ) );
            }
          }
        }
        getterMethodMap.put( attribute, getter );
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to find getter for " + attribute, e );
      }
    }
  }

  @SuppressWarnings( "rawtypes" )
  private void populateSetters( List<String> attributes ) {
    for ( String attribute : attributes ) {
      String setterMethodName = setterMap.get( attribute );
      try {
        Setter<?> setter;
        if ( setterMethodName != null ) {
          setter = new MethodSetter( clazz.getMethod( setterMethodName, getterMethodMap.get( attribute ).getType() ) );
        } else {
          try {
            setter =
                new MethodSetter( clazz.getMethod( getPrefixedName( "set", attribute ), getterMethodMap.get( attribute )
                    .getType() ) );
          } catch ( NoSuchMethodException e ) {
            setter = new FieldSetter( clazz.getField( attribute ) );
          }
        }
        setterMethodMap.put( attribute, setter );
      } catch ( Exception e ) {
        throw new RuntimeException( "Unable to find setter for " + attribute, e );
      }
    }
  }

  public Getter<?> getGetter( String attribute ) {
    return getterMethodMap.get( attribute );
  }

  public Setter<?> getSetter( String attribute ) {
    return setterMethodMap.get( attribute );
  }
}
