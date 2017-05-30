/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import java.awt.Desktop;
import java.net.URI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;

/**
 * Dialog that shows a warning when the Browser Environment is not supported.
 */
public class BrowserEnvironmentWarningDialog extends Dialog {

  private static Class<?> PKG = BrowserEnvironmentWarningDialog.class; // for i18n purposes, needed by Translator2!!
  private Shell shell;
  private PropsUI props;
  private Label warningIcon;
  private Text  description;
  private Link  link;
  private Button closeButton;
  private final int margin = Const.FORM_MARGIN * 3; //15
  private final int padding = margin * 2; //30
  private final int MAX_TEXT_WIDTH_UBUNTU = 418;
  private final int MAX_TEXT_WIDTH_WINDOWS = 286;
  private final int MAX_TEXT_WIDTH_MAC = 326;
  private final LogChannelInterface log = new LogChannel( this );
  private static final String URI_PATH = "/0D0/160/000";

  public BrowserEnvironmentWarningDialog( Shell parent ) {
    super( parent, SWT.NONE );
  }

  public void showWarningDialog( EnvironmentCase environment ) {
    switch ( environment ) {
      case UBUNTU:
        showUbuntuWarningDialog();
        break;
      case UBUNTU_THIN:
        showUbuntuThinWarningDialog();
        break;
      case MAC_OS_X:
        showMacWarningDialog();
        break;
      case MAC_OS_X_THIN:
        showMacThinWarningDialog();
        break;
      case WINDOWS:
        showWindowsWarningDialog();
        break;
      case WINDOWS_THIN:
        showWindowsThinWarningDialog();
        break;
      default:
        log.logBasic( "Unknown Environment" );
    }
  }

  private void showMacWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Mac" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink" ),
        EnvironmentCase.MAC_OS_X,
        MAX_TEXT_WIDTH_MAC );
  }

  private void showUbuntuWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title.Ubuntu" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Ubuntu" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink.Ubuntu" ),
        EnvironmentCase.UBUNTU,
        MAX_TEXT_WIDTH_UBUNTU );
  }

  private void showWindowsWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Windows" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink" ),
        EnvironmentCase.WINDOWS,
        MAX_TEXT_WIDTH_WINDOWS );
  }

  private void showMacThinWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Mac.Thin" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink" ),
        EnvironmentCase.MAC_OS_X_THIN,
        MAX_TEXT_WIDTH_MAC );
  }

  private void showUbuntuThinWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title.Ubuntu" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Ubuntu.Thin" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink.Ubuntu" ),
        EnvironmentCase.UBUNTU_THIN,
        MAX_TEXT_WIDTH_UBUNTU );
  }

  private void showWindowsThinWarningDialog() {
    showWarningDialog(
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Title" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.Message.Windows.Thin" ),
        BaseMessages.getString( PKG, "BrowserEnvironmentWarningDialog.HelpLink" ),
        EnvironmentCase.WINDOWS_THIN,
        MAX_TEXT_WIDTH_WINDOWS );
  }

  /**
   * showWarningDialog
   *
   * Shows a SWT dialog warning the user that something is wrong with the browser environment.
   *
   * @param title the title on the top of the window.
   * @param message the message at the center of the screen.
   * @param helpLink a string that contains a hyperlink to a help web page.
   * @param maxTextWidth the width for the text inside the dialog.
   */
  private void showWarningDialog( String title, String message, String helpLink, EnvironmentCase environment,
                                  int maxTextWidth )  {
    if ( this.getParent().isDisposed() ) {
      return;
    }

    this.props = PropsUI.getInstance();
    Display display = this.getParent().getDisplay();
    shell = new Shell( this.getParent(), SWT.TITLE | SWT.APPLICATION_MODAL );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = margin;
    formLayout.marginHeight = margin;
    shell.setLayout( formLayout ); // setting layout

    shell.setText( title ); //setting title of the window
    setWarningIcon( display ); //adding icon
    setWarningText( message, maxTextWidth ); //adding text
    setHelpLink( display, helpLink, maxTextWidth, environment ); //adding link
    setCloseButton(); //adding button

    shell.setSize( shell.computeSize( SWT.DEFAULT, SWT.DEFAULT, true ) );
    Rectangle screenSize = display.getPrimaryMonitor().getBounds();
    shell.setLocation( ( screenSize.width - shell.getBounds().width ) / 2, ( screenSize.height - shell.getBounds().height ) / 2 );
    closeButton.setFocus();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private void setWarningIcon( Display display ) {
    warningIcon = new Label( shell, SWT.NONE );
    Image image = display.getSystemImage( SWT.ICON_WARNING );
    warningIcon.setImage( image );
    props.setLook( warningIcon );
    FormData fdIcon = new FormData();
    fdIcon.left = new FormAttachment( 0, 0 );
    fdIcon.top = new FormAttachment( 0, 0 );
    fdIcon.right = new FormAttachment( 0, image.getBounds().width );
    fdIcon.bottom = new FormAttachment( 0, image.getBounds().height ); //icon should be at the top left corner
    warningIcon.setLayoutData( fdIcon );
  }

  private void setWarningText( String message, int maxTextWidth ) {
    description = new Text( shell, SWT.MULTI | SWT.LEFT | SWT.WRAP | SWT.NO_FOCUS | SWT.HIDE_SELECTION );
    description.setText( message );
    description.setEditable( false );
    FormData fdlDesc = new FormData();
    fdlDesc.left = new FormAttachment( warningIcon, margin ); // Text should be right of the icon and at the top
    fdlDesc.top = new FormAttachment( 0, 0 );
    fdlDesc.width = maxTextWidth;
    description.setLayoutData( fdlDesc );
    props.setLook( description );
  }

  private void setHelpLink( Display display, String helpLink, int maxTextWidth, EnvironmentCase environment ) {
    link = new Link( shell, SWT.SINGLE | SWT.WRAP );
    link.setText( helpLink );
    if ( environment == EnvironmentCase.MAC_OS_X || environment == EnvironmentCase.MAC_OS_X_THIN ) {
      FontData[] fD = link.getFont().getFontData();
      fD[0].setHeight( 13 );
      link.setFont( new Font( display, fD[0] ) );
    }
    FormData fdlink = new FormData();
    fdlink.left = new FormAttachment( warningIcon, margin ); // Link should be below description right of icon
    fdlink.top = new FormAttachment( description, margin );
    fdlink.width = maxTextWidth;
    link.setLayoutData( fdlink );
    props.setLook( link );

    link.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event event ) {
        if ( Desktop.isDesktopSupported() ) {
          try {
            Desktop.getDesktop().browse( new URI( Const.getDocUrl( URI_PATH ) ) );
          } catch ( Exception e ) {
            log.logError( "Error opening external browser", e );
          }
        }
      }
    } );
  }

  private void setCloseButton() {
    closeButton = new Button( shell, SWT.PUSH );
    closeButton.setText( BaseMessages.getString( PKG, "System.Button.Close" ) );
    FormData fdbutton = new FormData();
    fdbutton.right = new FormAttachment( 100, 0 ); //Button should below the link and separated by 30
    fdbutton.top = new FormAttachment( link, padding );
    fdbutton.height = padding;
    closeButton.setLayoutData( fdbutton );
    props.setLook( closeButton );

    // Add listeners
    closeButton.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        close();
      }
    } );
  }

  /**
   * dispose
   *
   * used to dispose the dialog.
   */
  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  private void close() {
    dispose();
  }

  public enum EnvironmentCase {
    UBUNTU, UBUNTU_THIN, MAC_OS_X, MAC_OS_X_THIN, WINDOWS, WINDOWS_THIN
  }

}
