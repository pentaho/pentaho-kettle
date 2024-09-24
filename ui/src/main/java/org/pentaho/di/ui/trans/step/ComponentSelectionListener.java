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

package org.pentaho.di.ui.trans.step;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.pentaho.di.trans.step.BaseStepMeta;

public class ComponentSelectionListener extends SelectionAdapter {
  private BaseStepMeta input;

  public ComponentSelectionListener( BaseStepMeta input ) {
    this.input = input;
  }
  @Override
  public void widgetSelected( SelectionEvent arg0 ) {
    input.setChanged();
  }
}
