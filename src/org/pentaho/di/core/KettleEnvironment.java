/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.lifecycle.KettleLifecycleSupport;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.CartePluginType;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.KettleLifecyclePluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;

/**
 * The KettleEnvironment class contains settings and properties for all of Kettle. Initialization of the 
 * environment is done by calling the init() method, which reads in properties file(s), registers plugins,
 * etc. Initialization should be performed once at application startup; for example, Spoon's main() method
 * calls KettleEnvironment.init() in order to prepare the environment for usage by Spoon.
 */
public class KettleEnvironment {

	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Indicates whether the Kettle environment has been initialized. */
	private static Boolean initialized;
	
	/**
	 * Initializes the Kettle environment. This method will attempt to configure Simple JNDI, 
	 * by simply calling init(true).
	 * 
	 * @see KettleEnvironment#init(boolean)
	 *
	 * @throws KettleException Any errors that occur during initialization will throw a KettleException.
	 */
	public static void init() throws KettleException {
	  init(true);
	}
	
	/**
	 * Initializes the Kettle environment. This method performs the following operations:
	 * 
	 *  - Creates a Kettle "home" directory if it does not already exist
	 *  - Reads in the kettle.properties file
	 *  - Initializes the logging back-end
	 *  - Sets the console log level to debug
	 *  - If specified by parameter, configures Simple JNDI
	 *  - Registers the native types and the plugins for the various plugin types
	 *  - Reads the list of variables
	 *  - Initializes the Lifecycle listeners   
	 *
	 * @param simpleJndi true to configure Simple JNDI, false otherwise
	 * @throws KettleException Any errors that occur during initialization will throw a KettleException.
	 */
	public static void init(boolean simpleJndi) throws KettleException {
		if (initialized==null) {
			
			// Create a home for Kettle if it doesn't exist yet.
			//
			createKettleHome();
			
			// Read the kettle.properties file before anything else
			//
			EnvUtil.environmentInit();
			
			// Initialize the logging back-end.
			//
			CentralLogStore.init();
			
			// Set the console log level to debug
			//
			LogWriter.setConsoleAppenderDebug();
			
			// Configure Simple JNDI when we run in stand-alone mode (spoon, pan, kitchen, carte, ... NOT on the platform
			//
			if (simpleJndi) {
			  JndiUtil.initJNDI();
			}
			
			// Register the native types and the plugins for the various plugin types...
			//
			PluginRegistry.addPluginType(StepPluginType.getInstance());
			PluginRegistry.addPluginType(PartitionerPluginType.getInstance());
			PluginRegistry.addPluginType(JobEntryPluginType.getInstance());
			PluginRegistry.addPluginType(RepositoryPluginType.getInstance());
			PluginRegistry.addPluginType(DatabasePluginType.getInstance());
			PluginRegistry.addPluginType(LifecyclePluginType.getInstance());
			PluginRegistry.addPluginType(KettleLifecyclePluginType.getInstance());
      PluginRegistry.addPluginType(ImportRulePluginType.getInstance());
      PluginRegistry.addPluginType(CartePluginType.getInstance());
			PluginRegistry.init();
			
			// Also read the list of variables.
			//
			KettleVariablesList.init();

			// Initialize the Lifecycle Listeners
			//
			initLifecycleListeners();
						
			initialized = true;
		}
	}

	/**
	 * Alert all Lifecycle plugins that the Kettle environment is being initialized.
	 * @throws KettleException when a lifecycle listener throws an exception
	 */
	private static void initLifecycleListeners() throws KettleException {
	  final KettleLifecycleSupport s = new KettleLifecycleSupport();
    s.onEnvironmentInit();

	   // Register a shutdown hook to invoke the listener's onExit() methods 
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          s.onEnvironmentShutdown();
        } catch (Throwable t) {
          System.err.println(BaseMessages.getString(PKG, "LifecycleSupport.ErrorInvokingKettleEnvironmentShutdownListeners"));
          t.printStackTrace();
        }
      };
    });

  }

  /**
   * Creates the kettle home area, which is a directory containing a default kettle.properties file
   */
  public static void createKettleHome() {

		// Try to create the directory...
		//
		String directory = Const.getKettleDirectory();
		File dir = new File(directory);
		try 
		{ 
			dir.mkdirs();
			
			// Also create a file called kettle.properties
			//
			createDefaultKettleProperties(directory);
		} 
		catch(Exception e) 
		{ 
			
		}
	}
	
	/**
	 * Creates the default kettle properties file, containing the standard header.
	 *
	 * @param directory the directory
	 */
	private static void createDefaultKettleProperties(String directory) {
		
		String kpFile = directory+Const.FILE_SEPARATOR+Const.KETTLE_PROPERTIES;
		File file = new File(kpFile);
		if (!file.exists()) 
		{
			FileOutputStream out = null;
			try 
			{
				out = new FileOutputStream(file);
				out.write(Const.getKettlePropertiesFileHeader().getBytes());
			} 
			catch (IOException e) 
			{
				System.err.println(BaseMessages.getString(PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile));
				System.err.println(e.getStackTrace());
			}
			finally 
			{
				if (out!=null) {
					try {
						out.close();
					} catch (IOException e) {
						System.err.println(BaseMessages.getString(PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile));
						System.err.println(e.getStackTrace());
					}
				}
			}
		}
	}


	/**
	 * Checks if the Kettle environment has been initialized.
	 *
	 * @return true if initialized, false otherwise
	 */
	public static boolean isInitialized() {
		if (initialized==null) return false; else return true;
	}
	
	/**
	 * Loads the plugin registry.
	 *
	 * @throws KettlePluginException if any errors are encountered while loading the plugin registry.
	 */
	public void loadPluginRegistry() throws KettlePluginException {
		
	}
}
