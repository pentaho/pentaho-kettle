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

package org.pentaho.di.kitchen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.pan.CommandLineOption;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.version.BuildVersion;

public class Kitchen
{
	public static final String STRING_KITCHEN = "Kitchen";
	
	public static void main(String[] a) throws KettleException
	{
		EnvUtil.environmentInit();
		
	    List<String> args = new ArrayList<String>();
	    for (int i=0;i<a.length;i++)
	    {
	        if (a[i].length()>0) args.add(a[i]);
	    }

		RepositoryMeta repinfo  = null;
		UserInfo       userinfo = null;
		Job            job      = null;
		
		StringBuffer optionRepname, optionUsername, optionPassword, optionJobname, optionDirname, optionFilename, optionLoglevel;
        StringBuffer optionLogfile, optionLogfileOld, optionListdir, optionListjobs, optionListrep, optionNorep, optionVersion;

		CommandLineOption options[] = new CommandLineOption[] 
            {
			    new CommandLineOption("rep", Messages.getString("Kitchen.CmdLine.RepName"), optionRepname=new StringBuffer()),
			    new CommandLineOption("user", Messages.getString("Kitchen.CmdLine.RepUsername"), optionUsername=new StringBuffer()),
			    new CommandLineOption("pass", Messages.getString("Kitchen.CmdLine.RepPassword"), optionPassword=new StringBuffer()),
			    new CommandLineOption("job", Messages.getString("Kitchen.CmdLine.RepJobName") , optionJobname=new StringBuffer()),
			    new CommandLineOption("dir", Messages.getString("Kitchen.CmdLine.RepDir"), optionDirname=new StringBuffer()),
			    new CommandLineOption("file", Messages.getString("Kitchen.CmdLine.XMLJob"), optionFilename=new StringBuffer()),
			    new CommandLineOption("level", Messages.getString("Kitchen.CmdLine.LogLevel"), optionLoglevel=new StringBuffer()),
			    new CommandLineOption("logfile", Messages.getString("Kitchen.CmdLine.LogFile"), optionLogfile=new StringBuffer()),
			    new CommandLineOption("log", Messages.getString("Kitchen.CmdLine.LogFileOld"), optionLogfileOld=new StringBuffer(), false, true),
			    new CommandLineOption("listdir", Messages.getString("Kitchen.CmdLine.ListDir"), optionListdir=new StringBuffer(), true, false),
			    new CommandLineOption("listjobs", Messages.getString("Kitchen.CmdLine.ListJobsDir"), optionListjobs=new StringBuffer(), true, false),
			    new CommandLineOption("listrep", Messages.getString("Kitchen.CmdLine.ListAvailableReps"), optionListrep=new StringBuffer(), true, false),
		        new CommandLineOption("norep", Messages.getString("Kitchen.CmdLine.NoRep"), optionNorep=new StringBuffer(), true, false),
                new CommandLineOption("version", Messages.getString("Kitchen.CmdLine.Version") , optionVersion=new StringBuffer(), true, false),
            };

		if (args.size()==0 ) 
		{
		    CommandLineOption.printUsage(options);
		    exitJVM(9);
		}
        
        LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        CommandLineOption.parseArguments(args, options, log);

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (!Const.isEmpty(kettleRepname )) optionRepname  = new StringBuffer(kettleRepname );
        if (!Const.isEmpty(kettleUsername)) optionUsername = new StringBuffer(kettleUsername);
        if (!Const.isEmpty(kettlePassword)) optionPassword = new StringBuffer(kettlePassword);
        
        LogWriter.setConsoleAppenderDebug();
        
        if (Const.isEmpty(optionLogfile) && !Const.isEmpty(optionLogfileOld))
        {
           // if the old style of logging name is filled in, and the new one is not
           // overwrite the new by the old
           optionLogfile = optionLogfileOld;
        }
        
        if (Const.isEmpty(optionLogfile))
        {
            log=LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
        }
        else
        {
            log=LogWriter.getInstance( optionLogfile.toString(), true, LogWriter.LOG_LEVEL_BASIC );
        }
        
        if (!Const.isEmpty(optionLoglevel)) 
        {
            log.setLogLevel(optionLoglevel.toString());
            log.logMinimal(STRING_KITCHEN, Messages.getString("Kitchen.Log.LogLevel",log.getLogLevelLongDesc()));
        } 
		
        if (!Const.isEmpty(optionVersion))
        {
            BuildVersion buildVersion = BuildVersion.getInstance();
            log.logBasic("Kitchen", Messages.getString("Kitchen.Log.KettleVersion",Const.VERSION,""+buildVersion.getVersion(),""+buildVersion.getBuildDate()));
            if (a.length==1) exitJVM(6);
        }
        
        // Start the action...
        //
        if (!Const.isEmpty(optionRepname) && !Const.isEmpty(optionUsername)) 	
        {
        	if(log.isDetailed()) log.logDetailed(STRING_KITCHEN,  Messages.getString("Kitchen.Log.RepUsernameSupplied"));
        }

		log.logMinimal(STRING_KITCHEN, Messages.getString("Kitchen.Log.Starting"));
		
		
		/* Load the plugins etc.*/
		try {
			StepLoader.init();
		}
		catch(KettleException e)
		{
			log.logError(STRING_KITCHEN, Messages.getString("Kitchen.Error.LoadingSteps"), e);
			
			exitJVM(8);
		}
		StepLoader stepLoader = StepLoader.getInstance();
        
        /* Load the plugins etc.*/
		try 
		{
			JobEntryLoader.init();
		}
		catch(KettleException e)
        {
            log.logError(STRING_KITCHEN, Messages.getString("Kitchen.Error.LoadingJobEntries"), e);
            
            return;
        }

		Date start, stop;
		Calendar cal;
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		cal=Calendar.getInstance();
		start=cal.getTime();
				
		if(log.isDebug()) log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.AllocateNewJob"));
		
		
		JobMeta jobMeta = new JobMeta(log);
        
        // In case we use a repository...
        Repository repository = null;

		try
		{
			// Read kettle job specified on command-line?
			if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename))
			{
				if(log.isDebug()) log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.ParsingCommandLine"));
				if (!Const.isEmpty(optionRepname) && !"Y".equalsIgnoreCase(optionNorep.toString()))
				{
					if(log.isDebug()) log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.LoadingRep"));
					
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						if(log.isDebug())log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.FindingRep",""+optionRepname));
						repinfo = repsinfo.findRepository(optionRepname.toString());
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							if(log.isDebug())log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.Alocate&ConnectRep"));
							 
							repository = new Repository(log, repinfo, userinfo);
							if (repository.connect("Kitchen commandline"))
							{
								RepositoryDirectory directory = repository.getDirectoryTree(); // Default = root
								
								// Find the directory name if one is specified...
								if (!Const.isEmpty(optionDirname))
								{
									directory = repository.getDirectoryTree().findDirectory(optionDirname.toString());
								}
								
								if (directory!=null)
								{
									// Check username, password
									if(log.isDebug())log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.CheckUserPass"));
									
									userinfo = new UserInfo(repository, optionUsername.toString(), optionPassword.toString());
									if (userinfo.getID()>0)
									{
									    // Load a job
										if (!Const.isEmpty(optionJobname))
										{
											if(log.isDebug())log.logDebug(STRING_KITCHEN,Messages.getString("Kitchen.Log.LoadingJobInfo"));
											
											jobMeta =  new JobMeta(log, repository, optionJobname.toString(), directory);
											if(log.isDebug())log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.AllocateJob"));
											
											job = new Job(log, stepLoader, repository, jobMeta);
										}
										else
										// List the jobs in the repository
										if ("Y".equalsIgnoreCase(optionListjobs.toString()))
										{
											if(log.isDebug())log.logDebug(STRING_KITCHEN, Messages.getString("Kitchen.Log.GettingLostJobsInDirectory",""+directory));
											
											String jobnames[] = repository.getJobNames(directory.getID());
											for (int i=0;i<jobnames.length;i++)
											{
												System.out.println(jobnames[i]);
											}
										}
										else
										// List the directories in the repository
										if ("Y".equalsIgnoreCase(optionListdir.toString()))
										{
											String dirnames[] = repository.getDirectoryNames(directory.getID());
											for (int i=0;i<dirnames.length;i++)
											{
												System.out.println(dirnames[i]);
											}
										}
									}
									else
									{
										System.out.println(Messages.getString("Kitchen.Error.CanNotVerifyUserPass"));
										
										userinfo=null;
										repinfo=null;
									}
								}
								else
								{
									System.out.println(Messages.getString("Kitchen.Error.CanNotFindSuppliedDirectory",optionDirname+""));
									
									userinfo=null;
									repinfo=null;
								}
							}
							else
							{
								System.out.println(Messages.getString("Kitchen.Error.CanNotConnectRep"));
								
							}
						}
						else
						{
							System.out.println(Messages.getString("Kitchen.Error.NoRepProvided"));
							
						}
					}
					else
					{
						System.out.println(Messages.getString("Kitchen.Error.NoRepDefinied"));
						
					}
				}
				
                // Try to load if from file anyway.
				if (!Const.isEmpty(optionFilename) && job==null)
				{
					jobMeta = new JobMeta(log, optionFilename.toString(), null, null);
					job = new Job(log, stepLoader, null, jobMeta);
				}
			}
			else
			if ("Y".equalsIgnoreCase(optionListrep.toString()))
			{
				RepositoriesMeta ri = new RepositoriesMeta(log);
				if (ri.readData())
				{
					System.out.println(Messages.getString("Kitchen.Log.ListRep"));
					
					for (int i=0;i<ri.nrRepositories();i++)
					{
						RepositoryMeta rinfo = ri.getRepository(i);
						System.out.println("#"+(i+1)+" : "+rinfo.getName()+" ["+rinfo.getDescription()+"] ");
					}
				}
				else
				{
					System.out.println(Messages.getString("Kitchen.Error.UnableToReadXMLFile"));
					
				}
			}
		}
		catch(KettleException e)
		{
			job=null;
			jobMeta=null;
			System.out.println(Messages.getString("Kitchen.Error.StopProcess",e.getMessage()));
			
		}

		if (job==null)
		{
			if (!"Y".equalsIgnoreCase(optionListjobs.toString()) &&  
				!"Y".equalsIgnoreCase(optionListdir.toString()) && 
				!"Y".equalsIgnoreCase(optionListrep.toString()) 
			    )
			{
				System.out.println(Messages.getString("Kitchen.Error.canNotLoadJob"));			    
			}

			exitJVM(7);
		}
		
		Result result = null;

        int returnCode=0;
        
		try
		{
            // Set the arguments on the job metadata as well...
            if ( args.size() == 0 )
            {
                job.getJobMeta().setArguments(null);
            }
            else
            {
                job.getJobMeta().setArguments((String[]) args.toArray(new String[args.size()]));
            }
            job.initializeVariablesFrom(null);
            job.getJobMeta().setInternalKettleVariables(job);
            
			result = job.execute(); // Execute the selected job.		
			job.endProcessing(Database.LOG_STATUS_END, result);  // The bookkeeping...
		}
		catch(KettleJobException je)
		{
            if (result==null)
            {
                result = new Result();
            }
            result.setNrErrors(1L);
            
			try
			{
				job.endProcessing("error", result);
			}
			catch(KettleJobException je2)
			{
				log.logError(job.getJobname(), Messages.getString("Kitchen.Error.SeriousError",je2.getMessage()));
                log.logError(job.getJobname(), Messages.getString("Kitchen.Error.SeriousError",je.getMessage()));
				
                returnCode = 2;
			}
		}
        finally
        {
            if (repository!=null) repository.disconnect();
        }
        
		log.logMinimal(STRING_KITCHEN, Messages.getString("Kitchen.Log.Finished"));
		
		
		if (result!=null && result.getNrErrors()!=0)
		{
			log.logError(STRING_KITCHEN, Messages.getString("Kitchen.Error.FinishedWithErrors"));
            returnCode = 1;
		}
		cal=Calendar.getInstance();
		stop=cal.getTime();
		String begin=df.format(start).toString();
		String end  =df.format(stop).toString();

		log.logMinimal(STRING_KITCHEN, Messages.getString("Kitchen.Log.StartStop",begin,end));
		
		long seconds = (stop.getTime() - start.getTime()) / 1000;
		if (seconds <= 60) {
		    log.logMinimal(STRING_KITCHEN,  Messages.getString("Kitchen.Log.ProcessEndAfter", String.valueOf(seconds)));
		}
		else if (seconds <= 60 * 60) {
		    int min = (int)(seconds / 60);
		    int rem = (int)(seconds % 60);
            log.logMinimal(STRING_KITCHEN,  Messages.getString("Kitchen.Log.ProcessEndAfterLong", String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
		}
		else if (seconds <= 60 * 60 * 24) {
		    int rem;
            int hour = (int)(seconds / (60 * 60));
            rem = (int)(seconds % (60 * 60)); 
            int min = rem / 60;
            rem = rem % 60;
            log.logMinimal(STRING_KITCHEN,  Messages.getString("Kitchen.Log.ProcessEndAfterLonger", String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
		}
		else {
            int rem;
            int days = (int)(seconds / (60 * 60 * 24));
            rem = (int)(seconds % (60 * 60 * 24));
            int hour = rem / (60 * 60);
            rem = rem % (60 * 60); 
            int min = rem / 60;
            rem = rem % 60;
            log.logMinimal(STRING_KITCHEN,  Messages.getString("Kitchen.Log.ProcessEndAfterLongest", String.valueOf(days), String.valueOf(hour), String.valueOf(min), String.valueOf(rem), String.valueOf(seconds)));
		}
		        
        exitJVM(returnCode);

	}
	
	private static final void exitJVM(int status) {
		// Close the open appenders...
		//
		LogWriter.getInstance().close();

		System.exit(status);
	}
}
