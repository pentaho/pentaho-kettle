/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.abort;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.abort.Abort;
import org.pentaho.di.trans.steps.abort.AbortMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

public class AbortDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = Abort.class; // for i18n purposes, needed by Translator2!!

  private Label wlRowThreshold;
  private TextVar wRowThreshold;
  private FormData fdlRowThreshold, fdRowThreshold;

  private Label wlMessage;
  private TextVar wMessage;
  private FormData fdlMessage, fdMessage;

  private Button wAlwaysLogRows;
  private FormData fdAlwaysLogRows;

  private Button wAbortWithError;
  private FormData fdAbortWithError;

  private AbortMeta input;

  public AbortDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (AbortMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "AbortDialog.Shell.Title" ) );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "AbortDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, 0 );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.width = 150;
    fdStepname.left = new FormAttachment( 0, 0 );
    fdStepname.top = new FormAttachment( wlStepname, 5 );
    wStepname.setLayoutData( fdStepname );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.left = new FormAttachment( wStepname, 10 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    Label spacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 1;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    spacer.setLayoutData( fdSpacer );

    // RowThreshold line
    wlRowThreshold = new Label( shell, SWT.RIGHT );
    wlRowThreshold.setText( BaseMessages.getString( PKG, "AbortDialog.RowThreshold.Label" ) );
    props.setLook( wlRowThreshold );
    fdlRowThreshold = new FormData();
    fdlRowThreshold.left = new FormAttachment( 0, 0 );
    fdlRowThreshold.top = new FormAttachment( spacer, 20 );
    wlRowThreshold.setLayoutData( fdlRowThreshold );
    wRowThreshold = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wRowThreshold.setText( "" );
    props.setLook( wRowThreshold );
    wRowThreshold.addModifyListener( lsMod );
    wRowThreshold.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.RowThreshold.Tooltip" ) );
    wRowThreshold.addModifyListener( lsMod );
    fdRowThreshold = new FormData();
    fdRowThreshold.left = new FormAttachment( 0, 0 );
    fdRowThreshold.top = new FormAttachment( wlRowThreshold, 5 );
    fdRowThreshold.width = 174;
    wRowThreshold.setLayoutData( fdRowThreshold );

    // Message line
    wlMessage = new Label( shell, SWT.RIGHT );
    wlMessage.setText( BaseMessages.getString( PKG, "AbortDialog.AbortMessage.Label" ) );
    props.setLook( wlMessage );
    fdlMessage = new FormData();
    fdlMessage.left = new FormAttachment( 0, 0 );
    fdlMessage.top = new FormAttachment( wRowThreshold, 10 );
    wlMessage.setLayoutData( fdlMessage );
    wMessage = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wMessage.setText( "" );
    props.setLook( wMessage );
    wMessage.addModifyListener( lsMod );
    wMessage.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.AbortMessage.Tooltip" ) );
    wMessage.addModifyListener( lsMod );
    fdMessage = new FormData();
    fdMessage.left = new FormAttachment( 0, 0 );
    fdMessage.top = new FormAttachment( wlMessage, 5 );
    fdMessage.right = new FormAttachment( 100, 0 );
    fdMessage.width = 274;
    wMessage.setLayoutData( fdMessage );

    wAlwaysLogRows = new Button( shell, SWT.CHECK );
    wAlwaysLogRows.setText( BaseMessages.getString( PKG, "AbortDialog.AlwaysLogRows.Label" ) );
    props.setLook( wAlwaysLogRows );
    wAlwaysLogRows.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.AlwaysLogRows.Tooltip" ) );
    fdAlwaysLogRows = new FormData();
    fdAlwaysLogRows.left = new FormAttachment( 0, 0 );
    fdAlwaysLogRows.top = new FormAttachment( wMessage, 10 );
    wAlwaysLogRows.setLayoutData( fdAlwaysLogRows );
    wAlwaysLogRows.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    wAbortWithError = new Button( shell, SWT.CHECK );
    wAbortWithError.setText( BaseMessages.getString( PKG, "AbortDialog.AbortWithError.Label" ) );
    props.setLook( wAbortWithError );
    wAbortWithError.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.AbortWithError.Tooltip" ) );
    fdAbortWithError = new FormData();
    fdAbortWithError.left = new FormAttachment( 0, 0 );
    fdAbortWithError.top = new FormAttachment( wAlwaysLogRows, 10 );
    wAbortWithError.setLayoutData( fdAbortWithError );
    wAbortWithError.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Some buttons
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );

    Label hSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdhSpacer = new FormData();
    fdhSpacer.height = 1;
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.top = new FormAttachment( wAbortWithError, 10 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wRowThreshold.addSelectionListener( lsDef );
    wMessage.addSelectionListener( lsDef );
    wAbortWithError.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private Image getImage() {
    return SwtSvgImageUtil.getImage( shell.getDisplay(), getClass().getClassLoader(), "ABR.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getRowThreshold() != null ) {
      wRowThreshold.setText( input.getRowThreshold() );
    }
    if ( input.getMessage() != null ) {
      wMessage.setText( input.getMessage() );
    }
    wAlwaysLogRows.setSelection( input.isAlwaysLogRows() );
    wAbortWithError.setSelection( input.isAbortWithError() );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void getInfo( AbortMeta in ) {
    input.setRowThreshold( wRowThreshold.getText() );
    input.setMessage( wMessage.getText() );
    input.setAlwaysLogRows( wAlwaysLogRows.getSelection() );
    input.setAbortWithError( wAbortWithError.getSelection() );
  }

  /**
   * Cancel the dialog.
   */
  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( input );
    stepname = wStepname.getText(); // return value
    dispose();
  }
}
