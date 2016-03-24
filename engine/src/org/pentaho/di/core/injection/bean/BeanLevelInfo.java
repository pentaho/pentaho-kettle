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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionTypeConverter;

/**
 * Storage for one step on the bean deep level.
 */
class BeanLevelInfo {
  enum DIMENSION {
    NONE, ARRAY, LIST
  };

  /** Parent step or null for root. */
  public BeanLevelInfo parent;
  /** Class for step from field or methods. */
  public Class<?> leafClass;
  /** Field of step, or null if bean has getter/setter. */
  public Field field;
  /** Getter and setter. */
  public Method getter, setter;
  /** Dimension of level. */
  public DIMENSION dim = DIMENSION.NONE;
  /** Values converter. */
  public InjectionTypeConverter converter;
  /** False if source empty value shoudn't affect on target field. */
  public boolean convertEmpty;

  public void init( BeanInjectionInfo info ) {
    introspect( info, leafClass );
  }

  /**
   * Introspect class and all interfaces and ancestors recursively.
   */
  private void introspect( BeanInjectionInfo info, Class<?> clazz ) {
    while ( clazz != null ) {
      introspect( info, clazz.getDeclaredFields(), clazz.getDeclaredMethods() );
      for ( Class<?> intf : clazz.getInterfaces() ) {
        introspect( info, intf );
      }
      clazz = clazz.getSuperclass();
    }
  }

  /**
   * Introspect fields and methods of some class.
   */
  protected void introspect( BeanInjectionInfo info, Field[] fields, Method[] methods ) {
    for ( Field f : fields ) {
      Injection annotationInjection = f.getAnnotation( Injection.class );
      InjectionDeep annotationInjectionDeep = f.getAnnotation( InjectionDeep.class );
      if ( annotationInjection == null && annotationInjectionDeep == null ) {
        // no injection annotations
        continue;
      }
      if ( annotationInjection != null && annotationInjectionDeep != null ) {
        // both annotations exist - wrong
        throw new RuntimeException( "Field can't be annotated twice for injection " + f );
      }
      if ( f.isSynthetic() || f.isEnumConstant() || Modifier.isStatic( f.getModifiers() ) ) {
        // fields can't contain real data with such modifier
        throw new RuntimeException( "Wrong modifier for anotated field " + f );
      }
      BeanLevelInfo leaf = new BeanLevelInfo();
      leaf.parent = this;
      leaf.field = f;
      if ( f.getType().isArray() ) {
        leaf.dim = DIMENSION.ARRAY;
        leaf.leafClass = f.getType().getComponentType();
      } else if ( List.class.isAssignableFrom( f.getType() ) ) {
        leaf.dim = DIMENSION.LIST;
        Type fieldType = f.getGenericType();
        Type listType = ( (ParameterizedType) fieldType ).getActualTypeArguments()[0];
        try {
          leaf.leafClass = Class.forName( listType.getTypeName(), false, leafClass.getClassLoader() );
        } catch ( Throwable ex ) {
          throw new RuntimeException( "Can't retrieve type from List for " + f );
        }
      } else {
        leaf.dim = DIMENSION.NONE;
        leaf.leafClass = f.getType();
      }
      if ( annotationInjection != null ) {
        try {
          leaf.converter = annotationInjection.converter().newInstance();
        } catch ( Exception ex ) {
          throw new RuntimeException( "Error instantiate converter for " + f, ex );
        }
        leaf.convertEmpty = annotationInjection.convertEmpty();
        info.addInjectionProperty( annotationInjection, leaf );
      } else if ( annotationInjectionDeep != null ) {
        // introspect deeper
        leaf.init( info );
      }
    }
    for ( Method m : methods ) {
      Injection annotationInjection = m.getAnnotation( Injection.class );
      InjectionDeep annotationInjectionDeep = m.getAnnotation( InjectionDeep.class );
      if ( annotationInjection == null && annotationInjectionDeep == null ) {
        // no injection annotations
        continue;
      }
      if ( annotationInjection != null && annotationInjectionDeep != null ) {
        // both annotations exist - wrong
        throw new RuntimeException( "Method can't be annotated twice for injection " + m );
      }
      if ( m.isSynthetic() || Modifier.isStatic( m.getModifiers() ) ) {
        // method is static
        throw new RuntimeException( "Wrong modifier for anotated method " + m );
      }
      BeanLevelInfo leaf = new BeanLevelInfo();
      leaf.parent = this;
      if ( annotationInjectionDeep != null ) {
        Class<?> getterClass = isGetter( m );
        if ( getterClass == null || getterClass.isArray() ) {
          throw new RuntimeException( "Method should be getter: " + m );
        }
        leaf.getter = m;
        leaf.leafClass = getterClass;
        leaf.init( info );
      } else {
        Class<?> setterClass = isSetter( m );
        if ( setterClass == null || setterClass.isArray() ) {
          throw new RuntimeException( "Method should be setter: " + m );
        }
        leaf.setter = m;
        leaf.leafClass = setterClass;
        try {
          leaf.converter = annotationInjection.converter().newInstance();
        } catch ( Exception ex ) {
          throw new RuntimeException( "Error instantiate converter for " + m, ex );
        }
        leaf.convertEmpty = annotationInjection.convertEmpty();
        info.addInjectionProperty( annotationInjection, leaf );
      }
    }
  }

  private Class<?> isGetter( Method m ) {
    if ( m.getReturnType() == void.class ) {
      return null;
    }
    if ( m.getParameterTypes().length == 0 ) {
      // getter without parameters
      return m.getReturnType();
    }
    return null;
  }

  private Class<?> isSetter( Method m ) {
    if ( m.getReturnType() != void.class ) {
      return null;
    }
    if ( m.getParameterTypes().length == 1 ) {
      // setter with one parameter
      return m.getParameterTypes()[0];
    }
    return null;
  }

  protected List<BeanLevelInfo> createCallStack() {
    List<BeanLevelInfo> stack = new ArrayList<>();
    BeanLevelInfo p = this;
    while ( p != null ) {
      if ( p.field != null ) {
        p.field.setAccessible( true );
      }
      stack.add( p );
      p = p.parent;
    }
    Collections.reverse( stack );
    return stack;
  }

  @Override
  public String toString() {
    String r = "";
    if ( field != null ) {
      r += "field " + field.getName();
    } else {
      r += "<root field>";
    }
    r += "(class " + leafClass.getSimpleName() + ")";
    return r;
  }
}
