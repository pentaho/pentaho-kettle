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
