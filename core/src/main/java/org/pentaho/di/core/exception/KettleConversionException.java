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

package org.pentaho.di.core.exception;

import java.util.List;

import org.pentaho.di.core.row.ValueMetaInterface;

public class KettleConversionException extends KettleException {

  private List<Exception> causes;
  private List<ValueMetaInterface> fields;
  private Object[] rowData;

  /**
   *
   */
  private static final long serialVersionUID = 1697154653111622296L;

  /**
   * Constructs a new throwable with null as its detail message.
   */
  public KettleConversionException() {
    super();
  }

  /**
   * Constructs a new throwable with the specified detail message and cause.
   *
   * @param message
   *          the detail message (which is saved for later retrieval by the getMessage() method).
   * @param causes
   *          the causes of the conversion errors
   * @param fields
   *          the failing fields
   * @param rowData
   *          the row with the failed fields set to null.
   */
  public KettleConversionException( String message, List<Exception> causes, List<ValueMetaInterface> fields,
    Object[] rowData ) {
    super( message );
    this.causes = causes;
    this.fields = fields;
    this.rowData = rowData;
  }

  /**
   * @return the causes
   */
  public List<Exception> getCauses() {
    return causes;
  }

  /**
   * @param causes
   *          the causes to set
   */
  public void setCauses( List<Exception> causes ) {
    this.causes = causes;
  }

  /**
   * @return the fields
   */
  public List<ValueMetaInterface> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the fields to set
   */
  public void setFields( List<ValueMetaInterface> fields ) {
    this.fields = fields;
  }

  /**
   * @return the rowData
   */
  public Object[] getRowData() {
    return rowData;
  }

  /**
   * @param rowData
   *          the rowData to set
   */
  public void setRowData( Object[] rowData ) {
    this.rowData = rowData;
  }
}
