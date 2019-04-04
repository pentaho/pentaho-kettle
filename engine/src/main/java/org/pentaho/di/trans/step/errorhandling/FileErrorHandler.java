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

package org.pentaho.di.trans.step.errorhandling;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.exception.KettleException;

/**
 *
 * @author Johnny Vanhentenyk
 *
 */
public interface FileErrorHandler {

  /**
   * Tells the handler which file is being processed.
   *
   * @param file
   * @throws KettleException
   */
  void handleFile( FileObject file ) throws KettleException;

  /**
   * This method handles an error when processing the line with corresponding lineNr.
   *
   * @param lineNr
   * @param filePart
   *          allows us to split error according to a filePart
   * @throws KettleException
   */
  void handleLineError( long lineNr, String filePart ) throws KettleException;

  /**
   * This method closes the handler;
   *
   */
  void close() throws KettleException;

  /**
   * This method handles a file that is required, but does not exist.
   *
   * @param file
   * @throws KettleException
   */
  void handleNonExistantFile( FileObject file ) throws KettleException;

  /**
   * This method handles a file that is required, but is not accessible.
   *
   * @param file
   * @throws KettleException
   */
  void handleNonAccessibleFile( FileObject file ) throws KettleException;
}
