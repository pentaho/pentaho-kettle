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
package org.eclipse.swt.internal.custom.scrolledcompositekit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ScrollBar;


public final class ScrolledCompositeLCA extends WidgetLCA<ScrolledComposite> {

  public static final ScrolledCompositeLCA INSTANCE = new ScrolledCompositeLCA();

  private static final String TYPE = "rwt.widgets.ScrolledComposite";
  private static final String[] ALLOWED_STYLES = { "H_SCROLL", "V_SCROLL", "BORDER" };

  // Property names
  private static final String PROP_ORIGIN = "origin";
  private static final String PROP_CONTENT = "content";
  private static final String PROP_SHOW_FOCUSED_CONTROL = "showFocusedControl";

  // Default values
  private static final Point DEFAULT_ORIGIN = new Point( 0, 0 );

  @Override
  public void preserveValues( ScrolledComposite composite ) {
    preserveProperty( composite, PROP_ORIGIN, getOrigin( composite ) );
    preserveProperty( composite, PROP_CONTENT, composite.getContent() );
    preserveProperty( composite, PROP_SHOW_FOCUSED_CONTROL, composite.getShowFocusedControl() );
  }

  @Override
  public void renderInitialization( ScrolledComposite composite ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( composite, TYPE );
    remoteObject.setHandler( new ScrolledCompositeOperationHandler( composite ) );
    remoteObject.set( "parent", getId( composite.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( composite, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( ScrolledComposite composite ) throws IOException {
    ControlLCAUtil.renderChanges( composite );
    WidgetLCAUtil.renderCustomVariant( composite );
    renderProperty( composite, PROP_CONTENT, composite.getContent(), null );
    renderProperty( composite, PROP_ORIGIN, getOrigin( composite ), DEFAULT_ORIGIN );
    renderProperty( composite,
                    PROP_SHOW_FOCUSED_CONTROL,
                    composite.getShowFocusedControl(),
                    false );
  }

  private static Point getOrigin( ScrolledComposite composite ) {
    Point result = new Point( 0, 0 );
    ScrollBar horizontalBar = composite.getHorizontalBar();
    if( horizontalBar != null ) {
      result.x = horizontalBar.getSelection();
    }
    ScrollBar verticalBar = composite.getVerticalBar();
    if( verticalBar != null ) {
      result.y = verticalBar.getSelection();
    }
    return result;
  }

  private ScrolledCompositeLCA() {
    // prevent instantiation
  }

}
