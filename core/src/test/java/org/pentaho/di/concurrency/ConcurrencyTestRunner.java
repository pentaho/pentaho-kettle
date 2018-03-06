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

package org.pentaho.di.concurrency;

import com.google.common.base.Throwables;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is aimed to be a general runner for concurrency tests. You need to follow a convention while using it. By
 * it, there are two types of actors in multithreaded environment: monitored and background. The formers are active and
 * are considered to do some mutations of class undergoing testing. The latter group is all about accessors, that
 * normally should not change the state, but just ask for some information, e.g. invoke getters.
 * <p/>
 * There is a special condition flag, shared among all actors. Each of them must stop when it has found out the flag has
 * been cleared. Also, in most cases it makes sense to clear the flag after any exception has raised (see {@linkplain
 * StopOnErrorCallable}, because any actor can face with it in concurrency environment.
 * <p/>
 * The runner stores results of all actors, though in most cases this information is needless - what is important that
 * is the fact the execution has completed with no errors.
 *
 * @author Andrey Khayrutdinov
 */
class ConcurrencyTestRunner<M, B> {

  /**
   * Runs all tasks and simply checks no exceptions were thrown during the execution. The timeout is 5 minutes.
   *
   * @param monitoredTasks  active actors
   * @param backgroundTasks background actors
   * @param condition       stop condition
   * @throws Exception exception
   */
  @SuppressWarnings( "unchecked" )
  static void runAndCheckNoExceptionRaised( List<? extends Callable<?>> monitoredTasks,
                                            List<? extends Callable<?>> backgroundTasks,
                                            AtomicBoolean condition ) throws Exception {
    ConcurrencyTestRunner<?, ?> runner = new ConcurrencyTestRunner( monitoredTasks, backgroundTasks, condition );
    runner.runConcurrentTest();
    runner.checkNoExceptionRaised();
  }


  private final List<? extends Callable<? extends M>> monitoredTasks;
  private final List<? extends Callable<? extends B>> backgroundTasks;
  private final AtomicBoolean condition;

  private final long timeout;

  private final Map<Callable<? extends M>, ExecutionResult<M>> monitoredResults;
  private final Map<Callable<? extends B>, ExecutionResult<B>> backgroundResults;

  private Exception exception;

  ConcurrencyTestRunner( List<? extends Callable<? extends M>> monitoredTasks,
                         List<? extends Callable<? extends B>> backgroundTasks,
                         AtomicBoolean condition ) {
    this( monitoredTasks, backgroundTasks, condition, TimeUnit.MINUTES.toMillis( 5 ) );
  }

  ConcurrencyTestRunner( List<? extends Callable<? extends M>> monitoredTasks,
                         List<? extends Callable<? extends B>> backgroundTasks,
                         AtomicBoolean condition,
                         long timeout ) {
    this.monitoredTasks = monitoredTasks;
    this.backgroundTasks = backgroundTasks;
    this.condition = condition;
    this.timeout = timeout;

    this.monitoredResults = new HashMap<Callable<? extends M>, ExecutionResult<M>>( monitoredTasks.size() );
    this.backgroundResults = new HashMap<Callable<? extends B>, ExecutionResult<B>>( backgroundTasks.size() );
  }

  void runConcurrentTest() throws Exception {
    this.exception = null;

    final int tasksAmount = monitoredTasks.size() + backgroundTasks.size();
    final ExecutorService executors = Executors.newFixedThreadPool( tasksAmount );
    try {
      List<Future<? extends B>> background = new ArrayList<Future<? extends B>>( backgroundTasks.size() );
      for ( Callable<? extends B> task : backgroundTasks ) {
        background.add( executors.submit( task ) );
      }

      List<Future<? extends M>> monitored = new ArrayList<Future<? extends M>>( monitoredTasks.size() );
      for ( Callable<? extends M> task : monitoredTasks ) {
        monitored.add( executors.submit( task ) );
      }

      try {
        final long start = System.currentTimeMillis();
        while ( condition.get() && !isDone( monitored ) && checkTimeout( start ) ) {
          Thread.sleep( 200 );
        }
      } catch ( Exception e ) {
        exception = e;
      }
      condition.set( false );

      for ( int i = 0; i < monitored.size(); i++ ) {
        Future<? extends M> future = monitored.get( i );
        monitoredResults.put( monitoredTasks.get( i ), ExecutionResult.from( future ) );
      }

      for ( int i = 0; i < background.size(); i++ ) {
        Future<? extends B> future = background.get( i );
        //CHECKSTYLE IGNORE EmptyBlock FOR NEXT 3 LINES
        while ( !future.isDone() ) {
          // wait: condition flag is cleared, thus background tasks must complete by convention
        }
        backgroundResults.put( backgroundTasks.get( i ), ExecutionResult.from( future ) );
      }

    } finally {
      executors.shutdown();
    }
  }

  private boolean isDone( List<? extends Future<?>> futures ) {
    for ( Future<?> future : futures ) {
      if ( !future.isDone() ) {
        return false;
      }
    }
    return true;
  }

  private boolean checkTimeout( long start ) throws TimeoutException {
    if ( this.timeout > 0 ) {
      if ( System.currentTimeMillis() - start > timeout ) {
        throw new TimeoutException( "Execution time limit is exceeded: " + timeout + " ms." );
      }
    }
    return true;
  }

  Exception getException() {
    return exception;
  }

  List<Throwable> getTasksErrors() {
    List<Throwable> errors = new ArrayList<Throwable>();
    errors.addAll( pickupErrors( monitoredResults.values() ) );
    errors.addAll( pickupErrors( backgroundResults.values() ) );
    return errors;
  }

  private List<Throwable> pickupErrors( Collection<? extends ExecutionResult<?>> collection ) {
    List<Throwable> errors = new ArrayList<Throwable>( collection.size() );
    for ( ExecutionResult<?> result : collection ) {
      if ( result.isError() ) {
        errors.add( result.getThrowable() );
      }
    }
    return errors;
  }


  void checkNoExceptionRaised() {
    List<Throwable> errors = getTasksErrors();
    if ( !errors.isEmpty() ) {
      StringBuilder message = new StringBuilder( 1024 );
      message.append( "There are expected no exceptions during the test, but " )
        .append( errors.size() ).append( " raised:" );

      for ( Throwable throwable : errors ) {
        String stacktrace = Throwables.getStackTraceAsString( throwable );
        message.append( '\n' ).append( stacktrace );
      }
      Assert.fail( message.toString() );
    }
  }


  List<M> getMonitoredTasksResults() {
    return pickupResults( monitoredResults.values() );
  }

  private <T> List<T> pickupResults( Collection<? extends ExecutionResult<T>> collection ) {
    List<T> errors = new ArrayList<T>( collection.size() );
    for ( ExecutionResult<T> result : collection ) {
      if ( !result.isError() ) {
        errors.add( result.getResult() );
      }
    }
    return errors;
  }


  Map<Callable<? extends M>, ExecutionResult<M>> getMonitoredResults() {
    return Collections.unmodifiableMap( monitoredResults );
  }
}
