/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;

/**
 * Provides a composite containing a TextVar, Variable Icon, and browse button.  The browse button will open the file
 * chooser.
 */
public class FileChooserVar extends Composite {
  private TextVar wPath;
  private Button wBrowseButton;
  VariableSpace space;
  private Bowl bowl;
  protected ObjectLocationSpecificationMethod specificationMethod;
  PropsUI props = PropsUI.getInstance();

  public FileChooserVar( Bowl bowl, VariableSpace space, Composite composite, int flags, String buttonLabel ) {
    this( bowl, space, composite, flags, buttonLabel, null, null, null );
  }

  /** @deprecated Kept for backwards compatibility only. Use the version with the Bowl */
  @Deprecated
  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel ) {
    this( space, composite, flags, buttonLabel, null, null, null );
  }

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel, String toolTipText ) {
    this( space, composite, flags, buttonLabel, toolTipText, null, null );
  }

  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel,
                         GetCaretPositionInterface getCaretPositionInterface,
                         InsertTextInterface insertTextInterface ) {
    this( space, composite, flags, buttonLabel, null, getCaretPositionInterface, insertTextInterface );
  }

  /** @deprecated Kept for backwards compatibility only. Use the version with the Bowl */
  @Deprecated
  public FileChooserVar( VariableSpace space, Composite composite, int flags, String buttonLabel, String toolTipText,
                         GetCaretPositionInterface getCaretPositionInterface,
                         InsertTextInterface insertTextInterface ) {
    this( DefaultBowl.getInstance(), space, composite, flags, buttonLabel, toolTipText, getCaretPositionInterface,
          insertTextInterface );
  }
  public FileChooserVar( Bowl bowl, VariableSpace space, Composite composite, int flags, String buttonLabel,
                         String toolTipText, GetCaretPositionInterface getCaretPositionInterface,
                         InsertTextInterface insertTextInterface ) {
    super( composite, SWT.NONE );
    this.bowl = bowl;
    this.space = space;
    initialize( flags, buttonLabel, toolTipText, getCaretPositionInterface, insertTextInterface );
  }

  protected void initialize( int flags, String buttonLabel,
                             String toolTipText, GetCaretPositionInterface getCaretPositionInterface,
                             InsertTextInterface insertTextInterface ) {

    props.setLook( this );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;
    this.setLayout( formLayout );

    this.wBrowseButton = new Button( this, SWT.PUSH );
    props.setLook( wBrowseButton );
    wBrowseButton.setText( buttonLabel );
    FormData fdButton = new FormData();
    fdButton.top = new FormAttachment( 0, 0 );
    fdButton.right = new FormAttachment( 100, 0 );
    wBrowseButton.setLayoutData( fdButton );

    wPath = new TextVar( space, this, flags, toolTipText, getCaretPositionInterface, insertTextInterface );
    props.setLook( wPath );
    FormData fdTextVar = new FormData();
    fdTextVar.top = new FormAttachment( 0, 0 );
    fdTextVar.left = new FormAttachment( 0, 0 );
    fdTextVar.right = new FormAttachment( wBrowseButton, -8 );
    wPath.setLayoutData( fdTextVar );

    wBrowseButton.addListener( SWT.Selection, event -> openFileBrowser() );
  }

  public TextVar getTextVarWidget() {
    return wPath;
  }

  public String getText() {
    return wPath.getText();
  }

  public Button getButton() {
    return wBrowseButton;
  }

  private void openFileBrowser() {
    LogChannel log = new LogChannel();
    TransMeta meta = new TransMeta( space );
    SelectionAdapterFileDialogTextVar selectionAdapterFileDialogTextVar =
      new SelectionAdapterFileDialogTextVar( log, wPath, meta,
        new SelectionAdapterOptions( bowl, SelectionOperation.FILE,
          new FilterType[] { FilterType.ALL }, FilterType.ALL ) );
    selectionAdapterFileDialogTextVar.widgetSelected( null );
    if ( wPath.getText() != null && Const.isWindows() ) {
      wPath.setText( wPath.getText().replace( '\\', '/' ) );
    }
  }

  public void addModifyListener( ModifyListener modifyListener ) {
    wPath.addModifyListener( modifyListener );
  }

  public void setText( String text ) {
    wPath.setText( text );
  }

}

