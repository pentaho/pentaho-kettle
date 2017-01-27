package org.pentaho.di.engine.api.events;

/**
 * Created by nbaker on 1/7/17.
 */
public interface PDIEventSink<T extends DataEvent> {
  void subscribeTo( PDIEventSource<T> source );
}
