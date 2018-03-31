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

package org.pentaho.di.core.injection.bean;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
  /** Name prefix on the path. */
  public String prefix;

  public void init( BeanInjectionInfo info ) {
    introspect( info, leafClass, new TreeMap<>() );
  }

  /**
   * Introspect class and all interfaces and ancestors recursively.
   */
  private void introspect( BeanInjectionInfo info, Type type, Map<String, Type> ownerGenericsInfo ) {
    Map<String, Type> genericsInfo = new TreeMap<>( ownerGenericsInfo );

    while ( type != null ) {
      Class<?> clazz;
      ParameterizedType pt;
      if ( type instanceof ParameterizedType ) {
        pt = (ParameterizedType) type;
        clazz = (Class<?>) pt.getRawType();
      } else {
        pt = null;
        clazz = (Class<?>) type;
      }
      // introspect generics
      TypeVariable<?>[] tps = clazz.getTypeParameters();
      if ( tps.length > 0 ) {
        if ( pt == null ) {
          throw new RuntimeException( "Can't introspect class with parameters on the high level" );
        }
        Type[] args = pt.getActualTypeArguments();
        if ( tps.length != args.length ) {
          throw new RuntimeException( "Wrong generics declaration" );
        }
        Map<String, Type> prevGenerics = genericsInfo;
        genericsInfo = new TreeMap<>();
        for ( int i = 0; i < tps.length; i++ ) {
          if ( args[i] instanceof TypeVariable ) {
            TypeVariable<?> argsi = (TypeVariable<?>) args[i];
            Type prev = prevGenerics.get( argsi.getName() );
            if ( prev == null ) {
              throw new RuntimeException( "Generic '" + args[i] + "' was not declared yet" );
            }
            genericsInfo.put( tps[i].getName(), prev );
          } else {
            genericsInfo.put( tps[i].getName(), args[i] );
          }
        }
        System.out.println();
      }

      introspect( info, clazz.getDeclaredFields(), clazz.getDeclaredMethods(), genericsInfo );
      for ( Type intf : clazz.getGenericInterfaces() ) {
        introspect( info, intf, genericsInfo );
      }
      type = clazz.getGenericSuperclass();
    }
  }

  /**
   * Introspect fields and methods of some class.
   */
  protected void introspect( BeanInjectionInfo info, Field[] fields, Method[] methods,
      Map<String, Type> genericsInfo ) {
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
      Type t;
      if ( f.getType().isArray() ) {
        Type ff = f.getGenericType();
        leaf.dim = DIMENSION.ARRAY;
        if ( ff instanceof GenericArrayType ) {
          GenericArrayType ffg = (GenericArrayType) ff;
          t = resolveGenericType( ffg.getGenericComponentType(), genericsInfo );
        } else {
          t = f.getType().getComponentType();
        }
      } else if ( List.class.equals( f.getType() ) ) {
        leaf.dim = DIMENSION.LIST;
        Type fieldType = f.getGenericType();
        Type listType = ( (ParameterizedType) fieldType ).getActualTypeArguments()[0];
        try {
          t = resolveGenericType( listType, genericsInfo );
        } catch ( Throwable ex ) {
          throw new RuntimeException( "Can't retrieve type from List for " + f, ex );
        }
      } else {
        leaf.dim = DIMENSION.NONE;
        t = resolveGenericType( f.getGenericType(), genericsInfo );
      }
      if ( t instanceof ParameterizedType ) {
        ParameterizedType pt = (ParameterizedType) t;
        leaf.leafClass = (Class<?>) pt.getRawType();
      } else {
        leaf.leafClass = (Class<?>) t;
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
        leaf.prefix = annotationInjectionDeep.prefix();
        TreeMap<String, Type> gi = new TreeMap<>( genericsInfo );
        leaf.introspect( info, t, gi );
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
        Type getterClass = isGetter( m );
        if ( getterClass == null ) {
          throw new RuntimeException( "Method should be getter: " + m );
        }
        if ( m.getReturnType() != null && List.class.equals( m.getReturnType() ) ) {
          // returns list
          leaf.dim = DIMENSION.LIST;
          ParameterizedType getterType = (ParameterizedType) getterClass;
          getterClass = getterType.getActualTypeArguments()[0];
        }
        Class<?> getter = (Class<?>) resolveGenericType( getterClass, genericsInfo );
        if ( getter.isArray() ) {
          throw new RuntimeException( "Method should be getter: " + m );
        }
        leaf.getter = m;
        leaf.leafClass = getter;
        leaf.prefix = annotationInjectionDeep.prefix();
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

  /**
   * Resolves class by generic map if required(when type is TypeVariable).
   */
  private Type resolveGenericType( Type type, Map<String, Type> genericsInfo ) {
    if ( type instanceof TypeVariable ) {
      String name = ( (TypeVariable<?>) type ).getName();
      type = genericsInfo.get( name );
      if ( type == null ) {
        throw new RuntimeException( "Unknown generics for '" + name + "'" );
      }
    }
    return type;
  }

  private Type isGetter( Method m ) {
    if ( m.getReturnType() == void.class ) {
      return null;
    }
    if ( m.getParameterTypes().length == 0 ) {
      // getter without parameters
      return m.getGenericReturnType();
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
