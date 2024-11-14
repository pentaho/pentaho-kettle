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


package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.FormDataBuilder;

/**
 * A simple dialog with a message and button that closes the dialog.
 */
public class BaseMessageDialog extends BaseDialog {
  private static Class<?> PKG = BaseMessageDialog.class;

  private String message;

  public BaseMessageDialog( final Shell shell, final String title, final String message ) {
    this( shell, title, message, BaseMessages.getString( PKG, "System.Button.OK" ), -1 );
  }

  public BaseMessageDialog( final Shell shell, final String title, final String message, final int width ) {
    this( shell, title, message, BaseMessages.getString( PKG, "System.Button.OK" ), width );
  }

  public BaseMessageDialog( final Shell shell, final String title, final String message, final String buttonLabel,
                            final int width ) {
    super( shell, title, width );
    this.message = message;
    this.buttons.put( buttonLabel, event -> {
      dispose();
    } );
  }

  @Override
  protected Control buildBody() {
    final Label message = new Label( shell, SWT.WRAP | SWT.LEFT );
    message.setText( this.message );
    props.setLook( message );
    message.setLayoutData( new FormDataBuilder().top().left().right( 100, 0 ).result() );
    return message;
  }
}
