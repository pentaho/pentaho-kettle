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

package org.pentaho.di.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.pentaho.di.core.annotations.KettleLifecyclePlugin;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;

/**
 * Tests for the Kettle Environment
 * 
 */
public class KettleEnvironmentTest {

  private static final AtomicBoolean environmentInitCalled = new AtomicBoolean( false );
  private static final String pluginId = "MockLifecycleListener";

  @KettleLifecyclePlugin( id = pluginId )
  public static class MockLifecycleListener implements KettleLifecycleListener {
    @Override
    public void onEnvironmentInit() throws LifecycleException {
      environmentInitCalled.set( true );
    }

    @Override
    public void onEnvironmentShutdown() {
    }
  }

  @KettleLifecyclePlugin( id = pluginId )
  public static class FailingMockLifecycleListener extends MockLifecycleListener {
    @Override
    public void onEnvironmentInit() throws LifecycleException {
      throw new LifecycleException( false );
    }
  }

  @KettleLifecyclePlugin( id = pluginId )
  public static class SevereFailingMockLifecycleListener extends MockLifecycleListener {
    @Override
    public void onEnvironmentInit() throws LifecycleException {
      throw new LifecycleException( true );
    }
  }

  @KettleLifecyclePlugin( id = pluginId )
  public static class ThrowableFailingMockLifecycleListener extends MockLifecycleListener {
    @Override
    public void onEnvironmentInit() throws LifecycleException {
      // Simulate a LifecycleListener that wasn't updated to the latest API
      throw new AbstractMethodError();
    }
  }

  private void resetKettleEnvironmentInitializationFlag() throws SecurityException, NoSuchFieldException,
    IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException,
    InstantiationException {
    Field f = KettleEnvironment.class.getDeclaredField( "initialized" );
    f.setAccessible( true );
    f.set( null, new AtomicReference<SettableFuture<Boolean>>( null ) );
    Constructor<KettleVFS> constructor;
    constructor = KettleVFS.class.getDeclaredConstructor();
    constructor.setAccessible( true );
    KettleVFS KVFS = constructor.newInstance();
    f = KVFS.getClass().getDeclaredField( "kettleVFS" );
    f.setAccessible( true );
    Field modifiersField = Field.class.getDeclaredField( "modifiers" );
    modifiersField.setAccessible( true );
    modifiersField.setInt( f, f.getModifiers() & ~Modifier.FINAL | Modifier.VOLATILE );
    f.set( null, KVFS );
    f = KVFS.getClass().getDeclaredField( "defaultVariableSpace" );
    f.setAccessible( true );
    modifiersField.setInt( f, f.getModifiers() & ~Modifier.FINAL );
    Variables var = new Variables();
    var.initializeVariablesFrom( null );
    f.set( null, var );

  }

  /**
   * Validate that a LifecycleListener's environment init callback is called when the Kettle Environment is initialized.
   */
  @Test
  public void lifecycleListenerEnvironmentInitCallback() throws Exception {
    resetKettleEnvironmentInitializationFlag();
    assertFalse( "This test only works if the Kettle Environment is not yet initialized", KettleEnvironment
        .isInitialized() );
    System.setProperty( Const.KETTLE_PLUGIN_CLASSES, MockLifecycleListener.class.getName() );
    KettleEnvironment.init();

    PluginInterface pi = PluginRegistry.getInstance().findPluginWithId( KettleLifecyclePluginType.class, pluginId );
    MockLifecycleListener l =
        (MockLifecycleListener) PluginRegistry.getInstance().loadClass( pi, KettleLifecycleListener.class );
    assertNotNull( "Test plugin not registered properly", l );

    assertTrue( environmentInitCalled.get() );
  }

  /**
   * Validate that a LifecycleListener's environment init callback is called when the Kettle Environment is initialized.
   */
  @Test
  public void lifecycleListenerEnvironmentInitCallback_exception_thrown() throws Exception {
    resetKettleEnvironmentInitializationFlag();
    assertFalse( "This test only works if the Kettle Environment is not yet initialized", KettleEnvironment
        .isInitialized() );
    System.setProperty( Const.KETTLE_PLUGIN_CLASSES, FailingMockLifecycleListener.class.getName() );
    KettleEnvironment.init();

    PluginInterface pi = PluginRegistry.getInstance().findPluginWithId( KettleLifecyclePluginType.class, pluginId );
    MockLifecycleListener l =
        (MockLifecycleListener) PluginRegistry.getInstance().loadClass( pi, KettleLifecycleListener.class );
    assertNotNull( "Test plugin not registered properly", l );

    assertTrue( KettleEnvironment.isInitialized() );
  }

  /**
   * Validate that a LifecycleListener's environment init callback is called when the Kettle Environment is initialized.
   */
  @Test
  public void lifecycleListenerEnvironmentInitCallback_exception_thrown_severe() throws Exception {
    resetKettleEnvironmentInitializationFlag();
    assertFalse( "This test only works if the Kettle Environment is not yet initialized", KettleEnvironment
        .isInitialized() );
    System.setProperty( Const.KETTLE_PLUGIN_CLASSES, SevereFailingMockLifecycleListener.class.getName() );
    try {
      KettleEnvironment.init();
      fail( "Expected exception" );
    } catch ( KettleException ex ) {
      assertEquals( LifecycleException.class, ex.getCause().getClass() );
      ex.printStackTrace();
    }

    assertFalse( KettleEnvironment.isInitialized() );
  }

  @Test
  public void lifecycleListenerEnvironmentInitCallback_throwable_thrown() throws Exception {
    resetKettleEnvironmentInitializationFlag();
    assertFalse( "This test only works if the Kettle Environment is not yet initialized", KettleEnvironment
        .isInitialized() );
    System.setProperty( Const.KETTLE_PLUGIN_CLASSES, ThrowableFailingMockLifecycleListener.class.getName() );
    try {
      KettleEnvironment.init();
      fail( "Expected exception" );
    } catch ( KettleException ex ) {
      assertEquals( AbstractMethodError.class, ex.getCause().getClass() );
      ex.printStackTrace();
    }

    assertFalse( KettleEnvironment.isInitialized() );
  }

}
