/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SchedulerRequestTest {
  // input file
  private static final String TEST_REPOSITORY_DIRECTORY = "/home/admin";
  private static final String TEST_JOB_NAME = "jobName and special characters & < >";
  private static final String JOB_EXTENSION = "kjb";

  // job parameters
  private static final String STRING_PARAM_TYPE = "string";
  private static final String LOG_LEVEL_PARAM_NAME = "logLevel";
  private static final String TEST_LOG_LEVEL_PARAM_VALUE = "Rowlevel";
  private static final String CLEAR_LOG_PARAM_NAME = "clearLog";
  private static final String TEST_CLEAR_LOG_PARAM_VALUE = "true";
  private static final String RUN_SAFE_MODE_PARAM_NAME = "runSafeMode";
  private static final String TEST_RUN_SAFE_MODE_PARAM_VALUE = "false";
  private static final String GATHERING_METRICS_PARAM_NAME = "gatheringMetrics";
  private static final String TEST_GATHERING_METRICS_PARAM_VALUE = "false";
  private static final String START_COPY_NAME_PARAM_NAME = "startCopyName";
  private static final String TEST_START_COPY_NAME_PARAM_VALUE = "stepName";
  private static final String EXPANDING_REMOTE_JOB_PARAM_NAME = "expandingRemoteJob";
  private static final String TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE = "false";

  // pdi parameters
  private static final String TEST_PDI_PARAM_NAME = "paramName";
  private static final String TEST_PDI_PARAM_VALUE = "paramValue";
  private static final String[] ARRAY_WITH_TEST_PDI_PARAM_NAME = new String[]{TEST_PDI_PARAM_NAME};

  private static final String REFERENCE_TEST_REQUEST = String.format(
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<jobScheduleRequest>"
      + "<inputFile>%s/%s.%s</inputFile>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<jobParameters>"
      + "<name>%s</name>" + "<type>%s</type>" + "<stringValue>%s</stringValue>"
      + "</jobParameters>"
      + "<pdiParameters>"
      + "<entry>"
      + "<key>%s</key>" + "<value>%s</value>"
      + "</entry>"
      + "</pdiParameters>"
      + "</jobScheduleRequest>", TEST_REPOSITORY_DIRECTORY,
    TEST_JOB_NAME.replace( "&", "&amp;" ).replace( "<", "&lt;" )
      .replace( ">", "&gt;" ),
    JOB_EXTENSION, LOG_LEVEL_PARAM_NAME, STRING_PARAM_TYPE, TEST_LOG_LEVEL_PARAM_VALUE,
        CLEAR_LOG_PARAM_NAME, STRING_PARAM_TYPE, TEST_CLEAR_LOG_PARAM_VALUE,
        RUN_SAFE_MODE_PARAM_NAME, STRING_PARAM_TYPE, TEST_RUN_SAFE_MODE_PARAM_VALUE,
        GATHERING_METRICS_PARAM_NAME, STRING_PARAM_TYPE, TEST_GATHERING_METRICS_PARAM_VALUE,
        START_COPY_NAME_PARAM_NAME, STRING_PARAM_TYPE, TEST_START_COPY_NAME_PARAM_VALUE,
        EXPANDING_REMOTE_JOB_PARAM_NAME, STRING_PARAM_TYPE, TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE,
        TEST_PDI_PARAM_NAME, TEST_PDI_PARAM_VALUE );

  private SchedulerRequest schedulerRequest;

  @Before
  public void before() {
    schedulerRequest = mock( SchedulerRequest.class );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testBuildSchedulerRequestEntity() throws UnknownParamException, UnsupportedEncodingException {
    AbstractMeta meta = mock( JobMeta.class );
    RepositoryDirectoryInterface repositoryDirectory = mock( RepositoryDirectoryInterface.class );

    doReturn( repositoryDirectory ).when( meta ).getRepositoryDirectory();
    doReturn( TEST_REPOSITORY_DIRECTORY ).when( repositoryDirectory ).getPath();
    doReturn( TEST_JOB_NAME ).when( meta ).getName();
    doReturn( JOB_EXTENSION ).when( meta ).getDefaultExtension();

    doReturn( LogLevel.getLogLevelForCode( TEST_LOG_LEVEL_PARAM_VALUE ) ).when( meta ).getLogLevel();
    doReturn( Boolean.valueOf( TEST_CLEAR_LOG_PARAM_VALUE ) ).when( meta ).isClearingLog();
    doReturn( Boolean.valueOf( TEST_RUN_SAFE_MODE_PARAM_VALUE ) ).when( meta ).isSafeModeEnabled();
    doReturn( Boolean.valueOf( TEST_GATHERING_METRICS_PARAM_VALUE ) ).when( meta ).isGatheringMetrics();
    doReturn( TEST_START_COPY_NAME_PARAM_VALUE ).when( (JobMeta) meta ).getStartCopyName();
    doReturn( Boolean.valueOf( TEST_EXPANDING_REMOTE_JOB_PARAM_VALUE ) ).when( (JobMeta) meta ).isExpandingRemoteJob();

    doReturn( ARRAY_WITH_TEST_PDI_PARAM_NAME ).when( meta ).listParameters();
    doReturn( TEST_PDI_PARAM_VALUE ).when( meta ).getParameterValue( TEST_PDI_PARAM_NAME );

    doCallRealMethod().when( schedulerRequest ).buildSchedulerRequestEntity( meta );

    assertTrue( compareContentOfStringEntities( schedulerRequest.buildSchedulerRequestEntity( meta ),
            new StringEntity( REFERENCE_TEST_REQUEST ) ) );
  }

  private boolean compareContentOfStringEntities( StringEntity entity1, StringEntity entity2 ) {
    if ( entity1.getContentLength() == entity2.getContentLength() ) {
      try ( InputStream stream1 = entity1.getContent();
            InputStream stream2 = entity2.getContent() ) {
        while ( stream1.available() > 0 ) {
          if ( stream1.read() != stream2.read() ) {
            return false;
          }
        }
        return true;
      } catch ( IOException e ) {
        return false;
      }
    } else {
      return false;
    }
  }
}
