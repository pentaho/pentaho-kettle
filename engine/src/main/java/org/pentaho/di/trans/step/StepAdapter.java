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

import org.pentaho.di.trans.Trans;

public class StepAdapter implements StepListener {

  @Override
  public void stepActive( Trans trans, StepMeta stepMeta, StepInterface step ) {
  }

  @Override
  public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface step ) {
  }

}
