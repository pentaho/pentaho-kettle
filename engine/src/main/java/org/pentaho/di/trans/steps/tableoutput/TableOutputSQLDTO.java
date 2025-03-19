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


package org.pentaho.di.trans.steps.tableoutput;

import org.pentaho.di.core.row.RowMetaInterface;

import java.util.List;

public class TableOutputSQLDTO {

  public RowMetaInterface bufferRowMeta;
  public List<Object[]> bufferRowData;

  boolean isQuery;

  boolean isError;

  String message;


  public RowMetaInterface getBufferRowMeta() {
    return bufferRowMeta;
  }

  public void setBufferRowMeta( RowMetaInterface bufferRowMeta ) {
    this.bufferRowMeta = bufferRowMeta;
  }

  public List<Object[]> getBufferRowData() {
    return bufferRowData;
  }

  public void setBufferRowData( List<Object[]> bufferRowData ) {
    this.bufferRowData = bufferRowData;
  }

  public boolean isQuery() {
    return isQuery;
  }

  public void setQuery( boolean query ) {
    isQuery = query;
  }

  public boolean isError() {
    return isError;
  }

  public void setError( boolean error ) {
    isError = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }
}
