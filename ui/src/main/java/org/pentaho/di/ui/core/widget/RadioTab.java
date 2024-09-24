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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.pentaho.di.ui.core.PropsUI;

public class RadioTab  extends Composite {
  Group radioGroup;
  Composite contentArea;
  private PropsUI props;
  private final FormData fdMaximize;
  private final FormLayout noMarginLayout;

  public RadioTab( Composite composite, int i, String title, PropsUI props ) {
    super( composite, i );
    this.props = props;
    props.setLook( this );
    noMarginLayout = new FormLayout();
    this.setLayout( noMarginLayout );

    fdMaximize = new FormData();
    fdMaximize.left = new FormAttachment( 0 );
    fdMaximize.top = new FormAttachment( 0 );
    fdMaximize.right = new FormAttachment( 100 );
    fdMaximize.bottom = new FormAttachment( 100 );
    this.setLayoutData( fdMaximize );

    FormLayout marginLayout = new FormLayout();
    marginLayout.marginWidth = 15;
    marginLayout.marginHeight = 15;

    radioGroup = new Group( this, i );
    radioGroup.setLayout( marginLayout );
    radioGroup.setText( title );
    FormData fdRadioGroup = new FormData();
    fdRadioGroup.top = new FormAttachment( 0 );
    fdRadioGroup.left = new FormAttachment( 0 );
    fdRadioGroup.right = new FormAttachment( 100 );
    radioGroup.setLayoutData( fdRadioGroup );
    props.setLook( radioGroup );

    contentArea = new Composite( this, i );
    contentArea.setLayout( noMarginLayout );
    FormData fdContentArea = new FormData();
    fdContentArea.left = new FormAttachment( 0 );
    fdContentArea.top = new FormAttachment( radioGroup, 15 );
    fdContentArea.bottom = new FormAttachment( 100 );
    fdContentArea.right = new FormAttachment( 100 );
    contentArea.setLayoutData( fdContentArea );
    props.setLook( contentArea );
  }

  public Composite createContent( String radioText ) {
    Control[] existingButtons = radioGroup.getChildren();
    Button button = new Button( radioGroup, SWT.RADIO );
    button.setText( radioText );
    props.setLook( button );
    FormData fdButton = new FormData();
    fdButton.top = new FormAttachment( 0 );
    fdButton.left = existingButtons.length == 0
      ? new FormAttachment( 0 ) : new FormAttachment( existingButtons[existingButtons.length - 1], 40 );
    button.setLayoutData( fdButton );
    button.setSelection( existingButtons.length == 0 );
    Composite content = new Composite( contentArea, SWT.NONE );
    content.setVisible( existingButtons.length == 0 );
    props.setLook( content );
    content.setLayout( noMarginLayout );
    content.setLayoutData( fdMaximize );
    button.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        for ( Control control : contentArea.getChildren() ) {
          control.setVisible( false );
        }
        content.setVisible( true );
      }
    } );
    return content;
  }

  public int selectedIndex() {
    Control[] children = radioGroup.getChildren();
    for ( int i = 0; i < children.length; i++ ) {
      Control child = children[ i ];
      Button button = (Button) child;
      if ( button.getSelection() ) {
        return i;
      }
    }
    return -1;
  }

  public void setSelectedIndex( int index ) {
    for ( Control control : radioGroup.getChildren() ) {
      ( (Button) control ).setSelection( false );
    }
    ( (Button) radioGroup.getChildren()[ index ] ).setSelection( true );
    radioGroup.getChildren()[index].notifyListeners( SWT.Selection, new Event() );
  }
}
