/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.pentaho.di.core.row.ValueMetaInterface;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Simulates the deployed plugin layout, where the plugin ships its own Rhino jar and its classes are loaded by a
 * plugin-first classloader while another copy of Rhino is visible on the parent classloader. JavaScript values produced
 * by the plugin's Rhino must be converted by code linked against the same (plugin) Rhino classes.
 */
public class JavaScriptUtilsClassLoaderTest {

  private static final String PLUGIN_PACKAGE = JavaScriptUtils.class.getPackage().getName() + ".";

  @Test
  public void convertFromJsHandlesValuesFromPluginLoadedRhino() throws Exception {
    URL rhinoJar = Context.class.getProtectionDomain().getCodeSource().getLocation();
    URL pluginClasses = JavaScriptUtils.class.getProtectionDomain().getCodeSource().getLocation();

    try ( PluginFirstClassLoader loader =
            new PluginFirstClassLoader( new URL[] { rhinoJar, pluginClasses }, getClass().getClassLoader() ) ) {
      Class<?> pluginContext = loader.loadClass( Context.class.getName() );
      assertNotSame( "Rhino must be loaded by the plugin classloader", Context.class, pluginContext );

      Object cx = pluginContext.getMethod( "enter" ).invoke( null );
      try {
        Object scope = pluginContext.getMethod( "initStandardObjects" ).invoke( cx );
        Class<?> pluginScriptable = loader.loadClass( Scriptable.class.getName() );
        Object jsResult = pluginContext
          .getMethod( "evaluateString", pluginScriptable, String.class, String.class, int.class, Object.class )
          .invoke( cx, scope, "new java.math.BigDecimal( '1.5' )", "test", 1, null );

        Class<?> pluginUtils = loader.loadClass( JavaScriptUtils.class.getName() );
        assertNotSame( JavaScriptUtils.class, pluginUtils );
        Object converted = pluginUtils
          .getMethod( "convertFromJs", Object.class, int.class, String.class )
          .invoke( null, jsResult, ValueMetaInterface.TYPE_BIGNUMBER, "field" );

        assertEquals( new BigDecimal( "1.5" ), converted );
      } finally {
        pluginContext.getMethod( "exit" ).invoke( null );
      }
    }
  }

  private static class PluginFirstClassLoader extends URLClassLoader {
    PluginFirstClassLoader( URL[] urls, ClassLoader parent ) {
      super( urls, parent );
    }

    @Override
    protected Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
      if ( !name.startsWith( "org.mozilla." ) && !name.startsWith( PLUGIN_PACKAGE ) ) {
        return super.loadClass( name, resolve );
      }
      synchronized ( getClassLoadingLock( name ) ) {
        Class<?> c = findLoadedClass( name );
        if ( c == null ) {
          c = findClass( name );
        }
        if ( resolve ) {
          resolveClass( c );
        }
        return c;
      }
    }
  }
}
