/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.collections.ListUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This test consists of two similar cases. There are three type of actors: getters, searchers and updaters. They work
 * simultaneously within their own threads. Getters invoke {@linkplain JobTracker#getJobTracker(int)} with a random
 * index, searchers call {@linkplain JobTracker#findJobTracker(JobEntryCopy)}, updaters add new children
 * <tt>updatersCycles</tt> times. The difference between two cases is the second has a small limit of stored children,
 * so the parent JobTracker will be forced to remove some of its elements.
 *
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
@Ignore
public class JobTrackerConcurrencyTest {

  private static final int gettersAmount = 10;

  private static final int searchersAmount = 20;

  private static final int updatersAmount = 5;
  private static final int updatersCycles = 10;

  private static final int jobsLimit = 20;

  @SuppressWarnings( "ConstantConditions" )
  @BeforeClass
  public static void setUp() {
    // a guarding check for tests' parameters
    int jobsToBeAdded = updatersAmount * updatersCycles;
    assertTrue( "The limit of stored jobs must be less than the amount of children to be added",
      jobsLimit < jobsToBeAdded );
  }


  @Parameterized.Parameters
  public static List<Object[]> getData() {
    return Arrays.asList(
      new Object[] { new JobTracker( mockJobMeta( "parent" ) ) },
      new Object[] { new JobTracker( mockJobMeta( "parent" ), jobsLimit ) }
    );
  }

  private static JobMeta mockJobMeta( String name ) {
    JobMeta meta = mock( JobMeta.class );
    when( meta.getName() ).thenReturn( name );
    return meta;
  }


  private final JobTracker tracker;

  public JobTrackerConcurrencyTest( JobTracker tracker ) {
    this.tracker = tracker;
  }

  @Test
  public void readAndUpdateTrackerConcurrently() throws Exception {
    final AtomicBoolean condition = new AtomicBoolean( true );

    List<Getter> getters = new ArrayList<Getter>( gettersAmount );
    for ( int i = 0; i < gettersAmount; i++ ) {
      getters.add( new Getter( condition, tracker ) );
    }

    List<Searcher> searchers = new ArrayList<Searcher>( searchersAmount );
    for ( int i = 0; i < searchersAmount; i++ ) {
      int lookingFor = updatersAmount * updatersCycles / 2 + i;
      assertTrue( "We are looking for reachable index", lookingFor < updatersAmount * updatersCycles );
      searchers.add( new Searcher( condition, tracker, mockJobEntryCopy( "job-entry-" + lookingFor, lookingFor ) ) );
    }

    final AtomicInteger generator = new AtomicInteger( 0 );
    List<Updater> updaters = new ArrayList<Updater>( updatersAmount );
    for ( int i = 0; i < updatersAmount; i++ ) {
      updaters.add( new Updater( tracker, updatersCycles, generator, "job-entry-%d" ) );
    }

    //noinspection unchecked
    ConcurrencyTestRunner.runAndCheckNoExceptionRaised( updaters, ListUtils.union( getters, searchers ), condition );
    assertEquals( updatersAmount * updatersCycles, generator.get() );
  }

  static JobEntryCopy mockJobEntryCopy( String name, int number ) {
    JobEntryCopy copy = mock( JobEntryCopy.class );
    when( copy.getName() ).thenReturn( name );
    when( copy.getNr() ).thenReturn( number );
    return copy;
  }


  private static class Getter extends StopOnErrorCallable<Object> {
    private final JobTracker tracker;
    private final Random random;

    public Getter( AtomicBoolean condition, JobTracker tracker ) {
      super( condition );
      this.tracker = tracker;
      this.random = new Random();
    }

    @Override
    public Object doCall() throws Exception {
      while ( condition.get() ) {
        int amount = tracker.nrJobTrackers();
        if ( amount == 0 ) {
          continue;
        }
        int i = random.nextInt( amount );
        JobTracker t = tracker.getJobTracker( i );
        if ( t == null ) {
          throw new IllegalStateException(
            String.format( "Returned tracker must not be null. Index = %d, trackers' amount = %d", i, amount ) );
        }
      }
      return null;
    }
  }


  private static class Searcher extends StopOnErrorCallable<Object> {
    private final JobTracker tracker;
    private final JobEntryCopy copy;

    public Searcher( AtomicBoolean condition, JobTracker tracker, JobEntryCopy copy ) {
      super( condition );
      this.tracker = tracker;
      this.copy = copy;
    }

    @Override Object doCall() throws Exception {
      while ( condition.get() ) {
        // can be null, it is OK here
        tracker.findJobTracker( copy );
      }
      return null;
    }
  }


  private static class Updater implements Callable<Exception> {
    private final JobTracker tracker;
    private final int cycles;
    private final AtomicInteger idGenerator;
    private final String resultNameTemplate;

    public Updater( JobTracker tracker, int cycles, AtomicInteger idGenerator, String resultNameTemplate ) {
      this.tracker = tracker;
      this.cycles = cycles;
      this.idGenerator = idGenerator;
      this.resultNameTemplate = resultNameTemplate;
    }

    @Override
    public Exception call() throws Exception {
      Exception exception = null;
      try {
        for ( int i = 0; i < cycles; i++ ) {
          int id = idGenerator.getAndIncrement();
          JobEntryResult result = new JobEntryResult();
          result.setJobEntryName( String.format( resultNameTemplate, id ) );
          result.setJobEntryNr( id );
          JobTracker child = new JobTracker( mockJobMeta( "child-" + id ), result );
          tracker.addJobTracker( child );
        }
      } catch ( Exception e ) {
        exception = e;
      }
      return exception;
    }
  }
}
