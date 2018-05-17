/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
