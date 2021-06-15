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
package org.eclipse.swt.internal.widgets.listkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.IListAdapter;
import org.eclipse.swt.widgets.List;


public class ListLCA extends WidgetLCA<List> {

  public static final ListLCA INSTANCE = new ListLCA();

  private static final String TYPE = "rwt.widgets.List";
  private static final String[] ALLOWED_STYLES = { "SINGLE", "MULTI", "BORDER" };

  private static final String PROP_ITEMS = "items";
  private static final String PROP_SELECTION_INDICES = "selectionIndices";
  private static final String PROP_TOP_INDEX = "topIndex";
  private static final String PROP_FOCUS_INDEX = "focusIndex";
  private static final String PROP_ITEM_DIMENSIONS = "itemDimensions";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  private static final String[] DEFAUT_ITEMS = new String[ 0 ];
  private static final int[] DEFAUT_SELECTION_INDICES = new int[ 0 ];
  private static final int DEFAULT_TOP_INDEX = 0;
  private static final int DEFAULT_FOCUS_INDEX = -1;
  private static final Point DEFAULT_ITEM_DIMENSIONS = new Point( 0, 0 );

  @Override
  public void preserveValues( List list ) {
    preserveProperty( list, PROP_ITEMS, list.getItems() );
    preserveProperty( list, PROP_SELECTION_INDICES, list.getSelectionIndices() );
    preserveProperty( list, PROP_TOP_INDEX, list.getTopIndex() );
    preserveProperty( list, PROP_FOCUS_INDEX, list.getFocusIndex() );
    preserveProperty( list, PROP_ITEM_DIMENSIONS, getItemDimensions( list ) );
  }

  @Override
  public void renderInitialization( List list ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( list, TYPE );
    remoteObject.setHandler( new ListOperationHandler( list ) );
    remoteObject.set( "parent", getId( list.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( list, ALLOWED_STYLES ) ) );
    remoteObject.set( PROP_MARKUP_ENABLED, isMarkupEnabledFor( list ) );
  }

  @Override
  public void renderChanges( List list ) throws IOException {
    ControlLCAUtil.renderChanges( list );
    WidgetLCAUtil.renderCustomVariant( list );
    renderProperty( list, PROP_ITEMS, list.getItems(), DEFAUT_ITEMS );
    renderProperty( list,
                    PROP_SELECTION_INDICES,
                    list.getSelectionIndices(),
                    DEFAUT_SELECTION_INDICES );
    renderProperty( list, PROP_TOP_INDEX, list.getTopIndex(), DEFAULT_TOP_INDEX );
    renderProperty( list, PROP_FOCUS_INDEX, list.getFocusIndex(), DEFAULT_FOCUS_INDEX );
    renderListenSelection( list );
    renderListenDefaultSelection( list );
    renderProperty( list,
                    PROP_ITEM_DIMENSIONS,
                    getItemDimensions( list ),
                    DEFAULT_ITEM_DIMENSIONS );
  }

  private static Point getItemDimensions( List list ) {
    return getAdapter( list ).getItemDimensions();
  }

  private static IListAdapter getAdapter( List list ) {
    return list.getAdapter( IListAdapter.class );
  }

  private ListLCA() {
    // prevent instantiation
  }

}
