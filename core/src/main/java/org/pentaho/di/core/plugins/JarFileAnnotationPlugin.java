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

package org.pentaho.di.core.plugins;

import java.net.URL;

public class JarFileAnnotationPlugin {
  private URL jarFile;
  private URL pluginFolder;
  private String className;

  /**
   * @param jarFile
   * @param classFile
   * @param annotation
   */
  public JarFileAnnotationPlugin( String className, URL jarFile, URL pluginFolder ) {
    this.className = className;
    this.jarFile = jarFile;
    this.pluginFolder = pluginFolder;
  }

  @Override
  public String toString() {
    return jarFile.toString();
  }

  /**
   * @return the jarFile
   */
  public URL getJarFile() {
    return jarFile;
  }

  /**
   * @param jarFile
   *          the jarFile to set
   */
  public void setJarFile( URL jarFile ) {
    this.jarFile = jarFile;
  }

  public URL getPluginFolder() {
    return pluginFolder;
  }

  public void setPluginFolder( URL pluginFolder ) {
    this.pluginFolder = pluginFolder;
  }

  public String getClassName() {
    return className;
  }

}
