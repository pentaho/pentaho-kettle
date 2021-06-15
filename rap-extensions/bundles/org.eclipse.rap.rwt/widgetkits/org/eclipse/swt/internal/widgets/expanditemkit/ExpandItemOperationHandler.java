/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.expanditemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.widgets.ExpandItem;


public class ExpandItemOperationHandler extends WidgetOperationHandler<ExpandItem> {

  private static final String PROP_EXPANDED = "expanded";

  public ExpandItemOperationHandler( ExpandItem item ) {
    super( item );
  }

  @Override
  public void handleSet( ExpandItem item, JsonObject properties ) {
    handleSetExpanded( item, properties );
  }

  /*
   * PROTOCOL SET expanded
   *
   * @param expanded (boolean) true if the item was expanded, false otherwise
   */
  public void handleSetExpanded( final ExpandItem item, JsonObject properties ) {
    final JsonValue expanded = properties.get( PROP_EXPANDED );
    if( expanded != null ) {
      ProcessActionRunner.add( new Runnable() {
        @Override
        public void run() {
          item.setExpanded( expanded.asBoolean() );
          preserveProperty( item, PROP_EXPANDED, item.getExpanded() );
        }
      } );
    }
  }

}
