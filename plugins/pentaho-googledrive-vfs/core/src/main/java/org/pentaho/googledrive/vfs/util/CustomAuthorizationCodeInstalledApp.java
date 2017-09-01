/*!
* Copyright 2010 - 2017 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
