/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.api.overwrite;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.Arrays;

public class OverwriteDialog extends Dialog {
  private static final Class<?> PKG = OverwriteDialog.class; // for i18n purposes, needed by Translator2!!

  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int MARGIN = Const.MARGIN;

  private final PropsUI props;
  private final int width;
  private final int height;
  OverwriteStatus.OverwriteMode[] disableButtons;

  private Shell shell;
  private OverwriteStatus overwriteStatus;

  public OverwriteDialog( Shell shell, int width, int height, OverwriteStatus.OverwriteMode[] disableButtons ) {
    super( shell, SWT.NONE );
    overwriteStatus = new OverwriteStatus( shell );
    this.width = width;
    this.height = height;
    if ( disableButtons == null ) {
      disableButtons = new OverwriteStatus.OverwriteMode[] {};
    }
    this.disableButtons = disableButtons;
    props = PropsUI.getInstance();
  }

  //This open called for a new connection
  public OverwriteStatus open( String title, String path, String type, String notes ) {

    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    shell.setSize( width, height );
    props.setLook( shell );
    shell.setImage( LOGO );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( title );
    shell.setLayout( formLayout );

    Button wOverwrite = null;
    Button wCancel = null;
    Button wSkip = null;
    Button wRename = null;
    ArrayList<Button> activeButtons = new ArrayList<>();

    // First, add the buttons...
    if ( !isButtonDisabled( OverwriteStatus.OverwriteMode.OVERWRITE ) ) {
      wOverwrite = new Button( shell, SWT.PUSH );
      wOverwrite.setText( BaseMessages.getString( PKG, "OverwriteDialog.Overwrite" ) );
      activeButtons.add( wOverwrite );
      wOverwrite.addListener( SWT.Selection, e -> overwrite() );
    }

    if ( !isButtonDisabled( OverwriteStatus.OverwriteMode.SKIP ) ) {
      wSkip = new Button( shell, SWT.PUSH );
      wSkip.setText( BaseMessages.getString( PKG, "OverwriteDialog.Skip" ) );
      activeButtons.add( wSkip );
      wSkip.addListener( SWT.Selection, e -> skip() );
    }

    if ( !isButtonDisabled( OverwriteStatus.OverwriteMode.RENAME ) ) {
      wRename = new Button( shell, SWT.PUSH );
      wRename.setText( BaseMessages.getString( PKG, "OverwriteDialog.Rename" ) );
      activeButtons.add( wRename );
      wRename.addListener( SWT.Selection, e -> rename() );
    }

    if ( !isButtonDisabled( OverwriteStatus.OverwriteMode.CANCEL ) ) {
      wCancel = new Button( shell, SWT.PUSH );
      wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
      activeButtons.add( wCancel );
      wCancel.addListener( SWT.Selection, e -> cancel() );
    }

    Button[] buttons = activeButtons.toArray( new Button[ activeButtons.size() ] );
    BaseStepDialog.positionBottomRightButtons( shell, buttons, MARGIN, null );

    // The rest stays above the buttons...
    Composite wComposite = new Composite( shell, SWT.NONE );
    props.setLook( wComposite );

    FormLayout genLayout = new FormLayout();
    genLayout.marginWidth = Const.FORM_MARGIN;
    genLayout.marginHeight = Const.FORM_MARGIN;
    wComposite.setLayout( genLayout );

    FormData fdComposite = new FormData();
    fdComposite.top = new FormAttachment( 0, MARGIN );
    fdComposite.left = new FormAttachment( 0, 0 );
    fdComposite.right = new FormAttachment( 100, 0 );
    fdComposite.bottom = new FormAttachment( wCancel, -MARGIN );
    wComposite.setLayoutData( fdComposite );

    // Warning image
    Label wlImage = new Label( wComposite, SWT.NONE );
    wlImage.setImage( GUIResource.getInstance().getImageWarning32() );
    FormData fdWarnImage = new FormData();
    fdWarnImage.left = new FormAttachment( 0, MARGIN );
    fdWarnImage.top = new FormAttachment( 0, MARGIN * 2 );
    fdWarnImage.height = 32;
    fdWarnImage.width = 32;
    wlImage.setLayoutData( fdWarnImage );
    PropsUI.getInstance().setLook( wlImage );

    // Dialog text
    Label wlText = new Label( wComposite, SWT.LEFT | SWT.WRAP );
    props.setLook( wlText );
    wlText.setText( BaseMessages.getString( PKG, "OverwriteDialog.maintext", StringUtils.capitalize( type ), path ) );
    FormData wlFormData = new FormData();
    wlFormData.top = new FormAttachment( 0, MARGIN * 2 );
    wlFormData.left = new FormAttachment( wlImage, MARGIN * 2 );
    wlFormData.right = new FormAttachment( 100, -MARGIN );
    wlText.setLayoutData( wlFormData );

    Label wlNotesText = new Label( wComposite, SWT.LEFT | SWT.WRAP );
    props.setLook( wlNotesText );
    wlNotesText.setText( notes );
    FormData wlNotesFormData = new FormData();
    wlNotesFormData.top = new FormAttachment( wlText, MARGIN * 3 );
    wlNotesFormData.left = new FormAttachment( wlImage, MARGIN * 2 );
    wlNotesFormData.right = new FormAttachment( 100, -MARGIN );
    wlNotesText.setLayoutData( wlNotesFormData );

    Button applyAllButton = new Button( wComposite, SWT.CHECK );
    applyAllButton.setText( BaseMessages.getString( PKG, "OverwriteDialog.applyToAll.label" ) );
    FormData fdApplyAllButton = new FormData();
    fdApplyAllButton.bottom = new FormAttachment( 100, -MARGIN );
    fdApplyAllButton.right = new FormAttachment( 100, -3 * MARGIN );
    applyAllButton.setLayoutData( fdApplyAllButton );
    applyAllButton.addListener( SWT.Selection,
      event -> overwriteStatus.setApplyToAll( ( (Button) event.widget ).getSelection() ) );
    PropsUI.getInstance().setLook( applyAllButton );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    shell.open();
    while ( !shell.isDisposed() ) {
      Display display = parent.getDisplay();
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return overwriteStatus;
  }

  private void cancel() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.CANCEL );
    dispose();
  }

  private void overwrite() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.OVERWRITE );
    dispose();
  }

  private void rename() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.RENAME );
    dispose();
  }

  private void skip() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.SKIP );
    dispose();
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  private boolean isButtonDisabled( OverwriteStatus.OverwriteMode overwriteMode ) {
    return Arrays.stream( disableButtons ).anyMatch( x -> x.equals( overwriteMode ) );
  }

}
