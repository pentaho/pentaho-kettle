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
import org.junit.Test;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.www.CarteObjectEntry;
import org.pentaho.di.www.JobMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobMapConcurrencyTest {
  public static final String JOB_NAME_STRING = "job";
  public static final String JOB_ID_STRING = "job";
  public static final int INITIAL_JOB_MAP_SIZE = 100;

  private static final int gettersAmount = 20;
  private static final int replaceAmount = 20;
  private static final int updatersAmount = 5;
  private static final int updatersCycles = 10;

  private static JobMap jobMap;

  @BeforeClass
  public static void init() {
    jobMap = new JobMap();
    for ( int i = 0; i < INITIAL_JOB_MAP_SIZE; i++ ) {
      jobMap.addJob( JOB_NAME_STRING + i, JOB_ID_STRING + i, mockJob( i ), mock( JobConfiguration.class ) );
    }
  }

  private static Job mockJob( int id ) {
    Job job = mock( Job.class );
    when( job.getContainerObjectId() ).thenReturn( JOB_NAME_STRING + id );
    return job;
  }

  @Test
  public void updateGetAndReplaceConcurrently() throws Exception {
    AtomicBoolean condition = new AtomicBoolean( true );
    AtomicInteger generator = new AtomicInteger( 10 );

    List<Updater> updaters = new ArrayList<>();
    for ( int i = 0; i < updatersAmount; i++ ) {
      Updater updater = new Updater( jobMap, generator, updatersCycles );
      updaters.add( updater );
    }

    List<Getter> getters = new ArrayList<>();
    for ( int i = 0; i < gettersAmount; i++ ) {
      getters.add( new Getter( jobMap, condition ) );
    }

    List<Replacer> replacers = new ArrayList<>();
    for ( int i = 0; i < replaceAmount; i++ ) {
      replacers.add( new Replacer( jobMap, condition ) );
    }

    //noinspection unchecked
    ConcurrencyTestRunner.runAndCheckNoExceptionRaised( updaters, ListUtils.union( replacers, getters ), condition );

  }

  private static class Getter extends StopOnErrorCallable<Object> {
    private final JobMap jobMap;
    private final Random random;

    public Getter( JobMap jobMap, AtomicBoolean condition ) {
      super( condition );
      this.jobMap = jobMap;
      this.random = new Random();
    }

    @Override
    public Object doCall() throws Exception {
      while ( condition.get() ) {

        int i = random.nextInt( INITIAL_JOB_MAP_SIZE );
        CarteObjectEntry entry = jobMap.getJobObjects().get( i );

        if ( entry == null ) {
          throw new IllegalStateException(
            String.format( "Returned CarteObjectEntry must not be null. EntryId = %d", i ) );
        }
        final String jobName = JOB_NAME_STRING + i;

        Job job = jobMap.getJob( entry.getName() );
        if ( job == null ) {
          throw new IllegalStateException( String.format( "Returned job must not be null. Job name = %s", jobName ) );
        }

        JobConfiguration jobConfiguration = jobMap.getConfiguration( entry.getName() );
        if ( jobConfiguration == null ) {
          throw new IllegalStateException(
            String.format( "Returned jobConfiguration must not be null. Job name = %s", jobName ) );
        }
      }

      return null;
    }
  }


  private static class Updater implements Callable<Exception> {
    private final JobMap jobMap;
    private final AtomicInteger generator;
    private final int cycles;

    public Updater( JobMap jobMap, AtomicInteger generator, int cycles ) {
      this.jobMap = jobMap;
      this.generator = generator;
      this.cycles = cycles;
    }

    @Override
    public Exception call() throws Exception {
      Exception exception = null;
      try {
        for ( int i = 0; i < cycles; i++ ) {
          int id = generator.get();
          jobMap.addJob( JOB_NAME_STRING + id, JOB_ID_STRING + id, mockJob( id ), mock( JobConfiguration.class ) );
        }
      } catch ( Exception e ) {
        exception = e;
      }
      return exception;
    }
  }

  private static class Replacer extends StopOnErrorCallable<Object> {
    private final JobMap jobMap;
    private final Random random;

    public Replacer( JobMap jobMap, AtomicBoolean condition ) {
      super( condition );
      this.jobMap = jobMap;
      this.random = new Random();
    }

    @Override
    public Object doCall() throws Exception {

      int i = random.nextInt( INITIAL_JOB_MAP_SIZE );

      final String jobName = JOB_NAME_STRING + i;
      final String jobId = JOB_ID_STRING + i;

      CarteObjectEntry entry = new CarteObjectEntry( jobName, jobId );

      jobMap.replaceJob( entry, mockJob( i + 1 ), mock( JobConfiguration.class ) );

      return null;
    }
  }
}
