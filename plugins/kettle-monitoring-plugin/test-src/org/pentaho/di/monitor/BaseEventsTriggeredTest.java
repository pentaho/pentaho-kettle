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
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.monitor.base.IKettleMonitoringEvent;

public class BaseEventsTriggeredTest {

  protected DummyMonitor dummyMonitor;

  @Before
  public void setUp() throws Exception {

    KettleEnvironment.init();

    PluginRegistry.init();
    PluginRegistry.getInstance().registerPluginType( ExtensionPointPluginType.class );

    dummyMonitor = new DummyMonitor();
  }

  @Test
  public void testBaseSetUp() throws Exception {
    Assert.assertTrue( KettleEnvironment.isInitialized() );
    Assert.assertTrue( PluginRegistry.getInstance().getPluginType( ExtensionPointPluginType.class ) != null );
  }

  @After
  public void tearDown() throws Exception {
    dummyMonitor = null;
  }

  public class MockPlugin extends Plugin implements ClassLoadingPluginInterface {

    ExtensionPointInterface monitor;

    public MockPlugin( ExtensionPointInterface monitor, String[] ids, String name ) {
      super( ids, ExtensionPointPluginType.class, null, null, name, null, null, false, false, null, null, null, null );
      this.monitor = monitor;
    }

    @Override public <T> T loadClass( Class<T> tClass ) {
      return (T) monitor; // will always return this (dummy) extension point
    }

    @Override public ClassLoader getClassLoader() {
      return getClass().getClassLoader();
    }
  }

  public class DummyMonitor extends MonitorAbstract implements ExtensionPointInterface {

    public boolean wasTriggered = false;
    public Object eventObject;

    @Override public IKettleMonitoringEvent toKettleEvent( Object o ) throws KettleException {
      return null;
    }

    @Override
    public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {
      wasTriggered = true;
      eventObject = o;
    }

    public void reset() {
      this.wasTriggered = false;
    }
  }
}
