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

package org.pentaho.di.core.database;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A {@link URLClassLoader} that inverts the standard parent-delegation model.
 * <p>
 * Standard delegation: parent first → JAR second (child)<br>
 * This loader:        JAR first (child) → platform parent second
 * <p>
 * <b>Why this matters for driver isolation:</b><br>
 * A KTR can reference multiple database connections each backed by a different
 * version of the same JDBC driver (e.g. MySQL 5, MySQL 8, PostgreSQL 16).
 * Each {@link Database} instance gets its own {@code ChildFirstURLClassLoader}
 * pointing at its own JAR, so their driver classes are in fully independent
 * namespaces — no version can bleed into another connection.
 * <p>
 * JDK classes ({@code java.*}, {@code javax.*}, etc.) are always delegated to
 * the platform parent because they are not in the driver JAR.
 */
class ChildFirstURLClassLoader extends URLClassLoader {

  ChildFirstURLClassLoader( URL[] urls, ClassLoader parent ) {
    super( urls, parent );
  }

  @Override
  protected Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
    synchronized ( getClassLoadingLock( name ) ) {

      // 1. Return already-loaded class from this loader's cache
      Class<?> loaded = findLoadedClass( name );
      if ( loaded != null ) {
        if ( resolve ) {
          resolveClass( loaded );
        }
        return loaded;
      }

      // 2. Always delegate JDK classes to the platform parent —
      //    they are not in the driver JAR and must come from the JDK module system.
      if ( isJdkClass( name ) ) {
        return super.loadClass( name, resolve );
      }

      // 3. Try to load from the JAR first (child-first)
      try {
        Class<?> found = findClass( name );
        if ( resolve ) {
          resolveClass( found );
        }
        return found;
      } catch ( ClassNotFoundException ignored ) {
        // not in JAR — fall through to parent
      }

      // 4. Delegate to the platform parent as last resort
      return super.loadClass( name, resolve );
    }
  }

  /**
   * Returns {@code true} for classes that must always come from the JDK or platform —
   * these are never present in a driver JAR and must be loaded by the bootstrap or
   * platform class loader.
   * <p>
   * All other classes (including third-party libraries bundled inside the driver JAR
   * such as logging frameworks or Apache Commons) are intentionally loaded child-first
   * so that the driver gets its own versions without interfering with PDI's classpath.
   * The sole interface between PDI and the driver is {@code java.sql.*}, which is
   * covered by the {@code java.*} prefix below.
   */
  private boolean isJdkClass( String name ) {
    return name.startsWith( "java." )
      || name.startsWith( "javax." )
      || name.startsWith( "jakarta." )   // Jakarta EE APIs provided by the platform
      || name.startsWith( "sun." )
      || name.startsWith( "com.sun." )
      || name.startsWith( "jdk." )
      || name.startsWith( "org.ietf." )  // JDK-bundled GSSAPI / Kerberos
      || name.startsWith( "org.w3c." )   // JDK-bundled DOM API
      || name.startsWith( "org.xml." );  // JDK-bundled SAX API
  }
}
