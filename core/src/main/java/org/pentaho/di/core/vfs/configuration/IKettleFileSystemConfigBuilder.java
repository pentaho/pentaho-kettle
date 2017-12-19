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

package org.pentaho.di.core.vfs.configuration;

import java.io.IOException;

import org.apache.commons.vfs2.FileSystemOptions;

/**
 * @author cboyden
 */
public interface IKettleFileSystemConfigBuilder {

  /**
   * Extract the FileSystemOptions parameter name from a Kettle variable
   *
   * @param parameter
   * @return
   */
  public String parseParameterName( String parameter, String scheme );

  /**
   * Publicly expose a generic way to set parameters
   */
  public void setParameter( FileSystemOptions opts, String name, String value, String fullParameterName,
    String vfsUrl ) throws IOException;
}
