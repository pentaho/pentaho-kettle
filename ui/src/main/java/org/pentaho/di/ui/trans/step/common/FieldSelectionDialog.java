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


package org.pentaho.di.ui.trans.step.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.BaseDialog;

/**
 * A dialog with options to (re)load all fields, or only add new ones. Upon submitting the dialog, the "Sample data"
 * dialog is opened to load fields.
 */
public class FieldSelectionDialog extends BaseDialog {
  private static Class<?> PKG = FieldSelectionDialog.class;

  private int numFields;
  protected boolean reloadAllFields;

  private static final int WIDTH = 360;

  public FieldSelectionDialog( final Shell shell, final int numNewFields ) {
    super( shell, BaseMessages.getString( PKG, "System.GetFields.NewFieldsFound.Title" ), WIDTH );
    this.numFields = numNewFields;

    // Define buttons
    this.buttons.put( BaseMessages.getString( PKG, "System.Button.Cancel" ), event -> {
      cancel();
    } );
    this.buttons.put( BaseMessages.getString( PKG, "System.Button.OK" ), event -> {
      ok();
    } );
  }

  @Override
  protected Control buildBody() {

    final Label message = new Label( shell, SWT.WRAP | SWT.LEFT );
    message.setText( BaseMessages.getString( PKG, "System.GetFields.NewFieldsFound.Message", numFields ) );
    props.setLook( message );
    message.setLayoutData( new FormDataBuilder().top().left().right( 100, 0 ).result() );

    final Button newFieldsOnly = new Button( shell, SWT.RADIO );
    newFieldsOnly.setSelection( true );
    props.setLook( newFieldsOnly );
    newFieldsOnly.setText( BaseMessages.getString( PKG, "System.GetFields.AddNewOnly.Label" ) );
    newFieldsOnly.setLayoutData( new FormDataBuilder().top( message, MARGIN_SIZE ).left().result() );

    final Button clearAndAddAll = new Button( shell, SWT.RADIO );
    props.setLook( clearAndAddAll );
    clearAndAddAll.setText( BaseMessages.getString( PKG, "System.GetFields.ClearAndAddAll.Label" ) );
    clearAndAddAll.setLayoutData( new FormDataBuilder().top( newFieldsOnly, ELEMENT_SPACING ).left().result() );

    newFieldsOnly.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        newFieldsOnly.setSelection( true );
        clearAndAddAll.setSelection( false );
        reloadAllFields = false;
      }
    } );
    clearAndAddAll.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        clearAndAddAll.setSelection( true );
        newFieldsOnly.setSelection( false );
        reloadAllFields = true;
      }
    } );

    return clearAndAddAll;
  }

  /**
   * Override to provide specific behavior, other than just disposing the dialog.
   */
  protected void cancel() {
    dispose();
  }

  /**
   * Override to provide specific behavior, other than just disposing the dialog.
   */
  protected void ok() {
    dispose();
  }
}
