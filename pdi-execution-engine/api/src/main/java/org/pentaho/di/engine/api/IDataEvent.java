package org.pentaho.di.engine.api;

import java.util.List;

/**
 * An {@link IPDIEvent} associated with an {@link IData} element. This event contains the data, the IPDIEventSource
 * which emitted the event and the direction of the flow.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public interface IDataEvent extends IPDIEvent {
  enum TYPE {IN, OUT, ERROR}

  enum STATE {ACTIVE, COMPLETE, EMPTY}

  TYPE getType();

  STATE getState();

  /**
   * Row of data or otherwise
   *
   * @return
   */
  List<IData> getData();

  /**
   * Component which emitted the event
   *
   * @return
   */
  IPDIEventSource getEventSource();
}
