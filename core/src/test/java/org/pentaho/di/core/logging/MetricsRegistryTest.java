/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
