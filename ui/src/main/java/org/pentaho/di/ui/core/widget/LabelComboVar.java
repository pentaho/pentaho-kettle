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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Adds a line of text with a label and a variable to a composite (like a dialog shell)
 *
 * @author Matt
 * @since 17-may-2006
 *
 */
public class LabelComboVar extends Composite {
  private static final PropsUI props = PropsUI.getInstance();

  private Label wLabel;
  private ComboVar wCombo;

  public LabelComboVar( VariableSpace space, Composite composite, String labelText, String toolTipText ) {
    this( space, composite, SWT.NONE, labelText, toolTipText );
  }

  public LabelComboVar( VariableSpace space, Composite composite, int flags, String labelText, String toolTipText ) {
    super( composite, SWT.NONE );
    props.setLook( this );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;

    this.setLayout( formLayout );

    int textFlags = SWT.SINGLE | SWT.LEFT | SWT.BORDER;
    if ( flags != SWT.NONE ) {
      textFlags = flags;
    }

    wCombo = new ComboVar( space, this, textFlags, toolTipText );
    FormData fdText = new FormData();
    fdText.left = new FormAttachment( middle, margin );
    fdText.right = new FormAttachment( 100, 0 );
    wCombo.setLayoutData( fdText );
    wCombo.getCComboWidget().setToolTipText( toolTipText );

    wLabel = new Label( this, SWT.RIGHT );
    props.setLook( wLabel );
    wLabel.setText( labelText );
    FormData fdLabel = new FormData();
    fdLabel.left = new FormAttachment( 0, 0 );
    fdLabel.right = new FormAttachment( middle, 0 );
    fdLabel.top = new FormAttachment( wCombo, 0, SWT.CENTER );
    wLabel.setLayoutData( fdLabel );
    wLabel.setToolTipText( toolTipText );
  }

  public void addModifyListener( ModifyListener lsMod ) {
    wCombo.addModifyListener( lsMod );
  }

  public void addSelectionListener( SelectionAdapter lsDef ) {
    wCombo.addSelectionListener( lsDef );
  }

  public void setText( String name ) {
    wCombo.setText( name );
  }

  public String getText() {
    return wCombo.getText();
  }

  public void setItems( String[] items ) {
    wCombo.setItems( items );
  }

  public void add( String item ) {
    wCombo.add( item );
  }

  public String[] getItems() {
    return wCombo.getItems();
  }

  public int getItemCount() {
    return wCombo.getItemCount();
  }

  public void removeAll() {
    wCombo.removeAll();
  }

  public void remove( int index ) {
    wCombo.remove( index );
  }

  public void select( int index ) {
    wCombo.select( index );
  }

  public void setEnabled( boolean flag ) {
    wCombo.setEnabled( flag );
    wLabel.setEnabled( flag );
  }

  public boolean setFocus() {
    return wCombo.setFocus();
  }

  public void addTraverseListener( TraverseListener tl ) {
    wCombo.addTraverseListener( tl );
  }

  public CCombo getComboWidget() {
    return wCombo.getCComboWidget();
  }

  public Label getLabelWidget() {
    return wLabel;
  }
}
