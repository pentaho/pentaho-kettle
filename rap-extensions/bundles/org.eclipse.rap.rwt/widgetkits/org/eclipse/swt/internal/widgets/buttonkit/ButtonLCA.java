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
package org.eclipse.swt.internal.widgets.buttonkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.util.MnemonicUtil.findMnemonicCharacterIndex;
import static org.eclipse.rap.rwt.internal.util.MnemonicUtil.removeAmpersandControlCharacters;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;


public final class ButtonLCA extends WidgetLCA<Button> {

  public static final ButtonLCA INSTANCE = new ButtonLCA();

  private static final String TYPE = "rwt.widgets.Button";
  private static final String[] ALLOWED_STYLES = {
    "ARROW", "CHECK", "PUSH", "RADIO", "TOGGLE", "FLAT", "WRAP", "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_GRAYED = "grayed";
  private static final String PROP_ALIGNMENT = "alignment";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";
  private static final String PROP_BADGE = "badge";

  private static final String DEFAULT_ALIGNMENT = "center";

  @Override
  public void preserveValues( Button button ) {
    preserveProperty( button, PROP_TEXT, button.getText() );
    preserveProperty( button, PROP_IMAGE, button.getImage() );
    preserveProperty( button, PROP_SELECTION, Boolean.valueOf( button.getSelection() ) );
    preserveProperty( button, PROP_GRAYED, Boolean.valueOf( button.getGrayed() ) );
    preserveProperty( button, PROP_ALIGNMENT, getAlignment( button ) );
    preserveProperty( button, PROP_BADGE, getBadge( button ) );
  }

  @Override
  public void renderInitialization( Button button ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( button, TYPE );
    remoteObject.setHandler( new ButtonOperationHandler( button ) );
    remoteObject.set( "parent", getId( button.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( button, ALLOWED_STYLES ) ) );
    renderProperty( button, PROP_MARKUP_ENABLED, isMarkupEnabledFor( button ), false );
  }

  @Override
  public void renderChanges( Button button ) throws IOException {
    ControlLCAUtil.renderChanges( button );
    WidgetLCAUtil.renderCustomVariant( button );
    renderText( button );
    renderMnemonicIndex( button );
    renderProperty( button, PROP_IMAGE, button.getImage(), null );
    renderProperty( button, PROP_ALIGNMENT, getAlignment( button ), DEFAULT_ALIGNMENT );
    renderProperty( button, PROP_SELECTION, button.getSelection(), false );
    renderProperty( button, PROP_GRAYED, button.getGrayed(), false );
    renderProperty( button, PROP_BADGE, getBadge( button ), null );
    renderListenSelection( button );
    renderClientListeners( button );
  }

  private static String getAlignment( Button button ) {
    int alignment = button.getAlignment();
    String result;
    if( ( alignment & SWT.LEFT ) != 0 ) {
      result = "left";
    } else if( ( alignment & SWT.CENTER ) != 0 ) {
      result = "center";
    } else if( ( alignment & SWT.RIGHT ) != 0 ) {
      result = "right";
    } else if( ( alignment & SWT.UP ) != 0 ) {
      result = "up";
    } else if( ( alignment & SWT.DOWN ) != 0 ) {
      result = "down";
    } else {
      result = "left";
    }
    return result;
  }

  private static String getBadge( Button button ) {
    return ( String )button.getData( RWT.BADGE );
  }

  private static void renderText( Button button ) {
    String newValue = button.getText();
    if( hasChanged( button, PROP_TEXT, newValue, "" ) ) {
      String text = removeAmpersandControlCharacters( newValue );
      getRemoteObject( button ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( Button button ) {
    String text = button.getText();
    if( hasChanged( button, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        getRemoteObject( button ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

  private ButtonLCA() {
    // prevent instantiation
  }

}
