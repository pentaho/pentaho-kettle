/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

/**
 * This widget works like PasswordTextVar except it also provides a button that will toggle visibility of the text in
 * the textbox.
 */
public class PasswordVisibleTextVar extends Composite {
  VariableSpace space;
  PropsUI props = PropsUI.getInstance();
  private Button wVisibleButton;
  private static final Class<?> PKG = PasswordVisibleTextVar.class;
  public static final String NON_VISIBILE_IMAGE = "ui/images/show-inactive.png";
  public static final String VISIBLE_IMAGE = "ui/images/hide-inactive.png";

  private Composite parentComposite;
  private TextVar wTextVar;
  private int flags;
  private String toolTipText;
  private GetCaretPositionInterface getCaretPositionInterface;
  private InsertTextInterface insertTextInterface;
  private ModifyListener modifyListener;
  private boolean isTextVisible = false; //Holds the visibility state of the text

  public PasswordVisibleTextVar( VariableSpace space, Composite composite, int flags ) {
    this( space, composite, flags, null, null, null );
  }

  public PasswordVisibleTextVar( VariableSpace space, Composite composite, int flags, String toolTipText ) {
    this( space, composite, flags, toolTipText, null, null );
  }

  public PasswordVisibleTextVar( VariableSpace space, Composite composite, int flags,
                                 GetCaretPositionInterface getCaretPositionInterface,
                                 InsertTextInterface insertTextInterface ) {
    this( space, composite, flags, null, getCaretPositionInterface, insertTextInterface );
  }

  public PasswordVisibleTextVar( VariableSpace space, Composite composite, int flags, String toolTipText,
                                 GetCaretPositionInterface getCaretPositionInterface,
                                 InsertTextInterface insertTextInterface ) {
    super( composite, SWT.NONE );
    this.space = space;
    this.parentComposite = composite;
    this.flags = flags;
    this.toolTipText = toolTipText;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;

    initialize( flags, toolTipText, getCaretPositionInterface, insertTextInterface );
  }

  protected void initialize( int flags, String toolTipText, GetCaretPositionInterface getCaretPositionInterface,
                             InsertTextInterface insertTextInterface ) {
    props.setLook( this );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;
    this.setLayout( formLayout );

    this.wVisibleButton = new Button( this, SWT.PUSH );
    props.setLook( wVisibleButton );
    FormData fdButton = new FormData();
    fdButton.top = new FormAttachment( 0, 0 );
    fdButton.right = new FormAttachment( 100, 0 );
    wVisibleButton.setLayoutData( fdButton );

    setTextVisible( false );
    wVisibleButton.addListener( SWT.Selection, event -> toggleVisible() );
  }

  private void setTextVisible( boolean visible ) {
    int flagsInEffect = visible ? flags : flags | SWT.PASSWORD;
    TextVar newTextVar = new TextVar( space, this, flagsInEffect, toolTipText, getCaretPositionInterface,
      insertTextInterface );
    String value = null;
    if ( wTextVar != null ) {
      value = wTextVar.getText();
      newTextVar.setText( value );
      newTextVar.setLayoutData( wTextVar.getLayoutData() );
      wTextVar.dispose();
    } else {
      FormData fdTextVar = new FormData();
      fdTextVar.top = new FormAttachment( 0, 0 );
      fdTextVar.left = new FormAttachment( 0, 0 );
      fdTextVar.right = new FormAttachment( wVisibleButton, 0 );
      newTextVar.setLayoutData( fdTextVar );
    }
    wTextVar = newTextVar;
    props.setLook( wTextVar );
    wVisibleButton.setImage(
      ImageUtil.getImageAsResource( parentComposite.getDisplay(), visible ? VISIBLE_IMAGE : NON_VISIBILE_IMAGE ) );
    wVisibleButton.setToolTipText( BaseMessages.getString( PKG, "PasswordVisibleTextVar.tooltip" ) );
    if ( modifyListener != null ) {
      addModifyListener( modifyListener );
    }
    isTextVisible = visible;
    this.layout();
  }

  private void toggleVisible() {
    setTextVisible( !isTextVisible );
  }

  public TextVar getTextVarWidget() {
    return wTextVar;
  }

  public String getText() {
    return wTextVar.getText();
  }

  public Button getButton() {
    return wVisibleButton;
  }

  public void addModifyListener( ModifyListener modifyListener ) {
    this.modifyListener = modifyListener;
    wTextVar.addModifyListener( modifyListener );
  }

  public void setText( String text ) {
    wTextVar.setText( text );
  }

  protected ModifyListener getModifyListenerTooltipText( final Text textField ) {
    return new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        textField.setToolTipText( toolTipText );
      }
    };
  }
}
