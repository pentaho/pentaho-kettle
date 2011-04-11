/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
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
		LogChannelInterface log = new LogChannel(Const.KETTLE_PROPERTIES);
		
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
				log.logError(BaseMessages.getString(PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile), e);
			}
			finally 
			{
				if (out!=null) {
					try {
						out.close();
					} catch (IOException e) {
						log.logError(BaseMessages.getString(PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES, kpFile), e);
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
