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
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.repository.RepositoriesMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.RepositoryMeta;
import be.ibridge.kettle.repository.UserInfo;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.version.BuildVersion;


public class Pan
{
	public static void main(String[] a) throws KettleException
	{
		EnvUtil.environmentInit();
		
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

		// The options: 
		StringBuffer optionRepname, optionUsername, optionPassword, optionTransname, optionDirname, optionFilename, optionLoglevel;
		StringBuffer optionLogfile, optionListdir, optionListtrans, optionListrep, optionExprep, optionNorep, optionSafemode, optionVersion;
        
		CommandLineOption options[] = new CommandLineOption[] 
            {
			    new CommandLineOption("rep", "Repository name", optionRepname=new StringBuffer()),
			    new CommandLineOption("user", "Repository username", optionUsername=new StringBuffer()),
			    new CommandLineOption("pass", "Repository password", optionPassword=new StringBuffer()),
			    new CommandLineOption("trans", "The name of the transformation to launch", optionTransname=new StringBuffer()),
			    new CommandLineOption("dir", "The directory (don't forget the leading /)", optionDirname=new StringBuffer()),
			    new CommandLineOption("file", "The filename (Transformation in XML) to launch", optionFilename=new StringBuffer()),
			    new CommandLineOption("level", "The logging level (Basic, Detailed, Debug, Rowlevel, Error, Nothing)", optionLoglevel=new StringBuffer()),
			    new CommandLineOption("logfile", "The logging file to write to", optionLogfile=new StringBuffer()),
			    new CommandLineOption("log", "The logging file to write to (deprecated)", optionLogfile=new StringBuffer(), false, true),
			    new CommandLineOption("listdir", "List the directories in the repository", optionListdir=new StringBuffer(), true, false),
			    new CommandLineOption("listtrans", "List the transformations in the specified directory", optionListtrans=new StringBuffer(), true, false),
			    new CommandLineOption("listrep", "List the available repositories", optionListrep=new StringBuffer(), true, false),
		        new CommandLineOption("exprep", "Export all repository objects to one XML file", optionExprep=new StringBuffer(), true, false),
		        new CommandLineOption("norep", "Do not log into the repository", optionNorep=new StringBuffer(), true, false),
		        new CommandLineOption("safemode", "Run in safe mode: with extra checking enabled", optionSafemode=new StringBuffer(), true, false),
                new CommandLineOption("version", "show the version, revision and build date", optionVersion=new StringBuffer(), true, false),
            };

		if (args.size()==0 ) 
		{
			CommandLineOption.printUsage(options);
            System.exit(9);
		}

		// Parse the options...
		CommandLineOption.parseArguments(args, options);
		
		String kettleRepname  = Const.getEnvironmentVariable("KETTLE_REPOSITORY", null);
        String kettleUsername = Const.getEnvironmentVariable("KETTLE_USER", null);
        String kettlePassword = Const.getEnvironmentVariable("KETTLE_PASSWORD", null);
        
        if (kettleRepname !=null && kettleRepname .length()>0) optionRepname  = new StringBuffer(kettleRepname);
        if (kettleUsername!=null && kettleUsername.length()>0) optionUsername = new StringBuffer(kettleUsername);
        if (kettlePassword!=null && kettlePassword.length()>0) optionPassword = new StringBuffer(kettlePassword);
        
        LogWriter log;
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
            log.logMinimal("Pan", "Logging is at level : "+log.getLogLevelDesc());
        }
        
        if (!Const.isEmpty(optionVersion))
        {
            BuildVersion buildVersion = BuildVersion.getInstance();
            log.logBasic("Pan", "Kettle version "+Const.VERSION+", revision "+buildVersion.getRevision()+", build date : "+buildVersion.getBuildDate());
            if (a.length==1) System.exit(6);
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
		    	if (!options[i].isHiddenOption()) System.out.println(Const.rightPad(options[i].getOption(),12)+" : "+options[i].getArgument());
		    }
		    System.out.println("");
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////


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
			log.logDebug("Pan", "Starting to look at options...");
			// Read kettle transformation specified on command-line?
			if (!Const.isEmpty(optionRepname) || !Const.isEmpty(optionFilename))
			{			
				log.logDebug("Pan", "Parsing command line options.");
				if (!Const.isEmpty(optionRepname) && !"Y".equalsIgnoreCase(optionNorep.toString()))
				{
					log.logDebug("Pan", "Loading available repositories.");
					RepositoriesMeta repsinfo = new RepositoriesMeta(log);
					if (repsinfo.readData())
					{
						log.logDebug("Pan", "Finding repository ["+optionRepname+"]");
						repinfo = repsinfo.findRepository(optionRepname.toString());
						if (repinfo!=null)
						{
							// Define and connect to the repository...
							log.logDebug("Pan", "Allocate & connect to repository.");
							Repository rep = new Repository(log, repinfo, userinfo);
							if (rep.connect("Pan commandline"))
							{
								RepositoryDirectory directory = rep.getDirectoryTree(); // Default = root
								
								// Find the directory name if one is specified...
								if (!Const.isEmpty(optionDirname))
								{
									directory = rep.getDirectoryTree().findDirectory(optionDirname.toString());
								}
								
								if (directory!=null)
								{
									// Check username, password
									log.logDebug("Pan", "Check supplied username and password.");
									userinfo = new UserInfo(rep, optionUsername.toString(), optionPassword.toString());
									if (userinfo.getID()>0)
									{
										// Load a transformation
										if (!Const.isEmpty(optionTransname))
										{
											log.logDebug("Pan", "Load the transformation info...");
											transMeta = new TransMeta(rep, optionTransname.toString(), directory);
											log.logDebug("Pan", "Allocate transformation...");
											trans = new Trans(log, transMeta);
										}
										else
										// List the transformations in the repository
										if ("Y".equalsIgnoreCase(optionListtrans.toString()))
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
										if ("Y".equalsIgnoreCase(optionListdir.toString()))
										{
											String dirnames[] = rep.getDirectoryNames(directory.getID());
											for (int i=0;i<dirnames.length;i++)
											{
												System.out.println(dirnames[i]);
											}
										}
                                        else
                                        // Export the repository
                                        if (!Const.isEmpty(optionExprep))
                                        {
                                            System.out.println("Exporting all objects in the repository to file ["+optionExprep+"]");
                                            rep.exportAllObjects(null, optionExprep.toString());
                                            System.out.println("Finished exporting all objects in the repository to file ["+optionExprep+"]");
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
									System.out.println("ERROR: Can't find the specified directory ["+optionDirname+"]");
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

				// Try to load the transformation from file, even if it failed to load from the repository
                // You could implement some failover mechanism this way.
                //
				if (trans==null && !Const.isEmpty(optionFilename))
				{
                    log.logDetailed("Pan", "Loading transformation from XML file ["+optionFilename+"]");
					transMeta = new TransMeta(optionFilename.toString());
					trans = new Trans(log, transMeta);
				}
			}
			
			if ("Y".equalsIgnoreCase(optionListrep.toString()))
			{
				log.logDebug("Pan", "Getting the list of repositories...");
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
			trans=null;
			transMeta=null;
			System.out.println("Processing has stopped because of an error: "+e.getMessage());
		}

		if (trans==null)
		{
			if (!"Y".equalsIgnoreCase(optionListtrans.toString()) && 
                !"Y".equalsIgnoreCase(optionListdir.toString()) && 
                !"Y".equalsIgnoreCase(optionListrep.toString()) &&
                Const.isEmpty(optionExprep)
               )
            {
                System.out.println("ERROR: Pan can't continue because the transformation couldn't be loaded.");
            }
            System.exit(7);
		}
		
		try
		{
			// See if we want to run in safe mode:
			if ("Y".equalsIgnoreCase(optionSafemode.toString()))
			{
				trans.setSafeModeEnabled(true);
			}
			
		    // allocate & run the required sub-threads
			boolean ok = trans.execute((String[])args.toArray(new String[args.size()])); 
            if (!ok)
            {
                System.out.println("Unable to prepare and initialize this transformation");
                System.exit(3);
            }
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
			if (trans.getResult().getNrErrors()==0) 
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
