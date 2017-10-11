/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
