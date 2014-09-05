/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.monitor.base.IKettleMonitoringEvent;
import org.pentaho.di.monitor.carte.CarteEvent;
import org.pentaho.di.monitor.carte.CarteShutdownMonitor;
import org.pentaho.di.monitor.carte.CarteStartupMonitor;
import org.pentaho.di.monitor.database.DatabaseConnectedMonitor;
import org.pentaho.di.monitor.database.DatabaseDisconnectedMonitor;
import org.pentaho.di.monitor.database.DatabaseEvent;
import org.pentaho.di.monitor.job.JobEvent;
import org.pentaho.di.monitor.job.JobFinishMonitor;
import org.pentaho.di.monitor.job.JobMetaLoadedMonitor;
import org.pentaho.di.monitor.job.JobStartMonitor;
import org.pentaho.di.monitor.step.StepAfterInitializeMonitor;
import org.pentaho.di.monitor.step.StepBeforeInitializeMonitor;
import org.pentaho.di.monitor.step.StepBeforeStartMonitor;
import org.pentaho.di.monitor.step.StepEvent;
import org.pentaho.di.monitor.step.StepFinishedMonitor;
import org.pentaho.di.monitor.trans.TransformationEvent;
import org.pentaho.di.monitor.trans.TransformationFinishMonitor;
import org.pentaho.di.monitor.trans.TransformationPrepareExecutionMonitor;
import org.pentaho.di.monitor.trans.TransformationStartMonitor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInitThread;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.www.WebServer;

import static org.mockito.Mockito.*;

public class MonitorToKettleEventTest {

  LogChannelInterface mockLogChannel = mock( LogChannelInterface.class );

  // carte monitors
  CarteStartupMonitor carteStartupMonitor;
  CarteShutdownMonitor carteShutdownMonitor;

  // database monitors
  DatabaseConnectedMonitor databaseConnectedMonitor;
  DatabaseDisconnectedMonitor databaseDisconnectedMonitor;

  // job monitors
  JobStartMonitor jobStartMonitor;
  JobFinishMonitor jobFinishMonitor;
  JobMetaLoadedMonitor jobMetaLoadedMonitor;

  // transformation monitors
  TransformationPrepareExecutionMonitor transformationPrepareExecutionMonitor;
  TransformationStartMonitor transformationStartMonitor;
  TransformationFinishMonitor transformationFinishMonitor;

  // step monitors
  StepBeforeInitializeMonitor stepBeforeInitializeMonitor = new StepBeforeInitializeMonitor();
  StepAfterInitializeMonitor stepAfterInitializeMonitor = new StepAfterInitializeMonitor();
  StepBeforeStartMonitor stepBeforeStartMonitor = new StepBeforeStartMonitor();
  StepFinishedMonitor stepFinishedMonitor = new StepFinishedMonitor();

  @Before
  public void setUp() throws Exception {

    // carte monitors

    carteStartupMonitor = mock( CarteStartupMonitor.class );
    when( carteStartupMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( carteStartupMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    carteShutdownMonitor = mock( CarteShutdownMonitor.class );
    when( carteShutdownMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( carteShutdownMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    // Database monitors

    databaseConnectedMonitor = mock( DatabaseConnectedMonitor.class );
    when( databaseConnectedMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( databaseConnectedMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    databaseDisconnectedMonitor = mock( DatabaseDisconnectedMonitor.class );
    when( databaseDisconnectedMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( databaseDisconnectedMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    // job monitors

    jobStartMonitor = mock( JobStartMonitor.class );
    when( jobStartMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( jobStartMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    jobFinishMonitor = mock( JobFinishMonitor.class );
    when( jobFinishMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( jobFinishMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    jobMetaLoadedMonitor = mock( JobMetaLoadedMonitor.class );
    when( jobMetaLoadedMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( jobMetaLoadedMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    // transformation monitors

    transformationPrepareExecutionMonitor = mock( TransformationPrepareExecutionMonitor.class );
    when( transformationPrepareExecutionMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( transformationPrepareExecutionMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    transformationStartMonitor = mock( TransformationStartMonitor.class );
    when( transformationStartMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( transformationStartMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    transformationFinishMonitor = mock( TransformationFinishMonitor.class );
    when( transformationFinishMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( transformationFinishMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    // step monitors

    stepBeforeInitializeMonitor = mock( StepBeforeInitializeMonitor.class );
    when( stepBeforeInitializeMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( stepBeforeInitializeMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    stepAfterInitializeMonitor = mock( StepAfterInitializeMonitor.class );
    when( stepAfterInitializeMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( stepAfterInitializeMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    stepBeforeStartMonitor = mock( StepBeforeStartMonitor.class );
    when( stepBeforeStartMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( stepBeforeStartMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();

    stepFinishedMonitor = mock( StepFinishedMonitor.class );
    when( stepFinishedMonitor.getLogChannelInterface() ).thenReturn( mockLogChannel );
    when( stepFinishedMonitor.toKettleEvent( anyObject() ) ).thenCallRealMethod();
  }

  @After
  public void tearDown() throws Exception {
    carteStartupMonitor = null;
    carteShutdownMonitor = null;

    databaseConnectedMonitor = null;
    databaseDisconnectedMonitor = null;

    jobStartMonitor = null;
    jobFinishMonitor = null;
    jobMetaLoadedMonitor = null;

    transformationPrepareExecutionMonitor = null;
    transformationStartMonitor = null;
    transformationFinishMonitor = null;

    stepBeforeInitializeMonitor = null;
    stepAfterInitializeMonitor = null;
    stepBeforeStartMonitor = null;
    stepFinishedMonitor = null;
  }

  @Test
  public void testCarteMonitorsToKettleEvents() throws Exception {

    final String DUMMY_HOST = "dummy.host";
    final int DUMMY_PORT = 80;

    WebServer mockWebServer = mock( WebServer.class );

    when( mockWebServer.getHostname() ).thenReturn( DUMMY_HOST );
    when( mockWebServer.getPort() ).thenReturn( DUMMY_PORT );

    // call monitor.toKettleEvent() with event object other than WebServer
    Assert.assertNull( carteStartupMonitor.toKettleEvent( new String( "not a WebServer Object" ) ) );
    Assert.assertNull( carteShutdownMonitor.toKettleEvent( new String( "not a WebServer Object" ) ) );

    // call monitor.toKettleEvent()
    IKettleMonitoringEvent event = carteStartupMonitor.toKettleEvent( mockWebServer );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof CarteEvent );
    Assert.assertTrue( EventType.Carte.STARTUP == ( (CarteEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_HOST.equals( ( (CarteEvent) event ).getHostname() ) );
    Assert.assertTrue( DUMMY_PORT == ( (CarteEvent) event ).getPort() );

    // call monitor.toKettleEvent()
    event = carteShutdownMonitor.toKettleEvent( mockWebServer );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof CarteEvent );
    Assert.assertTrue( EventType.Carte.SHUTDOWN == ( (CarteEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_HOST.equals( ( (CarteEvent) event ).getHostname() ) );
    Assert.assertTrue( DUMMY_PORT == ( (CarteEvent) event ).getPort() );
  }

  @Test
  public void testDatabaseMonitorsToKettleEvents() throws Exception {

    final String DUMMY_CONNECTION_URL = "dummy.connection.url";
    final String DUMMY_DATABASE_NAME = "dummy.database.name";
    final String DUMMY_DRIVER_CLASS = "dummy.driver.class";

    Database mockDatabase = mock( Database.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );

    when( mockDatabaseMeta.getURL() ).thenReturn( DUMMY_CONNECTION_URL );
    when( mockDatabaseMeta.getDatabaseName() ).thenReturn( DUMMY_DATABASE_NAME );
    when( mockDatabaseMeta.getDriverClass() ).thenReturn( DUMMY_DRIVER_CLASS );

    when( mockDatabase.getDatabaseMeta() ).thenReturn( mockDatabaseMeta );

    // call monitor.toKettleEvent() with event object other than Database
    Assert.assertNull( databaseConnectedMonitor.toKettleEvent( new String( "not a Database Object" ) ) );
    Assert.assertNull( databaseDisconnectedMonitor.toKettleEvent( new String( "not a Database Object" ) ) );

    // call monitor.toKettleEvent()
    IKettleMonitoringEvent event = databaseConnectedMonitor.toKettleEvent( mockDatabase );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof DatabaseEvent );
    Assert.assertTrue( EventType.Database.CONNECTED == ( (DatabaseEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_CONNECTION_URL.equals( ( (DatabaseEvent) event ).getConnectionUrl() ) );
    Assert.assertTrue( DUMMY_DATABASE_NAME == ( (DatabaseEvent) event ).getDatabaseName() );
    Assert.assertTrue( DUMMY_DRIVER_CLASS == ( (DatabaseEvent) event ).getDriver() );

    // call monitor.toKettleEvent()
    event = databaseDisconnectedMonitor.toKettleEvent( mockDatabase );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof DatabaseEvent );
    Assert.assertTrue( EventType.Database.DISCONNECTED == ( (DatabaseEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_CONNECTION_URL.equals( ( (DatabaseEvent) event ).getConnectionUrl() ) );
    Assert.assertTrue( DUMMY_DATABASE_NAME == ( (DatabaseEvent) event ).getDatabaseName() );
    Assert.assertTrue( DUMMY_DRIVER_CLASS == ( (DatabaseEvent) event ).getDriver() );
  }

  @Test
  public void testJobMonitorsToKettleEvents() throws Exception {

    final String DUMMY_EXECUTING_SERVER = "dummy.executing.server";
    final String DUMMY_EXECUTING_USER = "dummy.executing.user";
    final String DUMMY_JOB_NAME = "dummy.job.name";
    final String DUMMY_JOB_XML_CONTENT = "<dummy-job>content</dummy-job>";

    Job mockJob = mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );

    when( mockJob.getExecutingServer() ).thenReturn( DUMMY_EXECUTING_SERVER );
    when( mockJob.getExecutingUser() ).thenReturn( DUMMY_EXECUTING_USER );
    when( mockJobMeta.getName() ).thenReturn( DUMMY_JOB_NAME );
    when( mockJobMeta.getXML() ).thenReturn( DUMMY_JOB_XML_CONTENT );

    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );

    // call monitor.toKettleEvent() with event object other than Database
    Assert.assertNull( jobStartMonitor.toKettleEvent( new String( "not a Job Object" ) ) );
    Assert.assertNull( jobMetaLoadedMonitor.toKettleEvent( new String( "not a Job Meta Object" ) ) );
    Assert.assertNull( jobFinishMonitor.toKettleEvent( new String( "not a Job Object" ) ) );

    // call monitor.toKettleEvent()
    IKettleMonitoringEvent event = jobStartMonitor.toKettleEvent( mockJob );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof JobEvent );
    Assert.assertTrue( EventType.Job.STARTED == ( (JobEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_EXECUTING_SERVER.equals( ( (JobEvent) event ).getExecutingServer() ) );
    Assert.assertTrue( DUMMY_EXECUTING_USER.equals( ( (JobEvent) event ).getExecutingUser() ) );
    Assert.assertTrue( DUMMY_JOB_NAME.equals( ( (JobEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_JOB_XML_CONTENT.equals( ( (JobEvent) event ).getXml() ) );

    // call monitor.toKettleEvent()
    event = jobMetaLoadedMonitor.toKettleEvent( mockJobMeta );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof JobEvent );
    Assert.assertTrue( EventType.Job.META_LOADED == ( (JobEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_JOB_NAME.equals( ( (JobEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_JOB_XML_CONTENT.equals( ( (JobEvent) event ).getXml() ) );

    // call monitor.toKettleEvent()
    event = jobFinishMonitor.toKettleEvent( mockJob );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof JobEvent );
    Assert.assertTrue( EventType.Job.FINISHED == ( (JobEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_EXECUTING_SERVER.equals( ( (JobEvent) event ).getExecutingServer() ) );
    Assert.assertTrue( DUMMY_EXECUTING_USER.equals( ( (JobEvent) event ).getExecutingUser() ) );
    Assert.assertTrue( DUMMY_JOB_NAME.equals( ( (JobEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_JOB_XML_CONTENT.equals( ( (JobEvent) event ).getXml() ) );
  }

  @Test
  public void testTransformationMonitorsToKettleEvents() throws Exception {

    final String DUMMY_EXECUTING_SERVER = "dummy.executing.server";
    final String DUMMY_EXECUTING_USER = "dummy.executing.user";
    final String DUMMY_TRANS_NAME = "dummy.job.name";
    final String DUMMY_TRANS_XML_CONTENT = "<dummy-trans>content</dummy-trans>";

    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );

    when( mockTrans.getExecutingServer() ).thenReturn( DUMMY_EXECUTING_SERVER );
    when( mockTrans.getExecutingUser() ).thenReturn( DUMMY_EXECUTING_USER );
    when( mockTransMeta.getName() ).thenReturn( DUMMY_TRANS_NAME );
    when( mockTransMeta.getXML() ).thenReturn( DUMMY_TRANS_XML_CONTENT );

    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );

    // call monitor.toKettleEvent() with event object other than Database
    Assert
      .assertNull( transformationPrepareExecutionMonitor.toKettleEvent( new String( "not a Trans Object" ) ) );
    Assert.assertNull( transformationStartMonitor.toKettleEvent( new String( "not a Trans Meta Object" ) ) );
    Assert.assertNull( transformationFinishMonitor.toKettleEvent( new String( "not a Trans Object" ) ) );

    // call monitor.toKettleEvent()
    IKettleMonitoringEvent event = transformationPrepareExecutionMonitor.toKettleEvent( mockTrans );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof TransformationEvent );
    Assert.assertTrue(
      EventType.Transformation.BEGIN_PREPARE_EXECUTION == ( (TransformationEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_EXECUTING_SERVER.equals( ( (TransformationEvent) event ).getExecutingServer() ) );
    Assert.assertTrue( DUMMY_EXECUTING_USER.equals( ( (TransformationEvent) event ).getExecutingUser() ) );
    Assert.assertTrue( DUMMY_TRANS_NAME.equals( ( (TransformationEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_TRANS_XML_CONTENT.equals( ( (TransformationEvent) event ).getXml() ) );

    // call monitor.toKettleEvent()
    event = transformationStartMonitor.toKettleEvent( mockTrans );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof TransformationEvent );
    Assert.assertTrue( EventType.Transformation.STARTED == ( (TransformationEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_EXECUTING_SERVER.equals( ( (TransformationEvent) event ).getExecutingServer() ) );
    Assert.assertTrue( DUMMY_EXECUTING_USER.equals( ( (TransformationEvent) event ).getExecutingUser() ) );
    Assert.assertTrue( DUMMY_TRANS_NAME.equals( ( (TransformationEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_TRANS_XML_CONTENT.equals( ( (TransformationEvent) event ).getXml() ) );

    // call monitor.toKettleEvent()
    event = transformationFinishMonitor.toKettleEvent( mockTrans );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof TransformationEvent );
    Assert.assertTrue( EventType.Transformation.FINISHED == ( (TransformationEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_EXECUTING_SERVER.equals( ( (TransformationEvent) event ).getExecutingServer() ) );
    Assert.assertTrue( DUMMY_EXECUTING_USER.equals( ( (TransformationEvent) event ).getExecutingUser() ) );
    Assert.assertTrue( DUMMY_TRANS_NAME.equals( ( (TransformationEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_TRANS_XML_CONTENT.equals( ( (TransformationEvent) event ).getXml() ) );
  }

  @Test
  public void testStepMonitorsToKettleEvents() throws Exception {

    final String DUMMY_STEP_NAME = "dummy.step.name";
    final String DUMMY_STEP_XML_CONTENT = "<dummy-step>content</dummy-step>";
    final boolean DUMMY_STEP_IS_CLUSTERED = true;

    StepInitThread mockStepInitThread = mock( StepInitThread.class );
    StepMetaDataCombi mockStepMetaDataCombi = mock( StepMetaDataCombi.class );
    mockStepMetaDataCombi.stepname = DUMMY_STEP_NAME;

    StepMeta mockStepMeta = mock ( StepMeta.class );
    when( mockStepMeta.getName() ).thenReturn( DUMMY_STEP_NAME );
    when( mockStepMeta.isClustered() ).thenReturn( DUMMY_STEP_IS_CLUSTERED );
    when( mockStepMeta.getXML() ).thenReturn( DUMMY_STEP_XML_CONTENT );

    mockStepMetaDataCombi.stepMeta = mockStepMeta;

    when( mockStepInitThread.getCombi() ).thenReturn( mockStepMetaDataCombi );

    // call monitor.toKettleEvent() with event object other than StepInitThread
    Assert.assertNull( stepBeforeInitializeMonitor.toKettleEvent( new String( "not a StepInitThread Object" ) ) );
    Assert.assertNull( stepAfterInitializeMonitor.toKettleEvent( new String( "not a StepInitThread Object" ) ) );
    Assert.assertNull( stepBeforeStartMonitor.toKettleEvent( new String( "not a StepMetaDataCombi Object" ) ) );
    Assert.assertNull( stepFinishedMonitor.toKettleEvent( new String( "not a StepMetaDataCombi Object" ) ) );

    // call monitor.toKettleEvent()
    IKettleMonitoringEvent event = stepBeforeInitializeMonitor.toKettleEvent( mockStepInitThread );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof StepEvent );
    Assert.assertTrue( EventType.Step.BEFORE_INIT == ( (StepEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_STEP_NAME.equals( ( (StepEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_STEP_XML_CONTENT.equals( ( (StepEvent) event ).getXmlContent() ) );
    Assert.assertTrue( DUMMY_STEP_IS_CLUSTERED == ( (StepEvent) event ).isClustered() );

    // call monitor.toKettleEvent()
    event = stepAfterInitializeMonitor.toKettleEvent( mockStepInitThread );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof StepEvent );
    Assert.assertTrue( EventType.Step.AFTER_INIT == ( (StepEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_STEP_NAME.equals( ( (StepEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_STEP_XML_CONTENT.equals( ( (StepEvent) event ).getXmlContent() ) );
    Assert.assertTrue( DUMMY_STEP_IS_CLUSTERED == ( (StepEvent) event ).isClustered() );

    // call monitor.toKettleEvent()
    event = stepBeforeStartMonitor.toKettleEvent( mockStepMetaDataCombi );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof StepEvent );
    Assert.assertTrue( EventType.Step.BEFORE_START == ( (StepEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_STEP_NAME.equals( ( (StepEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_STEP_XML_CONTENT.equals( ( (StepEvent) event ).getXmlContent() ) );
    Assert.assertTrue( DUMMY_STEP_IS_CLUSTERED == ( (StepEvent) event ).isClustered() );

    // call monitor.toKettleEvent()
     event = stepFinishedMonitor.toKettleEvent( mockStepMetaDataCombi );

    Assert.assertNotNull( event );
    Assert.assertTrue( event instanceof StepEvent );
    Assert.assertTrue( EventType.Step.FINISHED == ( (StepEvent) event ).getEventType() );
    Assert.assertTrue( DUMMY_STEP_NAME.equals( ( (StepEvent) event ).getName() ) );
    Assert.assertTrue( DUMMY_STEP_XML_CONTENT.equals( ( (StepEvent) event ).getXmlContent() ) );
    Assert.assertTrue( DUMMY_STEP_IS_CLUSTERED == ( (StepEvent) event ).isClustered() );
  }
}
