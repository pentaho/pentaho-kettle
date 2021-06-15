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
package org.eclipse.swt.internal.widgets.textkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenModifyVerify;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;


public final class TextLCA extends WidgetLCA<Text> {

  public static final TextLCA INSTANCE = new TextLCA();

  private static final String TYPE = "rwt.widgets.Text";
  private static final String[] ALLOWED_STYLES = {
    "CENTER",
    "LEFT",
    "RIGHT",
    "MULTI",
    "SINGLE",
    "PASSWORD",
    "SEARCH",
    "WRAP",
    "H_SCROLL",
    "V_SCROLL",
    "BORDER"
  };
  private static final String[] ALLOWED_STYLES_WITH_SEARCH = {
    "CENTER",
    "LEFT",
    "RIGHT",
    "SINGLE",
    "SEARCH",
    "ICON_CANCEL",
    "ICON_SEARCH",
    "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_TEXT_LIMIT = "textLimit";
  private static final String PROP_SELECTION = "selection";
  private static final String PROP_EDITABLE = "editable";
  private static final String PROP_ECHO_CHAR = "echoChar";
  private static final String PROP_MESSAGE = "message";

  private static final Point ZERO_SELECTION = new Point( 0, 0 );

  @Override
  public void preserveValues( Text text ) {
    preserveProperty( text, PROP_TEXT, text.getText() );
    preserveProperty( text, PROP_SELECTION, text.getSelection() );
    preserveProperty( text, PROP_TEXT_LIMIT, getTextLimit( text ) );
    preserveProperty( text, PROP_EDITABLE, text.getEditable() );
    preserveProperty( text, PROP_ECHO_CHAR, getEchoChar( text ) );
    preserveProperty( text, PROP_MESSAGE, text.getMessage() );
  }

  @Override
  public void renderInitialization( Text text ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( text, TYPE );
    remoteObject.setHandler( new TextOperationHandler( text ) );
    remoteObject.set( "parent", getId( text.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( text, getAllowedStyles( text ) ) ) );
  }

  @Override
  public void renderChanges( Text text ) throws IOException {
    ControlLCAUtil.renderChanges( text );
    WidgetLCAUtil.renderCustomVariant( text );
    renderProperty( text, PROP_TEXT, text.getText(), "" );
    renderProperty( text, PROP_EDITABLE, text.getEditable(), true );
    renderSelection( text );
    renderProperty( text, PROP_TEXT_LIMIT, getTextLimit( text ), null );
    renderProperty( text, PROP_ECHO_CHAR, getEchoChar( text ), null );
    renderProperty( text, PROP_MESSAGE, text.getMessage(), "" );
    renderListenModifyVerify( text );
    renderListenDefaultSelection( text );
    renderClientListeners( text );
  }

  private static void renderSelection( Text text ) {
    Point newValue = text.getSelection();
    boolean changed = hasChanged( text, PROP_SELECTION, newValue, ZERO_SELECTION );
    if( !changed ) {
      changed = hasChanged( text, PROP_TEXT, text.getText() ) && !newValue.equals( ZERO_SELECTION );
    }
    if( changed ) {
      RemoteObject remoteObject = getRemoteObject( text );
      remoteObject.set( PROP_SELECTION, new JsonArray().add( newValue.x ).add( newValue.y ) );
    }
  }

  private static String[] getAllowedStyles( Text text ) {
    return ( text.getStyle() & SWT.SEARCH ) != 0 ? ALLOWED_STYLES_WITH_SEARCH : ALLOWED_STYLES;
  }

  private static Integer getTextLimit( Text text ) {
    Integer result = null;
    int textLimit = text.getTextLimit();
    if( textLimit > 0 && textLimit != Text.LIMIT ) {
      result = Integer.valueOf( textLimit );
    }
    return result;
  }

  private static String getEchoChar( Text text ) {
    return text.getEchoChar() == 0 ? null : String.valueOf( text.getEchoChar() );
  }

  private TextLCA() {
    // prevent instantiation
  }

}
