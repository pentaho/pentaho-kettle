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

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.job.Job;

public class JobEventsTriggeredTest extends BaseEventsTriggeredTest {

  private static final String SAMPLE_JOB = "test-resources/sampleJob.ktr";

  @Test
  public void testJobBeginProcessingMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.JobBeginProcessing.id },
        KettleExtensionPoint.JobBeginProcessing.name() );

    // register dummyMonitor as an extension point plugin for JobPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJob();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testJobStartMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.JobStart.id },
        KettleExtensionPoint.JobStart.name() );

    // register dummyMonitor as an extension point plugin for JobPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJob();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testJobFinishMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.JobFinish.id },
        KettleExtensionPoint.JobFinish.name() );

    // register dummyMonitor as an extension point plugin for JobPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleJob();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  private void executeSampleJob() throws KettleException {
    Job job = new Job( SAMPLE_JOB, SAMPLE_JOB, null );
    job.start();
    job.waitUntilFinished();
  }
}
