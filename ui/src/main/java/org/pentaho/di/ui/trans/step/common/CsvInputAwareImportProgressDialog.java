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

package org.pentaho.di.ui.trans.step.common;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * A common interface for progress dialogs that import csv data.
 */
public interface CsvInputAwareImportProgressDialog {

  String open( final boolean failOnParseError );

  /**
   * When {@code failOnParseError} is set to {@code false}, returns the {@link String} value from {@link
   * org.pentaho.di.core.row.RowMeta} at the given {@code index}, or directly from the {@code row} object, if there is a
   * problem fetching the value from {@link org.pentaho.di.core.row.RowMeta}. When {@code failOnParseError} is {@code
   * true}, any {@link Exception} thrown by the call to {@link org.pentaho.di.core.row.RowMeta#getString(Object[], int)}
   * is reported back to the caller.
   *
   * @param rowMeta          an instance of {@link RowMetaInterface}
   * @param row              an Object array containing row data
   * @param index            the index representing the column in a row
   * @param failOnParseError when true, Exceptions are reported back to the called, when false, exceptions are ignored
   *                         and a null value is returned
   * @return the row value at the given index
   */
  default String getStringFromRow( final RowMetaInterface rowMeta, final Object[] row, final int index,
                                   final boolean failOnParseError ) throws KettleException {
    String string = null;
    Exception exc = null;
    try {
      string = rowMeta.getString( row, index );
    } catch ( final Exception e ) {
      exc = e;
    }


    // if 'failOnParseError' is true, and we caught an exception, we either re-throw the exception, or wrap its as a
    // KettleException, if it isn't one already
    if ( failOnParseError ) {
      if ( exc instanceof KettleException ) {
        throw (KettleException) exc;
      } else if ( exc != null ) {
        throw new KettleException( exc );
      }
    }

    // if 'failOnParseError' is false, or there is no exceptionotherwise, we get the string value straight from the row
    // object
    if ( string == null ) {
      if ( ( row.length <= index ) ) {
        if ( failOnParseError ) {
          throw new KettleException( new NullPointerException() );
        }
      }
      string = row.length <= index || row[ index ] == null ? null : row[ index ].toString();
    }

    return string;
  }
}
