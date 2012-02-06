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
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;

public class KettleEnvironment {

	private static Class<?> PKG = Const.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static Boolean initialized;
	
	public static void init() throws KettleException {
	  init(true);
	}
	
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
      PluginRegistry.addPluginType(ImportRulePluginType.getInstance());
			PluginRegistry.init();
			
			// Also read the list of variables.
			//
			KettleVariablesList.init();
						
			initialized = true;
		}
	}
	
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


	public static boolean isInitialized() {
		if (initialized==null) return false; else return true;
	}
	
	public void loadPluginRegistry() throws KettlePluginException {
		
	}
}
