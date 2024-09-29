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
