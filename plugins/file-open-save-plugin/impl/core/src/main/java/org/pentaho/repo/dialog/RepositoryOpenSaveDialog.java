/*
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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
 */

package org.pentaho.repo.dialog;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.ui.core.dialog.ThinDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.platform.settings.ServerPort;
import org.pentaho.platform.settings.ServerPortRegistry;

/**
 * Created by bmorrise on 5/23/17.
 */
public class RepositoryOpenSaveDialog extends ThinDialog {

  public static final String STATE_SAVE = "save";
  public static final String STATE_OPEN = "open";
  private static final String DIALOG_TITLE = "Save";
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final String OSGI_SERVICE_PORT = "OSGI_SERVICE_PORT";
  private static final String CLIENT_PATH = "/file-open-save-impl-core/8.0-SNAPSHOT/index.html";
  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;

  public RepositoryOpenSaveDialog( Shell shell, int width, int height ) {
    super( shell, width, height );
  }

  public void open( String directory, String state ) {
    StringBuilder clientPath = new StringBuilder();
    clientPath.append( CLIENT_PATH );
    clientPath.append( !Utils.isEmpty( directory ) ? "#?path=" + directory : "#?" );
    clientPath.append( !Utils.isEmpty( directory ) ? "&" : "" );
    clientPath.append( !Utils.isEmpty( state ) ? "state=" + state : "" );
    super.createDialog( StringUtils.capitalize( state ), getRepoURL( clientPath.toString() ), OPTIONS, LOGO );

    new BrowserFunction( browser, "close" ) {
      @Override public Object function( Object[] arguments ) {
        browser.dispose();
        dialog.close();
        dialog.dispose();
        return true;
      }
    };

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private static Integer getOsgiServicePort() {
    // if no service port is specified try getting it from
    ServerPort osgiServicePort = ServerPortRegistry.getPort( OSGI_SERVICE_PORT );
    if ( osgiServicePort != null ) {
      return osgiServicePort.getAssignedPort();
    }
    return null;
  }

  private static String getRepoURL( String path ) {
    return "http://localhost:" + getOsgiServicePort() + path;
  }
}
