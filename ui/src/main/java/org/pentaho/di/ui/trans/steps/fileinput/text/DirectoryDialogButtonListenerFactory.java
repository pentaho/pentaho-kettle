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


package org.pentaho.di.ui.trans.steps.fileinput.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DirectoryDialogButtonListenerFactory {
  public static final SelectionAdapter getSelectionAdapter( final Shell shell, final Text destination ) {
    // Listen to the Browse... button
    return new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        DirectoryDialog dialog = new DirectoryDialog( shell, SWT.OPEN );
        if ( destination.getText() != null ) {
          String fpath = destination.getText();
          // String fpath = StringUtil.environmentSubstitute(destination.getText());
          dialog.setFilterPath( fpath );
        }

        if ( dialog.open() != null ) {
          String str = dialog.getFilterPath();
          destination.setText( str );
        }
      }
    };
  }
}
