/*! ******************************************************************************
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Note, this test must be run on separate JAVA instance, to be sure 
 * LoggingRegistry was not already initialized when using differed initialization
 * or do initialize immediate in static way.
 *
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
public class LoggingRegistrySingltonTest {

  /**
   * Test that LoggingRegistry is concurrent-sage initialized over multiple calls. Creating more than 1000 threads can
   * cause significant performance impact.
   * 
   * @throws InterruptedException
   * @throws ExecutionException
   * 
   */
  @Test( timeout = 30000 )
  public void testLoggingRegistryConcurrentInitialization() throws InterruptedException, ExecutionException {
    CountDownLatch start = new CountDownLatch( 1 );

    int count = 10;
    CompletionService<LoggingRegistry> drover = registerHounds( count, start );
    // fire!
    start.countDown();

    Set<LoggingRegistry> distinct = new HashSet<LoggingRegistry>();

    int i = 0;
    while ( i < count ) {
      Future<LoggingRegistry> complete = drover.poll( 15, TimeUnit.SECONDS );
      LoggingRegistry instance = complete.get();
      distinct.add( instance );
      i++;
    }
    Assert.assertEquals( "Only one singlton instance ;)", 1, distinct.size() );
  }

  CompletionService<LoggingRegistry> registerHounds( int count, CountDownLatch start ) {
    ExecutorService executor = Executors.newFixedThreadPool( count );
    CompletionService<LoggingRegistry> completionService = new ExecutorCompletionService<LoggingRegistry>( executor );
    for ( int i = 0; i < count; i++ ) {
      LogRegistryKicker hound = new LogRegistryKicker( start );
      completionService.submit( hound );
    }
    return completionService;
  }

  class LogRegistryKicker implements Callable<LoggingRegistry> {
    CountDownLatch start;

    LogRegistryKicker( CountDownLatch start ) {
      this.start = start;
    }

    @Override
    public LoggingRegistry call() throws Exception {
      try {
        start.await();
      } catch ( InterruptedException e ) {
        throw new RuntimeException();
      }
      return LoggingRegistry.getInstance();
    }
  }
}
