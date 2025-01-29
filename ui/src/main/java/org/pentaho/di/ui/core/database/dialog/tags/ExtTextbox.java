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


package org.pentaho.di.ui.core.database.dialog.tags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.impl.AbstractXulComponent;
import org.pentaho.ui.xul.swt.tags.SwtTextbox;
import org.pentaho.ui.xul.util.TextType;

public class ExtTextbox extends SwtTextbox {

  public TextVar extText;
  private VariableSpace variableSpace;
  private XulComponent xulParent;

  private int style = SWT.NONE;

  public ExtTextbox( Element self, XulComponent parent, XulDomContainer container, String tagName ) {
    super( self, parent, container, tagName );
    String typeAttribute = self.getAttributeValue( "type" );
    if ( typeAttribute != null ) {
      this.type = TextType.valueOf( typeAttribute.toUpperCase() );
    }
    createNewExtText( parent );
  }

  private void createNewExtText( XulComponent parent ) {
    xulParent = parent;

    if ( ( xulParent != null ) && ( xulParent instanceof XulTree ) ) {
      variableSpace = (DatabaseMeta) ( (XulTree) xulParent ).getData();

    } else {
      variableSpace = new DatabaseMeta();
      style = SWT.BORDER;
    }

    if ( type == TextType.PASSWORD ) {
      extText = new PasswordTextVar( variableSpace, parentComposite, style );
    } else {
      extText = new TextVar( variableSpace, parentComposite, style );
    }
    textBox = extText.getTextWidget();
    addKeyListener( textBox );
    setManagedObject( extText );
  }

  @Override
  public Text createNewText() {
    // Don't do anything here. We'll create our own with createNewExtText().
    return null;
  }

  @Override
  public Object getTextControl() {
    getManagedObject();
    return extText.getTextWidget();
  }

  public Object getManagedObject() {
    if ( textBox.isDisposed() ) {
      int thisStyle = isMultiline() ? SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL : style;
      extText = new TextVar( variableSpace, parentComposite, thisStyle );
      setDisabled( isDisabled() );
      setMaxlength( getMaxlength() );
      setValue( getValue() );
      setReadonly( isReadonly() );
      setType( getType() );
      textBox = extText.getTextWidget();
      setManagedObject( extText );
      layout();
    }
    return super.getManagedObject();
  }

  @Override
  public void layout() {
    ( (AbstractXulComponent) xulParent ).layout();
  }

  public void setVariableSpace( VariableSpace space ) {
    variableSpace = space;
    extText.setVariables( variableSpace );
  }

  @Override
  public void setType( TextType type ) {
    return;
  }
}
