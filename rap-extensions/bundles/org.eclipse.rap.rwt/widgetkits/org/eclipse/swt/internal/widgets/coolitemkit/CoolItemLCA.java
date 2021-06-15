/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.coolitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.CoolItem;


public class CoolItemLCA extends WidgetLCA<CoolItem> {

  public static final CoolItemLCA INSTANCE = new CoolItemLCA();

  private static final String TYPE = "rwt.widgets.CoolItem";
  private static final String[] ALLOWED_STYLES = { "DROP_DOWN", "VERTICAL" };

  static final String PROP_CONTROL = "control";

  /* (intentionally not JavaDoc'ed)
   * Unnecesary to call ItemLCAUtil.preserve, CoolItem does neither use text
   * nor image
   */
  @Override
  public void preserveValues( CoolItem item ) {
    preserveProperty( item, PROP_CONTROL, item.getControl() );
    preserveProperty( item, Props.BOUNDS, item.getBounds() );
  }

  @Override
  public void renderInitialization( CoolItem item ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new CoolItemOperationHandler( item ) );
    remoteObject.set( "parent", getId( item.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( item, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( CoolItem item ) throws IOException {
    WidgetLCAUtil.renderBounds( item, item.getBounds() );
    renderProperty( item, PROP_CONTROL, item.getControl(), null );
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
  }

  private CoolItemLCA() {
    // prevent instantiation
  }

}
