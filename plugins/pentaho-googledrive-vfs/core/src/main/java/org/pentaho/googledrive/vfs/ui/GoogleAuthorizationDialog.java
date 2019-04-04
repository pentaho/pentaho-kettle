/*!
* Copyright (C) 2010-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.googledrive.vfs.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.googledrive.vfs.util.CustomLocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

public class GoogleAuthorizationDialog extends Dialog {

  protected int width = 620;
  protected int height = 650;
  protected Browser browser;
  protected Shell dialog;
  protected Display display;

  private static final int OPTIONS = SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM;
  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private VerificationCodeReceiver receiver;

  public GoogleAuthorizationDialog( Shell shell, VerificationCodeReceiver receiver ) {
    super( shell );
    this.receiver = receiver;
  }

  public void open( String url ) {
    createDialog( "Google Drive", url, OPTIONS, LOGO );
    if ( receiver != null ) {
      ( (CustomLocalServerReceiver) receiver ).setUrl( url );
    }

    while ( !dialog.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private void createDialog( String title, String url, int options, Image logo ) {

    Shell parent = getParent();
    display = parent.getDisplay();

    dialog = new Shell( parent, options );
    dialog.setText( title );
    dialog.setImage( logo );
    PropsUI props = PropsUI.getInstance();
    props.setLook( dialog );
    dialog.setSize( width, height );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    dialog.setLayout( formLayout );

    try {

      Label helpButton = new Label( dialog, SWT.NONE );
      helpButton.setImage( new Image( display, GoogleAuthorizationDialog.class.getResourceAsStream( "/images/help.png" ) ) );
      FormData helpButtonFormData = new FormData();
      helpButtonFormData.left = new FormAttachment( 0, 15 );
      helpButtonFormData.bottom = new FormAttachment( 100, -24 );
      helpButton.setLayoutData( helpButtonFormData );

      StyledText helpLabel = new StyledText( dialog, SWT.NONE );
      helpLabel.setText( "Help" );
      helpLabel.setCaret( null );
      helpLabel.setEditable( false );

      props.setLook( helpLabel );
      helpLabel.setFont( new Font( display, "Open Sans Regular", 11, SWT.NORMAL ) );
      helpLabel.setForeground( new Color( display, 0, 94, 170 ) );
      FormData helpLabelFormData = new FormData();
      helpLabelFormData.left = new FormAttachment( 0, 40 );
      helpLabelFormData.bottom = new FormAttachment( 100, -27 );
      helpLabel.setLayoutData( helpLabelFormData );

      helpLabel.addListener( SWT.MouseUp, new Listener() {
        public void handleEvent( Event event ) {
        }
      } );

      helpLabel.addListener( SWT.MouseEnter, new Listener() {
        public void handleEvent( Event event ) {
          StyleRange style1 = new StyleRange();
          style1.start = 0;
          style1.length = 4;
          style1.underline = true;
          helpLabel.setStyleRange( style1 );
          helpLabel.setForeground( new Color( display, 0, 0, 0 ) );
          helpLabel.setCursor( new Cursor( display, SWT.CURSOR_HAND ) );
        }
      } );

      helpLabel.addListener( SWT.MouseExit, new Listener() {
        public void handleEvent( Event event ) {
          StyleRange style1 = new StyleRange();
          style1.start = 0;
          style1.length = 4;
          style1.underline = false;
          helpLabel.setStyleRange( style1 );
          helpLabel.setForeground( new Color( display, 0, 94, 170 ) );
        }
      } );

      Label cancelButton = new Label( dialog, SWT.NONE );
      cancelButton
          .setImage( new Image( display, GoogleAuthorizationDialog.class.getResourceAsStream( "/images/close-button.png" ) ) );
      FormData cancelButtonFormData = new FormData();
      cancelButtonFormData.right = new FormAttachment( 100, -15 );
      cancelButtonFormData.bottom = new FormAttachment( 100, -15 );
      cancelButton.setLayoutData( cancelButtonFormData );

      cancelButton.addListener( SWT.MouseUp, new Listener() {
        public void handleEvent( Event event ) {
          browser.dispose();
          dialog.close();
          dialog.dispose();
        }
      } );

      cancelButton.addListener( SWT.MouseEnter, new Listener() {
        public void handleEvent( Event event ) {
          cancelButton.setImage(
              new Image( display, GoogleAuthorizationDialog.class.getResourceAsStream( "/images/close-button-hover.png" ) ) );
          cancelButton.setCursor( new Cursor( display, SWT.CURSOR_HAND ) );
        }
      } );

      cancelButton.addListener( SWT.MouseExit, new Listener() {
        public void handleEvent( Event event ) {
          cancelButton.setImage(
              new Image( display, GoogleAuthorizationDialog.class.getResourceAsStream( "/images/close-button.png" ) ) );
        }
      } );

      Label separator = new Label( dialog, SWT.HORIZONTAL | SWT.SEPARATOR );
      FormData separatorFormData = new FormData();
      separatorFormData.left = new FormAttachment( 0, 15 );
      separatorFormData.right = new FormAttachment( 100, -15 );
      separatorFormData.bottom = new FormAttachment( cancelButton, -15 );
      separator.setLayoutData( separatorFormData );

      browser = new Browser( dialog, SWT.NONE );
      browser.setUrl( url );

      FormData browserFormData = new FormData();
      browserFormData.top = new FormAttachment( 0, 5 );
      browserFormData.bottom = new FormAttachment( separator, -5 );
      browserFormData.left = new FormAttachment( 0, 5 );
      browserFormData.right = new FormAttachment( 100, -5 );
      browser.setLayoutData( browserFormData );

      browser.addCloseWindowListener( new CloseWindowListener() {
        @Override public void close( WindowEvent event ) {
          Browser browser = (Browser) event.widget;
          Shell shell = browser.getShell();
          shell.close();
        }
      } );
    } catch ( Exception e ) {
      MessageBox messageBox = new MessageBox( dialog, SWT.ICON_ERROR | SWT.OK );
      messageBox.setMessage( "Browser cannot be initialized." );
      messageBox.setText( "Exit" );
      messageBox.open();
    }
    setPosition();
    dialog.open();
  }

  private void setPosition() {
    Rectangle shellBounds = getParent().getBounds();
    Point dialogSize = dialog.getSize();
    dialog.setLocation( shellBounds.x + ( shellBounds.width - dialogSize.x ) / 2,
        shellBounds.y + ( shellBounds.height - dialogSize.y ) / 2 );
  }
}
