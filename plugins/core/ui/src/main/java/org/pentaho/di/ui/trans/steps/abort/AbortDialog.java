/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.abort.AbortMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

@PluginDialog( id = "Abort", image = "ABR.svg", pluginType = PluginDialog.PluginType.STEP,
  documentationUrl = "mk-95pdia003/pdi-transformation-steps/abort" )
public class AbortDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = AbortDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlRowThreshold;
  private TextVar wRowThreshold;
  private FormData fdlRowThreshold, fdRowThreshold;

  private Label wlMessage;
  private TextVar wMessage;
  private FormData fdlMessage, fdMessage;

  private Button wAlwaysLogRows;
  private FormData fdAlwaysLogRows;

  private AbortMeta input;
  private ModifyListener lsMod;
  private SelectionAdapter lsSelMod;
  private Group wLoggingGroup;
  private Button wAbortButton;
  private Button wAbortWithErrorButton;
  private Button wSafeStopButton;
  private Group wOptionsGroup;
  private Label hSpacer;

  public AbortDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (AbortMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    shell.setMinimumSize( 400, 520 );
    setShellImage( shell, input );

    lsMod = e -> input.setChanged();
    lsSelMod = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
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
    fdStepname.width = 250;
    wStepname.setLayoutData( fdStepname );

    Label spacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    spacer.setLayoutData( fdSpacer );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    fdlicon.bottom = new FormAttachment( spacer, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

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

    hSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdhSpacer = new FormData();
    fdhSpacer.height = 2;
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );


    buildOptions( spacer );
    buildLogging( wOptionsGroup );

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

  private void buildOptions( Control widgetAbove ) {
    wOptionsGroup = new Group( shell, SWT.SHADOW_ETCHED_IN );
    props.setLook( wOptionsGroup );
    wOptionsGroup.setText( BaseMessages.getString( PKG, "AbortDialog.Options.Group.Label" ) );
    FormLayout flOptionsGroup = new FormLayout();
    flOptionsGroup.marginHeight = 15;
    flOptionsGroup.marginWidth = 15;
    wOptionsGroup.setLayout( flOptionsGroup );

    FormData fdOptionsGroup = new FormData();
    fdOptionsGroup.left = new FormAttachment( 0, 0 );
    fdOptionsGroup.top = new FormAttachment( widgetAbove, 15 );
    fdOptionsGroup.right = new FormAttachment( 100, 0 );
    wOptionsGroup.setLayoutData( fdOptionsGroup );

    wAbortButton = new Button( wOptionsGroup, SWT.RADIO );
    wAbortButton.addSelectionListener( lsSelMod );
    wAbortButton.setText( BaseMessages.getString( PKG, "AbortDialog.Options.Abort.Label" ) );
    FormData fdAbort = new FormData();
    fdAbort.left = new FormAttachment( 0, 0 );
    fdAbort.top = new FormAttachment( 0, 0 );
    wAbortButton.setLayoutData( fdAbort );
    props.setLook( wAbortButton );

    wAbortWithErrorButton = new Button( wOptionsGroup, SWT.RADIO );
    wAbortWithErrorButton.addSelectionListener( lsSelMod );
    wAbortWithErrorButton.setText( BaseMessages.getString( PKG, "AbortDialog.Options.AbortWithError.Label" ) );
    FormData fdAbortWithError = new FormData();
    fdAbortWithError.left = new FormAttachment( 0, 0 );
    fdAbortWithError.top = new FormAttachment( wAbortButton, 10 );
    wAbortWithErrorButton.setLayoutData( fdAbortWithError );
    props.setLook( wAbortWithErrorButton );

    wSafeStopButton = new Button( wOptionsGroup, SWT.RADIO );
    wSafeStopButton.addSelectionListener( lsSelMod );
    wSafeStopButton.setText( BaseMessages.getString( PKG, "AbortDialog.Options.SafeStop.Label" ) );
    FormData fdSafeStop = new FormData();
    fdSafeStop.left = new FormAttachment( 0, 0 );
    fdSafeStop.top = new FormAttachment( wAbortWithErrorButton, 10 );
    wSafeStopButton.setLayoutData( fdSafeStop );
    props.setLook( wSafeStopButton );

    wlRowThreshold = new Label( wOptionsGroup, SWT.RIGHT );
    wlRowThreshold.setText( BaseMessages.getString( PKG, "AbortDialog.Options.RowThreshold.Label" ) );
    props.setLook( wlRowThreshold );
    fdlRowThreshold = new FormData();
    fdlRowThreshold.left = new FormAttachment( 0, 0 );
    fdlRowThreshold.top = new FormAttachment( wSafeStopButton, 10 );
    wlRowThreshold.setLayoutData( fdlRowThreshold );

    wRowThreshold = new TextVar( transMeta, wOptionsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wRowThreshold.setText( "" );
    props.setLook( wRowThreshold );
    wRowThreshold.addModifyListener( lsMod );
    wRowThreshold.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.Options.RowThreshold.Tooltip" ) );
    fdRowThreshold = new FormData();
    fdRowThreshold.left = new FormAttachment( 0, 0 );
    fdRowThreshold.top = new FormAttachment( wlRowThreshold, 5 );
    fdRowThreshold.width = 174;
    wRowThreshold.setLayoutData( fdRowThreshold );
  }

  private void buildLogging( Composite widgetAbove ) {
    wLoggingGroup = new Group( shell, SWT.SHADOW_ETCHED_IN );
    props.setLook( wLoggingGroup );
    wLoggingGroup.setText( BaseMessages.getString( PKG, "AbortDialog.Logging.Group" ) );
    FormLayout flLoggingGroup = new FormLayout();
    flLoggingGroup.marginHeight = 15;
    flLoggingGroup.marginWidth = 15;
    wLoggingGroup.setLayout( flLoggingGroup );

    FormData fdLoggingGroup = new FormData();
    fdLoggingGroup.left = new FormAttachment( 0, 0 );
    fdLoggingGroup.top = new FormAttachment( widgetAbove, 15 );
    fdLoggingGroup.right = new FormAttachment( 100, 0 );
    fdLoggingGroup.bottom = new FormAttachment( hSpacer, -15 );
    wLoggingGroup.setLayoutData( fdLoggingGroup );

    wlMessage = new Label( wLoggingGroup, SWT.RIGHT );
    wlMessage.setText( BaseMessages.getString( PKG, "AbortDialog.Logging.AbortMessage.Label" ) );
    props.setLook( wlMessage );
    fdlMessage = new FormData();
    fdlMessage.left = new FormAttachment( 0, 0 );
    fdlMessage.top = new FormAttachment( 0, 0 );
    wlMessage.setLayoutData( fdlMessage );

    wMessage = new TextVar( transMeta, wLoggingGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wMessage.setText( "" );
    props.setLook( wMessage );
    wMessage.addModifyListener( lsMod );
    wMessage.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.Logging.AbortMessage.Tooltip" ) );
    fdMessage = new FormData();
    fdMessage.left = new FormAttachment( 0, 0 );
    fdMessage.top = new FormAttachment( wlMessage, 5 );
    fdMessage.right = new FormAttachment( 100, 0 );
    wMessage.setLayoutData( fdMessage );

    wAlwaysLogRows = new Button( wLoggingGroup, SWT.CHECK );
    wAlwaysLogRows.setText( BaseMessages.getString( PKG, "AbortDialog.Logging.AlwaysLogRows.Label" ) );
    props.setLook( wAlwaysLogRows );
    wAlwaysLogRows.setToolTipText( BaseMessages.getString( PKG, "AbortDialog.Logging.AlwaysLogRows.Tooltip" ) );
    fdAlwaysLogRows = new FormData();
    fdAlwaysLogRows.left = new FormAttachment( 0, 0 );
    fdAlwaysLogRows.top = new FormAttachment( wMessage, 10 );
    wAlwaysLogRows.setLayoutData( fdAlwaysLogRows );
    wAlwaysLogRows.addSelectionListener( lsSelMod );
  }

  private Image getImage() {
    return SwtSvgImageUtil.getImage( shell.getDisplay(), getClass().getClassLoader(), "ABR.svg", ConstUI.LARGE_ICON_SIZE,
      ConstUI.LARGE_ICON_SIZE );
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

    wAbortButton.setSelection( input.isAbort() );
    wAbortWithErrorButton.setSelection( input.isAbortWithError() );
    wSafeStopButton.setSelection( input.isSafeStop() );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void getInfo( AbortMeta in ) {
    input.setRowThreshold( wRowThreshold.getText() );
    input.setMessage( wMessage.getText() );
    input.setAlwaysLogRows( wAlwaysLogRows.getSelection() );

    AbortMeta.AbortOption abortOption = AbortMeta.AbortOption.ABORT;
    if ( wAbortWithErrorButton.getSelection() ) {
      abortOption = AbortMeta.AbortOption.ABORT_WITH_ERROR;
    } else if ( wSafeStopButton.getSelection() ) {
      abortOption = AbortMeta.AbortOption.SAFE_STOP;
    }
    input.setAbortOption( abortOption );
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
