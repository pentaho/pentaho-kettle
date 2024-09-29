/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.logging;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MetricsRegistryTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  private MetricsRegistry metricsRegistry;
  private List<String> logIds;
  private int threadCount = 100;
  private int logChannelIdCount = 20;
  private CountDownLatch countDownLatch = null;

  @Before
  public void setUp() {
    metricsRegistry = MetricsRegistry.getInstance();
    metricsRegistry.reset();
    logIds = new ArrayList<>( logChannelIdCount );
    for ( int i = 1; i <= logChannelIdCount; i++ ) {
      logIds.add( "logChannelId_" + i );
    }
    countDownLatch = new CountDownLatch( 1 );
  }


  @Test
  public void testConcurrencySnap() throws Exception {
    ExecutorService service = Executors.newFixedThreadPool( threadCount );
    for ( int i = 0; i < threadCount; i++ ) {
      service.submit( new ConcurrentPutIfAbsent( logIds.get( i % 20 ) ) );
    }
    countDownLatch.countDown();
    service.awaitTermination( 2000, TimeUnit.MILLISECONDS );
    int expectedQueueCount = logChannelIdCount > threadCount ? threadCount : logChannelIdCount;
    assertEquals( expectedQueueCount, metricsRegistry.getSnapshotLists().size() );
  }

  private class ConcurrentPutIfAbsent implements Callable<Queue> {
    private String id;
    ConcurrentPutIfAbsent( String id ) {
      this.id = id;
    }
    @Override
    public Queue call() throws Exception {
      countDownLatch.await();
      return metricsRegistry.getSnapshotList( id );
    }

  }

}
