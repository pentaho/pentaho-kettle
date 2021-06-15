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
package org.eclipse.swt.internal.widgets.tableitemkit;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.widgets.TableItem;


public class TableItemOperationHandler extends WidgetOperationHandler<TableItem> {

  private static final String PROP_CHECKED = "checked";

  public TableItemOperationHandler( TableItem item ) {
    super( item );
  }

  @Override
  public void handleSet( TableItem item, JsonObject properties ) {
    handleSetChecked( item, properties );
  }

  /*
   * PROTOCOL SET checked
   *
   * @param checked (boolean) true if the item was checked, false otherwise
   */
  public void handleSetChecked( TableItem item, JsonObject properties ) {
    JsonValue checked = properties.get( PROP_CHECKED );
    if( checked != null ) {
      item.setChecked( checked.asBoolean() );
    }
  }

}
