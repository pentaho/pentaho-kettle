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
