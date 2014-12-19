/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.cluster.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class NamedClusterDialog extends Dialog {

  private Shell shell;
  private static Class<?> PKG = NamedClusterDialog.class;
  private NamedConfiguration configuration;
  private boolean result;

  public NamedClusterDialog( Shell parent, NamedConfiguration configuration ) {
    super( parent, SWT.NONE );
    this.configuration = configuration;
  }

  public boolean open() {
    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    shell.setText( BaseMessages.getString( PKG, "ClusterSchemaDialog.Shell.Title" ) ); // TODO PENDING!!!!!
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout( formLayout );

    new NamedClusterComposite( shell, configuration );

    Button okButton = new Button( shell, SWT.PUSH );
    okButton.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    okButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );

    Button cancelButton = new Button( shell, SWT.PUSH );
    cancelButton.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    cancelButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );

    Button[] buttons = new Button[] { okButton, cancelButton };
    BaseStepDialog.positionBottomButtons( shell, buttons, Const.MARGIN, null );

    result = false;
    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return result;
  }

  private void ok() {
    result = true;
    shell.dispose();
  }

  private void cancel() {
    shell.dispose();
  }
}
