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

package org.pentaho.di.core.plugins;

import java.net.URL;

public class JarFileAnnotationPlugin {
  private URL jarFile;
  private URL pluginFolder;
  private String className;

  /**
   * @param className
   * @param jarFile
   * @param pluginFolder
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
