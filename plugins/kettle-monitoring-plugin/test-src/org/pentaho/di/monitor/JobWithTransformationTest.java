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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.monitor.base.EventType;
import org.pentaho.di.monitor.job.JobEvent;
import org.pentaho.di.monitor.trans.TransformationEvent;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class JobWithTransformationTest extends BaseEventsTriggeredTest {

  private static final String SAMPLE_TRANS_NAME = "Delay row - Basic example";
  private static final String SAMPLE_TRANS = "test-resources/Delay row - Basic example.ktr";
  private static final String SAMPLE_JOB_NAME = "Delay Row Job";
  private static final String SAMPLE_JOB_WITH_TRANS = "test-resources/Delay Row Job.kjb";

  @Test
  public void testJobStartMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.JobStart.id },
        KettleExtensionPoint.JobStart.name() );

    // register dummyMonitor as an extension point plugin for JobPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJobContainingTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );
    Assert.assertTrue( dummyMonitor.eventObject != null && dummyMonitor.eventObject instanceof Job );
    Assert.assertTrue( ( (Job) dummyMonitor.eventObject ).getJobMeta().getName().equals( SAMPLE_JOB_NAME ) );

    JobEvent e = new JobEvent( EventType.Job.STARTED ).build( ( (Job) dummyMonitor.eventObject ) );

    Assert.assertNotNull( e );
    Assert.assertTrue( SAMPLE_JOB_NAME.equals( e.getName() ) );
    Assert.assertTrue( SAMPLE_JOB_WITH_TRANS.equals( e.getFilename() ) );
    Assert.assertTrue( EventType.Job.STARTED.getSnmpId() == e.getStatus() );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testJobFinishMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.JobFinish.id },
        KettleExtensionPoint.JobFinish.name() );

    // register dummyMonitor as an extension point plugin for JobFinish events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJobContainingTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );
    Assert.assertTrue( dummyMonitor.eventObject != null && dummyMonitor.eventObject instanceof Job );
    Assert.assertTrue( ( (Job) dummyMonitor.eventObject ).getJobMeta().getName().contains( SAMPLE_JOB_NAME ) );

    JobEvent e = new JobEvent( EventType.Job.FINISHED ).build( ( (Job) dummyMonitor.eventObject ) );

    Assert.assertNotNull( e );
    Assert.assertTrue( SAMPLE_JOB_NAME.equals( e.getName() ) );
    Assert.assertTrue( SAMPLE_JOB_WITH_TRANS.equals( e.getFilename() ) );
    Assert.assertTrue( EventType.Job.FINISHED.getSnmpId() == e.getStatus() );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testTransformationMetaLoadedMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.TransformationMetaLoaded.id },
        KettleExtensionPoint.TransformationMetaLoaded.name() );

    // register dummyMonitor as an extension point plugin for TransformationStarted events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJobContainingTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );
    Assert.assertTrue( dummyMonitor.eventObject != null && dummyMonitor.eventObject instanceof TransMeta );
    Assert.assertTrue( ( (TransMeta) dummyMonitor.eventObject ).getName().equals( SAMPLE_TRANS_NAME ) );

    TransformationEvent e =
      new TransformationEvent( EventType.Transformation.META_LOADED ).build( ( (TransMeta) dummyMonitor.eventObject ) );

    Assert.assertNotNull( e );
    Assert.assertTrue( SAMPLE_TRANS_NAME.equals( e.getName() ) );
    Assert.assertTrue( SAMPLE_TRANS.equals( e.getFilename() ) );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testTransformationFinishMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.TransformationFinish.id },
        KettleExtensionPoint.TransformationFinish.name() );

    // register dummyMonitor as an extension point plugin for TransformationFinish events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJobContainingTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );
    Assert.assertTrue( dummyMonitor.eventObject != null && dummyMonitor.eventObject instanceof Trans );
    Assert.assertTrue( ( (Trans) dummyMonitor.eventObject ).getName().equals( SAMPLE_TRANS_NAME ) );

    TransformationEvent e =
      new TransformationEvent( EventType.Transformation.FINISHED ).build( ( (Trans) dummyMonitor.eventObject ) );

    Assert.assertNotNull( e );
    Assert.assertTrue( SAMPLE_TRANS_NAME.equals( e.getName() ) );
    Assert.assertTrue( SAMPLE_JOB_NAME.equals( e.getParentJobName() ) );
    Assert.assertTrue( SAMPLE_TRANS.equals( e.getFilename() ) );
    Assert.assertTrue( EventType.Transformation.FINISHED.getSnmpId() == e.getStatus() );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  private void executeSampleJobContainingTransformation() throws KettleException {
    Job job = new Job( null, new JobMeta( SAMPLE_JOB_WITH_TRANS , null ) );
    job.start();
    job.waitUntilFinished();
  }
}
