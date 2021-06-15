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
package org.eclipse.swt.internal.widgets.coolbarkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.CoolBar;


public class CoolBarLCA extends WidgetLCA<CoolBar> {

  public static final CoolBarLCA INSTANCE = new CoolBarLCA();

  private static final String TYPE = "rwt.widgets.CoolBar";
  private static final String[] ALLOWED_STYLES = {
    "FLAT", "HORIZONTAL", "VERTICAL", "NO_RADIO_GROUP", "BORDER"
  };

  public static final String PROP_LOCKED = "locked";

  @Override
  public void preserveValues( CoolBar coolBar ) {
    RemoteAdapter adapter = WidgetUtil.getAdapter( coolBar );
    adapter.preserve( PROP_LOCKED, Boolean.valueOf( coolBar.getLocked() ) );
  }

  @Override
  public void renderInitialization( CoolBar coolBar ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( coolBar, TYPE );
    remoteObject.setHandler( new CoolBarOperationHandler( coolBar ) );
    remoteObject.set( "parent", getId( coolBar.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( coolBar, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( CoolBar coolBar ) throws IOException {
    ControlLCAUtil.renderChanges( coolBar );
    renderProperty( coolBar, PROP_LOCKED ,coolBar.getLocked(), false );
    WidgetLCAUtil.renderCustomVariant( coolBar );
  }

  private CoolBarLCA() {
    // prevent instantiation
  }

}
