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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Created by bmorrise on 10/13/16.
 */
public class DetailsDialog extends MessageDialog {

  private String details;
  private Text detailsText;

  public DetailsDialog( Shell parentShell, String dialogTitle,
                        Image dialogTitleImage, String dialogMessage, int dialogImageType,
                        String[] dialogButtonLabels, int defaultIndex, String details ) {
    super( parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
      defaultIndex );

    this.details = details;
  }

  @Override
  protected Control createMessageArea( Composite composite ) {
    GridLayout gridLayout = (GridLayout) composite.getLayout();
    gridLayout.numColumns = 1;
    composite.setLayout( gridLayout );

    if ( this.message != null ) {
      this.messageLabel = new Label( composite, this.getMessageLabelStyle() );
      this.messageLabel.setText( this.message );
    }

    if ( this.details != null ) {
      this.detailsText = new Text( composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
      this.detailsText.pack();
      this.detailsText.setText( this.details );
      GridData gridData = new GridData( );
      gridData.widthHint = 1024;
      gridData.heightHint = 300;
      this.detailsText.setLayoutData( gridData );
      this.detailsText.setSelection( this.details.length() );
    }

    return composite;
  }

}
