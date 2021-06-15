/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.labelkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;


final class StandardLabelLCA extends AbstractLabelLCADelegate {

  static final StandardLabelLCA INSTANCE = new StandardLabelLCA();

  private static final String TYPE = "rwt.widgets.Label";
  private static final String[] ALLOWED_STYLES = { "WRAP", "BORDER" };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_ALIGNMENT = "alignment";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  private static final String DEFAULT_ALIGNMENT = "left";

  @Override
  void preserveValues( Label label ) {
    preserveProperty( label, PROP_TEXT, label.getText() );
    preserveProperty( label, PROP_IMAGE, label.getImage() );
    preserveProperty( label, PROP_ALIGNMENT, getAlignment( label ) );
  }

  @Override
  void renderInitialization( Label label ) throws IOException {
    RemoteObject remoteObject = RemoteObjectFactory.createRemoteObject( label, TYPE );
    remoteObject.setHandler( new LabelOperationHandler( label ) );
    remoteObject.set( "parent", getId( label.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( label, ALLOWED_STYLES ) ) );
    renderProperty( label, PROP_MARKUP_ENABLED, isMarkupEnabledFor( label ), false );
  }

  @Override
  void renderChanges( Label label ) throws IOException {
    ControlLCAUtil.renderChanges( label );
    WidgetLCAUtil.renderCustomVariant( label );
    renderText( label );
    renderClientListeners( label );
    renderMnemonicIndex( label );
    renderProperty( label, PROP_IMAGE, label.getImage(), null );
    renderProperty( label, PROP_ALIGNMENT, getAlignment( label ), DEFAULT_ALIGNMENT );
  }

  private static String getAlignment( Label label ) {
    int alignment = label.getAlignment();
    String result;
    if( ( alignment & SWT.LEFT ) != 0 ) {
      result = "left";
    } else if( ( alignment & SWT.CENTER ) != 0 ) {
      result = "center";
    } else if( ( alignment & SWT.RIGHT ) != 0 ) {
      result = "right";
    } else {
      result = "left";
    }
    return result;
  }

  private static void renderText( Label label ) {
    String newValue = label.getText();
    if( WidgetLCAUtil.hasChanged( label, PROP_TEXT, newValue, "" ) ) {
      String text = newValue;
      if( !isMarkupEnabledFor( label ) ) {
        text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      }
      getRemoteObject( label ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( Label label ) {
    if( !isMarkupEnabledFor( label ) ) {
      String text = label.getText();
      if( WidgetLCAUtil.hasChanged( label, PROP_TEXT, text, "" ) ) {
        int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
        if( mnemonicIndex != -1 ) {
          getRemoteObject( label ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
        }
      }
    }
  }

  public StandardLabelLCA() {
    // prevent instantiation
  }

}
