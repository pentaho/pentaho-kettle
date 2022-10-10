/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.util.Objects;

public class RenameDialog extends Dialog {
  private static final Class<?> PKG = RenameDialog.class;
  private Shell shell;
  private String renameTextValue;

  public RenameDialog( Shell parent ) {
    super( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
  }

  public String open( String fileName ) {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET );
    shell.setImage( Objects.requireNonNull( GUIResource.getInstance() ).getImageSpoon() );
    shell.setLayout( new FormLayout() );
    shell.setText( BaseMessages.getString( PKG, "rename-dialog.title" ) );
    Objects.requireNonNull( PropsUI.getInstance() ).setLook( shell );

    Text text = new Text( shell, SWT.SINGLE | SWT.BORDER );
    text.setSize( 20, text.getSize().y );
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment( null, 15, SWT.RIGHT );
    fdMessage.right = new FormAttachment( 100, -15 );
    fdMessage.top = new FormAttachment( null, 0, SWT.TOP );
    text.setLayoutData( fdMessage );
    PropsUI.getInstance().setLook( text );

    Button spacer = new Button( shell, SWT.NONE );
    FormData fdSpacer = new FormData();
    fdSpacer.right = new FormAttachment( 100, 0 );
    fdSpacer.bottom = new FormAttachment( 100, -15 );
    fdSpacer.left = new FormAttachment( 100, -11 );
    fdSpacer.top = new FormAttachment( text, 15, SWT.BOTTOM );
    spacer.setLayoutData( fdSpacer );
    spacer.setVisible( false );
    PropsUI.getInstance().setLook( spacer );

    Control attachTo = spacer;

    Button cancelButton = new Button( shell, SWT.PUSH );
    cancelButton.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.cancel.button" ) );
    FormData fd1Button = new FormData();
    fd1Button.right = new FormAttachment( attachTo, -Const.MARGIN, SWT.LEFT );
    fd1Button.bottom = new FormAttachment( attachTo, 0, SWT.BOTTOM );
    cancelButton.setLayoutData( fd1Button );
    PropsUI.getInstance().setLook( cancelButton );
    attachTo = cancelButton;

    Button okButton = new Button( shell, SWT.PUSH );
    okButton.setText( BaseMessages.getString( PKG, "file-open-save-plugin.app.ok.button" ) );
    FormData fdButton = new FormData();
    fdButton.right = new FormAttachment( attachTo, -Const.MARGIN, SWT.LEFT );
    fdButton.bottom = new FormAttachment( attachTo, 0, SWT.BOTTOM );
    okButton.setLayoutData( fdButton );
    PropsUI.getInstance().setLook( okButton );

    text.addListener( SWT.Modify, new Listener() {
      public void handleEvent( Event event ) {
        try {
          renameTextValue = text.getText();
        } catch ( Exception e ) {
        }
      }
    } );

    okButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        renameTextValue = text.getText();
        shell.dispose();
      }
    } );

    cancelButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        renameTextValue = null;
        shell.dispose();
      }
    } );

    shell.addListener( SWT.Traverse, new Listener() {
      public void handleEvent( Event event ) {
        if ( event.detail == SWT.TRAVERSE_ESCAPE ) {
          event.doit = false;
        }
      }
    } );

    shell.addListener( SWT.Traverse, new Listener() {
      public void handleEvent( Event event ) {
        if ( event.detail == SWT.TRAVERSE_ESCAPE ) {
          event.doit = false;
        }
      }
    } );

    text.setText( fileName );
    Point point = shell.computeSize( 436, SWT.DEFAULT );
    shell.setSize( point );
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return renameTextValue;
  }

}