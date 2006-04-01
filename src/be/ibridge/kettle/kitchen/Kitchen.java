 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 

/**
 *   Kettle was (re-)started in March 2003
 */

package be.ibridge.kettle.kitchen;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.StepLoader;


public class Kitchen
{
	public static final String STRING_KITCHEN = "Kitchen";
	
	public static void main(String[] a)
	{
	    ArrayList args = new ArrayList();
	    for (int i=0;i<a.length;i++) 
	    {
	        if (a[i].length()>0) args.add(a[i]);
	    }

		RepositoryMeta repinfo  = null;
		UserInfo       userinfo = null;
		Job            job      = null;

		if (args.size()==0 ) 
		{
		    System.out.println("Options:");
		    System.out.println("  -rep      : Repository name");
		    System.out.println("  -user     : Repository username");
		    System.out.println("  -pass     : Repository password");
		    System.out.println("  -job      : The name of the job to launch");
		    System.out.println("  -dir      : The directory (don't forget the leading / or \\)");
		    System.out.println("  -file     : The filename (Job XML) to launch");
		    System.out.println("  -level    : The logging level	(Basic, Detailed, Debug, Rowlevel, Error, Nothing)");
		    System.out.println("  -log      : The logging file to write to");
		    System.out.println("  -listdir  : List the directories in the repository");
		    System.out.println("  -listjobs : List the jobs in the specified directory");
		    System.out.println("  -listrep  : List the defined repositories");
            System.out.println("  -norep    : Don't log into the repository");
		    System.out.println("");
		    
		    System.exit(9);
		}

		String repname   = Const.getCommandlineOption(args, "rep");
		String username  = Const.getCommandlineOption(args, "user");
		String password  = Const.getCommandlineOption(args, "pass");
		String jobname   = Const.getCommandlineOption(args, "job");
		String dirname   = Const.getCommandlineOption(args, "dir");
		String filename  = Const.getCommandlineOption(args, "file");
		String loglevel  = Const.getCommandlineOption(args, "level");
		String logfile   = Const.getCommandlineOption(args, "log");
		String listdir   = Const.getCommandlineOption(args, "listdir");
		String listjobs  = Const.getCommandlineOption(args, "listjobs");
		String listrep   = Const.getCommandlineOption(args, "listrep");
        String norep     = Const.getCommandlineOption(args, "norep");

        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) repname  = kettleRepname;
        if (kettleUsername!=null && kettleUsername.length()>0) username = kettleUsername;
        if (kettlePassword!=null && kettlePassword.length()>0) password = kettlePassword;
        
		// System.out.println("Level="+loglevel);
        LogWriter log;
        if (logfile==null)
        {
            log=LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
        }
        else
        {
            log=LogWriter.getInstance( logfile, true, LogWriter.LOG_LEVEL_BASIC );
        }
        
        if (loglevel!=null) 
        {
            log.setLogLevel(loglevel);
            log.logMinimal(STRING_KITCHEN, "Logging is at level : "+log.getLogLevelDesc());
        } 
		
        if (repname!=null && username!=null) log.logDetailed(STRING_KITCHEN, "Repository and username supplied");

		log.logMinimal(STRING_KITCHEN, "Start of run.");
		
		/* Load the plugins etc.*/
		StepLoader steploader = StepLoader.getInstance();
		if (!steploader.read())
		{
			log.logError("Spoon", "Error loading steps... halting Kitchen!");
			System.exit(8);
		}

		Date start, stop;
		Calendar cal;
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		cal=Calendar.getInstance();
		start=cal.getTime();
				
		log.logDebug(STRING_KITCHEN, "Allocate new job.");
		JobMeta jobinfo = new JobMeta(log);
        
        
        // In case we use a repository...
        Repository repository = null;

		try
		{
			// Read kettle job specified on command-line?
			if (repname!=null || filename!=null)
			{
				log.logDebug(STRING_KITCHEN, "Parsing command line options.");
				if (repname!=null && !"Y".equalsIgnoreCase(norep))
				{
					log.logDebug(STRING_KITCHEN, "Loading available repositories.");
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						log.logDebug(STRING_KITCHEN, "Finding repository ["+repname+"]");
						repinfo = repsinfo.findRepository(repname);
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							log.logDebug(STRING_KITCHEN, "Allocate & connect to repository.");
							repository = new Repository(log, repinfo, userinfo);
							if (repository.connect("Kitchen commandline"))
							{
								RepositoryDirectory directory = repository.getDirectoryTree(); // Default = root
								
								// Find the directory name if one is specified...
								if (dirname!=null)
								{
									directory = repository.getDirectoryTree().findDirectory(dirname);
								}
								
								if (directory!=null)
								{
									// Check username, password
									log.logDebug(STRING_KITCHEN, "Check supplied username and password.");
									userinfo = new UserInfo(repository, username, password);
									if (userinfo.getID()>0)
									{
									    // Load a job
										if (jobname!=null && jobname.length()>0)
										{
											log.logDebug(STRING_KITCHEN, "Load the job info...");
											jobinfo =  new JobMeta(log, repository, jobname, directory);
											log.logDebug(STRING_KITCHEN, "Allocate job...");
											job = new Job(log, steploader, repository, jobinfo);
										}
										else
										// List the jobs in the repository
										if ("Y".equalsIgnoreCase(listjobs))
										{
										    log.logDebug(STRING_KITCHEN, "Getting list of jobs in directory: "+directory);
											String jobnames[] = repository.getJobNames(directory.getID());
											for (int i=0;i<jobnames.length;i++)
											{
												System.out.println(jobnames[i]);
											}
										}
										else
										// List the directories in the repository
										if ("Y".equalsIgnoreCase(listdir))
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
										System.out.println("ERROR: Can't verify username and password.");
										userinfo=null;
										repinfo=null;
									}
								}
								else
								{
									System.out.println("ERROR: Can't find the supplied directory ["+dirname+"]");
									userinfo=null;
									repinfo=null;
								}
							}
							else
							{
								System.out.println("ERROR: Can't connect to the repository.");
							}
						}
						else
						{
							System.out.println("ERROR: No repository provided, can't load job.");
						}
					}
					else
					{
						System.out.println("ERROR: No repositories defined on this system.");
					}
				}
				
                // Try to load if from file anyway.
				if (filename!=null && job==null)
				{
					jobinfo = new JobMeta(log, filename);
					job = new Job(log, steploader, null, jobinfo);
				}
			}
			else
			if ("Y".equalsIgnoreCase(listrep))
			{
				RepositoriesMeta ri = new RepositoriesMeta(log);
				if (ri.readData())
				{
					System.out.println("List of repositories:");
					for (int i=0;i<ri.nrRepositories();i++)
					{
						RepositoryMeta rinfo = ri.getRepository(i);
						System.out.println("#"+(i+1)+" : "+rinfo.getName()+" ["+rinfo.getDescription()+"] ");
					}
				}
				else
				{
					System.out.println("ERROR: Unable to read/parse the repositories XML file.");
				}
			}
		}
		catch(KettleException e)
		{
			job=null;
			jobinfo=null;
			System.out.println("Processing stopped because of an error: "+e.getMessage());
		}

		if (job==null)
		{
			if (!"Y".equalsIgnoreCase(listjobs) &&  !"Y".equalsIgnoreCase(listdir) && !"Y".equalsIgnoreCase(listrep) )
			{
				System.out.println("ERROR: Kitchen can't continue because the job couldn't be loaded.");			    
			}

            System.exit(7);
		}
		
		Result result = null;

        int returnCode=0;
        
		try
		{
			result = job.execute(); // Execute the selected job.
			job.endProcessing("end");  // The bookkeeping...
		}
		catch(KettleJobException je)
		{
			try
			{
				job.endProcessing("error");
			}
			catch(KettleJobException je2)
			{
				log.logError(job.getName(), "A serious error occured : "+je2.getMessage());
                returnCode = 2;
			}
		}
        finally
        {
            if (repository!=null) repository.disconnect();
        }
        
		log.logMinimal(STRING_KITCHEN, "Finished!");
		
		if (result!=null && result.getNrErrors()!=0)
		{
			log.logError(STRING_KITCHEN, "Finished with errors");
            returnCode = 1;
		}
		cal=Calendar.getInstance();
		stop=cal.getTime();
		String begin=df.format(start).toString();
		String end  =df.format(stop).toString();

		log.logMinimal(STRING_KITCHEN, "Start="+begin+", Stop="+end);
		long millis=stop.getTime()-start.getTime();
		log.logMinimal(STRING_KITCHEN, "Processing ended after "+(millis/1000)+" seconds.");
        
        System.exit(returnCode);

	}
}
