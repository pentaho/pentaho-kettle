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


package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.WidgetUtils;

public class InputButton extends Composite {
  private Button button;

  public InputButton( Composite composite, int width ) {
    super( composite, SWT.NONE );
    WidgetUtils.setFormLayout( this, 0 );

    button = new Button( this, SWT.PUSH );
    button.setLayoutData( new FormDataBuilder().right().bottom().width( width ).result() );
  }

  public void setText( String text ) {
    button.setText( text );
  }

  public Button getButton() {
    return button;
  }
}
