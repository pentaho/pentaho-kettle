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


package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;

public class AboutDialog extends Dialog {

  public AboutDialog( Shell parent ) {
    super( parent );
  }

  public void open() throws KettleException {
    Shell splashShell = new Shell( getParent(), SWT.APPLICATION_MODAL );
    final Splash splash = new Splash( getParent().getDisplay(), splashShell );
    splashShell.addMouseListener( new MouseAdapter() {

      public void mouseUp( MouseEvent mouseevent ) {
        splash.dispose();
      }

    } );
  }

}
