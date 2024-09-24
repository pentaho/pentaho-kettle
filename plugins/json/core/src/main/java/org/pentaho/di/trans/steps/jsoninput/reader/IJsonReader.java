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

package org.pentaho.di.trans.steps.jsoninput.reader;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;

import java.io.InputStream;

public interface IJsonReader {

  /**
   * Parse compiled Json Paths into a rowset
   */
  RowSet parse( InputStream in ) throws KettleException;
}
