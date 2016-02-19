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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;

/**
 * Storage for one step on the bean deep level.
 */
class BeanLevelInfo {
  /** Parent step or null for root. */
  public BeanLevelInfo parent;
  /** Class for step from field or methods. */
  public Class<?> leafClass;
  /** Field of step, or null if bean has getter/setter. */
  public Field field;
  /** Getter and setter. */
  public Method getter, setter;
  /** Flag for mark array. */
  public boolean array;

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
      if ( f.isSynthetic() || f.isEnumConstant() || Modifier.isStatic( f.getModifiers() ) ) {
        // fields can't contain real data
        continue;
      }
      BeanLevelInfo leaf = new BeanLevelInfo();
      leaf.parent = this;
      leaf.field = f;
      if ( f.getType().isArray() ) {
        leaf.array = true;
        leaf.leafClass = f.getType().getComponentType();
      } else {
        leaf.array = false;
        leaf.leafClass = f.getType();
      }
      Injection metaInj = f.getAnnotation( Injection.class );
      if ( metaInj != null ) {
        info.addInjectionProperty( metaInj, leaf );
      } else if ( f.isAnnotationPresent( InjectionDeep.class ) ) {
        // introspect deeper
        leaf.init( info );
      }
    }
    for ( Method m : methods ) {
      if ( m.isSynthetic() || Modifier.isStatic( m.getModifiers() ) ) {
        // method is static
        continue;
      }

      Injection metaInj = m.getAnnotation( Injection.class );
      if ( metaInj != null || m.isAnnotationPresent( InjectionDeep.class ) ) {
        // fill info
        BeanLevelInfo leaf = new BeanLevelInfo();
        leaf.parent = this;
        Class<?> getterClass = isGetter( m );
        if ( getterClass != null ) {
          leaf.getter = m;
          leaf.leafClass = getterClass;
        }
        Class<?> setterClass = isSetter( m );
        if ( setterClass != null ) {
          leaf.setter = m;
          leaf.leafClass = setterClass;
        }
        if ( leaf.leafClass == null ) {
          continue;
        }
        leaf.array = false;
        if ( setterClass != null && leaf.setter.getParameterTypes().length == 2 ) {
          leaf.array = true;
        }
        if ( metaInj != null ) {
          info.addInjectionProperty( metaInj, leaf );
        } else if ( m.isAnnotationPresent( InjectionDeep.class ) ) {
          // introspect deeper
          leaf.init( info );
        }
      }
    }
  }

  private Class<?> isGetter( Method m ) {
    if ( m.getReturnType() == void.class ) {
      return null;
    }
    switch ( m.getParameterTypes().length ) {
      case 0:
        // getter without parameters
        return m.getReturnType();
      case 1:
        if ( m.getParameterTypes()[0] == int.class ) {
          // getter with one parameter: index
          return m.getReturnType();
        }
        break;
    }
    return null;
  }

  private Class<?> isSetter( Method m ) {
    if ( m.getReturnType() != void.class ) {
      return null;
    }
    switch ( m.getParameterTypes().length ) {
      case 1:
        // setter with one parameter
        return m.getParameterTypes()[0];
      case 2:
        if ( m.getParameterTypes()[0] == int.class ) {
          // setter with two parameters: index and value
          return m.getParameterTypes()[1];
        }
        break;
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
