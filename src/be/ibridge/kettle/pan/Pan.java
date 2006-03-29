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

package be.ibridge.kettle.pan;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;


public class Pan
{
	public static void main(String[] a)
	{
	    ArrayList args = new ArrayList();
	    for (int i=0;i<a.length;i++) 
	    {
	        if (a[i].length()>0) 
            {
                args.add(a[i]);
            }
	    }

		RepositoryMeta repinfo  = null;
		UserInfo       userinfo = null;
		Trans          trans    = null;

		if (args.size()==0 ) 
		{
		    System.out.println("Options:");
		    System.out.println("  -rep       : Repository name");
		    System.out.println("  -user      : Repository username");
		    System.out.println("  -pass      : Repository password");
		    System.out.println("  -trans     : The name of the transformation to launch");
		    System.out.println("  -dir       : The directory (don't forget the leading / or \\)");
		    System.out.println("  -file      : The filename (Transformation in XML) to launch");
		    System.out.println("  -level     : The logging level	(Basic, Detailed, Debug, Rowlevel, Error, Nothing)");
		    System.out.println("  -logfile   : The logging file to write to");
		    System.out.println("  -listdir   : List the directories in the repository");
		    System.out.println("  -listtrans : List the transformations in the specified directory");
            System.out.println("  -exprep    : Export all repository objects to one XML file");
            System.out.println("  -norep     : do not log into the repository");
		    System.out.println("");
		    
            System.exit(9);
		}

		String repname   = Const.getCommandlineOption(args, "rep");
		String username  = Const.getCommandlineOption(args, "user");
		String password  = Const.getCommandlineOption(args, "pass");
		String transname = Const.getCommandlineOption(args, "trans");
		String dirname   = Const.getCommandlineOption(args, "dir");
		String filename  = Const.getCommandlineOption(args, "file");
		String loglevel  = Const.getCommandlineOption(args, "level");
		String logfile   = Const.getCommandlineOption(args, "log");
		String listdir   = Const.getCommandlineOption(args, "listdir");
		String listtrans = Const.getCommandlineOption(args, "listtrans");
		String listrep   = Const.getCommandlineOption(args, "listrep");
        String exprep    = Const.getCommandlineOption(args, "exprep");
        String norep     = Const.getCommandlineOption(args, "norep");
		
        String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) repname  = kettleRepname;
        if (kettleUsername!=null && kettleUsername.length()>0) username = kettleUsername;
        if (kettlePassword!=null && kettlePassword.length()>0) password = kettlePassword;
        
        /**
        System.out.println("Options:");
        System.out.println("-------------");
        if (repname!=null)   System.out.println("repository name :        "+repname);
        if (username!=null)  System.out.println("username :               "+username);
        if (password!=null)  System.out.println("password is set");
        if (dirname!=null)   System.out.println("directory :              "+dirname);
        if (loglevel!=null)  System.out.println("logging level :          "+loglevel);
        if (listdir!=null)   System.out.println("list directories");
        if (listtrans!=null) System.out.println("list transformations");
        if (exprep!=null)    System.out.println("export repository to:    "+exprep);
        */ 
        
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
            log.logMinimal("Pan", "Logging is at level : "+log.getLogLevelDesc());
        }
        
        log.logMinimal("Pan", "Start of run.");
		
		/* Load the plugins etc.*/
		StepLoader steploader = StepLoader.getInstance();
		if (!steploader.read())
		{
			log.logError("Spoon", "Error loading steps... halting Pan!");
            System.exit(8);
		}
		
		Date start, stop;
		Calendar cal;
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		cal=Calendar.getInstance();
		start=cal.getTime();

		log.logDebug("Pan", "Allocate new transformation.");
		TransMeta transMeta = new TransMeta();

		try
		{
			// Read kettle transformation specified on command-line?
			if (repname!=null || filename!=null)
			{			
				log.logDebug("Pan", "Parsing command line options.");
				if (repname!=null && !"Y".equalsIgnoreCase(norep))
				{
					log.logDebug("Pan", "Loading available repositories.");
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						log.logDebug("Pan", "Finding repository ["+repname+"]");
						repinfo = repsinfo.findRepository(repname);
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							log.logDebug("Pan", "Allocate & connect to repository.");
							Repository rep = new Repository(log, repinfo, userinfo);
							if (rep.connect("Pan commandline"))
							{
								RepositoryDirectory directory = rep.getDirectoryTree(); // Default = root
								
								// Find the directory name if one is specified...
								if (dirname!=null)
								{
									directory = rep.getDirectoryTree().findDirectory(dirname);
								}
								
								if (directory!=null)
								{
									// Check username, password
									log.logDebug("Pan", "Check supplied username and password.");
									userinfo = new UserInfo(rep, username, password);
									if (userinfo.getID()>0)
									{
										// Load a transformation
										if (transname!=null && transname.length()>0)
										{
											log.logDebug("Pan", "Load the transformation info...");
											transMeta = new TransMeta(rep, transname, directory);
											log.logDebug("Pan", "Allocate transformation...");
											trans = new Trans(log, transMeta);
										}
										else
										// List the transformations in the repository
										if ("Y".equalsIgnoreCase(listtrans))
										{
										    log.logDebug("Pan", "Getting list of transformations in directory: "+directory);
											String transnames[] = rep.getTransformationNames(directory.getID());
											for (int i=0;i<transnames.length;i++)
											{
												System.out.println(transnames[i]);
											}
										}
										else
										// List the directories in the repository
										if ("Y".equalsIgnoreCase(listdir))
										{
											String dirnames[] = rep.getDirectoryNames(directory.getID());
											for (int i=0;i<dirnames.length;i++)
											{
												System.out.println(dirnames[i]);
											}
										}
                                        else
                                        // Export the repository
                                        if (exprep!=null && exprep.length()>0)
                                        {
                                            System.out.println("Exporting all objects in the repository to file ["+exprep+"]");
                                            rep.exportAllObjects(null, exprep);
                                            System.out.println("Finished exporting all objects in the repository to file ["+exprep+"]");
                                        }
										else
										{
											System.out.println("ERROR: No transformation name supplied: which one should be run?");
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
									System.out.println("ERROR: Can't find the specified directory ["+dirname+"]");
									userinfo=null;
									repinfo=null;
								}
								rep.disconnect();
							}
							else
							{
								System.out.println("ERROR: Can't connect to the repository.");
							}
						}
						else
						{
							System.out.println("ERROR: No repository provided, can't load transformation.");
						}
					}
					else
					{
						System.out.println("ERROR: No repositories defined on this system.");
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
				
                // Try to load the transformation from file, even if it failed to load from the repository
                // You could implement some failover mechanism this way.
                //
				if (trans==null && filename!=null)
				{
                    log.logDetailed("Pan", "Loading transformation from XML file ["+filename+"]");
					transMeta = new TransMeta(filename);
					trans = new Trans(log, transMeta);
				}
			}
		}
		catch(KettleException e)
		{
			trans=null;
			transMeta=null;
			System.out.println("Processing has stopped because of an error: "+e.getMessage());
		}

		if (trans==null)
		{
			if (!"Y".equalsIgnoreCase(listtrans) && 
                !"Y".equalsIgnoreCase(listdir) && 
                !"Y".equalsIgnoreCase(listrep) &&
                ( exprep==null || exprep.length()==0 )
               )
            {
                System.out.println("ERROR: Pan can't continue because the transformation couldn't be loaded.");
            }
            System.exit(7);
		}
		
		try
		{
		    // allocate & run the required sub-threads
			boolean ok = trans.execute((String[])args.toArray(new String[args.size()])); 
			trans.waitUntilFinished();
			trans.endProcessing("end");

			log.logMinimal("Pan", "Finished!");
			
			cal=Calendar.getInstance();
			stop=cal.getTime();
			String begin=df.format(start).toString();
			String end  =df.format(stop).toString();

			log.logMinimal("Pan", "Start="+begin+", Stop="+end);
			long millis=stop.getTime()-start.getTime();
			log.logMinimal("Pan", "Processing ended after "+(millis/1000)+" seconds.");
			if (ok) 
			{
				trans.printStats((int)millis/1000);
                System.exit(0);
			}
			else
			{
                System.exit(1);
			}
		}
		catch(KettleException ke)
		{
			System.out.println("ERROR occurred: "+ke.getMessage());
            log.logError("Pan", "Unexpected error occurred: "+ke.getMessage());
            System.exit(2);
		}

	}
}
