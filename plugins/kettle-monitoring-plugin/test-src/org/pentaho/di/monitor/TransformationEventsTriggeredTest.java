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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class TransformationEventsTriggeredTest extends BaseEventsTriggeredTest {

  private static final String SAMPLE_TRANS = "test-resources/sampleTrans.ktr";

  @Test
  public void testTransformationPrepareExecutionMonitor() throws Exception {

    dummyMonitor.reset();

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.TransformationPrepareExecution.id },
        KettleExtensionPoint.TransformationPrepareExecution.name() );

    // register dummyMonitor as an extension point plugin for TransformationPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testStepBeforeInitMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.StepBeforeInitialize.id },
        KettleExtensionPoint.StepBeforeInitialize.name() );

    // register dummyMonitor as an extension point plugin for TransformationPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testStepAfterInitMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.StepAfterInitialize.id },
        KettleExtensionPoint.StepAfterInitialize.name() );

    // register dummyMonitor as an extension point plugin for TransformationPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testTransformationMetaLoadedMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.TransformationMetaLoaded.id },
        KettleExtensionPoint.TransformationMetaLoaded.name() );

    // register dummyMonitor as an extension point plugin for TransformationPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  @Test
  public void testTransformationFinishMonitor() throws Exception {

    MockPlugin mockPlugin =
      new MockPlugin( dummyMonitor, new String[] { KettleExtensionPoint.TransformationFinish.id },
        KettleExtensionPoint.TransformationFinish.name() );

    // register dummyMonitor as an extension point plugin for TransformationPrepareExecution events
    PluginRegistry.getInstance().registerPlugin( ExtensionPointPluginType.class, mockPlugin );

    executeSampleTransformation();

    Assert.assertTrue( dummyMonitor.wasTriggered );

    dummyMonitor.reset();
    PluginRegistry.getInstance().removePlugin( ExtensionPointPluginType.class, mockPlugin );
  }

  private void executeSampleTransformation() throws KettleException {
    Trans trans = new Trans( new TransMeta( SAMPLE_TRANS ) );
    trans.execute( null );
    trans.waitUntilFinished();
  }
}
