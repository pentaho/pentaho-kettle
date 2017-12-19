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
