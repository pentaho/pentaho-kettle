/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pentaho.di.core.database.util.DatabaseLogExceptionFactory;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.UnknownParamException;

@RunWith( value = Parameterized.class )
public class JobTrackerExecutionTest extends JobTrackerExecution {

  private Result res;

  public JobTrackerExecutionTest( Result res ) {
    this.res = res;
  }

  /**
   * Simulates log table exception at job start 5 cases: not set, set at kettle-variables (2) or kettle.properties (2)
   * 
   * @return
   */
  private static Result[] testJobStartLogException() {
    // this is default behavior out of the box
    Result res = new Result();
    res.fileName = "log_job_1.kjb";
    res.assertMessage =
        "[1] Log exception at start and end job execution: Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, true, true };

    // this is when kettle.properties key-value is set false
    Result resVarNotDef = new Result();
    resVarNotDef.fileName = "log_job_1.kjb";
    resVarNotDef.assertMessage =
        "[1-1] Log exception at start and end job execution:  Job trackers shows positive result for all records.";
    resVarNotDef.jobTrackerStatus = new Boolean[] { null, null, true, null, true, true };
    resVarNotDef.setAsVariable = false;

    // this is when kettle.properties key-value is set to true
    Result resVarDefTrue = new Result();
    resVarDefTrue.fileName = "log_job_1.kjb";
    resVarDefTrue.assertMessage =
        "[1-2] Log exception at start and end job execution: Job trackers shows negative result, job is failed.";
    resVarDefTrue.jobTrackerStatus = new Boolean[] { null, false };
    resVarDefTrue.setAsVariable = true;

    return new Result[] { res, resVarNotDef, resVarDefTrue };
  }

  /**
   * Simulates log table issue at job start and job end
   * 
   * @return
   */
  private static Result[] testJobEndLogException() {
    Result res = new Result();
    res.fileName = "log_job_2.kjb";
    res.assertMessage = "[2] Log exception at end only: Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, true, true };

    Result resVarFalse = new Result();
    resVarFalse.fileName = "log_job_2.kjb";
    resVarFalse.assertMessage = "[2-1] Log exception at end only: Job trackers shows positive result for all records.";
    resVarFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, true, true };
    resVarFalse.setAsVariable = false;

    Result resParFalse = new Result();
    resParFalse.fileName = "log_job_2.kjb";
    resParFalse.assertMessage = "[2-2] Log exception at end only: Job trackers shows negative result, job failed";
    resParFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, true, true, false };
    resParFalse.setAsVariable = true;

    return new Result[] { res, resVarFalse, resParFalse };
  }

  /**
   * Simulates log table issue when job has child job, and child job throws exception
   * 
   * @return
   */
  private static Result[] testJobLogCallerAtStartLogException() {
    Result res = new Result();
    res.fileName = "log_job_1_caller.kjb";
    res.assertMessage =
        "[3] Log exception at start (see [1] log_job_1.kjb results) in job call from parent job: "
        + "Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, null, true, null, true, true };

    Result resVarFalse = new Result();
    resVarFalse.fileName = "log_job_1_caller.kjb";
    resVarFalse.assertMessage =
        "[3-1] Log exception at start (see [1] log_job_1.kjb results) in job call from parent job: "
        + "Job trackers shows positive result for all records.";
    resVarFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, null, true, null, true, true };
    resVarFalse.setAsVariable = false;

    Result resParFalse = new Result();
    resParFalse.fileName = "log_job_1_caller.kjb";
    resParFalse.assertMessage =
        "[3-2] Log exception at start (see [1] log_job_1.kjb results) in job call from parent job: "
        + "Job trackers shows negative result, job failed.";
    resParFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, null, false, null, false, false };
    resParFalse.setAsVariable = true;

    return new Result[] { res, resVarFalse, resParFalse };
  }

  /**
   * Simulates log table issue when job has child job and child job throws exception at the end of his execution
   * 
   * @return
   */
  private static Result[] testJobLogCallerAtEndLogException() {
    Result res = new Result();
    res.fileName = "log_job_2_caller.kjb";
    res.assertMessage =
        "[4] Log exception at end (see [1] log_job_2.kjb results) in job call from parent job: "
        + "Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, null, true, null, true, true };

    Result resFalse = new Result();
    resFalse.fileName = "log_job_2_caller.kjb";
    resFalse.assertMessage =
        "[4-1] Log exception at end (see [1] log_job_2.kjb results) in job call from parent job: "
        + "Job trackers shows positive result for all records.";
    resFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, null, true, null, true, true };
    resFalse.setAsVariable = false;

    Result resTrue = new Result();
    resTrue.fileName = "log_job_2_caller.kjb";
    resTrue.assertMessage =
        "[4-2] Log exception at end (see [1] log_job_2.kjb results) in job call from parent job: "
        + "Job trackers shows negative result, job failed.";
    resTrue.jobTrackerStatus = new Boolean[] { null, null, true, null, null, false, null, false, false };
    resTrue.setAsVariable = true;

    return new Result[] { /* res, resFalse, */resTrue, res, resFalse };
  }

  /**
   * Simulates log table issue at trans start and trans end. Trans is called form parent job.
   * 
   * @return
   */
  private static Result[] testJobLogTransCallerStartException() {
    Result res = new Result();
    res.fileName = "log_trans_1_caller.kjb";
    res.assertMessage =
        "[5] Log exception at transformation start and end when it was called by job: "
        + "Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, true, null, true, true };

    Result resFalse = new Result();
    resFalse.fileName = "log_trans_1_caller.kjb";
    resFalse.assertMessage =
        "[5-1] Log exception at transformation start and end when it was called by job: "
        + "Job trackers shows positive result for all records.";
    resFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, true, null, true, true };
    resFalse.setAsVariable = false;

    Result resTrue = new Result();
    resTrue.fileName = "log_trans_1_caller.kjb";
    resTrue.assertMessage =
        "[5-2] Log exception at transformation start and end when it was called by job: "
        + "Job trackers shows negative result, job failed.";
    resTrue.jobTrackerStatus = new Boolean[] { null, null, true, null, false, null, false, false };
    resTrue.setAsVariable = true;

    return new Result[] { res, resFalse, resTrue };
  }

  /**
   * Simulates log table issue at trans end when transaction is called by parent job.
   * 
   * @return
   */
  private static Result[] testJobLogTransCallerEndException() {
    Result res = new Result();
    res.fileName = "log_trans_2_caller.kjb";
    res.assertMessage =
        "[6] Log exception at transformation and end when it was called by job: "
        + "Job trackers shows positive result for all records.";
    res.jobTrackerStatus = new Boolean[] { null, null, true, null, true, null, true, true };

    Result resFalse = new Result();
    resFalse.fileName = "log_trans_2_caller.kjb";
    resFalse.assertMessage =
        "[6-1] Log exception at transformation and end when it was called by job: "
        + "Job trackers shows positive result for all records.";
    resFalse.jobTrackerStatus = new Boolean[] { null, null, true, null, true, null, true, true };
    resFalse.setAsVariable = false;

    Result resTrue = new Result();
    resTrue.fileName = "log_trans_2_caller.kjb";
    resTrue.assertMessage =
        "[6-2] Log exception at transformation and end when it was called by job: "
        + "Job trackers shows negative result, job failed.";
    resTrue.jobTrackerStatus = new Boolean[] { null, null, true, null, false, null, false, false };
    resTrue.setAsVariable = true;

    return new Result[] { res, resFalse, resTrue };
  }

  /**
   * Test data provider. For better readability test data generation is moved to isolated methods. Every method call
   * generates Object with unique test data and test results. See specific methods javadoc for details.
   * 
   * @return
   */
  @Parameters
  public static List<Result[]> data() {
    ArrayList<Result> results = new ArrayList<Result>();
    results.addAll( Arrays.asList( testJobLogCallerAtEndLogException() ) );
    results.addAll( Arrays.asList( testJobStartLogException() ) );
    results.addAll( Arrays.asList( testJobEndLogException() ) );
    results.addAll( Arrays.asList( testJobLogCallerAtStartLogException() ) );
    results.addAll( Arrays.asList( testJobLogTransCallerStartException() ) );
    results.addAll( Arrays.asList( testJobLogTransCallerEndException() ) );
    results.trimToSize();

    Result[][] data = new Result[results.size()][1];
    for ( int i = 0; i < results.size(); i++ ) {
      data[i][0] = results.get( i );
    }
    return Arrays.asList( data );
  }

  @Test
  public void testJobTracker() throws UnknownParamException, KettleXMLException, URISyntaxException, IOException {
    if ( res.setAsVariable != null ) {
      System.getProperties().setProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME,
          res.setAsVariable.toString() );
    }

    try {
      Job job = new Job( null, getJobMeta( res.fileName ) );
      job.setLogLevel( LogLevel.BASIC );
      job.start();
      job.waitUntilFinished();

      // this simulates - Spoon 'Job Metrics' tab attempt to refresh:
      JobTracker tracker = job.getJobTracker();
      List<JobTracker> trackers = tracker.getJobTrackers();
      Assert.assertEquals( "Job trackers count is correct: " + res.assertMessage, res.jobTrackerStatus.length, trackers
          .size() );

      for ( int i = 0; i < res.jobTrackerStatus.length; i++ ) {
        JobTracker record = trackers.get( i );
        Boolean actual;
        JobEntryResult jer = record.getJobEntryResult();
        // don't look into nested JobTrackers
        if ( jer == null ) {
          actual = null;
        } else {
          actual =
              record.getJobEntryResult().getResult() == null ? null : Boolean.valueOf( record.getJobEntryResult()
                  .getResult().getResult() );
        }
        Assert.assertEquals( res.assertMessage + ": " + i, res.jobTrackerStatus[i], actual );
      }
    } finally {
      System.getProperties().remove( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME );
    }
  }

  private static class Result {
    /**
     * filename of job to reproduce issue
     */
    String fileName;
    /**
     * assert message for fine grained report
     */
    String assertMessage;
    /**
     * array illustrates job tracker with boolean value of specific job tracker record result
     */
    Boolean[] jobTrackerStatus;

    /**
     * when value is overridden (for example when user wants to edit default value - it will be set in
     * kettle.properties.)
     */
    Boolean setAsVariable = null;
  }
}
