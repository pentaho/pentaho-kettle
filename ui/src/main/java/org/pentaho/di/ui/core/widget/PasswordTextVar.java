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
