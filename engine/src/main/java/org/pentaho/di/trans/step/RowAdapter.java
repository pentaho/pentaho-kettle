/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
