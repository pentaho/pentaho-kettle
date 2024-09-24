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
package org.pentaho.di.ui.trans.steps.missing;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MissingTransDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = MissingTransDialog.class;

  private Shell shell;
  private Shell shellParent;
  private List<MissingTrans> missingTrans;
  private int mode;
  private PropsUI props;
  private String stepResult;

  public static final int MISSING_TRANS_STEPS = 1;
  public static final int MISSING_TRANS_STEP_ID = 2;

  public MissingTransDialog( Shell parent, List<MissingTrans> missingTrans, StepMetaInterface baseStepMeta,
      TransMeta transMeta, String stepname ) {
    super( parent, baseStepMeta, transMeta, stepname );
    this.shellParent = parent;
    this.missingTrans = missingTrans;
    this.mode = MISSING_TRANS_STEPS;
  }

  public MissingTransDialog( Shell parent, Object in, TransMeta transMeta, String stepname ) {
    super( parent, (BaseStepMeta) in, transMeta, stepname );
    this.shellParent = parent;
    this.mode = MISSING_TRANS_STEP_ID;
  }

  private String getErrorMessage( List<MissingTrans> missingTrans, int mode ) {
    String message = "";
    if ( mode == MISSING_TRANS_STEPS ) {
      StringBuilder entries = new StringBuilder();
      for ( MissingTrans entry : missingTrans ) {
        if ( missingTrans.indexOf( entry ) == missingTrans.size() - 1 ) {
          entries.append( "- " + entry.getStepName() + " - " + entry.getMissingPluginId() + "\n\n" );
        } else {
          entries.append( "- " + entry.getStepName() + " - " + entry.getMissingPluginId() + "\n" );
        }
      }
      message = BaseMessages.getString( PKG, "MissingTransDialog.MissingTransSteps", entries.toString() );
    }

    if ( mode == MISSING_TRANS_STEP_ID ) {
      message =
          BaseMessages.getString( PKG, "MissingTransDialog.MissingTransStepId", stepname + " - "
              + ( (MissingTrans) baseStepMeta ).getMissingPluginId() );
    }
    return message.toString();
  }

  public String open() {
    this.props = PropsUI.getInstance();
    Display display = shellParent.getDisplay();
    int margin = Const.MARGIN;

    shell =
        new Shell( shellParent, SWT.DIALOG_TRIM | SWT.CLOSE | SWT.ICON
            | SWT.APPLICATION_MODAL );

    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginLeft = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "MissingTransDialog.MissingPlugins" ) );
    shell.setLayout( formLayout );

    Label image = new Label( shell, SWT.NONE );
    props.setLook( image );
    Image icon = display.getSystemImage( SWT.ICON_QUESTION );
    image.setImage( icon );
    FormData imageData = new FormData();
    imageData.left = new FormAttachment( 0, 5 );
    imageData.right = new FormAttachment( 11, 0 );
    imageData.top = new FormAttachment( 0, 10 );
    image.setLayoutData( imageData );

    Label error = new Label( shell, SWT.WRAP );
    props.setLook( error );
    error.setText( getErrorMessage( missingTrans, mode ) );
    FormData errorData = new FormData();
    errorData.left = new FormAttachment( image, 5 );
    errorData.right = new FormAttachment( 100, -5 );
    errorData.top = new FormAttachment( 0, 10 );
    error.setLayoutData( errorData );

    Label separator = new Label( shell, SWT.WRAP );
    props.setLook( separator );
    FormData separatorData = new FormData();
    separatorData.top = new FormAttachment( error, 10 );
    separator.setLayoutData( separatorData );

    Button closeButton = new Button( shell, SWT.PUSH );
    props.setLook( closeButton );
    FormData fdClose = new FormData();
    fdClose.right = new FormAttachment( 98 );
    fdClose.top = new FormAttachment( separator );
    closeButton.setLayoutData( fdClose );
    closeButton.setText( BaseMessages.getString( PKG, "MissingTransDialog.Close" ) );
    closeButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        shell.dispose();
        stepResult = null;
      }
    } );

    FormData fdSearch = new FormData();
    if ( this.mode == MISSING_TRANS_STEPS ) {
      Button openButton = new Button( shell, SWT.PUSH );
      props.setLook( openButton );
      FormData fdOpen = new FormData();
      fdOpen.right = new FormAttachment( closeButton, -5 );
      fdOpen.bottom = new FormAttachment( closeButton, 0, SWT.BOTTOM );
      openButton.setLayoutData( fdOpen );
      openButton.setText( BaseMessages.getString( PKG, "MissingTransDialog.OpenFile" ) );
      openButton.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          shell.dispose();
          stepResult = stepname;
        }
      } );
      fdSearch.right = new FormAttachment( openButton, -5 );
      fdSearch.bottom = new FormAttachment( openButton, 0, SWT.BOTTOM );
    } else {
      fdSearch.right = new FormAttachment( closeButton, -5 );
      fdSearch.bottom = new FormAttachment( closeButton, 0, SWT.BOTTOM );
    }

    Button searchButton = new Button( shell, SWT.PUSH );
    props.setLook( searchButton );
    searchButton.setText( BaseMessages.getString( PKG, "MissingTransDialog.SearchMarketplace" ) );
    searchButton.setLayoutData( fdSearch );
    searchButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        try {
          shell.dispose();
          Spoon.getInstance().openMarketplace();
        } catch ( Exception ex ) {
          ex.printStackTrace();
        }
      }
    } );

    shell.pack();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepResult;
  }
}
