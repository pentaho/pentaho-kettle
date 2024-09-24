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

package org.pentaho.di.ui.trans.step.common;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.SimpleMessageDialog;

/**
 * A dialog that allows the user to select the number of  data rows to sample, when fetching fields.
 */
public class GetFieldsSampleDataDialog extends EnterNumberDialog {

  private static Class<?> PKG = GetFieldsSampleDataDialog.class;

  private static final int SAMPLE_SIZE = 100;

  private static final int SHELL_WIDTH = 300;

  private GetFieldsCapableStepDialog parentDialog;
  private boolean reloadAllFields;

  public GetFieldsSampleDataDialog( final Shell parentShell, final GetFieldsCapableStepDialog parentDialog,
                                    final boolean reloadAllFields ) {
    super( parentShell, SAMPLE_SIZE,
      BaseMessages.getString( PKG, "System.GetFields.SampleSize.Dialog.Title" ),
      BaseMessages.getString( PKG, "System.GetFields.SampleSize.Dialog.Message" ),
      BaseMessages.getString( PKG, "System.GetFields.SampleSize.Dialog.ShowSample.Message" ), SHELL_WIDTH );
    this.parentDialog = parentDialog;
    this.reloadAllFields = reloadAllFields;
  }

  @Override
  protected void ok() {
    try {
      samples = Integer.parseInt( wNumber.getText() );
      handleOk( samples );
      dispose();
    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( getParent(), SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "Dialog.Error.EnterInteger" ) );
      mb.setText( BaseMessages.getString( PKG, "Dialog.Error.Header" ) );
      mb.open();
      wNumber.selectAll();
    }
  }

  protected void handleOk( final int samples ) {
    if ( samples >= 0 ) {
      String message = parentDialog.loadFields( parentDialog.getPopulatedMeta(), samples, reloadAllFields );
      if ( wCheckbox != null && wCheckbox.getSelection() ) {
        if ( StringUtils.isNotBlank( message ) ) {
          final EnterTextDialog etd =
            new EnterTextDialog( parentDialog.getShell(),
              BaseMessages.getString( PKG, "System.GetFields.ScanResults.DialogTitle" ),
              BaseMessages.getString( PKG, "System.GetFields.ScanResults.DialogMessage" ), message, true );
          etd.setReadOnly();
          etd.setModal();
          etd.open();
        } else {
          final Dialog errorDlg = new SimpleMessageDialog( parentDialog.getShell(),
            BaseMessages.getString( PKG, "System.Dialog.Error.Title" ),
            BaseMessages.getString( PKG, "System.GetFields.ScanResults.Error.Message" ), MessageDialog.ERROR );
          errorDlg.open();
        }
      }
    }
  }
}
