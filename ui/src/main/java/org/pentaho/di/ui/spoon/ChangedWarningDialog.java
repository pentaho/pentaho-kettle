/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.spoon;

import org.eclipse.swt.SWT;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;

public class ChangedWarningDialog implements ChangedWarningInterface {

  private static ChangedWarningInterface instance = new ChangedWarningDialog();

  protected String result = null;

  protected XulDomContainer container = null;

  private static Class<?> PKG = Spoon.class;

  public ChangedWarningDialog() {
  }

  public static void setInstance( ChangedWarningInterface cwi ) {
    // Cannot null out the instance
    if ( cwi != null ) {
      instance = cwi;
    }
  }

  public static ChangedWarningInterface getInstance() {
    return instance;
  }

  public String getName() {
    return "changedWarningController";
  }

  public int show() throws Exception {
    return show( null );
  }

  public int show( String fileName ) throws Exception {
    return runXulChangedWarningDialog( fileName ).open();
  }

  protected XulMessageBox runXulChangedWarningDialog( String fileName ) throws IllegalArgumentException,
    XulException {
    container = Spoon.getInstance().getMainSpoonContainer();

    XulMessageBox messageBox = (XulMessageBox) container.getDocumentRoot().createElement( "messagebox" );
    messageBox.setTitle( BaseMessages.getString( PKG, "Spoon.Dialog.PromptSave.Title" ) );

    if ( fileName != null ) {
      messageBox.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.PromptToSave.Message", fileName ) );
    } else {
      messageBox.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.PromptSave.Message" ) );
    }

    messageBox.setButtons( new Integer[] { SWT.YES, SWT.NO, SWT.CANCEL } );

    return messageBox;
  }
}
