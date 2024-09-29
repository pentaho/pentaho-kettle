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

package org.pentaho.di.ui.spoon.dialog;

import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows a pop-up message dialog after the sub-transformation creation.
 *
 * @author Aliaksandr Kastenka
 *
 */
public class NewSubtransDialog extends Dialog {
  private static Class<?> PKG = NewSubtransDialog.class; // for i18n purposes, needed by Translator2!!

  private Button wShow;

  private Label wlInfo, wiInfo;
  private FormData fdlInfo, fdiInfo, fdShowButton;

  private Button wOK;
  private Listener lsOK;

  private Shell shell;
  private PropsUI props;
  boolean doNotShow = false;

  public NewSubtransDialog( Shell parent, int style ) {
    super( parent, style );
    props = PropsUI.getInstance();
  }

  public boolean open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "NewSubtransDialog.Title" ) );
    shell.setImage( GUIResource.getInstance().getImageLogoSmall() );

    wiInfo = new Label( shell, SWT.NONE );
    wiInfo.setImage( display.getSystemImage( SWT.ICON_INFORMATION ) );

    props.setLook( wiInfo );
    fdiInfo = new FormData();
    fdiInfo.left = new FormAttachment( 0, 0 );
    fdiInfo.top = new FormAttachment( 0, 0 );
    wiInfo.setLayoutData( fdiInfo );

    wlInfo = new Label( shell, SWT.WRAP );
    wlInfo.setText( BaseMessages.getString( PKG, "NewSubtransDialog.TransCreated" ) );

    props.setLook( wlInfo );
    fdlInfo = new FormData();
    fdlInfo.left = new FormAttachment( wiInfo, 15 );
    fdlInfo.right = new FormAttachment( 100, 0 );
    fdlInfo.width = 320;
    fdlInfo.top = new FormAttachment( wiInfo, 0, SWT.TOP );
    fdlInfo.bottom = new FormAttachment( wiInfo, 0, SWT.BOTTOM );
    wlInfo.setLayoutData( fdlInfo );

    wShow = new Button( shell, SWT.CHECK );
    wShow.setText( BaseMessages.getString( PKG, "NewSubtransDialog.DoNotShowAgain" ) );
    wShow.setLayoutData( new FormDataBuilder().left().result() );
    props.setLook( wShow );
    fdShowButton = new FormData();
    fdShowButton.left = new FormAttachment( wlInfo, 0, SWT.LEFT );
    fdShowButton.right = new FormAttachment( wlInfo, 0, SWT.RIGHT );
    fdShowButton.top = new FormAttachment( wlInfo, 15, SWT.BOTTOM );
    wShow.setLayoutData( fdShowButton );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( 100, 0 );
    fdOk.top = new FormAttachment( wShow, 30, SWT.BOTTOM );
    wOK.setLayoutData( fdOk );

    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        close();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        close();
      }
    } );

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return doNotShow;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  private void close() {
    doNotShow = wShow.getSelection();
    dispose();
  }
}
