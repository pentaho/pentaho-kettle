/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.expandbarkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IExpandBarAdapter;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;


public final class ExpandBarLCA extends WidgetLCA<ExpandBar> {

  public static final ExpandBarLCA INSTANCE = new ExpandBarLCA();

  private static final String TYPE = "rwt.widgets.ExpandBar";
  private static final String[] ALLOWED_STYLES = { "NO_RADIO_GROUP", "BORDER" };

  private static final String PROP_BOTTOM_SPACING_BOUNDS = "bottomSpacingBounds";
  private static final String PROP_VSCROLLBAR_MAX = "vScrollBarMax";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";
  private static final String PROP_EXPAND_LISTENER = "Expand";
  private static final String PROP_COLLAPSE_LISTENER = "Collapse";

  @Override
  public void preserveValues( ExpandBar expandBar ) {
    preserveProperty( expandBar, PROP_BOTTOM_SPACING_BOUNDS, getBottomSpacingBounds( expandBar ) );
    preserveProperty( expandBar, PROP_VSCROLLBAR_MAX, getVScrollBarMax( expandBar ) );
  }

  @Override
  public void renderInitialization( ExpandBar expandBar ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( expandBar, TYPE );
    remoteObject.setHandler( new ExpandBarOperationHandler( expandBar ) );
    remoteObject.set( "parent", getId( expandBar.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( expandBar, ALLOWED_STYLES ) ) );
    remoteObject.set( PROP_MARKUP_ENABLED, isMarkupEnabledFor( expandBar ) );
    // Always listen for Expand and Collapse.
    // Currently required for item's control visibility and bounds update.
    remoteObject.listen( PROP_EXPAND_LISTENER, true );
    remoteObject.listen( PROP_COLLAPSE_LISTENER, true );
  }

  @Override
  public void renderChanges( ExpandBar expandBar ) throws IOException {
    ControlLCAUtil.renderChanges( expandBar );
    WidgetLCAUtil.renderCustomVariant( expandBar );
    renderProperty( expandBar,
                    PROP_BOTTOM_SPACING_BOUNDS,
                    getBottomSpacingBounds( expandBar ),
                    null );
    renderProperty( expandBar, PROP_VSCROLLBAR_MAX, getVScrollBarMax( expandBar ), 0 );
  }

  private static Rectangle getBottomSpacingBounds( ExpandBar bar ) {
    return getExpandBarAdapter( bar ).getBottomSpacingBounds();
  }

  private static int getVScrollBarMax( ExpandBar bar ) {
    int result = 0;
    if( ( bar.getStyle() & SWT.V_SCROLL ) != 0 ) {
      IExpandBarAdapter expandBarAdapter = getExpandBarAdapter( bar );
      ExpandItem[] items = bar.getItems();
      for( int i = 0; i < items.length; i++ ) {
        result += expandBarAdapter.getBounds( items[ i ] ).height;
      }
      result += bar.getSpacing() * ( items.length + 1 );
    }
    return result;
  }

  public static IExpandBarAdapter getExpandBarAdapter( ExpandBar bar ) {
    return bar.getAdapter( IExpandBarAdapter.class );
  }

  private ExpandBarLCA() {
    // prevent instantiation
  }

}
