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

package org.pentaho.di.trans.steps.loadsave.initializer;

import org.pentaho.di.trans.step.StepMetaInterface;

public abstract class StepMetaInitializer<T extends StepMetaInterface>
  implements InitializerInterface<StepMetaInterface> {

  @Override
  public abstract void modify( StepMetaInterface object );

}
