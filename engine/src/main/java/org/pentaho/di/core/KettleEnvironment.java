/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core;

import org.pentaho.di.cluster.ClusterSchemaManagementInterface;
import org.pentaho.di.cluster.ClusterSchemaManager;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.cluster.SlaveServerManager;
import org.pentaho.di.core.auth.AuthenticationConsumerPluginType;
import org.pentaho.di.core.auth.AuthenticationProviderPluginType;
import org.pentaho.di.core.bowl.BowlManagerFactoryRegistry;
import org.pentaho.di.core.compress.CompressionPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleSupport;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.LogTablePluginType;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.JobEntryDialogFragmentType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepDialogFragmentType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchemaManagementInterface;
import org.pentaho.di.partition.PartitionSchemaManager;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.DatabaseConnectionManager;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.trans.step.RowDistributionPluginType;

import com.google.common.util.concurrent.SettableFuture;
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
    new AtomicReference<>( null );
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
      StepDialogFragmentType.getInstance(),
      PartitionerPluginType.getInstance(),
      JobEntryPluginType.getInstance(),
      JobEntryDialogFragmentType.getInstance(),
      LogTablePluginType.getInstance(),
      RepositoryPluginType.getInstance(),
      LifecyclePluginType.getInstance(),
      KettleLifecyclePluginType.getInstance(),
      ImportRulePluginType.getInstance(),
      CartePluginType.getInstance(),
      CompressionPluginType.getInstance(),
      AuthenticationProviderPluginType.getInstance(),
      AuthenticationConsumerPluginType.getInstance()
    ), simpleJndi );
  }

  public static void init( List<PluginTypeInterface> pluginClasses, boolean simpleJndi ) throws KettleException {

    SettableFuture<Boolean> ready;
    if ( initialized.compareAndSet( null, ready = SettableFuture.create() ) ) {

      // Swaps out System Properties for a thread safe version.  This is needed so Karaf can spawn multiple instances.
      // See https://jira.pentaho.com/browse/PDI-17496
      System.setProperties( ConcurrentMapProperties.convertProperties( System.getProperties() ) );

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

        // Update Variables for LoggingRegistry
        LoggingRegistry.getInstance().updateFromProperties();

        // Registering the Shared Objects Managers to Bowl
        registerSharedObjectManagersWithBowl();

        // Schedule the purge timer task
        LoggingRegistry.getInstance().schedulePurgeTimer();

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
        throw new KettleException( t );
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
    if ( isInitialized() ) {
      try {
        kettleLifecycleSupport.onEnvironmentShutdown();
      } catch ( Throwable t ) {
        System.err.println( BaseMessages.getString( PKG,
          "LifecycleSupport.ErrorInvokingKettleEnvironmentShutdownListeners" ) );
        t.printStackTrace();
      }
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

  // Note - this is only called from test cases
  public static void reset() {
    KettleClientEnvironment.reset();
    LoggingRegistry.getInstance().reset();
    initialized.set( null );
  }

  /**
   * Registers the SharedObject type specific factory with the Bowl factory registry.
   */
  private static void registerSharedObjectManagersWithBowl(){
    BowlManagerFactoryRegistry registry = BowlManagerFactoryRegistry.getInstance();
    registry.registerManagerFactory( DatabaseManagementInterface.class,
      new DatabaseConnectionManager.DatabaseConnectionManagerFactory() );
    registry.registerManagerFactory( SlaveServerManagementInterface.class,
      new SlaveServerManager.SlaveServerManagerFactory() );
    registry.registerManagerFactory( ClusterSchemaManagementInterface.class,
      new ClusterSchemaManager.ClusterSchemaManagerFactory() );
    registry.registerManagerFactory( PartitionSchemaManagementInterface.class,
      new PartitionSchemaManager.PartitionSchemaManagerFactory() );
  }
}
