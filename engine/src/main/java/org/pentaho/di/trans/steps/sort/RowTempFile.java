/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.sort;

/**
 * Keeps track of which temporary file a row is coming from
 */
public class RowTempFile {
  public Object[] row;
  public int fileNumber;

  public RowTempFile( Object[] row, int fileNumber ) {
    this.row = row;
    this.fileNumber = fileNumber;
  }
}
