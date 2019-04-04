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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;

public class PasswordTextVar extends TextVar {

  public PasswordTextVar( VariableSpace space, Composite composite, int flags ) {
    super( space, composite, flags | SWT.PASSWORD, null, null, null );
  }

  public PasswordTextVar( VariableSpace space, Composite composite, int flags, String toolTipText ) {
    super( space, composite, flags | SWT.PASSWORD, toolTipText, null, null );
  }

  public PasswordTextVar( VariableSpace space, Composite composite, int flags,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface ) {
    super( space, composite, flags | SWT.PASSWORD, null, getCaretPositionInterface, insertTextInterface );
  }

  public PasswordTextVar( VariableSpace space, Composite composite, int flags, String toolTipText,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface ) {
    super( space, composite, flags | SWT.PASSWORD, toolTipText, getCaretPositionInterface, insertTextInterface );
  }

  @Override
  protected ModifyListener getModifyListenerTooltipText( final Text textField ) {
    return new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        textField.setToolTipText( toolTipText );
      }
    };
  }
}
