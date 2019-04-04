/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.AfterInjection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Objects.requireNonNull;

/**
 * Engine for get/set metadata injection properties from bean.
 */
public class BeanInjector {
  private final BeanInjectionInfo info;

  public BeanInjector( BeanInjectionInfo info ) {
    this.info = info;
  }

  public Object getObject( Object root, String propName ) throws Exception {
    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    if ( prop == null ) {
      throw new RuntimeException( "Property not found" );
    }
    BeanLevelInfo beanLevelInfo = prop.path.get( 1 );
    return beanLevelInfo.field.get( root );
  }

  /**
   * Retrieves the raw prop value from root object.
   * <p>
   * The similar {@link #getProperty(Object, String)} method (also in this class )should be
   * revisited and possibly eliminated.  That version attempts to retrieve indexed prop
   * vals from lists/arrays, but doesn't provide a way to retrieve the list or array objects
   * themselves.
   */
  public Object getPropVal( Object root, String propName ) {
    Queue<BeanLevelInfo> beanInfos =
      newLinkedList( Optional.ofNullable( info.getProperties().get( propName ) )
        .orElseThrow( () -> new IllegalArgumentException( "Property not found: " + propName ) )
        .path );
    beanInfos.remove();  // pop off root
    return getPropVal( root, propName, beanInfos );
  }

  @SuppressWarnings ( "unchecked" )
  private Object getPropVal( Object obj, String propName, Queue<BeanLevelInfo> beanInfos ) {
    BeanLevelInfo info = beanInfos.remove();
    if ( beanInfos.isEmpty() ) {
      return getObjFromBeanInfo( obj, info );
    }
    obj = getObjFromBeanInfo( obj, info );
    switch ( info.dim ) {
      case LIST:
        return ( (List) requireNonNull( obj ) ).stream()
          .map( o -> getPropVal( o, propName, newLinkedList( beanInfos ) ) )
          .collect( Collectors.toList() );
      case ARRAY:
        return Arrays.stream( (Object[]) requireNonNull( obj ) )
          .map( o -> getPropVal( o, propName, newLinkedList( beanInfos ) ) )
          .toArray( Object[]::new );
      case NONE:
        return getPropVal( obj, propName, beanInfos );
    }
    throw new IllegalStateException( "Unexpected value of BeanLevelInfo.dim " + info.dim );
  }


  private Object getObjFromBeanInfo( Object obj, BeanLevelInfo beanLevelInfo ) {
    try {
      return beanLevelInfo.field == null ? null : beanLevelInfo.field.get( obj );
    } catch ( IllegalAccessException e ) {
      throw new RuntimeException( e );
    }
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

      switch ( s.dim ) {
        case ARRAY:
          int indexArray = extractedIndexes.get( arrIndex++ );
          if ( Array.getLength( obj ) <= indexArray ) {
            return null;
          }
          obj = Array.get( obj, indexArray );
          if ( obj == null ) {
            return null; // element is empty
          }
          break;
        case LIST:
          int indexList = extractedIndexes.get( arrIndex++ );
          List<?> list = (List<?>) obj;
          if ( list.size() <= indexList ) {
            return null;
          }
          obj = list.get( indexList );
          if ( obj == null ) {
            return null; // element is empty
          }
          break;
        case NONE:
          break;
      }
    }
    return obj;
  }

  public boolean hasProperty( Object root, String propName ) {
    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    return prop != null;
  }

  public void setProperty( Object root, String propName, List<RowMetaAndData> data, String dataN )
    throws KettleException {
    BeanInjectionInfo.Property prop = info.getProperties().get( propName );
    if ( prop == null ) {
      throw new KettleException( "Property '" + propName + "' not found for injection to " + root.getClass() );
    }

    String dataName;
    String dataValue;

    if ( data != null ) {
      dataName = dataN;
      dataValue = null;
    } else {
      dataName = null;
      dataValue = dataN;
    }
    if ( prop.pathArraysCount == 0 ) {
      // no arrays in path
      try {
        setProperty( root, prop, 0, data != null ? data.get( 0 ) : null, dataName, dataValue );
      } catch ( Exception ex ) {
        throw new KettleException( "Error inject property '" + propName + "' into " + root.getClass(), ex );
      }
    } else if ( prop.pathArraysCount == 1 ) {
      // one array in path
      try {
        if ( data != null ) {
          for ( int i = 0; i < data.size(); i++ ) {
            setProperty( root, prop, i, data.get( i ), dataName, dataValue );
          }
        } else {
          allocateCollectionField( root, info, propName );
          for ( int i = 0;; i++ ) {
            // NOTE: case when constant value is provided and need to fill out all entries with the same value
            // assumption is the field array/list size allocated to correct size
            boolean found = setProperty( root, prop, i, null, null, dataValue );
            if ( !found ) {
              break;
            }
          }
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

  /**
   * Sets data from RowMetaAndData, or constant value from dataValue depends on 'data != null'.
   */
  private boolean setProperty( Object root, BeanInjectionInfo.Property prop, int index, RowMetaAndData data,
      String dataName, String dataValue ) throws Exception {
    Object obj = root;
    for ( int i = 1; i < prop.path.size(); i++ ) {
      BeanLevelInfo s = prop.path.get( i );
      if ( i < prop.path.size() - 1 ) {       // NOTE: if prop.path.size() > 2, then @InjectionDeep field
        // get path
        Object next;
        switch ( s.dim ) {
          case ARRAY:
            // array
            Object existArray = data != null ? extendArray( s, obj, index + 1 ) : checkArray( s, obj, index );
            if ( existArray == null ) {
              // out of array for constant
              return false;
            }
            next = Array.get( existArray, index ); // get specific element
            if ( next == null ) {
              next = createObject( s.leafClass, root );
              Array.set( existArray, index, next );
            }
            obj = next;
            break;
          case LIST:
            // list
            List<Object> existList = data != null ? extendList( s, obj, index + 1 ) : checkList( s, obj, index );
            if ( existList == null ) {
              // out of array for constant
              return false;
            }
            next = existList.get( index ); // get specific element
            if ( next == null ) {
              next = createObject( s.leafClass, root );
              existList.set( index, next );
            }
            obj = next;
            break;
          case NONE:
            // plain field
            if ( s.field != null ) {
              next = s.field.get( obj );
              if ( next == null ) {
                next = createObject( s.leafClass, root );
                s.field.set( obj, next );
              }
              obj = next;
            } else if ( s.getter != null ) {
              next = s.getter.invoke( obj );
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
            break;
        }
      } else {
        // set to latest field
        if ( !s.convertEmpty ) {
          if ( data != null ) {
            if ( data.isEmptyValue( dataName ) ) {
              return true;
            }
          } else {
            if ( dataValue == null ) {
              return true;
            }
          }
        }
        if ( s.setter != null ) {
          // usual setter
          Object value;
          if ( data != null ) {
            value = data.getAsJavaType( dataName, s.leafClass, s.converter );
          } else {
            value = RowMetaAndData.getStringAsJavaType( dataValue, s.leafClass, s.converter );
          }
          s.setter.invoke( obj, value );
        } else if ( s.field != null ) {
          Object value;
          if ( data != null ) {
            value = data.getAsJavaType( dataName, s.leafClass, s.converter );
          } else {
            value = RowMetaAndData.getStringAsJavaType( dataValue, s.leafClass, s.converter );
          }
          switch ( s.dim ) {
            case ARRAY:
              Object existArray = data != null ? extendArray( s, obj, index + 1 ) : checkArray( s, obj, index );
              if ( existArray == null ) {
                // out of array for constant
                return false;
              }
              Array.set( existArray, index, value );
              break;
            case LIST:
              List<Object> existList = data != null ? extendList( s, obj, index + 1 ) : checkList( s, obj, index );
              if ( existList == null ) {
                // out of array for constant
                return false;
              }
              existList.set( index, value );
              break;
            case NONE:
              s.field.set( obj, value );
              break;
          }
        } else {
          throw new KettleException( "No field or setter defined for " + root.getClass() );
        }
      }
    }
    return true;
  }

  private Object createObject( Class<?> clazz, Object root ) throws KettleException {
    try {
      // Object can be inner of metadata class. In this case constructor will require parameter
      for ( Constructor<?> c : clazz.getConstructors() ) {
        if ( c.getParameterTypes().length == 0 ) {
          return clazz.newInstance();
        } else if ( c.getParameterTypes().length == 1 && c.getParameterTypes()[0].isAssignableFrom( info.clazz ) ) {
          return c.newInstance( root );
        }
      }
    } catch ( Throwable ex ) {
      throw new KettleException( "Can't create object " + clazz, ex );
    }
    throw new KettleException( "Constructor not found for " + clazz );
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

  private Object checkArray( BeanLevelInfo s, Object obj, int index ) throws Exception {
    Object existArray = s.field.get( obj );
    if ( existArray == null ) {
      return null;
    }
    int existSize = Array.getLength( existArray );
    return index < existSize ? existArray : null;
  }

  private List<Object> extendList( BeanLevelInfo s, Object obj, int newSize ) throws Exception {
    @SuppressWarnings( "unchecked" )
    List<Object> existList = (List<Object>) s.field.get( obj );
    if ( existList == null ) {
      existList = new ArrayList<>();
      s.field.set( obj, existList );
    }
    while ( existList.size() < newSize ) {
      existList.add( null );
    }

    return existList;
  }

  private List<Object> checkList( BeanLevelInfo s, Object obj, int index ) throws Exception {
    @SuppressWarnings( "unchecked" )
    List<Object> existList = (List<Object>) s.field.get( obj );
    if ( existList == null ) {
      return null;
    }

    return index < existList.size() ? existList : null;
  }

  /**
   * Allocate the number of spaces for the {@code fieldName}, in preparation to be filled with call to
   * {@link #setProperty(Object, String, List, String )} for constant values.
   * @param object class that implements org.pentaho.di.trans.step.StepMetaInterface
   * @param beanInjectionInfo
   * @param fieldName
   */
  void allocateCollectionField( Object object, BeanInjectionInfo beanInjectionInfo, String fieldName ) {

    BeanInjectionInfo.Property property = getProperty( beanInjectionInfo, fieldName );
    String groupName =  ( property != null ) ? property.getGroupName() : null;
    if ( groupName == null ) {
      return;
    }

    List<BeanInjectionInfo.Property> groupProperties;
    groupProperties = getGroupProperties( beanInjectionInfo, groupName );
    Integer maxGroupSize = getMaxSize( groupProperties, object );

    // not able to get numeric size
    if ( maxGroupSize == null ) {
      return;
    }

    // guaranteed to get at least one field for constant
    allocateCollectionField( property, object, Math.max( 1, maxGroupSize ) );
  }

  /**
   * Allocate the number of spaces for the {@code property} with number of space defined by {@code size}.
   * @param property
   * @param obj
   * @param size
   */
  void allocateCollectionField( BeanInjectionInfo.Property property, Object obj, int size ) {
    BeanLevelInfo beanLevelInfo = getFinalPath( property );
    allocateCollectionField( beanLevelInfo, obj, size );
  }

  /**
   * Allocate the number of spaces for the {@code property} with number of space defined by {@code size}.
   * @param beanLevelInfo
   * @param obj
   * @param size
   */
  void allocateCollectionField( BeanLevelInfo beanLevelInfo, Object obj, int size ) {
    try {
      if ( isArray( beanLevelInfo ) ) {
        // similar logic as #extendArray
        Object newArray = Array.newInstance( beanLevelInfo.getLeafClass(), size );
        beanLevelInfo.getField().set( obj, newArray );
      } else { // LIST
        // similar logic as #extendList
        List<Object> existList = new ArrayList<>();
        beanLevelInfo.getField().set( obj, existList );
        while ( existList.size() < size ) {
          existList.add( null );
        }
      }
    } catch ( Exception e ) {
      // do nothing
    }
  }

  /**
   * Determines if property is collection ie type list or array.
   * @param property
   * @return true if collection, false otherwise.
   */
  boolean isCollection( BeanInjectionInfo.Property property ) {
    if ( property == null ) { // not sure if this is necessary
      return false;
    }
    BeanLevelInfo beanLevelInfo = getFinalPath( property );
    return ( beanLevelInfo != null ) ? isCollection( beanLevelInfo ) : null;
  }

  /**
   * Determines if property is collection ie type list or array.
   * @param beanLevelInfo
   * @return true if collection, false otherwise.
   */
  boolean isCollection( BeanLevelInfo beanLevelInfo ) {
    return isList( beanLevelInfo ) || isArray( beanLevelInfo );
  }

  /**
   * Determines if property is array.
   * @param beanLevelInfo
   * @return true if collection, false otherwise.
   */
  boolean isArray( BeanLevelInfo beanLevelInfo ) {
    return beanLevelInfo.getDim() == BeanLevelInfo.DIMENSION.ARRAY;
  }

  /**
   * Determines if property is list.
   * @param beanLevelInfo
   * @return true if collection, false otherwise.
   */
  boolean isList( BeanLevelInfo beanLevelInfo ) {
    return beanLevelInfo.getDim() == BeanLevelInfo.DIMENSION.LIST;
  }

  /**
   * Get last path in property list.
   * @param property
   * @return
   */
  BeanLevelInfo getFinalPath( BeanInjectionInfo.Property property ) {
    return ( !property.getPath().isEmpty() ) ? property.getPath().get( property.getPath().size() - 1 ) : null;
  }

  /**
   * Get property by name.
   * @param beanInjectionInfo
   * @param fieldName
   * @return
   */
  BeanInjectionInfo.Property getProperty( BeanInjectionInfo beanInjectionInfo, String fieldName ) {
    return beanInjectionInfo.getProperties().get( fieldName );
  }

  /**
   * Get all properties that belong to the same {@code groupName}
   * @param beanInjectionInfo
   * @param groupName
   * @return
   */
  List<BeanInjectionInfo.Property> getGroupProperties( BeanInjectionInfo beanInjectionInfo, String groupName ) {
    BeanInjectionInfo.Group group = beanInjectionInfo.getGroups().stream()
       .filter( g -> g.getName().equals( groupName ) ).findFirst().orElse( null );

    return ( group != null ) ? group.getGroupProperties() : new ArrayList<>();
  }

  /**
   * Determine maximum size property in the collection of {@code properties}
   * @param properties
   * @param obj
   * @return
   */
  Integer getMaxSize( Collection<BeanInjectionInfo.Property> properties, Object obj ) {
    int max = Integer.MIN_VALUE;

    for ( BeanInjectionInfo.Property property: properties ) {
      max = Math.max( max,
        ( isCollection( property )
          ? getCollectionSize( property, obj )
          // if not collection then field of length one
          : 1 ) );
    }

    return ( max != Integer.MIN_VALUE ) ? max : null;
  }

  /**
   * Determine size of {@code property}.
   * @param property
   * @param obj
   * @return
   */
  int getCollectionSize( BeanInjectionInfo.Property property, Object obj ) {
    BeanLevelInfo beanLevelInfo = getFinalPath( property );
    return getCollectionSize( beanLevelInfo, obj );
  }

  /**
   * Determine size of {@code property}.
   * @param beanLevelInfo
   * @param obj
   * @return
   */
  int getCollectionSize( BeanLevelInfo beanLevelInfo, Object obj ) {
    int size = -1;
    try {
      if ( isArray( beanLevelInfo ) ) {
        // similar logic as BeanInjector#checkArray
        Object existArray = beanLevelInfo.getField().get( obj );
        size = Array.getLength( existArray );
      } else { // LIST
        // similar logic as BeanInjector#checkList
        List<Object> existList = (List<Object>) beanLevelInfo.getField().get( obj );
        size = existList.size();
      }
    } catch ( Exception e ) {
      // do nothing
    }

    return size;
  }

  public void runPostInjectionProcessing( Object object ) {
    Method[] methods = object.getClass().getDeclaredMethods();
    for ( Method m : methods ) {
      AfterInjection annotationAfterInjection = m.getAnnotation( AfterInjection.class );
      if ( annotationAfterInjection == null ) {
        // no after injection annotations
        continue;
      }
      if ( m.isSynthetic() || Modifier.isStatic( m.getModifiers() ) ) {
        // method is static
        throw new RuntimeException( "Wrong modifier for annotated method " + m );
      }
      try {
        m.invoke( object );
      } catch ( Exception e ) {
        throw new RuntimeException( "Can not invoke after injection method " + m, e );
      }
    }
  }
}
