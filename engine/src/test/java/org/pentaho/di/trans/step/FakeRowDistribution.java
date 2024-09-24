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
