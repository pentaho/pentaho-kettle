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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.WidgetUtils;

public class CheckBox extends Composite {
  private Button button;
  private Label label;

  public CheckBox( Composite composite ) {
    super( composite, SWT.NONE );
    WidgetUtils.setFormLayout( this, 0 );

    button = new Button( this, SWT.CHECK );
    button.setLayoutData( new FormDataBuilder().left().result() );

    label = new Label( this, SWT.LEFT );
    label.setLayoutData( new FormDataBuilder().left( button, ConstUI.SMALL_MARGIN ).result() );
  }

  public void setText( String text ) {
    label.setText( text );
  }

  public Button getButton() {
    return button;
  }

  public Label getLabel() {
    return label;
  }
}
