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


package org.pentaho.di.ui.trans.steps.textfileoutputlegacy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.textfileoutputlegacy.TextFileOutputLegacyMeta;
import org.pentaho.di.ui.trans.steps.textfileoutput.TextFileOutputDialog;

/**
 * This is deprecated version with capability run as command.
 * @deprecated use {@link org.pentaho.di.ui.trans.steps.textfileoutput.TextFileOutputDialog} instead.
 */
public class TextFileOutputLegacyDialog extends TextFileOutputDialog {
  private static Class<?> textFileOutputLegacyMetaClass = TextFileOutputLegacyMeta.class;  // for i18n purposes, needed by Translator2!!

  private Label wlFileIsCommand;
  private Button wFileIsCommand;
  private FormData fdlFileIsCommand, fdFileIsCommand;

  public TextFileOutputLegacyDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, in, transMeta, sname );
  }


  @Override
  protected Control addAdditionalComponentIfNeed( int middle, int margin, Composite wFileComp, Composite topComp ) {
    // Run this as a command instead?
    wlFileIsCommand = new Label( wFileComp, SWT.RIGHT );
    wlFileIsCommand
      .setText( BaseMessages.getString( textFileOutputLegacyMetaClass, "TextFileOutputLegacyDialog.FileIsCommand.Label" ) );
    props.setLook( wlFileIsCommand );
    fdlFileIsCommand = new FormData();
    fdlFileIsCommand.left = new FormAttachment( 0, 0 );
    fdlFileIsCommand.top = new FormAttachment( topComp, margin );
    fdlFileIsCommand.right = new FormAttachment( middle, -margin );
    wlFileIsCommand.setLayoutData( fdlFileIsCommand );
    wFileIsCommand = new Button( wFileComp, SWT.CHECK );
    props.setLook( wFileIsCommand );
    fdFileIsCommand = new FormData();
    fdFileIsCommand.left = new FormAttachment( middle, 0 );
    fdFileIsCommand.top = new FormAttachment( topComp, margin );
    fdFileIsCommand.right = new FormAttachment( 100, 0 );
    wFileIsCommand.setLayoutData( fdFileIsCommand );
    wFileIsCommand.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        enableParentFolder();
      }
    } );
    return wlFileIsCommand;
  }

  @Override
  protected void setFlagsServletOption() {
    super.setFlagsServletOption();
    boolean enableFilename = !wServletOutput.getSelection();
    wlFileIsCommand.setEnabled( enableFilename );
    wFileIsCommand.setEnabled( enableFilename );
  }

  @Override
  protected String getDialogTitle() {
    return BaseMessages.getString( textFileOutputLegacyMetaClass, "TextFileOutputLegacyDialog.DialogTitle" );
  }

  @Override
  public void getData() {
    wFileIsCommand.setSelection( ( (TextFileOutputLegacyMeta) input ).isFileAsCommand() );
    wServletOutput.setSelection( input.isServletOutput() );
    super.getData();
  }


  @Override
  protected void saveInfoInMeta( TextFileOutputMeta tfoi ) {
    ( (TextFileOutputLegacyMeta) tfoi ).setFileAsCommand( wFileIsCommand.getSelection() );
    super.saveInfoInMeta( tfoi );
  }

  @Override
  protected void enableParentFolder() {
    wlCreateParentFolder.setEnabled( !wFileIsCommand.getSelection() );
    wCreateParentFolder.setEnabled( !wFileIsCommand.getSelection() );
  }

}
