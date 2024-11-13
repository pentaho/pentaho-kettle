/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.util.Map;
import java.util.Objects;

public class PasteConfirmationDialog extends Dialog {
  private static final Class<?> PKG = PasteConfirmationDialog.class;
  private Shell shell;
  boolean isApplyToAll = false;
  private final Map<String, ActionListener> listenerMap;

  public PasteConfirmationDialog( Shell parent, Map<String, ActionListener> listeners ) {
    // Pass the default styles here
    super( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL );
    this.listenerMap = listeners;
  }

  public void open( String fileName ) {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET );
    shell.setImage( Objects.requireNonNull( GUIResource.getInstance() ).getImageSpoon() );
    shell.setLayout( new FormLayout() );
    shell.setText( BaseMessages.getString( PKG, "paste-confirmation-dialog.title" ) );
    Objects.requireNonNull( PropsUI.getInstance() ).setLook( shell );

    Label wlImage = new Label( shell, SWT.NONE );
    wlImage.setImage( GUIResource.getInstance().getImageWarning32() );
    FormData fdWarnImage = new FormData();
    fdWarnImage.left = new FormAttachment( 0, 15 );
    fdWarnImage.top = new FormAttachment( 0, 15 );
    fdWarnImage.height = 32;
    fdWarnImage.width = 32;
    wlImage.setLayoutData( fdWarnImage );
    PropsUI.getInstance().setLook( wlImage );

    Label wlMessage = new Label( shell, SWT.FLAT | SWT.WRAP );
    wlMessage.setText( BaseMessages.getString( PKG, "paste-confirmation-dialog.messages", fileName ) );
    FormData fdMessage = new FormData();
    fdMessage.left = new FormAttachment( wlImage, 15, SWT.RIGHT );
    fdMessage.right = new FormAttachment( 100, -15 );
    fdMessage.top = new FormAttachment( wlImage, 0, SWT.TOP );
    wlMessage.setLayoutData( fdMessage );
    PropsUI.getInstance().setLook( wlMessage );

    Button spacer = new Button( shell, SWT.NONE );
    FormData fdSpacer = new FormData();
    fdSpacer.right = new FormAttachment( 100, 0 );
    fdSpacer.bottom = new FormAttachment( 100, -15 );
    fdSpacer.left = new FormAttachment( 100, -11 );
    fdSpacer.top = new FormAttachment( wlMessage, 15, SWT.BOTTOM );
    spacer.setLayoutData( fdSpacer );
    spacer.setVisible( false );
    PropsUI.getInstance().setLook( spacer );

    Control attachTo = spacer;

    Button checkButton = new Button( shell, SWT.CHECK );
    checkButton.setText( BaseMessages.getString( PKG, "paste-confirmation-dialog.applyToAll.label" ) );
    FormData fdCheckButton = new FormData();
    fdCheckButton.bottom = new FormAttachment( 100, -15 );
    fdCheckButton.left = new FormAttachment( 0, 15 );
    checkButton.setLayoutData( fdCheckButton );
    checkButton.addListener( SWT.Selection, event -> isApplyToAll = ( (Button) event.widget ).getSelection() );
    PropsUI.getInstance().setLook( checkButton );


    for ( Map.Entry<String, ActionListener> item : listenerMap.entrySet() ) {
      Button wButton = new Button( shell, SWT.PUSH );
      wButton.setText( item.getKey() );
      FormData fdButton = new FormData();
      fdButton.right = new FormAttachment( attachTo, -Const.MARGIN, SWT.LEFT );
      fdButton.bottom = new FormAttachment( attachTo, 0, SWT.BOTTOM );
      wButton.setLayoutData( fdButton );
      wButton.addListener( SWT.Selection, listenAndDispose( item.getValue() ) );
      PropsUI.getInstance().setLook( wButton );
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

  private Listener listenAndDispose( final ActionListener listener ) {
    return event -> {
      if ( Objects.nonNull( listener ) ) {
        listener.handleEvent( event, isApplyToAll );
      }
      shell.dispose();
    };
  }

  public interface ActionListener {
    void handleEvent( Event event, boolean applyToAll );
  }
}