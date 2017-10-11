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

package org.pentaho.di.core;

import com.google.common.util.concurrent.SettableFuture;
import org.pentaho.di.core.auth.AuthenticationConsumerPluginType;
import org.pentaho.di.core.auth.AuthenticationProviderPluginType;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleSupport;
import org.pentaho.di.core.logging.LogTablePluginType;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.EnginePluginType;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.RowDistributionPluginType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The KettleEnvironment class contains settings and properties for all of Kettle. Initialization of the environment is
 * done by calling the init() method, which reads in properties file(s), registers plugins, etc. Initialization should
 * be performed once at application startup; for example, Spoon's main() method calls KettleEnvironment.init() in order
 * to prepare the environment for usage by Spoon.
 */
public class KettleEnvironment {

  private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!

  /**
   * Indicates whether the Kettle environment has been initialized.
   */
  private static AtomicReference<SettableFuture<Boolean>> initialized =
    new AtomicReference<SettableFuture<Boolean>>( null );
  private static KettleLifecycleSupport kettleLifecycleSupport;

  /**
   * Initializes the Kettle environment. This method will attempt to configure Simple JNDI, by simply calling
   * init(true).
   *
   * @throws KettleException Any errors that occur during initialization will throw a KettleException.
   * @see KettleEnvironment#init(boolean)
   */
  public static void init() throws KettleException {
    init( true );
  }

  public static void init( Class<? extends PluginTypeInterface> pluginClasses ) {

  }

  /**
   * Initializes the Kettle environment. This method performs the following operations:
   * <p/>
   * - Creates a Kettle "home" directory if it does not already exist - Reads in the kettle.properties file -
   * Initializes the logging back-end - Sets the console log level to debug - If specified by parameter, configures
   * Simple JNDI - Registers the native types and the plugins for the various plugin types - Reads the list of variables
   * - Initializes the Lifecycle listeners
   *
   * @param simpleJndi true to configure Simple JNDI, false otherwise
   * @throws KettleException Any errors that occur during initialization will throw a KettleException.
   */
  public static void init( boolean simpleJndi ) throws KettleException {
    init( Arrays.asList(
      RowDistributionPluginType.getInstance(),
      StepPluginType.getInstance(),
      PartitionerPluginType.getInstance(),
      JobEntryPluginType.getInstance(),
      LogTablePluginType.getInstance(),
      RepositoryPluginType.getInstance(),
      LifecyclePluginType.getInstance(),
      KettleLifecyclePluginType.getInstance(),
      ImportRulePluginType.getInstance(),
      CartePluginType.getInstance(),
      CompressionPluginType.getInstance(),
      AuthenticationProviderPluginType.getInstance(),
      AuthenticationConsumerPluginType.getInstance(),
      EnginePluginType.getInstance()
    ), simpleJndi );
  }

  public static void init( List<PluginTypeInterface> pluginClasses, boolean simpleJndi ) throws KettleException {
    SettableFuture<Boolean> ready;
    if ( initialized.compareAndSet( null, ready = SettableFuture.create() ) ) {

      try {
        // This creates .kettle and kettle.properties...
        //
        if ( !KettleClientEnvironment.isInitialized() ) {
          KettleClientEnvironment.init();
        }

        // Configure Simple JNDI when we run in stand-alone mode (spoon, pan, kitchen, carte, ... NOT on the platform
        //
        if ( simpleJndi ) {
          JndiUtil.initJNDI();
        }

        // Register the native types and the plugins for the various plugin types...
        //
        pluginClasses.forEach( PluginRegistry::addPluginType );
        PluginRegistry.init();

        // Also read the list of variables.
        //
        KettleVariablesList.init();

        // Initialize the Lifecycle Listeners
        //
        initLifecycleListeners();
        ready.set( true );
      } catch ( Throwable t ) {
        ready.setException( t );
        // If it's a KettleException, throw it, otherwise wrap it in a KettleException
        throw ( ( t instanceof KettleException ) ? (KettleException) t : new KettleException( t ) );
      }

    } else {
      // A different thread is initializing
      ready = initialized.get();
      // Block until environment is initialized
      try {
        ready.get();
      } catch ( Throwable t ) {
        // If it's a KettleException, throw it, otherwise wrap it in a KettleException
        throw ( ( t instanceof KettleException ) ? (KettleException) t : new KettleException( t ) );
      }
    }
  }

  /**
   * Alert all Lifecycle plugins that the Kettle environment is being initialized.
   *
   * @throws KettleException when a lifecycle listener throws an exception
   */
  private static void initLifecycleListeners() throws KettleException {
    kettleLifecycleSupport = new KettleLifecycleSupport();
    kettleLifecycleSupport.onEnvironmentInit();
    final KettleLifecycleSupport s = kettleLifecycleSupport;

    // Register a shutdown hook to invoke the listener's onExit() methods
    Runtime.getRuntime().addShutdownHook( new Thread() {
      public void run() {
        shutdown( s );
      }
    } );

  }

  // Shutdown the Kettle environment programmatically
  public static void shutdown() {
    shutdown( kettleLifecycleSupport );
  }

  private static void shutdown( KettleLifecycleSupport kettleLifecycleSupport ) {
    try {
      kettleLifecycleSupport.onEnvironmentShutdown();
    } catch ( Throwable t ) {
      System.err.println( BaseMessages.getString( PKG,
        "LifecycleSupport.ErrorInvokingKettleEnvironmentShutdownListeners" ) );
      t.printStackTrace();
    }
  }

  /**
   * Checks if the Kettle environment has been initialized.
   *
   * @return true if initialized, false otherwise
   */
  public static boolean isInitialized() {
    Future<Boolean> future = initialized.get();
    try {
      return future != null && future.get();
    } catch ( Throwable e ) {
      return false;
    }
  }

  /**
   * Loads the plugin registry.
   *
   * @throws KettlePluginException if any errors are encountered while loading the plugin registry.
   */
  public void loadPluginRegistry() throws KettlePluginException {

  }

  /**
   * Sets the executor's user and Server information
   */
  public static void setExecutionInformation( ExecutorInterface executor, Repository repository ) {
    // Capture the executing user and server name...
    executor.setExecutingUser( System.getProperty( "user.name" ) );
    if ( repository != null ) {
      IUser userInfo = repository.getUserInfo();
      if ( userInfo != null ) {
        executor.setExecutingUser( userInfo.getLogin() );
      }
    }
  }
}
