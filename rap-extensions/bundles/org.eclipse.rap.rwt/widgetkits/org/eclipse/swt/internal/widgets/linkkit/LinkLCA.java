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
package org.eclipse.swt.internal.widgets.linkkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.ILinkAdapter;
import org.eclipse.swt.widgets.Link;


public class LinkLCA extends WidgetLCA<Link> {

  public static final LinkLCA INSTANCE = new LinkLCA();

  private static final String TYPE = "rwt.widgets.Link";
  private static final String[] ALLOWED_STYLES = { "BORDER" };

  static final String PROP_TEXT = "text";

  @Override
  public void preserveValues( Link link ) {
    preserveProperty( link, PROP_TEXT, link.getText() );
  }

  @Override
  public void renderInitialization( Link link ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( link, TYPE );
    remoteObject.setHandler( new LinkOperationHandler( link ) );
    remoteObject.set( "parent", getId( link.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( link, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Link link ) throws IOException {
    ControlLCAUtil.renderChanges( link );
    WidgetLCAUtil.renderCustomVariant( link );
    renderText( link );
    renderListenSelection( link );
  }

  private static void renderText( Link link ) {
    String newValue = link.getText();
    if( hasChanged( link, PROP_TEXT, newValue, "" ) ) {
      getRemoteObject( link ).set( PROP_TEXT, getTextObject( link ) );
    }
  }

  private static JsonArray getTextObject( Link link ) {
    ILinkAdapter adapter = link.getAdapter( ILinkAdapter.class );
    String displayText = adapter.getDisplayText();
    Point[] offsets = adapter.getOffsets();
    JsonArray result = new JsonArray();
    int length = displayText.length();
    int pos = 0;
    for( int i = 0; i < offsets.length; i++ ) {
      int start = offsets[ i ].x;
      int end = offsets[ i ].y + 1;
      // before link
      if( pos < start ) {
        result.add( new JsonArray().add( displayText.substring( pos, start ) )
                                   .add( JsonValue.NULL ) );
      }
      // link itself
      if( start < end ) {
        result.add( new JsonArray().add( displayText.substring( start, end ) ).add( i ) );
      }
      pos = end;
    }
    // after last link
    if( pos < length ) {
      result.add( new JsonArray().add( displayText.substring( pos, length ) )
                                 .add( JsonValue.NULL ) );
    }
    return result;
  }

  private LinkLCA() {
    // prevent instantiation
  }

}
