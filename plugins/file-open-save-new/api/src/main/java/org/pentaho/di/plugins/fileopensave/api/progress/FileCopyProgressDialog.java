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
package org.pentaho.di.plugins.fileopensave.api.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class FileCopyProgressDialog extends Dialog {
  private static final Class<?> PKG = FileCopyProgressDialog.class; // for i18n purposes, needed by Translator2!!

  private static final Image LOGO = GUIResource.getInstance().getImageLogoSmall();
  private static final int MARGIN = Const.MARGIN;

  private final PropsUI props;
  private final int width;
  private final int height;
  private Label wlPresentFilePath;

  private Shell parentShell;
  private Shell shell;
  private OverwriteStatus overwriteStatus;

  public FileCopyProgressDialog( Shell shell ) {
    super( shell );
    setShellStyle( SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE );
    setBlockOnOpen( false );
    parentShell = shell;
    overwriteStatus = new OverwriteStatus( shell );
    this.width = 600;
    this.height = 200;
    props = PropsUI.getInstance();
  }

  public void open( String title ) {
    shell = new Shell( parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    shell.setSize( width, height );
    props.setLook( shell );
    shell.setImage( LOGO );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( title );
    shell.setLayout( formLayout );

    // First, add the buttons...
    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Button[] buttons = new Button[] { wCancel };
    BaseStepDialog.positionBottomRightButtons( shell, buttons, MARGIN, null );

    // Add listeners
    wCancel.addListener( SWT.Selection, e -> cancel() );

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

    // Dialog text
    Label wlText = new Label( wComposite, SWT.LEFT | SWT.WRAP );
    props.setLook( wlText );
    wlText.setText( BaseMessages.getString( PKG, "FileCopyProgressDialog.workingOn" ) );
    FormData fdText = new FormData();
    fdText.top = new FormAttachment( 0, MARGIN * 2 );
    fdText.left = new FormAttachment( 0, MARGIN * 2 );
    wlText.setLayoutData( fdText );

    wlPresentFilePath = new Label( wComposite, SWT.LEFT | SWT.WRAP );
    props.setLook( wlPresentFilePath );
    FormData fdPresentFilePath = new FormData();
    fdPresentFilePath.top = new FormAttachment( 0, MARGIN * 2 );
    fdPresentFilePath.left = new FormAttachment( wlText, MARGIN * 2 );
    fdPresentFilePath.right = new FormAttachment( 100, -MARGIN );
    fdPresentFilePath.bottom = new FormAttachment( 100, -MARGIN );
    wlPresentFilePath.setLayoutData( fdPresentFilePath );

    shell.open();
  }

  private void cancel() {
    overwriteStatus.setOverwriteMode( OverwriteStatus.OverwriteMode.CANCEL );
    dispose();
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void setFile( String filePath ) {
    wlPresentFilePath.setText( filePath );
  }

  @Override public Shell getShell() {
    return shell;
  }
}
