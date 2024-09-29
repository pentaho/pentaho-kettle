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


package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Adds a line of text with a label and a variable to a composite (like a dialog shell)
 *
 * @author Matt
 * @since 17-may-2006
 *
 */
public class LabelText extends Composite {
  private static final PropsUI props = PropsUI.getInstance();

  private Label wLabel;
  private Text wText;

  public LabelText( Composite composite, String labelText, String toolTipText ) {
    this(
      composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER, labelText, toolTipText, props.getMiddlePct(), Const.MARGIN );
  }

  public LabelText( Composite composite, String labelText, String toolTipText, int middle, int margin ) {
    this( composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER, labelText, toolTipText, middle, margin );
  }

  public LabelText( Composite composite, int textStyle, String labelText, String toolTipText, int middle,
    int margin ) {
    super( composite, SWT.NONE );
    props.setLook( this );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    this.setLayout( formLayout );

    wText = new Text( this, textStyle );
    FormData fdText = new FormData();
    fdText.left = new FormAttachment( middle, margin );
    fdText.right = new FormAttachment( 100, 0 );
    wText.setLayoutData( fdText );
    wText.setToolTipText( toolTipText );

    wLabel = new Label( this, SWT.RIGHT );
    props.setLook( wLabel );
    wLabel.setText( labelText );
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment( 0, 0 );
    fdLabel.right = new FormAttachment( middle, 0 );
    fdLabel.top = new FormAttachment( wText, 0, SWT.CENTER );
    wLabel.setLayoutData( fdLabel );
    wLabel.setToolTipText( toolTipText );
  }

  public String getText() {
    return wText.getText();
  }

  public void setText( String string ) {
    wText.setText( string );
  }

  public Text getTextWidget() {
    return wText;
  }

  public void addModifyListener( ModifyListener lsMod ) {
    wText.addModifyListener( lsMod );
  }

  public void addSelectionListener( SelectionListener lsDef ) {
    wText.addSelectionListener( lsDef );
  }

  public void setEnabled( boolean flag ) {
    wText.setEnabled( flag );
    wLabel.setEnabled( flag );
  }

  public void selectAll() {
    wText.selectAll();
  }

  public boolean setFocus() {
    return wText.setFocus();
  }
}
