/*******************************************************************************
 * Copyright (c) 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.scrollbarkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectImpl;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.ScrollBar;


public final class ScrollBarLCA extends WidgetLCA<ScrollBar> {

  public static final ScrollBarLCA INSTANCE = new ScrollBarLCA();

  private static final String TYPE = "rwt.widgets.ScrollBar";
  private static final String[] ALLOWED_STYLES = { "HORIZONTAL", "VERTICAL" };
  private static final String PROP_VISIBILITY = "visibility";

  @Override
  public void preserveValues( ScrollBar scrollBar ) {
    preserveProperty( scrollBar, PROP_VISIBILITY, scrollBar.getVisible() );
  }

  @Override
  public void renderInitialization( ScrollBar scrollBar ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( scrollBar, TYPE );
    remoteObject.setHandler( new ScrollBarOperationHandler( scrollBar ) );
    remoteObject.set( "parent", getId( scrollBar.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( scrollBar, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( ScrollBar scrollBar ) throws IOException {
    renderProperty( scrollBar, PROP_VISIBILITY, scrollBar.getVisible(), false );
    renderListenSelection( scrollBar );
  }

  @Override
  public void renderDispose( ScrollBar widget ) throws IOException {
    // Client scrollbars are part of the scrollable widget, they cannot be destroyed
    RemoteObjectImpl remoteObject = ( RemoteObjectImpl )getRemoteObject( widget );
    remoteObject.markDestroyed();
  }

  private ScrollBarLCA() {
    // prevent instantiation
  }

}
