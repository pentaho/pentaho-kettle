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


package org.pentaho.di.trans.steps.file;

import java.io.Closeable;

import org.pentaho.di.core.exception.KettleException;

/**
 * Content-based reader for file.
 */
public interface IBaseFileInputReader extends Closeable {
  boolean readRow() throws KettleException;
}
