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

package org.pentaho.di.core.row;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a list of data rows as well as the RowMetaInterface to describe it.
 *
 * @author matt
 *
 */
public class RowBuffer {
  private RowMetaInterface rowMeta;
  private List<Object[]> buffer;

  /**
   * @param rowMeta
   * @param buffer
   */
  public RowBuffer( RowMetaInterface rowMeta, List<Object[]> buffer ) {
    this.rowMeta = rowMeta;
    this.buffer = buffer;
  }

  /**
   * @param rowMeta
   */
  public RowBuffer( RowMetaInterface rowMeta ) {
    this( rowMeta, new ArrayList<Object[]>() );
  }

  /**
   * @return the rowMeta
   */
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }

  /**
   * @return the buffer
   */
  public List<Object[]> getBuffer() {
    return buffer;
  }

  /**
   * @param buffer
   *          the buffer to set
   */
  public void setBuffer( List<Object[]> buffer ) {
    this.buffer = buffer;
  }
}
