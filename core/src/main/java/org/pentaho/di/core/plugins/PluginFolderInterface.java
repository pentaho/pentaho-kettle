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

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleFileException;

/**
 * Describes a possible location for a plugin
 *
 * @author matt
 *
 */
public interface PluginFolderInterface {

  /**
   * @return The folder location
   */
  public String getFolder();

  /**
   * @return true if the folder needs to be searched for plugin.xml appearances
   */
  public boolean isPluginXmlFolder();

  /**
   * @return true if the folder needs to be searched for jar files with plugin annotations
   */
  public boolean isPluginAnnotationsFolder();

  /**
   * Find all the jar files in this plugin folder
   *
   * @return The jar files
   * @throws KettleFileException
   *           In case there is a problem reading
   */
  public FileObject[] findJarFiles() throws KettleFileException;

}
