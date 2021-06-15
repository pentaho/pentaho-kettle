/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.events;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.widgets.Event;


public class EventList {

  private static final String ATTR_EVENT_LIST = EventList.class.getName() + "#instance";

  public static EventList getInstance() {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    EventList result = ( EventList )serviceStore.getAttribute( ATTR_EVENT_LIST );
    if( result == null ) {
      result = new EventList();
      serviceStore.setAttribute( ATTR_EVENT_LIST, result );
    }
    return result;
  }

  private final List<Event> events;
  private final EventComparator eventComparator;

  EventList() {
    this( EventTypes.EVENT_ORDER );
  }

  EventList( int[] eventOrder ) {
    events = new LinkedList<Event>();
    eventComparator = new EventComparator( eventOrder );
  }

  public void add( Event event ) {
    events.add( event );
  }

  public void remove( Event event ) {
    ParamCheck.notNull( event, "event" );
    events.remove( event );
  }

  public Event[] getAll() {
    Event[] result = events.toArray( new Event[ events.size() ] );
    Arrays.sort( result, eventComparator );
    return result;
  }

  private static class EventComparator implements Comparator<Event> {

    private final int[] eventOrder;

    public EventComparator( int[] eventOrder ) {
      this.eventOrder = eventOrder;
    }

    public int compare( Event event1, Event event2 ) {
      int index1 = getIndex( event1 );
      int index2 = getIndex( event2 );
      int result = index1 - index2;
      if( result == 0 ) {
        result = event1.time - event2.time;
      }
      return result;
    }

    private int getIndex( Event event ) {
      int result = Integer.MAX_VALUE;
      for( int i = 0; result == Integer.MAX_VALUE && i < eventOrder.length; i++ ) {
        if( eventOrder[ i ] == event.type ) {
          result = i;
        }
      }
      return result;
    }

  }

}
