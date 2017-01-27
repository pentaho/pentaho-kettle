package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.Row;

import java.util.List;

/**
 * An {@link PDIEvent} associated with an {@link Row} element. This event contains the data, the IPDIEventSource
 * which emitted the event and the direction of the flow.
 * <p>
 * Created by nbaker on 5/30/16.
 */
public interface DataEvent extends PDIEvent {
  enum TYPE { IN, OUT, ERROR }

  enum STATE { ACTIVE, COMPLETE, EMPTY }

  TYPE getType();

  STATE getState();

  /**
   * Rows of data or otherwise
   *
   * @return
   */
  List<Row> getRows();

  /**
   * Component which emitted the event
   *
   * @return
   */
  PDIEventSource<?> getEventSource();
}
