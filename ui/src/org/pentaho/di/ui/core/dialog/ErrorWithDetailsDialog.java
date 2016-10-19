/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Created by bmorrise on 10/13/16.
 */
public class ErrorWithDetailsDialog extends MessageDialog {

  public static final Class<?> PKG = ErrorWithDetailsDialog.class;

  private int detailsIndex;
  private String details;

  public ErrorWithDetailsDialog( Shell parentShell, String dialogTitle, Image dialogTitleImage,
                                 String dialogMessage, int dialogImageType, String[] dialogButtonLabels,
                                 int defaultIndex, int detailsIndex, String details ) {
    super( parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
      defaultIndex );

    this.details = details;
    this.detailsIndex = detailsIndex;
  }

  @Override protected Point getInitialSize() {
    return getParentShell().computeSize( 368, 107 );
  }

  @Override protected void buttonPressed( int buttonId ) {
    super.buttonPressed( buttonId );
    if ( buttonId == detailsIndex ) {
      DetailsDialog detailsDialog =
        new DetailsDialog( getParentShell(), BaseMessages.getString( PKG, "ErrorDialog.ShowDetail.Title" ), null,
          BaseMessages.getString( PKG, "ErrorDialog.ShowDetail.Message" ), 0,
          new String[] { BaseMessages.getString( PKG, "ErrorDialog.ShowDetail.Close" ) }, 0, details );
      detailsDialog.open();
    }
  }
}
