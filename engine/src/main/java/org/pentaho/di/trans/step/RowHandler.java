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

package org.pentaho.di.trans.step;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Defines methods used for handling row data within steps.
 *
 * By default, the implementation used in BaseStep leverages
 * the logic defined within BaseStep.
 * (see {@link BaseStep#handleGetRow()}
 *      {@link BaseStep#handlePutRow(RowMetaInterface, Object[])}
 *      {@link BaseStep#handlePutError }
 *
 * {@link BaseStep#setRowHandler( RowHandler) } can be used to override
 * this behavior.
 */
public interface RowHandler {
  Class<?> PKG = BaseStep.class;

  Object[] getRow() throws KettleException;

  void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;

  void putError( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
                 String fieldNames, String errorCodes ) throws KettleStepException;

  default void putRowTo( RowMetaInterface rowMeta, Object[] row, RowSet rowSet )
    throws KettleStepException {
    throw new UnsupportedOperationException(
      BaseMessages.getString( PKG, "BaseStep.RowHandler.PutRowToNotSupported",
        this.getClass().getName() ) );
  }

  default Object[] getRowFrom( RowSet rowSet ) throws KettleStepException {
    throw new UnsupportedOperationException(
      BaseMessages.getString( PKG, "BaseStep.RowHandler.GetRowFromNotSupported",
        this.getClass().getName() ) );
  }

}
