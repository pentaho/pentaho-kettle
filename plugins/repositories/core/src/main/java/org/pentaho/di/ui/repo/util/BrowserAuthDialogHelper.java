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

package org.pentaho.di.ui.repo.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService;

/**
 * Shared UI helper for browser-authentication dialogs.
 */
public final class BrowserAuthDialogHelper {

  private static final Class<?> PKG = BrowserAuthDialogHelper.class;

  private BrowserAuthDialogHelper() {
  }

  /**
   * Ensures there is no active browser-authentication flow before starting a new one.
   *
   * @return true if caller should continue and start authentication; false to keep waiting for existing flow.
   */
  public static boolean confirmCancelExistingLoginIfNeeded( Shell parentShell,
                                                             BrowserAuthenticationService authService ) {
    if ( !authService.isAuthInProgress() ) {
      return true;
    }

    MessageBox mb = new MessageBox( parentShell, SWT.YES | SWT.NO | SWT.ICON_WARNING );
    mb.setText( BaseMessages.getString( PKG, "BrowserAuthDialogHelper.LoginInProgressTitle" ) );
    mb.setMessage( BaseMessages.getString( PKG, "BrowserAuthDialogHelper.LoginInProgressMessage" ) );

    if ( mb.open() == SWT.YES ) {
      authService.cancelCurrentAuth();
      return true;
    }

    return false;
  }
}