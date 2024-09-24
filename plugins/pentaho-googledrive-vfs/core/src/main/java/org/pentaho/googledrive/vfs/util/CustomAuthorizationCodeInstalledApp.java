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

package org.pentaho.googledrive.vfs.util;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.googledrive.vfs.ui.GoogleAuthorizationDialog;

import java.io.IOException;

public class CustomAuthorizationCodeInstalledApp extends AuthorizationCodeInstalledApp {

  public CustomAuthorizationCodeInstalledApp( AuthorizationCodeFlow flow, VerificationCodeReceiver receiver ) {
    super( flow, receiver );
  }

  protected void onAuthorization( AuthorizationCodeRequestUrl authorizationUrl ) throws IOException {
    String url = authorizationUrl.build();
    Spoon spoon = Spoon.getInstance();
    if ( spoon != null ) {

      Display.getDefault().syncExec( new Runnable() {

        public void run() {
          Shell shell = spoon.getShell();
          GoogleAuthorizationDialog authorizationDialog = new GoogleAuthorizationDialog( shell, getReceiver() );
          authorizationDialog.open( url );
        }
      } );

    } else {
      browse( url );
    }
  }
}
