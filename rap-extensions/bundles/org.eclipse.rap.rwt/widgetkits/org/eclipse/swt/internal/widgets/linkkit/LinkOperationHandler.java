/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.linkkit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_INDEX;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SELECTION;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.ControlOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.ILinkAdapter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;


public class LinkOperationHandler extends ControlOperationHandler<Link> {

  public LinkOperationHandler( Link link ) {
    super( link );
  }

  @Override
  public void handleNotify( Link link, String eventName, JsonObject properties ) {
    if( EVENT_SELECTION.equals( eventName ) ) {
      handleNotifySelection( link, properties );
    } else {
      super.handleNotify( link, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY Selection
   *
   * @param altKey (boolean) true if the ALT key was pressed
   * @param ctrlKey (boolean) true if the CTRL key was pressed
   * @param shiftKey (boolean) true if the SHIFT key was pressed
   * @param index (int) the index of activated hyperlink
   */
  public void handleNotifySelection( Link link, JsonObject properties ) {
    int index = properties.get( EVENT_PARAM_INDEX ).asInt();
    String[] ids = link.getAdapter( ILinkAdapter.class ).getIds();
    if( index < ids.length ) {
      Event event = createSelectionEvent( SWT.Selection, properties );
      event.text = ids[ index ];
      link.notifyListeners( SWT.Selection, event );
    }
  }

}
