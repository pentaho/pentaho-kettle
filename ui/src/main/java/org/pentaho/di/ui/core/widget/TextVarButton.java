/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

public class TextVarButton extends TextVar {

  public TextVarButton( VariableSpace space, Composite composite, int flags ) {
    super( space, composite, flags );
  }

  public TextVarButton( VariableSpace space, Composite composite, int flags, String toolTipText ) {
    super( space, composite, flags, toolTipText );
  }

  public TextVarButton( VariableSpace space, Composite composite, int flags,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface ) {
    super( space, composite, flags, getCaretPositionInterface, insertTextInterface );
  }

  public TextVarButton( VariableSpace space, Composite composite, int flags,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface,
      SelectionListener selectionListener ) {
    super( composite, space, flags, getCaretPositionInterface, insertTextInterface, selectionListener );
  }

  public TextVarButton( VariableSpace space, Composite composite, int flags, String toolTipText,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface ) {
    super( space, composite, flags, toolTipText, getCaretPositionInterface, insertTextInterface );
  }

  protected void initialize( VariableSpace space, Composite composite, int flags, String toolTipText,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface,
      SelectionListener selectionListener ) {
    this.toolTipText = toolTipText;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;
    this.variables = space;

    PropsUI.getInstance().setLook( this );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;
    this.setLayout( formLayout );

    Button button = new Button( this, SWT.PUSH );
    PropsUI.getInstance().setLook( button );
    button.setText( "..." );
    FormData fdButton = new FormData();
    fdButton.top = new FormAttachment( 0, 0 );
    fdButton.right = new FormAttachment( 100, 0 );
    fdButton.height = 25;
    fdButton.width = 30;
    button.setLayoutData( fdButton );
    if ( selectionListener != null ) {
      button.addSelectionListener( selectionListener );
    }

    wText = new Text( this, flags );
    controlDecoration = new ControlDecoration( wText, SWT.CENTER | SWT.RIGHT, this );
    Image image = GUIResource.getInstance().getImageVariable();
    controlDecoration.setImage( image );
    controlDecoration.setDescriptionText( BaseMessages.getString( PKG, "TextVar.tooltip.InsertVariable" ) );
    PropsUI.getInstance().setLook( controlDecoration.getControl() );

    modifyListenerTooltipText = getModifyListenerTooltipText( wText );
    wText.addModifyListener( modifyListenerTooltipText );

    controlSpaceKeyAdapter =
        new ControlSpaceKeyAdapter( variables, wText, getCaretPositionInterface, insertTextInterface );
    wText.addKeyListener( controlSpaceKeyAdapter );

    FormData fdText = new FormData();
    fdText.top = new FormAttachment( 0, 0 );
    fdText.left = new FormAttachment( 0, 0 );
    fdText.right = new FormAttachment( button, -image.getBounds().width );
    wText.setLayoutData( fdText );
  }

}
