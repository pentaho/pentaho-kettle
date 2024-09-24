/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.infobrightoutput;

/**
 * @author Infobright Inc.
 */
public class WindowsJNILibraryUtil {

  /**
   * adds kettle's libext to java.library.path so that we can pick up infobright_jni library.
   */
  public static void fixJavaLibraryPath() {
    String curLibPath = System.getProperty( "java.library.path" );
    if ( curLibPath != null ) {
      String libextPath = null;
      String[] paths = curLibPath.split( ";" );
      for ( String path : paths ) {
        if ( path.contains( "libswt\\win32" ) ) {
          libextPath = path.replace( "libswt\\win32", "libext" );
        }
      }
      if ( libextPath != null ) {
        System.setProperty( "java.library.path", curLibPath + ";" + libextPath );
      }
    }
  }
}
