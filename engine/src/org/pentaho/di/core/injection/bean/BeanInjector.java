/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.injection.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;

/**
 * Engine for get/set metadata injection properties from bean.
 */
public class BeanInjector {
  private final BeanInjectionInfo info;

  public BeanInjector( BeanInjectionInfo info ) {
    this.info = info;
  }

  public Object getProperty( Object root, String propName ) throws Exception {
    List<Integer> extractedIndexes = new ArrayList<>();

    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    if ( prop == null ) {
      throw new RuntimeException( "Property not found" );
    }

    Object obj = root;
    for ( int i = 1, arrIndex = 0; i < prop.path.size(); i++ ) {
      BeanLevelInfo s = prop.path.get( i );
      obj = s.field.get( obj );
      if ( obj == null ) {
        return null; // some value in path is null - return empty
      }
      if ( s.array ) {
        int index = extractedIndexes.get( arrIndex++ );
        if ( Array.getLength( obj ) <= index ) {
          return null;
        }
        obj = Array.get( obj, index );
        if ( obj == null ) {
          return null; // element is empty
        }
      }
    }
    return obj;
  }

  public boolean hasProperty( Object root, String propName ) {
    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    return prop != null;
  }

  public void setProperty( Object root, String propName, List<RowMetaAndData> data, String dataName )
    throws KettleException {
    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    if ( prop == null ) {
      throw new KettleException( "Property '" + propName + "' not found for injection to " + root.getClass() );
    }

    if ( prop.pathArraysCount == 0 ) {
      // no arrays in path
      try {
        setProperty( root, prop, 0, data.get( 0 ), dataName );
      } catch ( Exception ex ) {
        throw new KettleException( "Error inject property '" + propName + "' into " + root.getClass(), ex );
      }
    } else if ( prop.pathArraysCount == 1 ) {
      // one array in path
      try {
        for ( int i = 0; i < data.size(); i++ ) {
          setProperty( root, prop, i, data.get( i ), dataName );
        }
      } catch ( Exception ex ) {
        throw new KettleException( "Error inject property '" + propName + "' into " + root.getClass(), ex );
      }
    } else {
      if ( prop.pathArraysCount > 1 ) {
        throw new KettleException( "Property '" + propName + "' has more than one array in path for injection to "
            + root.getClass() );
      }
    }
  }

  private void setProperty( Object root, BeanInjectionInfo.Property prop, int index, RowMetaAndData data,
      String dataName ) throws Exception {
    Object obj = root;
    for ( int i = 1; i < prop.path.size(); i++ ) {
      BeanLevelInfo s = prop.path.get( i );
      if ( i < prop.path.size() - 1 ) {
        // get path
        if ( s.array ) {
          // array
          Object existArray = extendArray( s, obj, index + 1 );
          Object next = Array.get( existArray, index ); // get specific element
          if ( next == null ) {
            // Object can be inner of metadata class. In this case constructor will require parameter
            for ( Constructor<?> c : s.leafClass.getConstructors() ) {
              if ( c.getParameterTypes().length == 0 ) {
                next = s.leafClass.newInstance();
                break;
              } else if ( c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom(
                  info.clazz ) ) {
                next = c.newInstance( root );
                break;
              }
            }
            if ( next == null ) {
              throw new KettleException( "Empty constructor not found for " + s.leafClass );
            }
            Array.set( existArray, index, next );
          }
          obj = next;
        } else {
          // plain field
          if ( s.field != null ) {
            Object next = s.field.get( obj );
            if ( next == null ) {
              next = s.leafClass.newInstance();
              s.field.set( obj, next );
            }
            obj = next;
          } else if ( s.getter != null ) {
            Object next = s.getter.invoke( obj );
            if ( next == null ) {
              if ( s.setter == null ) {
                throw new KettleException( "No setter defined for " + root.getClass() );
              }
              next = s.leafClass.newInstance();
              s.setter.invoke( obj, next );
            }
            obj = next;
          } else {
            throw new KettleException( "No field or getter defined for " + root.getClass() );
          }
        }
      } else {
        // set to latest field
        if ( s.array ) {
          if ( s.field != null ) {
            Object existArray = extendArray( s, obj, index + 1 );
            Object value = data.getAsJavaType( dataName, s.leafClass );
            if ( value != null ) {
              Array.set( existArray, index, value );
            }
          } else if ( s.setter != null ) {
            Object value = data.getAsJavaType( dataName, s.leafClass );
            if ( value != null ) {
              if ( s.setter.getParameterTypes().length == 2 ) {
                // setter with index
                s.setter.invoke( obj, index, value );
              } else {
                // usual setter
                s.setter.invoke( obj, value );
              }
            }
          }
        } else {
          if ( s.field != null ) {
            Object value = data.getAsJavaType( dataName, s.leafClass );
            if ( value != null ) {
              s.field.set( obj, value );
            }
          } else if ( s.setter != null ) {
            Object value = data.getAsJavaType( dataName, s.leafClass );
            if ( value != null ) {
              if ( s.setter.getParameterTypes().length == 2 ) {
                // setter with index
                s.setter.invoke( obj, index, value );
              } else {
                // usual setter
                s.setter.invoke( obj, value );
              }
            }
          } else {
            throw new KettleException( "No field or setter defined for " + root.getClass() );
          }
        }
      }
    }
  }

  private Object extendArray( BeanLevelInfo s, Object obj, int newSize ) throws Exception {
    Object existArray = s.field.get( obj );
    if ( existArray == null ) {
      existArray = Array.newInstance( s.leafClass, newSize );
      s.field.set( obj, existArray );
    }
    int existSize = Array.getLength( existArray );
    if ( existSize < newSize ) {
      Object newSized = Array.newInstance( s.leafClass, newSize );
      System.arraycopy( existArray, 0, newSized, 0, existSize );
      existArray = newSized;
      s.field.set( obj, existArray );
    }
    return existArray;
  }
}
