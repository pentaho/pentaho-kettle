/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.dnd.dragsourcekit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_TIME;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_X;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_PARAM_Y;
import static org.eclipse.swt.internal.dnd.DNDUtil.cancel;
import static org.eclipse.swt.internal.dnd.DNDUtil.cancelDataTypeChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.cancelDetailChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.cancelFeedbackChanged;
import static org.eclipse.swt.internal.dnd.DNDUtil.getDetailChangedValue;
import static org.eclipse.swt.internal.dnd.DNDUtil.hasDetailChanged;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.rap.rwt.internal.protocol.WidgetOperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.dnd.DNDEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;


public class DragSourceOperationHandler extends WidgetOperationHandler<DragSource> {

  private static final String EVENT_DRAG_START = "DragStart";
  private static final String EVENT_DRAG_END = "DragEnd";

  public DragSourceOperationHandler( DragSource dragSource ) {
    super( dragSource );
  }

  @Override
  public void handleNotify( DragSource dragSource, String eventName, JsonObject properties ) {
    if( EVENT_DRAG_START.equals( eventName ) ) {
      handleNotifyDragStart( dragSource, properties );
    } else if( EVENT_DRAG_END.equals( eventName ) ) {
      handleNotifyDragEnd( dragSource, properties );
    } else {
      super.handleNotify( dragSource, eventName, properties );
    }
  }

  /*
   * PROTOCOL NOTIFY DragStart
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   */
  public void handleNotifyDragStart( final DragSource dragSource, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        Control control = dragSource.getControl();
        Point mappedPoint = control.getDisplay().map( null, control, x, y );
        control.notifyListeners( SWT.DragDetect, createDragDetectEvent( mappedPoint, time ) );
        DNDEvent event = createDragSourceEvent( mappedPoint, time, DND.DROP_NONE );
        dragSource.notifyListeners( DND.DragStart, event );
        if( !event.doit ) {
          cancel();
        }
      }
    } );
  }

  /*
   * PROTOCOL NOTIFY DragEnd
   *
   * @param x (int) the x coordinate of the pointer
   * @param y (int) the y coordinate of the pointer
   * @param time (int) the time when the event occurred
   */
  public void handleNotifyDragEnd( final DragSource dragSource, final JsonObject properties ) {
    ProcessActionRunner.add( new Runnable() {
      @Override
      public void run() {
        int x = properties.get( EVENT_PARAM_X ).asInt();
        int y = properties.get( EVENT_PARAM_Y ).asInt();
        int time = properties.get( EVENT_PARAM_TIME ).asInt();
        Control control = dragSource.getControl();
        Point mappedPoint = control.getDisplay().map( null, control, x, y );
        int detail = hasDetailChanged() ? getDetailChangedValue() : DND.DROP_NONE;
        DNDEvent event = createDragSourceEvent( mappedPoint, time, detail );
        dragSource.notifyListeners( DND.DragEnd, event );
        cancelDetailChanged();
        cancelFeedbackChanged();
        cancelDataTypeChanged();
      }
    } );
  }

  private static Event createDragDetectEvent( Point point, int time ) {
    Event event = new Event();
    event.x = point.x;
    event.y = point.y;
    event.time = time;
    event.button = 1;
    return event;
  }

  private static DNDEvent createDragSourceEvent( Point point, int time, int detail ) {
    DNDEvent event = new DNDEvent();
    event.x = point.x;
    event.y = point.y;
    event.time = time;
    event.detail = detail;
    event.doit = true;
    return event;
  }

}
