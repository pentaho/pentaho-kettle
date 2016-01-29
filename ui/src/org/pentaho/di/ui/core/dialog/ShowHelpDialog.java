/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ShowHelpDialog extends Dialog {
  private static Class<?> PKG = DatabaseDialog.class; // for i18n purposes, needed by Translator2!!

  private String dialogTitle;
  private String url;
  private String header;

  private Button wOK;
  private FormData fdOK;
  private Listener lsOK;

  private Browser wBrowser;
  private FormData fdBrowser;

  private Shell shell;
  private PropsUI props;

  private int buttonHeight = 30;
  private int headerHeight = 55;
  private int headerLabelPosition = 10;

  public ShowHelpDialog( Shell parent, String dialogTitle, String url, String header ) {
    super( parent, SWT.NONE );
    props = PropsUI.getInstance();
    this.dialogTitle = dialogTitle;
    this.header = header;
    this.url = url;
  }

  protected Shell createShell( Shell parent ) {
    return new Shell( parent, SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL );
  }

  public void open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = createShell( parent );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();

    shell.setLayout( formLayout );
    shell.setText( dialogTitle );

    int margin = Const.MARGIN;

    // Header
    Label wHeader = new Label( shell, SWT.NONE );
    wHeader.setText( header );
    wHeader.setBackground( wHeader.getParent().getBackground() );
    FontData[] fD = wHeader.getFont().getFontData();
    fD[ 0 ].setHeight( 16 );
    wHeader.setFont( new Font( display, fD[ 0 ] ) );
    FormData fdHeader = new FormData();
    fdHeader.top = new FormAttachment( 0, headerLabelPosition );
    fdHeader.left = new FormAttachment( 0, margin );
    wHeader.setLayoutData( fdHeader );

    // Canvas
    wBrowser = new Browser( shell, SWT.NONE );
    props.setLook( wBrowser );

    fdBrowser = new FormData();
    fdBrowser.left = new FormAttachment( 0, 0 );
    fdBrowser.top = new FormAttachment( 0, headerHeight );
    fdBrowser.right = new FormAttachment( 100, 0 );
    fdBrowser.bottom = new FormAttachment( 100, -buttonHeight );
    wBrowser.setLayoutData( fdBrowser );

    // Composite
    Composite buttonsPane = new Composite( shell, SWT.NONE );
    Color grey = new Color( display, 240, 240, 240 );
    buttonsPane.setBackground( grey );
    buttonsPane.setLayout( new FormLayout() );
    FormData fdButtonsPane = new FormData();
    fdButtonsPane.left = new FormAttachment( 0, 0 );
    fdButtonsPane.right = new FormAttachment( 100, 0 );
    fdButtonsPane.bottom = new FormAttachment( 100, 0 );
    buttonsPane.setLayoutData( fdButtonsPane );

    // Some buttons
    wOK = new Button( buttonsPane, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wOK.setBackground( grey );
    fdOK = new FormData();
    fdOK.left = new FormAttachment( 50, 0 );
    fdOK.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOK );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );

    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        ok();
      }
    } );

    wBrowser.setUrl( url );

    BaseStepDialog.setSize( shell, 800, 600, true );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  public void dispose() {
    shell.dispose();
  }

  private void ok() {
    dispose();
  }

}
