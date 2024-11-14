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


package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;

/**
 * RowListener is a listener interface for receiving row events. A class that is interested in processing a row event
 * implements this interface, and the object created with that class is registered with a component using the
 * component's
 *
 * <pre>
 * addRowListener
 * </pre>
 *
 * method. When the row event occurs, that object's appropriate method is invoked.
 *
 * @see RowEvent
 */
public interface RowListener {
  /**
   * This method is called when a row is read from another step
   *
   * @param rowMeta
   *          the metadata of the row
   * @param row
   *          the data of the row
   * @throws KettleStepException
   *           an exception that can be thrown to hard stop the step
   */
  public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;

  /**
   * This method is called when a row is written to another step (even if there is no next step)
   *
   * @param rowMeta
   *          the metadata of the row
   * @param row
   *          the data of the row
   * @throws KettleStepException
   *           an exception that can be thrown to hard stop the step
   */
  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;

  /**
   * This method is called when the error handling of a row is writing a row to the error stream.
   *
   * @param rowMeta
   *          the metadata of the row
   * @param row
   *          the data of the row
   * @throws KettleStepException
   *           an exception that can be thrown to hard stop the step
   */
  public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException;
}
