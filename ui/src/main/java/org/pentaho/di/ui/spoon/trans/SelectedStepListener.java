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
package org.pentaho.di.ui.spoon.trans;

import org.pentaho.di.trans.step.StepMeta;

/**
 * Created by saslan on 12/18/2014.
 */
public interface SelectedStepListener {

  public void onSelect( StepMeta selectedStep );
}
