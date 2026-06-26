/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith( MockitoJUnitRunner.class )
public class KafkaDialogHelperTest {

  private static Executor kafkaClassloaderExecutor() throws Exception {
    Field field = KafkaDialogHelper.class.getDeclaredField( "KAFKA_CLASSLOADER_EXECUTOR" );
    field.setAccessible( true );
    return (Executor) field.get( null );
  }

  @Test
  public void kafkaClassloaderExecutorIsInitialized() throws Exception {
    assertNotNull( kafkaClassloaderExecutor() );
  }

  @Test
  public void submittedTaskIsExecuted() throws Exception {
    CountDownLatch latch = new CountDownLatch( 1 );
    AtomicBoolean executed = new AtomicBoolean( false );

    kafkaClassloaderExecutor().execute( () -> {
      executed.set( true );
      latch.countDown();
    } );

    assertTrue( latch.await( 5, TimeUnit.SECONDS ) );
    assertTrue( executed.get() );
  }

  @Test
  public void taskRunsWithKafkaClientsAsContextClassloader() throws Exception {
    CountDownLatch latch = new CountDownLatch( 1 );
    AtomicReference<ClassLoader> capturedClassLoader = new AtomicReference<>();

    kafkaClassloaderExecutor().execute( () -> {
      capturedClassLoader.set( Thread.currentThread().getContextClassLoader() );
      latch.countDown();
    } );

    assertTrue( latch.await( 5, TimeUnit.SECONDS ) );
    assertSame( KafkaConsumer.class.getClassLoader(), capturedClassLoader.get() );
  }

  @Test
  public void taskRunsWithKafkaClientsClassloaderRegardlessOfCallingThreadClassloader() throws Exception {
    CountDownLatch latch = new CountDownLatch( 1 );
    AtomicReference<ClassLoader> capturedClassLoader = new AtomicReference<>();

    Thread callingThread = Thread.currentThread();
    ClassLoader originalCallerClassLoader = callingThread.getContextClassLoader();
    ClassLoader customClassLoader = new ClassLoader( originalCallerClassLoader ) { };
    callingThread.setContextClassLoader( customClassLoader );

    try {
      kafkaClassloaderExecutor().execute( () -> {
        capturedClassLoader.set( Thread.currentThread().getContextClassLoader() );
        latch.countDown();
      } );

      assertTrue( latch.await( 5, TimeUnit.SECONDS ) );
      assertSame( KafkaConsumer.class.getClassLoader(), capturedClassLoader.get() );
      assertNotSame( customClassLoader, capturedClassLoader.get() );
    } finally {
      callingThread.setContextClassLoader( originalCallerClassLoader );
    }
  }

  @Test
  public void subsequentTasksExecuteAfterPreviousTaskThrowsException() throws Exception {
    CountDownLatch firstTaskLatch = new CountDownLatch( 1 );
    CountDownLatch secondTaskLatch = new CountDownLatch( 1 );
    AtomicBoolean secondTaskExecuted = new AtomicBoolean( false );

    Executor executor = kafkaClassloaderExecutor();

    executor.execute( () -> {
      firstTaskLatch.countDown();
      throw new RuntimeException( "intentional failure to verify finally-block classloader restore" );
    } );

    assertTrue( firstTaskLatch.await( 5, TimeUnit.SECONDS ) );

    executor.execute( () -> {
      secondTaskExecuted.set( true );
      secondTaskLatch.countDown();
    } );

    assertTrue( secondTaskLatch.await( 5, TimeUnit.SECONDS ) );
    assertTrue( secondTaskExecuted.get() );
  }

  @Test
  public void taskAfterThrowingTaskStillRunsWithKafkaClientsClassloader() throws Exception {
    CountDownLatch firstTaskLatch = new CountDownLatch( 1 );
    CountDownLatch secondTaskLatch = new CountDownLatch( 1 );
    AtomicReference<ClassLoader> classloaderAfterFailure = new AtomicReference<>();

    Executor executor = kafkaClassloaderExecutor();

    executor.execute( () -> {
      firstTaskLatch.countDown();
      throw new RuntimeException( "intentional failure" );
    } );

    assertTrue( firstTaskLatch.await( 5, TimeUnit.SECONDS ) );

    executor.execute( () -> {
      classloaderAfterFailure.set( Thread.currentThread().getContextClassLoader() );
      secondTaskLatch.countDown();
    } );

    assertTrue( secondTaskLatch.await( 5, TimeUnit.SECONDS ) );
    assertSame( KafkaConsumer.class.getClassLoader(), classloaderAfterFailure.get() );
  }

  @Test
  public void multipleConcurrentTasksAllRunWithKafkaClientsClassloader() throws Exception {
    int taskCount = 5;
    CountDownLatch latch = new CountDownLatch( taskCount );
    AtomicBoolean allHadCorrectClassloader = new AtomicBoolean( true );
    ClassLoader expectedClassLoader = KafkaConsumer.class.getClassLoader();

    Executor executor = kafkaClassloaderExecutor();

    for ( int i = 0; i < taskCount; i++ ) {
      executor.execute( () -> {
        if ( Thread.currentThread().getContextClassLoader() != expectedClassLoader ) {
          allHadCorrectClassloader.set( false );
        }
        latch.countDown();
      } );
    }

    assertTrue( latch.await( 5, TimeUnit.SECONDS ) );
    assertTrue( allHadCorrectClassloader.get() );
  }
}

