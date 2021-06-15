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
package org.eclipse.swt.internal.widgets.coolitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.ICoolBarAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;


public class CoolItemOperationHandler extends WidgetOperationHandler<CoolItem> {

  private static final String METHOD_MOVE = "move";
  private static final String PROP_LEFT = "left";

  public CoolItemOperationHandler( CoolItem item ) {
    super( item );
  }

  @Override
  public void handleCall( CoolItem item, String method, JsonObject properties ) {
    if( METHOD_MOVE.equals( method ) ) {
      handleCallMove( item, properties );
    }
  }

  /*
   * PROTOCOL CALL move
   *
   * @param left (int) the left position of the cool item
   */
  public void handleCallMove( final CoolItem item, JsonObject properties ) {
    final int newLeft = properties.get( PROP_LEFT ).asInt();
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        moveItem( item, newLeft );
      }
    } );
  }

  static void moveItem( CoolItem coolItem, int newX ) {
    CoolItem[] items = coolItem.getParent().getItems();
    boolean changed = false;
    int newOrder = -1;
    int maxX = 0;
    int minX = 0;
    for( int i = 0; newOrder == -1 && i < items.length; i++ ) {
      CoolItem item = items[ i ];
      Rectangle itemBounds = item.getBounds();
      if( item != coolItem && itemBounds.contains( newX, itemBounds.y ) ) {
        if( coolItem.getBounds().x > newX ) {
          newOrder = i + 1;
        } else {
          newOrder = i;
        }
        changed = changeOrder( coolItem, newOrder );
      }
      maxX = Math.max( maxX, itemBounds.x + itemBounds.width );
      minX = Math.min( minX, itemBounds.x );
    }
    if( newOrder == -1 && newX > maxX ) {
      // item was moved after the last item
      int last = coolItem.getParent().getItemCount() - 1;
      changed = changeOrder( coolItem, last );
    } else if( newOrder == -1 && newX < minX ) {
      // item was moved before the first item
      changed = changeOrder( coolItem, 0 );
    }
    // In case an item was moved but that didn't cause it to change its order,
    // we need to let it 'snap back' to its previous position
    if( !changed ) {
      // TODO [rh] HACK: a decent solution would mark the item as 'bounds
      //      changed' and that mark could be evaluated by writeBounds.
      //      A more flexible writeBounds implementation on WidgetLCAUtil is
      //      necessary therefore.
      getAdapter( coolItem ).preserve( Props.BOUNDS, null );
    }
  }

  private static boolean changeOrder( CoolItem coolItem, int newOrder ) {
    boolean result;
    CoolBar coolBar = coolItem.getParent();
    int itemIndex = coolBar.indexOf( coolItem );
    int[] itemOrder = coolBar.getItemOrder();
    int length = itemOrder.length;
    int[] targetOrder = new int[ length ];
    int index = 0;
    if ( itemIndex != newOrder ) {
      for( int i = 0; i < length; i++ ) {
        if( i == newOrder ) {
          targetOrder[ i ] = itemOrder[ itemIndex ];
        } else {
          if( index == itemIndex ) {
            index++;
          }
          targetOrder[ i ] = itemOrder[ index ];
          index++;
        }
      }
      coolBar.getAdapter( ICoolBarAdapter.class ).setItemOrder( targetOrder );
      result = true;
    } else {
      result = false;
    }
    return result;
  }

}
