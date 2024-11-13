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
 * RowAdapter is an adapter class for receiving row events. The methods in this class are empty. This class exists as
 * convenience for creating row listener objects that may not need to implement all the methods of the RowListener
 * interface
 *
 * @see RowListener
 */
public class RowAdapter implements RowListener {

  /**
   * Instantiates a new row adapter.
   */
  public RowAdapter() {
  }

  /**
   * Empty method implementing the RowListener.errorRowWrittenEvent interface method
   *
   * @see org.pentaho.di.trans.step.RowListener#errorRowWrittenEvent(org.pentaho.di.core.row.RowMetaInterface,
   *      java.lang.Object[])
   */
  public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
  }

  /**
   * Empty method implementing the RowListener.rowReadEvent interface method
   *
   * @see org.pentaho.di.trans.step.RowListener#rowReadEvent(org.pentaho.di.core.row.RowMetaInterface,
   *      java.lang.Object[])
   */
  public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
  }

  /**
   * Empty method implementing the RowListener.rowWrittenEvent interface method
   *
   * @see org.pentaho.di.trans.step.RowListener#rowWrittenEvent(org.pentaho.di.core.row.RowMetaInterface,
   *      java.lang.Object[])
   */
  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
  }

}
