package org.pentaho.di.engine.api.events;

import org.reactivestreams.Publisher;

/**
 * Created by nbaker on 6/13/16.
 */
public interface IPDIEventSource<T extends IDataEvent> extends Publisher<T> {
  String getId();
}
