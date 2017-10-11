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
import org.pentaho.di.core.gui.PrimitiveGCInterface.EImage;
import org.pentaho.di.core.row.RowMetaInterface;

public interface RowDistributionInterface {

  /**
   * @return The row distribution code (plugin id)
   */
  public String getCode();

  /**
   * @return The row distribution description (plugin description)
   */
  public String getDescription();

  /**
   * Do the actual row distribution in the step
   *
   * @param rowMeta
   *          the meta-data of the row to distribute
   * @param row
   *          the data of the row data to distribute
   * @param stepInterface
   *          The step to distribute the rows in
   * @throws KettleStepException
   */
  public void distributeRow( RowMetaInterface rowMeta, Object[] row, StepInterface stepInterface ) throws KettleStepException;

  /**
   * Which mini-icon needs to be shown on the hop?
   *
   * @return the available code EImage or null if the standard icon needs to be used.
   */
  public EImage getDistributionImage();
}
