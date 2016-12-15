package org.pentaho.di.engine.api;

import org.reactivestreams.Processor;

/**
 * Created by nbaker on 6/13/16.
 */
public interface IPDIEventSource<T extends IPDIEvent> extends Processor<T, T> {
  String getId();

}
