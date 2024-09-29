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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.WidgetUtils;

public abstract class Input<Txt extends Control> extends Composite {
  protected Label label;
  protected Txt input;

  protected Input( VariableSpace space, Composite composite, int width1, int width2 ) {
    super( composite, SWT.NONE );
    WidgetUtils.setFormLayout( this, 0 );

    label = new Label( this, SWT.LEFT );
    initText( space, composite, SWT.LEFT | SWT.SINGLE | SWT.BORDER );
    input.setLayoutData( new FormDataBuilder().top( label ).left().right( width1, width2 ).result() );
  }

  abstract void initText( VariableSpace space, Composite composite, int flags );

  public void setText( String text ) {
    label.setText( text );
  }

  public Label getLabel() {
    return label;
  }

  public Txt getInput() {
    return input;
  }
}
