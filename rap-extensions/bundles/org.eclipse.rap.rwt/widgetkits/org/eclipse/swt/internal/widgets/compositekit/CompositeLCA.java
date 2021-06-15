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
package org.eclipse.swt.internal.widgets.compositekit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Composite;


public class CompositeLCA extends WidgetLCA<Composite> {

  public static final CompositeLCA INSTANCE = new CompositeLCA();

  private static final String TYPE = "rwt.widgets.Composite";
  private static final String[] ALLOWED_STYLES = { "NO_RADIO_GROUP", "BORDER" };
  private static final String PROP_CLIENT_AREA = "clientArea";

  @Override
  public void preserveValues( Composite composite ) {
    WidgetLCAUtil.preserveBackgroundGradient( composite );
    WidgetLCAUtil.preserveRoundedBorder( composite );
    WidgetLCAUtil.preserveProperty( composite, PROP_CLIENT_AREA, composite.getClientArea() );
  }

  @Override
  public void renderInitialization( Composite composite ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( composite, TYPE );
    remoteObject.setHandler( new CompositeOperationHandler( composite ) );
    remoteObject.set( "parent", getId( composite.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( composite, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Composite composite ) throws IOException {
    ControlLCAUtil.renderChanges( composite );
    WidgetLCAUtil.renderBackgroundGradient( composite );
    WidgetLCAUtil.renderRoundedBorder( composite );
    WidgetLCAUtil.renderCustomVariant( composite );
    renderClientArea( composite );
    renderClientListeners( composite );
  }

  public void renderClientArea( Composite composite ) {
    renderProperty( composite, PROP_CLIENT_AREA, composite.getClientArea(), null );
  }

  private CompositeLCA() {
    // prevent instantiation
  }

}
