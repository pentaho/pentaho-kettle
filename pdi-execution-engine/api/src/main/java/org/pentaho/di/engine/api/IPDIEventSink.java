package org.pentaho.di.engine.api;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;

import java.io.Serializable;

/**
 * Created by nbaker on 1/7/17.
 */
public interface IPDIEventSink<T extends IPDIEvent> extends Subscriber<T>, Serializable {
}
