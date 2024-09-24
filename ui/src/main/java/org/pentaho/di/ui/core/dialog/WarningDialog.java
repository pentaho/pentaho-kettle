/*! ******************************************************************************
 *
 * Pentaho Community Edition Project: data-refinery-pdi-plugin
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 * *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ********************************************************************************/

package org.pentaho.di.ui.core.dialog;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.util.Map;

public class WarningDialog extends Dialog {
  private final PropsUI props;
  private final String title;
  private final String message;
  private final Map<String, Listener> listenerMap;
  private Shell shell;

  public WarningDialog(
      final Shell parent, final String title, final String message, final Map<String, Listener> listenerMap ) {
    super( parent );
    this.title = title;
    this.message = message.trim();
    this.listenerMap = listenerMap;
    this.props = PropsUI.getInstance();

  }

  public void open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.RESIZE );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );
    shell.setLayout( new FormLayout() );
    shell.setText( title );
    props.setLook( shell );

    Label wlImage = new Label( shell, SWT.NONE );
    wlImage.setImage( GUIResource.getInstance().getImageWarning32() );
    FormData fdWarnImage = new FormData();
    fdWarnImage.left = new FormAttachment( 0, 15 );
    fdWarnImage.top = new FormAttachment( 0, 15 );
    fdWarnImage.height = 32;
    fdWarnImage.width = 32;
    wlImage.setLayoutData( fdWarnImage );
    props.setLook( wlImage );

    Label wlMessage = new Label( shell, SWT.FLAT  | SWT.WRAP );
    wlMessage.setText( message );
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment( wlImage, 15, SWT.RIGHT );
    fdMessage.right = new FormAttachment( 100, -15 );
    fdMessage.top = new FormAttachment( wlImage, 0, SWT.TOP );
    wlMessage.setLayoutData( fdMessage );
    props.setLook( wlMessage );


    Button spacer = new Button( shell, SWT.NONE );
    FormData fdSpacer = new FormData();
    fdSpacer.right = new FormAttachment( 100, 0 );
    fdSpacer.bottom = new FormAttachment( 100, -15 );
    fdSpacer.left = new FormAttachment( 100, -11 );
    fdSpacer.top = new FormAttachment( wlMessage, 15, SWT.BOTTOM );
    spacer.setLayoutData( fdSpacer );
    spacer.setVisible( false );
    props.setLook( spacer );

    Control attachTo = spacer;
    for ( String label : listenerMap.keySet() ) {
      Button wButton = new Button( shell, SWT.PUSH );
      wButton.setText( label );
      FormData fdButton = new FormData();
      fdButton.right = new FormAttachment( attachTo, -Const.MARGIN, SWT.LEFT );
      fdButton.bottom = new FormAttachment( attachTo, 0, SWT.BOTTOM );
      wButton.setLayoutData( fdButton );
      wButton.addListener( SWT.Selection, listenAndDispose( listenerMap.get( label ) ) );
      props.setLook( wButton );
      attachTo = wButton;
    }
    Point point = shell.computeSize( 436, SWT.DEFAULT );
    shell.setSize( point );
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  private Listener listenAndDispose( final Listener lsCancel ) {
    return new Listener() {
      @Override public void handleEvent( final Event event ) {
        lsCancel.handleEvent( event );
        shell.dispose();
      }
    };
  }
}
