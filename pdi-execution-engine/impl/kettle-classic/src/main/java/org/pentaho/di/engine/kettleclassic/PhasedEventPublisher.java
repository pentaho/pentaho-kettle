package org.pentaho.di.engine.kettleclassic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nbaker on 1/20/17.
 */
public class PhasedEventPublisher {

  ExecutorService executorService = Executors.newSingleThreadExecutor();
  AtomicInteger counter;
  private Runnable runnable;

  public PhasedEventPublisher( int count, Runnable runnable ) {
    counter = new AtomicInteger( count );
    this.runnable = runnable;
  }

  public void decriment() {
    int newCount = counter.decrementAndGet();
    if( newCount == 0 ){
      executorService.execute( runnable );
    }
  }
}
