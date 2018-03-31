/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

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
