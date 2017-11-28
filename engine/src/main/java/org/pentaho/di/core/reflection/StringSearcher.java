/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.reflection;

import org.pentaho.di.core.Condition;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class StringSearcher {
  private static final String LOCAL_PACKAGE = "org.pentaho.di";

  private static final String[] JAVA_PACKAGES = new String[] { "java.util", };

  private static List<String> stepPluginPackages;
  private static List<String> jobEntryPluginPackages;

  static {
    PluginRegistry registry = PluginRegistry.getInstance();
    stepPluginPackages = registry.getPluginPackages( StepPluginType.class );
    jobEntryPluginPackages = registry.getPluginPackages( JobEntryPluginType.class );
  }

  public static final void findMetaData( Object object, int level, List<StringSearchResult> stringList,
    Object parentObject, Object grandParentObject ) {
    // System.out.println(Const.rightPad(" ", level)+"Finding strings in "+object.toString());

    if ( ( object == null ) || level > 5 ) {
      return;
    }

    PluginRegistry registry = PluginRegistry.getInstance();

    if ( stepPluginPackages == null ) {
      stepPluginPackages = registry.getPluginPackages( StepPluginType.class );
    }
    if ( jobEntryPluginPackages == null ) {
      jobEntryPluginPackages = registry.getPluginPackages( JobEntryPluginType.class );
    }

    Class<? extends Object> baseClass = object.getClass();
    Field[] fields = baseClass.getDeclaredFields();
    for ( int i = 0; i < fields.length; i++ ) {
      Field field = fields[i];

      boolean processThisOne = true;

      if ( ( field.getModifiers() & Modifier.FINAL ) > 0 ) {
        processThisOne = false;
      }
      if ( ( field.getModifiers() & Modifier.STATIC ) > 0 ) {
        processThisOne = false;
      }

      // Investigate only if we're dealing with a sanctioned package.
      // A sanctioned package is either the local package (org.pentaho.di) or
      // a package of one of the plugins.
      //
      boolean sanctionedPackage = false;
      String fieldToString = field.toString();
      if ( fieldToString.indexOf( LOCAL_PACKAGE ) >= 0 ) {
        sanctionedPackage = true;
      }
      for ( int x = 0; x < JAVA_PACKAGES.length && !sanctionedPackage; x++ ) {
        if ( fieldToString.indexOf( JAVA_PACKAGES[x] ) >= 0 ) {
          sanctionedPackage = true;
        }
      }
      for ( int x = 0; x < stepPluginPackages.size() && !sanctionedPackage; x++ ) {
        if ( fieldToString.indexOf( stepPluginPackages.get( x ) ) >= 0 ) {
          sanctionedPackage = true;
        }
      }
      for ( int x = 0; x < jobEntryPluginPackages.size() && !sanctionedPackage; x++ ) {
        if ( fieldToString.indexOf( jobEntryPluginPackages.get( x ) ) >= 0 ) {
          sanctionedPackage = true;
        }
      }
      if ( !sanctionedPackage ) {
        processThisOne = false; // Stay in the sanctioned code-base.
      }

      // Dig into the metadata from here...
      //
      if ( processThisOne ) {
        try {
          Object obj = field.get( object );
          if ( obj != null ) {
            stringSearchInObject( obj, level, stringList, parentObject, grandParentObject, field );
          }
        } catch ( IllegalAccessException e ) {
          // OK, it's private, let's see if we can go there later on using
          // getters and setters...
          // fileName becomes: getFileName();
          // OK, how do we get the value now?
          try {

            Method method = findMethod( baseClass, field.getName() );
            if ( method != null ) {
              // String fullMethod =
              // baseClass.getName()+"."+method.getName()+"()";

              Object string = method.invoke( object, (Object[]) null );
              if ( string != null ) {
                stringSearchInObject( string, level, stringList, parentObject, grandParentObject, field );
              }
            }
          } catch ( Throwable ex ) {
            // Ignore this error silently. If we can't access the method there
            // is nothing you can do about it.
          }
        }
      }
    }
  }

  private static void stringSearchInObject( Object obj, int level, List<StringSearchResult> stringList,
    Object parentObject, Object grandParentObject, Field field ) {
    String fieldName = field.getName();
    if ( obj instanceof String ) {
      // OK, let's add the String
      stringList.add( new StringSearchResult( (String) obj, parentObject, grandParentObject, fieldName ) );
    } else if ( obj instanceof String[] ) {
      String[] array = (String[]) obj;
      for ( int x = 0; x < array.length; x++ ) {
        if ( array[x] != null ) {
          stringList.add( new StringSearchResult( array[x], parentObject, grandParentObject, fieldName
            + " #" + ( x + 1 ) ) );
        }
      }
    } else if ( obj instanceof Boolean ) {
      // OK, let's add the String
      stringList.add( new StringSearchResult( ( (Boolean) obj ).toString(), parentObject, grandParentObject, fieldName
        + " (Boolean)" ) );
    } else if ( obj instanceof Condition ) {
      stringList.add( new StringSearchResult( ( (Condition) obj).toString(), parentObject, grandParentObject, fieldName + " (Condition)" ) );
    } else if ( obj instanceof DatabaseInterface ) {
      // Make sure we read the attributes. This is not picked up by default. (getDeclaredFields doesn't pick up
      // inherited fields)
      //
      DatabaseInterface databaseInterface = (DatabaseInterface) obj;
      findMapMetaData(
        databaseInterface.getAttributes(), level + 1, stringList, parentObject, grandParentObject, field );
      findMetaData( obj, level + 1, stringList, parentObject, grandParentObject );
    } else if ( obj instanceof Map ) {
      findMapMetaData( (Map<?, ?>) obj, level, stringList, parentObject, grandParentObject, field );
    } else if ( obj instanceof Object[] ) {
      for ( int j = 0; j < ( (Object[]) obj ).length; j++ ) {
        findMetaData( ( (Object[]) obj )[j], level + 1, stringList, parentObject, grandParentObject );
      }
    } else {
      findMetaData( obj, level + 1, stringList, parentObject, grandParentObject );
    }
  }

  private static void findMapMetaData( Map<?, ?> map, int level, List<StringSearchResult> stringList,
    Object parentObject, Object grandParentObject, Field field ) {

    String fieldName = field.getName();
    for ( Object key : map.keySet() ) {
      Object value = map.get( key );
      if ( key != null ) {
        stringList.add( new StringSearchResult( key.toString(), parentObject, grandParentObject, fieldName
          + " (Map key)" ) );
      }
      if ( value != null ) {
        stringList.add( new StringSearchResult( value.toString(), parentObject, grandParentObject, fieldName
          + " (Map value)" ) );
      }
    }
  }

  private static Method findMethod( Class<? extends Object> baseClass, String name ) {
    // baseClass.getMethod(methodName[m], null);
    Method[] methods = baseClass.getDeclaredMethods();
    Method method = null;

    // getName()
    if ( method == null ) {
      String getter = constructGetter( name );
      method = searchGetter( getter, baseClass, methods );
    }

    // isName()
    if ( method == null ) {
      String getter = constructIsGetter( name );
      method = searchGetter( getter, baseClass, methods );
    }

    // name()
    if ( method == null ) {
      String getter = name;
      method = searchGetter( getter, baseClass, methods );
    }

    return method;

  }

  private static Method searchGetter( String getter, Class<?> baseClass, Method[] methods ) {
    Method method = null;
    try {
      method = baseClass.getMethod( getter );

    } catch ( Exception e ) {
      // Nope try case insensitive.
      for ( int i = 0; i < methods.length; i++ ) {
        String methodName = methods[i].getName();
        if ( methodName.equalsIgnoreCase( getter ) ) {
          return methods[i];
        }
      }

    }

    return method;
  }

  public static final String constructGetter( String name ) {
    StringBuilder buf = new StringBuilder();
    buf.append( "get" );
    buf.append( name.substring( 0, 1 ).toUpperCase() );
    buf.append( name.substring( 1 ) );

    return buf.toString();
  }

  public static final String constructIsGetter( String name ) {
    StringBuilder buf = new StringBuilder();
    buf.append( "is" );
    buf.append( name.substring( 0, 1 ).toUpperCase() );
    buf.append( name.substring( 1 ) );

    return buf.toString();
  }

}
