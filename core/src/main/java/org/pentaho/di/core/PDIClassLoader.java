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

package org.pentaho.di.core;

import java.net.URL;
import java.net.URLClassLoader;

public class PDIClassLoader extends URLClassLoader {

  public PDIClassLoader( URL[] url, ClassLoader parent ) {
    super( url, parent );
  }

  public PDIClassLoader( ClassLoader parent ) {
    super( new URL[] {}, parent );
  }

  @Override
  protected synchronized Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
    try {
      return super.loadClass( name, resolve );
    } catch ( NoClassDefFoundError e ) {
      return super.findClass( name );
    }
  }

}
