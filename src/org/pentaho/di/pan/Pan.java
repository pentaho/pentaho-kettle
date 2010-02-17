 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

/**
 *   Kettle was (re-)started in March 2003
 */

package org.pentaho.di.pan;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryExporter;
import org.pentaho.di.repository.RepositoryLoader;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.version.BuildVersion;
import org.w3c.dom.Document;


public class Pan
{
	private static Class<?> PKG = Pan.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String	STRING_PAN	= "Pan";

	public static void main(String[] a) throws KettleException
	{
		KettleEnvironment.init();

	    List<String> args = new ArrayList<String>();
	    for (int i=0;i<a.length;i++) 
	    {
	        if (a[i].length()>0) 
            {
                args.add(a[i]);
            }
	    }

		RepositoryMeta repinfo  = null;
		Trans          trans    = null;

		// The options: 
		StringBuffer optionRepname, optionUsername, optionPassword, optionTransname, optionDirname, optionFilename, optionLoglevel;
		StringBuffer optionLogfile, optionLogfileOld, optionListdir, optionListtrans, optionListrep, optionExprep, optionNorep, optionSafemode, optionVersion, optionJarFilename, optionListParam;
		NamedParams optionParams = new NamedParamsDefault();
        
		CommandLineOption options[] = new CommandLineOption[] 
            {
			    new CommandLineOption("rep", BaseMessages.getString(PKG, "Pan.ComdLine.RepName"), optionRepname=new StringBuffer()),
			    new CommandLineOption("user", BaseMessages.getString(PKG, "Pan.ComdLine.RepUsername"), optionUsername=new StringBuffer()),
			    new CommandLineOption("pass", BaseMessages.getString(PKG, "Pan.ComdLine.RepPassword"), optionPassword=new StringBuffer()),
			    new CommandLineOption("trans", BaseMessages.getString(PKG, "Pan.ComdLine.TransName"), optionTransname=new StringBuffer()),
			    new CommandLineOption("dir", BaseMessages.getString(PKG, "Pan.ComdLine.RepDir"), optionDirname=new StringBuffer()),
			    new CommandLineOption("file", BaseMessages.getString(PKG, "Pan.ComdLine.XMLTransFile"), optionFilename=new StringBuffer()),
			    new CommandLineOption("level", BaseMessages.getString(PKG, "Pan.ComdLine.LogLevel"), optionLoglevel=new StringBuffer()),
			    new CommandLineOption("logfile", BaseMessages.getString(PKG, "Pan.ComdLine.LogFile"), optionLogfile=new StringBuffer()),
			    new CommandLineOption("log",  BaseMessages.getString(PKG, "Pan.ComdLine.LogOldFile"), optionLogfileOld=new StringBuffer(), false, true),
			    new CommandLineOption("listdir", BaseMessages.getString(PKG, "Pan.ComdLine.ListDirRep") , optionListdir=new StringBuffer(), true, false),
			    new CommandLineOption("listtrans", BaseMessages.getString(PKG, "Pan.ComdLine.ListTransDir"), optionListtrans=new StringBuffer(), true, false),
			    new CommandLineOption("listrep", BaseMessages.getString(PKG, "Pan.ComdLine.ListReps"), optionListrep=new StringBuffer(), true, false),
		        new CommandLineOption("exprep", BaseMessages.getString(PKG, "Pan.ComdLine.ExpObjectsXML"), optionExprep=new StringBuffer(), true, false),
		        new CommandLineOption("norep", BaseMessages.getString(PKG, "Pan.ComdLine.NoRep"), optionNorep=new StringBuffer(), true, false),
		        new CommandLineOption("safemode", BaseMessages.getString(PKG, "Pan.ComdLine.SafeMode"), optionSafemode=new StringBuffer(), true, false),
                new CommandLineOption("version", BaseMessages.getString(PKG, "Pan.ComdLine.Version"), optionVersion=new StringBuffer(), true, false),
                new CommandLineOption("jarfile", BaseMessages.getString(PKG, "Pan.ComdLine.JarFile") , optionJarFilename=new StringBuffer(), false, true),
                new CommandLineOption("param", BaseMessages.getString(PKG, "Pan.ComdLine.Param") , optionParams, false),
		        new CommandLineOption("listparam", BaseMessages.getString(PKG, "Pan.ComdLine.ListParam"), optionListParam=new StringBuffer(), true, false),
            };

		if (args.size()==0 ) 
		{
			CommandLineOption.printUsage(options);
			exitJVM(9);
		}

		// start with the default logger until we find out otherwise
		LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
		LogWriter.setConsoleAppenderDebug();

		LogChannelInterface log = new LogChannel(STRING_PAN);
        
        // Parse the options...
		if( !CommandLineOption.parseArguments(args, options, log) ) {
            log.logError(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Error.CommandLineError"));
           
            exitJVM(8);
		}
		
		String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) optionRepname  = new StringBuffer(kettleRepname);
        if (kettleUsername!=null && kettleUsername.length()>0) optionUsername = new StringBuffer(kettleUsername);
        if (kettlePassword!=null && kettlePassword.length()>0) optionPassword = new StringBuffer(kettlePassword);
        
        
        if (Const.isEmpty(optionLogfile) && !Const.isEmpty(optionLogfileOld))
        {
           // if the old style of logging name is filled in, and the new one is not
           // overwrite the new by the old
           optionLogfile = optionLogfileOld;
        }
        
        if (!Const.isEmpty(optionLogfile))
        {
            LogWriter.getInstance( optionLogfile.toString(), true, LogWriter.LOG_LEVEL_BASIC );
        }
        
        if (!Const.isEmpty(optionLoglevel)) 
        {
        	LogWriter.getInstance().setLogLevel(optionLoglevel.toString());
            log.logMinimal(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Log.Loglevel", LogWriter.getInstance().getLogLevelLongDesc()));
           
        }
        
        if (!Const.isEmpty(optionVersion))
        {
            BuildVersion buildVersion = BuildVersion.getInstance();
            if(log.isBasic()) log.logBasic(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.KettleVersion", buildVersion.getVersion(), buildVersion.getRevision(), buildVersion.getBuildDate()));
            
            if (a.length==1) exitJVM(6);
        }
        
        /////////////////////////////////////////////////////////////////////////////////////////////////////
        // This is where the action starts.
        // Print the options before we start processing when running in Debug or Rowlevel
        //
        if (log.isDebug())
        {
		    System.out.println("Arguments:");
		    for (int i=0;i<options.length;i++) 
		    {
		    	/*if (!options[i].isHiddenOption())*/
                System.out.println(Const.rightPad(options[i].getOption(),12)+" : "+options[i].getArgument());
		    }
		    System.out.println("");
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////

        log.logMinimal(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.StartingToRun"));       
		
		Date start, stop;
		Calendar cal;
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		cal=Calendar.getInstance();
		start=cal.getTime();

		if(log.isDebug()) log.logDebug(STRING_PAN,BaseMessages.getString(PKG, "Pan.Log.AllocatteNewTrans"));
		
		TransMeta transMeta = new TransMeta();

		// In case we use a repository...
		Repository rep=null;
		try
		{
			if(log.isDebug()) log.logDebug(STRING_PAN,BaseMessages.getString(PKG, "Pan.Log.StartingToLookOptions"));
			
			// Read kettle transformation specified on command-line?
			if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename) || !Const.isEmpty(optionJarFilename))
			{			
				if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.ParsingCommandline"));
				
				if (!Const.isEmpty(optionRepname) && !"Y".equalsIgnoreCase(optionNorep.toString()))
				{
					if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.LoadingAvailableRep"));
					
					RepositoriesMeta repsinfo = new RepositoriesMeta();
					
					try { repsinfo.readData(); }
					catch(Exception e) {
						throw new KettleException(BaseMessages.getString(PKG, "Pan.Error.NoRepsDefined"), e);
					}
					
					if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.FindingRep",""+optionRepname));
					
					repinfo = repsinfo.findRepository(optionRepname.toString());
					if (repinfo!=null)
					{
						// Define and connect to the repository...
						if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.Allocate&ConnectRep"));
						
						rep = RepositoryLoader.createRepository(repinfo);
						rep.connect(optionUsername != null ? optionUsername.toString() : null, optionPassword != null ? optionPassword.toString() : null);
						
						RepositoryDirectory directory = rep.loadRepositoryDirectoryTree(); // Default = root
						
						// Find the directory name if one is specified...
						if (!Const.isEmpty(optionDirname))
						{
							directory = directory.findDirectory(optionDirname.toString());
						}
						
						if (directory!=null)
						{
							// Check username, password
							if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.CheckSuppliedUserPass"));

							// Load a transformation
							if (!Const.isEmpty(optionTransname))
							{
								if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.LoadTransInfo"));
								
								transMeta = rep.loadTransformation(optionTransname.toString(), directory, null, true, null); // reads last version
								if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.AllocateTrans"));
								
								trans = new Trans(transMeta);
								trans.setRepository(rep);
							}
							else
							// List the transformations in the repository
							if ("Y".equalsIgnoreCase(optionListtrans.toString()))
							{
								if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.GettingListTransDirectory",""+directory));
								
								String transnames[] = rep.getTransformationNames(directory.getObjectId(), false);
								for (int i=0;i<transnames.length;i++)
								{
									System.out.println(transnames[i]);
								}
							}
							else
							// List the directories in the repository
							if ("Y".equalsIgnoreCase(optionListdir.toString()))
							{
								String dirnames[] = rep.getDirectoryNames(directory.getObjectId());
								for (int i=0;i<dirnames.length;i++)
								{
									System.out.println(dirnames[i]);
								}
							}
                              else
                              // Export the repository
                              if (!Const.isEmpty(optionExprep))
                              {
                                  System.out.println(BaseMessages.getString(PKG, "Pan.Log.ExportingObjectsRepToFile",""+optionExprep));
                                  
                                  new RepositoryExporter(rep).exportAllObjects(null, optionExprep.toString(), directory,"all");
                                  System.out.println(BaseMessages.getString(PKG, "Pan.Log.FinishedExportObjectsRepToFile",""+optionExprep));
                              }
							else
							{
								System.out.println(BaseMessages.getString(PKG, "Pan.Error.NoTransNameSupplied"));
							}
						}
						else
						{
							System.out.println(BaseMessages.getString(PKG, "Pan.Error.CanNotFindSpecifiedDirectory",""+optionDirname));
							repinfo=null;
						}
					}
					else
					{
						System.out.println(BaseMessages.getString(PKG, "Pan.Error.NoRepProvided"));
					}
				}			

				// Try to load the transformation from file, even if it failed to load from the repository
                // You could implement some fail-over mechanism this way.
                //
				if (trans==null && !Const.isEmpty(optionFilename))
				{
					if(log.isDetailed()) log.logDetailed(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.LoadingTransXML",""+optionFilename));
					
					transMeta = new TransMeta(optionFilename.toString());
					trans = new Trans(transMeta);
				}
                
                // Try to load the transformation from a jar file
                // 
                if (trans==null && !Const.isEmpty(optionJarFilename))
                {
                    try
                    {
                    	if(log.isDetailed())  log.logDetailed(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.LoadingTransJar",""+optionJarFilename));
                    	
                    	InputStream inputStream = Pan.class.getResourceAsStream(optionJarFilename.toString());
                        StringBuffer xml = new StringBuffer();
                        int c;
                        while ((c=inputStream.read()) != -1) xml.append((char)c);
                        inputStream.close();
                        Document document = XMLHandler.loadXMLString(xml.toString());
                        transMeta = new TransMeta(XMLHandler.getSubNode(document, "transformation"), null);
                        trans = new Trans(transMeta);
                    }
                    catch(Exception e)
                    {
                        System.out.println(BaseMessages.getString(PKG, "Pan.Error.ReadingJar",e.toString()));
                        
                        System.out.println(Const.getStackTracker(e));
                        throw e;
                    }
                }
			}
			
			if ("Y".equalsIgnoreCase(optionListrep.toString()))
			{
				if(log.isDebug()) log.logDebug(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.GettingListReps"));
				
				RepositoriesMeta ri = new RepositoriesMeta();
				try { ri.readData(); }
				catch(Exception e) {
					throw new KettleException(BaseMessages.getString(PKG, "Pan.Error.UnableReadXML"), e);
				}

				System.out.println(BaseMessages.getString(PKG, "Pan.Log.ListReps"));
					
				for (int i=0;i<ri.nrRepositories();i++)
				{
					RepositoryMeta rinfo = ri.getRepository(i);
					System.out.println(BaseMessages.getString(PKG, "Pan.Log.RepNameDesc",""+(i+1),rinfo.getName(),rinfo.getDescription()));					
				}
			}
		}
		catch(Exception e)
		{
			trans=null;
			transMeta=null;
			if(rep!=null) rep.disconnect();
			System.out.println(BaseMessages.getString(PKG, "Pan.Error.ProcessStopError",e.getMessage()));
			
			e.printStackTrace();
			exitJVM(1);
		}

		if (trans==null)
		{
			if(rep!=null) rep.disconnect();
			
			if (!"Y".equalsIgnoreCase(optionListtrans.toString()) && 
                !"Y".equalsIgnoreCase(optionListdir.toString()) && 
                !"Y".equalsIgnoreCase(optionListrep.toString()) &&
                Const.isEmpty(optionExprep)
               )
            {
                System.out.println(BaseMessages.getString(PKG, "Pan.Error.CanNotLoadTrans"));
                
                exitJVM(7);
            }
			else
			{
				exitJVM(0);
			}
            
		}
		
		try
		{
			trans.initializeVariablesFrom(null);
			trans.getTransMeta().setInternalKettleVariables(trans);
			
			// Map the command line named parameters to the actual named parameters. Skip for
			// the moment any extra command line parameter not known in the transformation.
			String[] transParams = trans.listParameters();
			for ( String param : transParams )  {
				String value = optionParams.getParameterValue(param);
				if ( value != null )  {
					trans.setParameterValue(param, value);
				}
			}
			// Put the parameters over the already defined variable space. Parameters
			// get priority.
			trans.activateParameters();
			
			// See if we want to run in safe mode:
			if ("Y".equalsIgnoreCase(optionSafemode.toString()))
			{
				trans.setSafeModeEnabled(true);
			}

    		// List the parameters defined in this transformation 
    		// Then simply exit...
    		//
    		if ("Y".equalsIgnoreCase(optionListParam.toString())) {
    			for (String parameterName : trans.listParameters()) {
    				String value = trans.getParameterValue(parameterName);
    				String deflt = trans.getParameterDefault(parameterName);
    				String descr = trans.getParameterDescription(parameterName);
    				
    				if ( deflt != null )  {
    					System.out.println("Parameter: "+parameterName+"="+Const.NVL(value, "")+", default="+deflt+" : "+Const.NVL(descr, ""));
    				} else {
    					System.out.println("Parameter: "+parameterName+"="+Const.NVL(value, "")+" : "+Const.NVL(descr, ""));
    				}
    			}
    			
    			// stop right here...
    			//
    			exitJVM(7); // same as the other list options
    		}

		    // allocate & run the required sub-threads
			try {
				trans.execute((String[])args.toArray(new String[args.size()]));
			}
			catch(KettleException e) {
                System.out.println( BaseMessages.getString(PKG, "Pan.Error.UnablePrepareInitTrans"));
               
                exitJVM(3);
			}

			trans.waitUntilFinished();

			log.logMinimal(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.Finished"));
			 
			
			cal=Calendar.getInstance();
			stop=cal.getTime();
			String begin=df.format(start).toString();
			String end  =df.format(stop).toString();

			log.logMinimal(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.StartStop",begin,end));
			
			long millis=stop.getTime()-start.getTime();
	        int seconds = (int)(millis / 1000);
	        if (seconds <= 60) {
	            log.logMinimal(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Log.ProcessingEndAfter", String.valueOf(seconds)));
	        }
	        else if (seconds <= 60 * 60) {
	            int min = (seconds / 60);
	            int rem = (seconds % 60);
	            log.logMinimal(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Log.ProcessingEndAfterLong", String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
	        }
	        else if (seconds <= 60 * 60 * 24) {
	            int rem;
	            int hour = (seconds / (60 * 60));
	            rem = (seconds % (60 * 60)); 
	            int min = rem / 60;
	            rem = rem % 60;
	            log.logMinimal(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Log.ProcessingEndAfterLonger", String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
	        }
	        else {
	            int rem;
	            int days = (seconds / (60 * 60 * 24));
	            rem = (seconds % (60 * 60 * 24));
	            int hour = rem / (60 * 60);
	            rem = rem % (60 * 60); 
	            int min = rem / 60;
	            rem = rem % 60;
	            log.logMinimal(STRING_PAN,  BaseMessages.getString(PKG, "Pan.Log.ProcessingEndAfterLongest", String.valueOf(days), String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
	        }
			
			if (trans.getResult().getNrErrors()==0) 
			{
				trans.printStats(seconds);
				exitJVM(0);
			}
			else
			{
				exitJVM(1);
			}
		}
		catch(KettleException ke)
		{
			System.out.println(BaseMessages.getString(PKG, "Pan.Log.ErrorOccurred",""+ke.getMessage()));
			
            log.logError(STRING_PAN, BaseMessages.getString(PKG, "Pan.Log.UnexpectedErrorOccurred",""+ke.getMessage()));
            
			// Close the file appender if any...
			//
			LogWriter.closeAndRemoveFileAppender();
			
            exitJVM(2);
		}
		finally
        {
            if (rep!=null) rep.disconnect();
        }

	}
	
	private static final void exitJVM(int status) {
		// Close the open appenders...
		//
		LogWriter.getInstance().close();
		
		System.exit(status);
	}
}