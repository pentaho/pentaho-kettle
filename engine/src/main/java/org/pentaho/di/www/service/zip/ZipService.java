/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www.service.zip;

import org.pentaho.di.core.exception.KettleException;

/**
 * Simple class to handle zip file operations.
 */
public interface ZipService {

  /**
   * Decompress the <code>zipFile</code> and place contents into <code>destinationDirectory</code>.
   * @param zipFile path to zip file.
   * @param destinationDirectory directory where the contents of the file will be placed.
   * @throws KettleException
   */
  void extract( String zipFile, String destinationDirectory ) throws KettleException;
}
