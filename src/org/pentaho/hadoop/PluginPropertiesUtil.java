/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.hadoop;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility for working with pentaho-mapreduce.properties
 */
public class PluginPropertiesUtil {
  /**
   * Loads a properties file from the plugin directory for the plugin interface provided
   *
   * @param plugin
   * @return
   * @throws KettleFileException
   * @throws FileSystemException
   * @throws IOException
   */
  public Properties loadPluginProperties(PluginInterface plugin, String relativeName) throws KettleFileException, IOException {
    if (plugin == null) {
      throw new NullPointerException();
    }
    FileObject propFile = KettleVFS.getFileObject(plugin.getPluginDirectory().getPath() + Const.FILE_SEPARATOR + relativeName);
    if (!propFile.exists()) {
      throw new FileNotFoundException(propFile.toString());
    }
    Properties p = new Properties();
    p.load(KettleVFS.getInputStream(propFile));
    return p;
  }
}
