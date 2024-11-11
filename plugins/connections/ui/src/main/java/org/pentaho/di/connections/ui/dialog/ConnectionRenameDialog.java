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


package org.pentaho.di.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

/**
 * Created by tkafalas.
 */
public class ConnectionRenameDialog extends Dialog {

  private static Class<?> PKG = ConnectionRenameDialog.class;

  private Shell shell;
  private PropsUI props;
  private int returnValue;


  public ConnectionRenameDialog( Shell shell ) {
    super( shell );
    props = PropsUI.getInstance();
  }

  public int open( String originalName, String connectionName ) {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "ConnectionRenameDialog.renameOrCopy.Title" ) );
    shell.setImage( getImage() );

    Image image = display.getSystemImage( SWT.ICON_WARNING );

    Label wIcon = new Label( shell, SWT.NONE );
    props.setLook( wIcon );
    wIcon.setImage( image );
    FormData fdIcon = new FormData();
    fdIcon.left = new FormAttachment( 0, 0 );
    fdIcon.top = new FormAttachment( 0, 0 );
    fdIcon.right = new FormAttachment( 0, image.getBounds().width );
    fdIcon.bottom = new FormAttachment( 0, image.getBounds().height );
    wIcon.setLayoutData( fdIcon );

    Composite wcMessage = new Composite( shell, SWT.NONE );
    props.setLook( wcMessage );
    wcMessage.setLayout( new GridLayout() );

    Text wlMessage = new Text( wcMessage, SWT.MULTI | SWT.WRAP );
    wlMessage.setEditable( false );
    props.setLook( wlMessage );
    wlMessage.setText(
      BaseMessages.getString( PKG, "ConnectionRenameDialog.renameOrCopy.Label", originalName, connectionName ) );
    GridData gdlLocal = new GridData( GridData.FILL_HORIZONTAL );
    gdlLocal.widthHint = 300;
    wlMessage.setLayoutData( gdlLocal );

    FormData fdcMessage = new FormData();
    fdcMessage.left = new FormAttachment( wIcon, 15 );
    fdcMessage.top = new FormAttachment( 0 );
    fdcMessage.right = new FormAttachment( 100 );
    wcMessage.setLayoutData( fdcMessage );

    Button wbNo = new Button( shell, SWT.PUSH );
    wbNo.setText( BaseMessages.getString( PKG, "ConnectionRenameDialog.renameOrCopy.rename" ) );
    wbNo.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        quit( SWT.NO );
      }
    } );
    FormData fdlNo = new FormData();
    fdlNo.top = new FormAttachment( wcMessage, 30 );
    fdlNo.right = new FormAttachment( 100 );
    wbNo.setLayoutData( fdlNo );

    Button wbYes = new Button( shell, SWT.PUSH );
    wbYes.setText( BaseMessages.getString( PKG, "ConnectionRenameDialog.renameOrCopy.copy" ) );
    wbYes.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        quit( SWT.YES );
      }
    } );

    FormData fdlYes = new FormData();
    fdlYes.top = new FormAttachment( wcMessage, 30 );
    fdlYes.right = new FormAttachment( wbNo, -10 );
    wbYes.setLayoutData( fdlYes );

    Button wbCancel = new Button( shell, SWT.PUSH );
    wbCancel.setText( BaseMessages.getString( PKG, "ConnectionRenameDialog.cancel" ) );
    wbCancel.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        quit( SWT.CANCEL );
      }
    } );

    FormData fdlCancel = new FormData();
    fdlCancel.top = new FormAttachment( wcMessage, 30 );
    fdlCancel.right = new FormAttachment( wbYes, -10 );
    wbCancel.setLayoutData( fdlCancel );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    shell.layout();
    shell.pack( true );
    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return returnValue;
  }

  private Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "images/environments.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
  }

  private void cancel() {
    quit( SWT.CANCEL );
  }

  private void quit( int returnValue ) {
    this.returnValue = returnValue;
    shell.dispose();
  }
}
