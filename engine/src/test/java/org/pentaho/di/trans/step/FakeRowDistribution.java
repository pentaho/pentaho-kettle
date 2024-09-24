/*
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
import org.pentaho.di.core.gui.PrimitiveGCInterface;
import org.pentaho.di.core.row.RowMetaInterface;

@RowDistributionPlugin( code = "FakeDistribution", name = "Fake distribution",
    description = "Useful only for unit testing" )
public class FakeRowDistribution implements RowDistributionInterface {

  @Override
  public String getCode() {
    return "FakeDistribution";
  }

  @Override
  public String getDescription() {
    return "Fake distribution";
  }

  @Override
  public void distributeRow( RowMetaInterface paramRowMetaInterface, Object[] paramArrayOfObject,
      StepInterface paramStepInterface ) throws KettleStepException {
    // TODO: Implement some distribution for test cases
  }

  @Override
  public PrimitiveGCInterface.EImage getDistributionImage() {
    return PrimitiveGCInterface.EImage.LOAD_BALANCE;
  }
}
