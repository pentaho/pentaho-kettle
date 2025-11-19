/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Vector;

import org.pentaho.di.i18n.BaseMessages;

public class KettleURLClassLoader extends URLClassLoader {
  private static Class<?> PKG = KettleURLClassLoader.class; // for i18n purposes, needed by Translator2!!

  private String name;

  public KettleURLClassLoader( URL[] url, ClassLoader classLoader ) {
    super( url, classLoader );
  }

  public KettleURLClassLoader( URL[] url, ClassLoader classLoader, String name ) {
    this( url, classLoader );
    this.name = name;
  }

  @Override
  protected void addURL( URL url ) {
    super.addURL( url );
  }

  @Override
  public String toString() {
    return super.toString() + " : " + name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected Class<?> loadClassFromThisLoader( String arg0, boolean arg1 ) throws ClassNotFoundException {
    Class<?> clz;
    if ( ( clz = findLoadedClass( arg0 ) ) != null ) {
      if ( arg1 ) {
        resolveClass( clz );
      }
      return clz;
    }

    if ( ( clz = findClass( arg0 ) ) != null ) {
      if ( arg1 ) {
        resolveClass( clz );
      }
      return clz;
    }
    return clz;
  }

  protected Class<?> loadClassFromParent( String arg0, boolean arg1 ) throws ClassNotFoundException {
    Class<?> clz;
    if ( ( clz = getParent().loadClass( arg0 ) ) != null ) {
      if ( arg1 ) {
        resolveClass( clz );
      }
      return clz;
    }
    throw new ClassNotFoundException( "Could not find :" + arg0 );
  }

  @Override
  protected synchronized Class<?> loadClass( String arg0, boolean arg1 ) throws ClassNotFoundException {
    try {
      return loadClassFromThisLoader( arg0, arg1 );
    } catch ( ClassNotFoundException | NoClassDefFoundError e ) {
      // ignore
    } catch ( SecurityException e ) {
      System.err.println( BaseMessages.getString( PKG, "KettleURLClassLoader.Exception.UnableToLoadClass",
              e.toString() ) );
    }

    return loadClassFromParent( arg0, arg1 );
  }

  /*
   * Cglib doe's not creates custom class loader (to access package methods and classes ) it uses reflection to invoke
   * "defineClass", but you can call protected method in subclass without problems:
   */
  public Class<?> loadClass( String name, ProtectionDomain protectionDomain ) {
    Class<?> loaded = findLoadedClass( name );
    if ( loaded == null ) {
      // Get the jar, load the bytes from the jar file, construct class from scratch as in snippet below...

      /*
       * 
       * loaded = super.findClass(name);
       * 
       * URL url = super.findResource(newName);
       * 
       * InputStream clis = getResourceAsStream(newName);
       */

      String newName = name.replace( '.', '/' );
      InputStream is = getResourceAsStream( newName );
      byte[] driverBytes = toBytes( is );

      loaded = super.defineClass( name, driverBytes, 0, driverBytes.length, protectionDomain );

    }
    return loaded;
  }

  private byte[] toBytes( InputStream is ) {
    byte[] retval = new byte[0];
    try {
      int a = is.available();
      while ( a > 0 ) {
        byte[] buffer = new byte[a];
        is.read( buffer );

        byte[] newretval = new byte[retval.length + a];

        for ( int i = 0; i < retval.length; i++ ) {
          newretval[i] = retval[i]; // old part
        }
        for ( int i = 0; i < a; i++ ) {
          newretval[retval.length + i] = buffer[i]; // new part
        }

        retval = newretval;

        a = is.available(); // see what's left
      }
      return retval;
    } catch ( Exception e ) {
      System.out.println( BaseMessages.getString( PKG, "KettleURLClassLoader.Exception.UnableToReadClass" )
          + e.toString() );
      return null;
    }
  }

  private static Object getFieldObject( Class<?> clazz, String name, Object obj ) throws Exception {
    Field field = clazz.getDeclaredField( name );
    field.setAccessible( true );
    return field.get( obj );
  }

  /**
   * Close and clean up this class loader using supported APIs.
   */
  @SuppressWarnings("all")
  public void closeClassLoader() {
    // Try the supported URLClassLoader close() method first.
    try {
      this.close();
    } catch (IOException e) {
      // Optionally log; possibly some jars could not be closed
      e.printStackTrace();
    }

    // Attempt to run cleanup of native library references if still present.
    // Note: accessing ClassLoader.nativeLibraries is still reflection-based and
    // may fail on some JVMs. Keep it non-fatal.
    try {
      Field nativeLibsField = ClassLoader.class.getDeclaredField("nativeLibraries");
      nativeLibsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Vector<?> nativeLibArr = (Vector<?>) nativeLibsField.get(this);
      for (Object lib : nativeLibArr) {
        try {
          Method finalize = lib.getClass().getDeclaredMethod("finalize");
          finalize.setAccessible(true);
          finalize.invoke(lib);
        } catch (Exception ex) {
          // ignore per previous behavior
        }
      }
    } catch (Throwable t) {
      // ignore — we cannot rely on JVM internals here
    }

    // Important: do NOT attempt to access sun.net.www.protocol.jar internals here.
    // That reflection is blocked by the Java module system (and is unsafe).
  }

  @Override
  public URL getResource( String name ) {
    URL url;
    url = findResource( name );
    if ( url == null && getParent() != null ) {
      url = getParent().getResource( name );
    }
    return url;
  }

}
