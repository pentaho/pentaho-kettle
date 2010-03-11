package org.pentaho.di.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.JobEntryPluginType;
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
			
			createKettleHome();
			
			EnvUtil.environmentInit();
			
			CentralLogStore.init();
			
			if (simpleJndi) {
			  JndiUtil.initJNDI();
			}
			
			PluginRegistry.addPluginType(StepPluginType.getInstance());
			PluginRegistry.addPluginType(PartitionerPluginType.getInstance());
			PluginRegistry.addPluginType(JobEntryPluginType.getInstance());
			PluginRegistry.addPluginType(RepositoryPluginType.getInstance());
			PluginRegistry.addPluginType(DatabasePluginType.getInstance());
			
			PluginRegistry.init();
						
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
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line01", Const.VERSION)+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line02")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line03")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line04")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line05")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line06")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line07")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line08")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line09")+Const.CR).getBytes());
				out.write((BaseMessages.getString(PKG, "Props.Kettle.Properties.Sample.Line10")+Const.CR).getBytes());
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
