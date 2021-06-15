/*******************************************************************************
 * Copyright (c) 2009, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.internal.widgets.controldecoratorkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.ControlDecorator;


public class ControlDecoratorLCA extends WidgetLCA<ControlDecorator> {

  public static final ControlDecoratorLCA INSTANCE = new ControlDecoratorLCA();

  private static final String TYPE = "rwt.widgets.ControlDecorator";
  private static final String[] ALLOWED_STYLES = {
    "TOP", "BOTTOM", "LEFT", "RIGHT", "CENTER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_VISIBLE = "visible";
  private static final String PROP_SHOW_HOVER = "showHover";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  @Override
  public void preserveValues( ControlDecorator decorator ) {
    WidgetLCAUtil.preserveBounds( decorator, decorator.getBounds() );
    preserveProperty( decorator, PROP_TEXT, decorator.getText() );
    preserveProperty( decorator, PROP_IMAGE, decorator.getImage() );
    preserveProperty( decorator, PROP_VISIBLE, decorator.isVisible() );
    preserveProperty( decorator, PROP_SHOW_HOVER, decorator.getShowHover() );
  }

  @Override
  public void renderInitialization( ControlDecorator decorator ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( decorator, TYPE );
    remoteObject.setHandler( new ControlDecoratorOperationHandler( decorator ) );
    remoteObject.set( "parent", getId( decorator.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( decorator, ALLOWED_STYLES ) ) );
    renderProperty( decorator, PROP_MARKUP_ENABLED, isMarkupEnabledFor( decorator ), false );
  }

  @Override
  public void renderChanges( ControlDecorator decorator ) throws IOException {
    WidgetLCAUtil.renderBounds( decorator, decorator.getBounds() );
    renderProperty( decorator, PROP_TEXT, decorator.getText(), "" );
    renderProperty( decorator, PROP_IMAGE, decorator.getImage(), null );
    renderProperty( decorator, PROP_VISIBLE, decorator.isVisible(), false );
    renderProperty( decorator, PROP_SHOW_HOVER, decorator.getShowHover(), true );
    WidgetLCAUtil.renderListenSelection( decorator );
    WidgetLCAUtil.renderListenDefaultSelection( decorator );
  }

  @Override
  public void renderDispose( ControlDecorator decorator ) throws IOException {
    getRemoteObject( decorator ).destroy();
  }

  private ControlDecoratorLCA() {
    // prevent instantiation
  }

}
