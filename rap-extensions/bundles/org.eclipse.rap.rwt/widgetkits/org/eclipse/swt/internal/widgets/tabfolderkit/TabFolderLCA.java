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
package org.eclipse.swt.internal.widgets.tabfolderkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.TabFolder;


public class TabFolderLCA extends WidgetLCA<TabFolder> {

  public static final TabFolderLCA INSTANCE = new TabFolderLCA();

  private static final String TYPE = "rwt.widgets.TabFolder";
  private static final String[] ALLOWED_STYLES = { "TOP", "BOTTOM", "NO_RADIO_GROUP", "BORDER" };

  private static final String PROP_SELECTION = "selection";
  private static final String PROP_SELECTION_LISTENER = "Selection";

  @Override
  public void preserveValues( TabFolder folder ) {
    preserveProperty( folder, PROP_SELECTION, getSelection( folder ) );
  }

  @Override
  public void renderInitialization( TabFolder folder ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( folder, TYPE );
    remoteObject.setHandler( new TabFolderOperationHandler( folder ) );
    remoteObject.set( "parent", getId( folder.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( folder, ALLOWED_STYLES ) ) );
    // Always listen for Selection.
    // Currently required for item's control visibility and bounds update.
    remoteObject.listen( PROP_SELECTION_LISTENER, true );
  }

  @Override
  public void renderChanges( TabFolder folder ) throws IOException {
    ControlLCAUtil.renderChanges( folder );
    WidgetLCAUtil.renderCustomVariant( folder );
    renderProperty( folder, PROP_SELECTION, getSelection( folder ), null );
  }

  private static String getSelection( TabFolder folder ) {
    String selection = null;
    int selectionIndex = folder.getSelectionIndex();
    if( selectionIndex != -1 ) {
      selection = getId( folder.getItem( selectionIndex ) );
    }
    return selection;
  }

  private TabFolderLCA() {
    // prevent instantiation
  }

}
