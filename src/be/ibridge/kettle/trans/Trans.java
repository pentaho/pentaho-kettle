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
 

package be.ibridge.kettle.trans;

import java.util.ArrayList;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleTransException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.mappinginput.MappingInput;
import be.ibridge.kettle.trans.step.mappingoutput.MappingOutput;


/**
 * This class is responsible for the execution of Transformations.
 * It loads, instantiates, initializes, runs, monitors the execution of the transformation contained in the TransInfo object you feed it.
 * 
 * @author Matt
 * @since 07-04-2003
 * 
 */
public class Trans
{	
	private LogWriter log;
	private TransMeta transMeta;

	/**
	 * Indicates that we are running in preview mode...
	 */
	private boolean preview;
	
	/**
	 * Indicates that we want to monitor the running transformation in a GUI
	 */
	private boolean monitored;
	
	private Date      startDate, endDate, currentDate, logDate, depDate;
	
	/**
	 * An arraylist of all the rowsets
	 */
	private ArrayList rowsets;
	
	/**
	 * A list of all the steps
	 */
	private ArrayList steps;
		
	public  int class_nr;
	
	public final static int TYPE_DISP_1_1    = 1;
	public final static int TYPE_DISP_1_N    = 2;
	public final static int TYPE_DISP_N_1    = 3;
	public final static int TYPE_DISP_N_N    = 4;
		
	private String preview_steps[];
	private int    preview_sizes[];

	public class StepMetaDataCombi
	{
		public String stepname;
		public int    copy;
		
		public StepInterface     step;
		public StepMetaInterface meta;
		public StepDataInterface data;
	};
	
	/*
	 * Initialize new empty transformation...
	 */	
	public Trans(LogWriter lw, String file, String name, String args[])
	{
		log=lw;
		class_nr = 1;
		transMeta = new TransMeta(file, name, args);
		preview=false;
	}

	/*
	 * Initialize transformation for preview
	 */
	public Trans(LogWriter lw, TransMeta transMeta, String prev_steps[], int prev_sizes[])
	{
		this(lw, (String)null, (String)null, new String[] { "no filename, preview mode" });
		this.transMeta=transMeta;
		preview=true;
		preview_steps=prev_steps;
		preview_sizes=prev_sizes;
		log.logBasic(toString(), "Transformation is in preview mode...");
		log.logDebug(toString(), "nr of steps to preview : "+transMeta.nrSteps()+", nr of hops : "+transMeta.nrTransHops());
	}

	/*
	 * Initialize transformation from transformation defined in memory
	 */
	public Trans(LogWriter lw, TransMeta transMeta)
	{
		this(lw, (String)null, (String)null, new String[] { "no filename, preloaded transformation" });
		this.transMeta=transMeta;
		preview=false;
		preview_steps=null;
		preview_sizes=null;
		log.logBasic(toString(), "Transformation is pre-loaded from repository.");
		log.logDebug(toString(), "nr of steps to run : "+transMeta.nrSteps()+", nr of hops : "+transMeta.nrTransHops());
	}
		
	public String getName()
	{
		if (transMeta==null) return null;
		
		return transMeta.getName();
	}
	
	public void open(Repository rep, String name, String dirname, String filename)
		throws KettleException
	{
		try
		{
			if (rep!=null)
			{
				RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirname);
				if (repdir!=null)
				{
					transMeta = new TransMeta(rep, name, repdir);
				}
				else
				{
					throw new KettleException("Unable to load transformation ["+name+"] because directory could not be found: ["+dirname+"]");
				}
			}
			else
			{
				transMeta = new TransMeta(filename);
			}
		}
		catch(KettleException e)
		{
			throw new KettleException("Transformation was unable to open ["+name+"]", e);
		}
	}
	
    /**
     * Execute this transformation.
     * @return true if the execution went well, false if an error occurred.
     */
    public boolean execute(String[] arguments)
    {
        if (prepareExecution(arguments))
        {
            startThreads();
            return true;
        }
        return false;
    }
    
    
    /**
     * Prepare the execution of the transformation.
     * @param arguments the arguments to use for this transformation
     * @return true if the execution went well, false if an error occurred.
     */
    public boolean prepareExecution(String[] arguments)
    {
        RowSet    rs;
        int    nroutput;
        int    nrcopies;
        int    prevcopies;
        int    nextcopies;
        
        startDate = null;
        
        /*
         * Set the arguments on the transformation...
         */
        transMeta.setArguments(arguments);
        
        /* OK, see if we need to capture the logging into a String and 
         * then put it in a database field later on.
         * From here until the execution is finished, the log will be captured.
         * 
         */
        if (transMeta.isLogfieldUsed())
        {
            log.startStringCapture();
            log.setString("START"+Const.CR);
        }
        
        if (transMeta.getName()==null)
        {
            log.logBasic(toString(), "Dispatching started for filename ["+transMeta.getFilename()+"]");
        }
        else
        {
            log.logBasic(toString(), "Dispatching started for transformation ["+transMeta.getName()+"]");
        }
                
        if (transMeta.getArguments()!=null)
        {
            log.logBasic(toString(), "Nr of arguments detected: "+transMeta.getArguments().length);
        }
        
        steps    = new ArrayList();
        rowsets  = new ArrayList();
        
        //
        // Sort the steps & hops for visual pleasure...
        // 
        if (isMonitored())
        {
            transMeta.sortStepsNatural();
            transMeta.sortHopsNatural();
        }
        
        ArrayList hopsteps=transMeta.getTransHopSteps(false);
        
        log.logDetailed(toString(), "I found "+hopsteps.size()+" different steps to launch.");  
        log.logDetailed(toString(), "Allocating rowsets...");
        
        // First allocate all the rowsets required!
        for (int i=0;i<hopsteps.size();i++)
        {
            StepMeta stepMeta=(StepMeta)hopsteps.get(i);
            log.logDetailed(toString(), " Allocating rowsets for step "+i+" --> "+stepMeta.getName());
            
            nroutput = transMeta.findNrNextSteps(stepMeta);
    
            for (int n=0;n<nroutput;n++)
            {
                // What's the next step?
                StepMeta nsi = transMeta.findNextStep(stepMeta, n);
                
                // How many times do we start the target step?
                nextcopies=nsi.getCopies();
                prevcopies=stepMeta.getCopies();
                log.logDetailed(toString(), "  prevcopies = "+prevcopies+", nextcopies="+nextcopies);
                int disptype;
                     if (prevcopies==1 && nextcopies==1) { disptype=TYPE_DISP_1_1; nrcopies = 1; } 
                else if (prevcopies==1 && nextcopies >1) { disptype=TYPE_DISP_1_N; nrcopies = nextcopies; } 
                else if (prevcopies >1 && nextcopies==1) { disptype=TYPE_DISP_N_1; nrcopies = prevcopies; } 
                else if (prevcopies==nextcopies)         { disptype=TYPE_DISP_N_N; nrcopies = nextcopies; } // > 1!
                else 
                {
                    log.logError(toString(), "Only 1-1, 1-n, n-1 and n-n relationships are allowed!");
                    log.logError(toString(), "This means you can't have x-y relationships!");
                    return false;
                }
                
                // At least run once...
                // 
                for (int c=0;c<nrcopies;c++)
                {
                    rs=new RowSet(transMeta.getSizeRowset());
                    switch(disptype)
                    {
                    case TYPE_DISP_1_1: rs.setThreadNameFromToCopy(stepMeta.getName(), 0, nsi.getName(), 0); break; 
                    case TYPE_DISP_1_N: rs.setThreadNameFromToCopy(stepMeta.getName(), 0, nsi.getName(), c); break; 
                    case TYPE_DISP_N_1: rs.setThreadNameFromToCopy(stepMeta.getName(), c, nsi.getName(), 0); break; 
                    case TYPE_DISP_N_N: rs.setThreadNameFromToCopy(stepMeta.getName(), c, nsi.getName(), c); break; 
                    }
                    rowsets.add(rs);
                    log.logDetailed(toString(), "Transformation allocated new rowset ["+rs.toString()+"]");
                }
            }
            log.logDetailed(toString(), " Allocated "+rowsets.size()+" rowsets for step "+i+" --> "+stepMeta.getName()+" ");
        }
        
        log.logDetailed(toString(), "Allocating Steps & StepData...");
        // Allocate the steps & the data...
        for (int i=0;i<hopsteps.size();i++)
        {
            StepMeta stepMeta=(StepMeta)hopsteps.get(i);
            String stepid = stepMeta.getStepID();
            
            log.logDetailed(toString(), " Transformation is about to allocate step ["+stepMeta.getName()+"] of type ["+stepid+"]");
            
            // How many copies are launched of this step?
            nrcopies=stepMeta.getCopies(); 

            log.logDebug(toString(), "  Step has nrcopies="+nrcopies);
                     
            // At least run once...
            for (int c=0;c<nrcopies;c++)
            {
                // Make sure we haven't started it yet!
                if (!hasStepStarted(stepMeta.getName(), c))
                {
                    StepMetaDataCombi combi = new StepMetaDataCombi();
                    
                    combi.stepname = stepMeta.getName();
                    combi.copy     = c;
                    
                    // The meta-data
                    combi.meta = stepMeta.getStepMetaInterface();

                    // Allocate the step data
                    StepDataInterface data = combi.meta.getStepData();
                    combi.data = data;

                    // Allocate the step
                    StepInterface step=combi.meta.getStep(stepMeta, data, c, transMeta, this);
                    combi.step = step;
                    
                    
                    // Add to the bunch...
                    steps.add(combi);
                    
                    log.logDetailed(toString(), " Transformation has allocated a new step: ["+stepMeta.getName()+"]."+c);
                }
            }
        }
        
        // Link the threads to the rowsets
        setThreadsOnRowSets();
        
        // Now (optionally) write start log record!
        try
        {
            beginProcessing();
        }
        catch(KettleTransException kte)
        {
            log.logError(toString(), kte.getMessage());
            return false;
        }
        
        // Set preview sizes
        if (preview && preview_steps!=null)
        {
            for (int i=0;i<steps.size();i++)
            {
                StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
                
                BaseStep rt=(BaseStep)sid.step;
                for (int x=0;x<preview_steps.length;x++)
                {
                    if (preview_steps[x].equalsIgnoreCase(rt.getStepname()) && rt.getCopy()==0)
                    {
                        rt.previewSize=preview_sizes[x];
                        rt.previewBuffer=new ArrayList();
                    }
                }
            }
        }
        
        // Initialize all the threads...
        boolean ok = true;
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid=(StepMetaDataCombi)steps.get(i);
            if (sid.step.init(sid.meta, sid.data)) 
            {
                sid.data.setStatus(StepDataInterface.STATUS_IDLE);
            }
            else
            {
                sid.step.setErrors(1);
                log.logError(toString(), "Error initializing step ["+sid.step.getStepname()+"]");
                ok = false;
            }
        }
        
        if (!ok)
        {
            log.logError(toString(), "We failed to initialize at least one step.  Execution can not begin!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Start the threads prepared by prepareThreads();
     * Before you start the threads, you can add Rowlisteners to them.
     */
    public void startThreads()
    {
        // Now start all the threads...
        for (int i=0;i<steps.size();i++)
        {
            final StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
            sid.step.markStart();
            sid.step.start();
        }

        log.logDetailed(toString(), "Transformation has allocated "+steps.size()+" threads and "+rowsets.size()+" rowsets.");
    }
	
	public void logSummary(StepInterface si)
	{
		log.logBasic(si.getStepname(), "Finished processing (I="+si.getLinesInput()+", O="+si.getLinesOutput()+", R="+si.getLinesRead()+", W="+si.getLinesWritten()+", U="+si.getLinesUpdated()+", E="+si.getErrors());
	}

	//
	// Wait until all RunThreads have finished.
	// 
	public void waitUntilFinished()
	{
		int ended=0;
		int errors=0;
		
		try
		{
			while (ended!=steps.size() && errors==0)
			{
				ended=getEnded();
				errors=getErrors();
				Thread.sleep(100); // sleep 1/10th of a second
			}
			if (errors==0)
			{
				log.logBasic(toString(), "Transformation ended.");
			}
			else
			{
				log.logBasic(toString(), "Transformation detected "+errors+" steps with errors!");
				log.logBasic(toString(), "Transformation is killing the other steps!");
				killAll();
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), "Transformation error: "+e.toString());
		}
	}

	public int getErrors()
	{	
        if (steps==null) return 0;
        
		int errors=0;
		
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			if (sid.step.getErrors()!=0L) errors++;
		}
		if (errors>0) log.logError(toString(), "Errors detected!");
		
		return errors;
	}

	public int getEnded()
	{
		int ended=0;
		
		if (steps==null) return 0;
		
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep thr=(BaseStep)sid.step;
			if (thr!=null && !thr.isAlive()) ended++;
		}
		
		return ended;
	}

	
	public boolean isFinished()
	{
        if (steps==null) return false;
        
		int ended=getEnded();
		
		return ended==steps.size();
	}
	
	public void killAll()
	{
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep thr = (BaseStep)sid.step;
			
			log.logBasic(toString(), "Looking at step: "+thr.getStepname());
			while (thr.isAlive())
			{
				thr.stopAll();
				try
				{
					Thread.sleep(20);
				}
				catch(Exception e)
				{
					log.logError(toString(), "Transformation error: "+e.toString());
					return;
				}
			}
		}
	}
	
	public void printStats(int seconds)
	{
		int i;
		BaseStep thr;
		long proc;
		
		log.logBasic(toString(), " ");
		if (steps==null) return;
		
		for (i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			thr=(BaseStep)sid.step;
			proc=thr.getProcessed();
			if (seconds!=0)
			{
				if (thr.getErrors()==0)
				{
					log.logBasic(toString(), "Process '"+thr.getStepname()+"'."+thr.getCopy()+" ended successfully, processed "+proc+" lines. ("+(proc/seconds)+" lines/s)");
				}
				else
				{
					log.logError(toString(), "Process '"+thr.getStepname()+"'."+thr.getCopy()+" ended with "+thr.getErrors()+" errors after "+proc+" lines. ("+(proc/seconds)+" lines/s)");
				}							
			}
			else
			{
				if (thr.getErrors()==0)
				{
					log.logBasic(toString(), "Process '"+thr.getStepname()+"'."+thr.getCopy()+" ended successfully, processed "+proc+" lines in "+seconds+" seconds.");
				}
				else
				{
					log.logError(toString(), "Process '"+thr.getStepname()+"'."+thr.getCopy()+" ended with "+thr.getErrors()+" errors after processing "+proc+" lines in "+seconds+" seconds.");
				}							
			}
		}
	}
	
	public long getLastProcessed()
	{
		BaseStep thr;
		
		if (steps==null) return 0L;
		
		int i=steps.size()-1;
		
		if (i<=0) return 0L;
		
		StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
		thr=(BaseStep)sid.step;
		
		return thr.getProcessed();
	}
	
	//
	// Find the RowSet of a step-name.
	// 
	public RowSet findRowSet(String rowsetname)
	{
		// Start with the transformation.
		for (int i=0;i<rowsets.size();i++)
		{
			//log.logDetailed("DIS: looking for RowSet ["+rowsetname+"] in nr "+i+" of "+threads.size()+" threads...");
			RowSet rs=(RowSet)rowsets.get(i);
			if (rs.getName().equalsIgnoreCase(rowsetname)) return rs;
		}
		
		return null;
	}

	//
	// Find the RowSet of a step-name.
	// 
	public RowSet findRowSet(String from, int fromcopy, String to, int tocopy)
	{
		// Start with the transformation.
		for (int i=0;i<rowsets.size();i++)
		{
			RowSet rs=(RowSet)rowsets.get(i);
			if (rs.getOriginStepName().equalsIgnoreCase(from) &&
			    rs.getDestinationStepName().equalsIgnoreCase(to) &&
			    rs.getOriginStepCopy() == fromcopy && 
			    rs.getDestinationStepCopy()   == tocopy
			    ) return rs;
		}
		
		return null;
	}

	
	//
	// Find a step by name: if it is running, return true.
	//
	public boolean hasStepStarted(String sname, int copy)
	{
		//log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
		//log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			boolean started=(sid.stepname!=null && sid.stepname.equalsIgnoreCase(sname)) && sid.copy==copy;
			if (started) return true;
		}
		return false;
	}

	//
	// Ask all steps to stop running.
	//
	public void stopAll()
	{
		//log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
		//log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep rt=(BaseStep)sid.step;
			rt.stopAll();

			// Cancel queries etc. by force...
			StepInterface si = (StepInterface)rt;
			si.stopRunning();
		}
	}
	
	public int nrSteps()
	{
		if (steps==null) return 0;
		return steps.size();
	}
	
	public int nrActiveSteps()
	{
		if (steps==null) return 0;

		int nr = 0;
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			if ( sid.step.isAlive() ) nr++;
		}
		return nr;
	}

	public BaseStep getRunThread(int i)
	{
		if (steps==null) return null;
		StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
		return (BaseStep)sid.step;
	}
	
	public BaseStep getRunThread(String name, int copy)
	{
		if (steps==null) return null;

		int i;
		
		for( i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			if (rt.getStepname().equalsIgnoreCase(name) && rt.getCopy()==copy)
			{
				return rt;
			}
		}
		
		return null;
	}
	
	public void setThreadsOnRowSets()
	{
		int i;
		for (i=0;i<rowsets.size();i++)
		{
			RowSet rs = (RowSet)rowsets.get(i);
			BaseStep from = getRunThread(rs.getOriginStepName(), rs.getOriginStepCopy());
			BaseStep to   = getRunThread(rs.getDestinationStepName()  , rs.getDestinationStepCopy()  );
			rs.setThreadFromTo(from, to);
		}
	}

	//
	// Handle logging at start
	public void beginProcessing()
		throws KettleTransException
	{
		try
		{
			// if (preview) return true;
		
			currentDate = new Date();
			logDate     = new Date();
			startDate   = Const.MIN_DATE;
			endDate     = currentDate;
            
            Database ldb = null;
            try
            {
				DatabaseMeta logcon = transMeta.getLogConnection();
    			if (logcon!=null)
    			{
    			    ldb = new Database(logcon);
				    log.logDetailed(toString(), "Opening log connection ["+transMeta.getLogConnection()+"]");
					ldb.connect();
					
					//
					// Get the date range from the logging table: from the last end_date to now. (currentDate)
					//
					Row lastr = ldb.getLastLogDate(transMeta.getLogTable(), transMeta.getName(), false, "end");
					if (lastr!=null && lastr.size()>0)
					{
						Value last = lastr.getValue(0); // #0: last enddate
						if (last!=null && !last.isNull())
						{
							startDate = last.getDate();
						    log.logDetailed(toString(), "Start date found from previous log entry: "+startDate);
						}
					}
					
					// 
					// OK, we have a date-range.
					// However, perhaps we need to look at a table before we make a final judment?
					//
					if (transMeta.getMaxDateConnection()!=null && 
						transMeta.getMaxDateTable()!=null && transMeta.getMaxDateTable().length()>0 &&
						transMeta.getMaxDateField()!=null && transMeta.getMaxDateField().length()>0
						)
					{
						log.logDetailed(toString(), "Looking for maxdate connection: ["+transMeta.getMaxDateConnection()+"]");
						DatabaseMeta maxcon = transMeta.getMaxDateConnection();
						if (maxcon!=null)
						{
							Database maxdb = new Database(maxcon);
							try
							{
							    log.logDetailed(toString(), "Opening maximum date connection...");
								maxdb.connect();
								
								//
								// Determine the endDate by looking at a field in a table...
								//
								String sql = "SELECT MAX("+transMeta.getMaxDateField()+") FROM "+transMeta.getMaxDateTable();
								Row r1 = maxdb.getOneRow(sql);
								if (r1!=null)
								{
									// OK, we have a value, what's the offset?
									Value maxvalue = r1.getValue(0);
									if (maxvalue!=null && !maxvalue.isNull() && maxvalue.getDate()!=null)
									{
									    log.logDetailed(toString(), "Last date found on the maxDate connection: "+r1);
										endDate.setTime( (long)( maxvalue.getDate().getTime() + ( transMeta.getMaxDateOffset()*1000 ) ));
									}
								}
								else
								{
								    log.logDetailed(toString(), "No last date found on the maxDate connection!");
								}
							}
							catch(KettleException e)
							{
								throw new KettleTransException("Error connecting to database ["+transMeta.getMaxDateConnection()+"]", e);
							}
							finally
							{
								maxdb.disconnect();
							}
						}
						else
						{
							throw new KettleTransException("Maximum date connection ["+transMeta.getMaxDateConnection()+"] couldn't be found!");
						}
					}
	
	
					// Determine the last date of all dependend tables...
					// Get the maximum in depdate...
					if (transMeta.nrDependencies()>0)
					{
						log.logDetailed(toString(), "Checking for max dependency date!");
						// 
						// Maybe one of the tables where this transformation is dependend on has changed?
						// If so we need to change the start-date!
						//
						depDate = Const.MIN_DATE;
						Date maxdepdate = Const.MIN_DATE;
						if (lastr!=null && lastr.size()>0)
						{
							Value dep = lastr.getValue(1); // #1: last depdate
							if (dep!=null && !dep.isNull()) 
							{
								maxdepdate = dep.getDate();
								depDate    = dep.getDate();
							} 
						}
	
						for (int i=0;i<transMeta.nrDependencies();i++)
						{
							TransDependency td = transMeta.getDependency(i);
							DatabaseMeta depcon = td.getDatabase();
							if (depcon!=null)
							{
								Database depdb = new Database(depcon);
								try
								{
									depdb.connect();
									
									String sql = "SELECT MAX("+td.getFieldname()+") FROM "+td.getTablename();
									Row r1 = depdb.getOneRow(sql);
									if (r1!=null)
									{
										// OK, we have a row, get the result!
										Value maxvalue = r1.getValue(0);
										if (maxvalue!=null && !maxvalue.isNull() && maxvalue.getDate()!=null)
										{
											log.logDetailed(toString(), "found date from table "+td.getTablename()+"."+td.getFieldname()+" = "+maxvalue.toString());
											if ( maxvalue.getDate().getTime() > maxdepdate.getTime())
											{
												maxdepdate=maxvalue.getDate();
											}
										}
										else
										{
											throw new KettleTransException("Unable to get dependency info from ["+td.getDatabase().getName()+"."+td.getTablename()+"."+td.getFieldname()+"]");
										}
									}
									else
									{
										throw new KettleTransException("Unable to get dependency info from ["+td.getDatabase().getName()+"."+td.getTablename()+"."+td.getFieldname()+"]");
									}
								}
								catch(KettleException e)
								{
									throw new KettleTransException("Error in database ["+td.getDatabase()+"]", e);
								}
								finally
								{
									depdb.disconnect();
								}
							}
							else
							{
								throw new KettleTransException("Connection ["+td.getDatabase()+"] couldn't be found!");
							}
							log.logDetailed(toString(), "maxdepdate = "+(new Value("maxdepdate", maxdepdate)).toString());
						}
						
						// OK, so we now have the maximum depdate;
						// If it is larger, it means we have to read everything back in again.
						// Maybe something has changed that we need!
						//
						if (maxdepdate.getTime() > depDate.getTime())
						{
							depDate = maxdepdate;
							startDate = Const.MIN_DATE;						
						}
					}
					else
					{
						depDate = currentDate;
					}
					
					// See if we have to add a batch id...
					Value id_batch = new Value("ID_BATCH", (long)1);
					if (transMeta.isBatchIdUsed())
					{
						ldb.getNextValue(transMeta, transMeta.getLogTable(), id_batch);
						transMeta.setBatchId( id_batch.getInteger() );
					}
					
				}

                // OK, now we have a date-range.  See if we need to set a maximum!
                if (transMeta.getMaxDateDifference()>0.0 && // Do we have a difference specified? 
                    startDate.getTime() > Const.MIN_DATE.getTime() // Is the startdate > Minimum?
                    )
                {
                    // See if the end-date is larger then Start_date + DIFF?
                    Date maxdesired = new Date( startDate.getTime()+((long)transMeta.getMaxDateDifference()*1000) );
                    
                    // If this is the case: lower the end-date. Pick up the next 'region' next time around.
                    // We do this to limit the workload in a single update session (e.g. for large fact tables)
                    // 
                    if ( endDate.compareTo( maxdesired )>0) endDate = maxdesired;
                }

                if (logcon!=null && transMeta.getLogTable()!=null && transMeta.getName()!=null)
                {
                    ldb.writeLogRecord(transMeta.getLogTable(), 
                               transMeta.isBatchIdUsed(), 
                               transMeta.getBatchId(), 
                               false, 
                               transMeta.getName(), 
                               "start", 
                               0L, 0L, 0L, 0L, 0L, 0L, 
                               startDate, endDate, logDate, depDate,
                               null
                             );
                }
                
            }
			catch(KettleException e)
			{
				throw new KettleTransException("Error writing log record to table ["+transMeta.getLogTable()+"]", e);
			}
			finally
			{
				if (ldb!=null) ldb.disconnect();
			}
            

		}
		catch(KettleException e)
		{
			throw new KettleTransException("Unable to begin processing transformation", e);
		}
	}
	
	public Result getResult()
	{
		if (steps==null) return null;

		Result result = new Result();

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			
			result.setNrErrors(result.getNrErrors()+sid.step.getErrors());
			// System.out.println("Getresult: sid.Stepname = "+sid.stepname+", outputstep=["+transMeta.outputstep+"], lines_output="+rt.lines_output);
			
			if (transMeta.getReadStep()  !=null && rt.getStepname().equals(transMeta.getReadStep().getName()))   result.setNrLinesRead(result.getNrLinesRead()+ rt.linesRead); 
			if (transMeta.getInputStep() !=null && rt.getStepname().equals(transMeta.getInputStep().getName()))  result.setNrLinesInput(result.getNrLinesInput() + rt.linesInput); 
			if (transMeta.getWriteStep() !=null && rt.getStepname().equals(transMeta.getWriteStep().getName()))  result.setNrLinesWritten(result.getNrLinesWritten()+rt.linesWritten);
			if (transMeta.getOutputStep()!=null && rt.getStepname().equals(transMeta.getOutputStep().getName())) result.setNrLinesOutput(result.getNrLinesOutput()+rt.linesOutput);
			if (transMeta.getUpdateStep()!=null && rt.getStepname().equals(transMeta.getUpdateStep().getName())) result.setNrLinesUpdated(result.getNrLinesUpdated()+rt.linesUpdated);
		}
		
		result.rows = transMeta.getResultRows();

		return result;
	}
	
	//
	// Handle logging at end
	// TODO: perform an update in the logging table when the batch ID is known and an entry exists in the table...
	//
	public boolean endProcessing(String status)
		throws KettleException
	{
		if (preview) return true;
		
		Result result = getResult();

		logDate     = new Date();
		
		// Change the logging back to stream...
		String log_string = null;
		if (transMeta.isLogfieldUsed())
		{
			log_string = log.getString();
			log_string+=Const.CR+"END";
			log.setString("");
			log.endStringCapture();
		}
		
		DatabaseMeta logcon = transMeta.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();
				
				ldb.writeLogRecord(transMeta.getLogTable(), transMeta.isBatchIdUsed(), transMeta.getBatchId(), false, transMeta.getName(), status, 
					result.getNrLinesRead(), 
					result.getNrLinesWritten(),
					result.getNrLinesUpdated(),
					result.getNrLinesInput()+result.getNrFilesRetrieved(),
					result.getNrLinesOutput(),
					result.getNrErrors(), 
				    startDate, endDate, logDate, depDate,
					log_string
				);
			}
			catch(Exception e)
			{
				throw new KettleException("Error writing log record to table ["+transMeta.getLogTable()+"]", e);
			}
			finally
			{
				ldb.disconnect();
			}
		}
		return true;
	}
	
	public boolean previewComplete()
	{
		if (steps==null) return true;

		// if (!preview || !isFinished()) return false;
		
		for (int i=0;i<nrSteps();i++)
		{
			BaseStep rt = getRunThread(i);
			if (rt.previewSize>0)
			{
				if (rt.isAlive() && rt.previewBuffer.size() < rt.previewSize) 
				{
					// System.out.println("Step: ["+rt.getName()+"] preview not complete ["+rt.preview_buffer.size()+"/"+rt.preview_size+"]");
					return false;
				} 
			}
		}
		return true;
	}
	
	public void setSourceRows(ArrayList rows)
	{
		transMeta.setSourceRows( rows );
	}
	
	public BaseStep findRunThread(String name)
	{
		if (steps==null) return null;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			if (rt.getStepname().equalsIgnoreCase(name)) return rt;
		}
		return null;
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate()
	{
		return startDate;
	}
	
	/**
	 * @return Returns the endDate.
	 */
	public Date getEndDate()
	{
		return endDate;
	}
	
	/**
	 * @return Returns the log.
	 */
	public LogWriter getLog()
	{
		return log;
	}
	
	/**
	 * @param log The log to set.
	 */
	public void setLog(LogWriter log)
	{
		this.log = log;
	}
	
	/**
	 * @return Returns the preview.
	 */
	public boolean isPreview()
	{
		return preview;
	}
	
	/**
	 * @param preview The preview to set.
	 */
	public void setPreview(boolean preview)
	{
		this.preview = preview;
	}
	
	/**
	 * @return See if the running transformation is monitored.
	 */
	public boolean isMonitored()
	{
		return monitored;
	}
	
	/**
	 * @param monitored Indicate we want to monitor the running transformation
	 */
	public void setMonitored(boolean monitored)
	{
		this.monitored = monitored;
	}
	
	/**
	 * @return Returns the preview_sizes.
	 */
	public int[] getPreview_sizes()
	{
		return preview_sizes;
	}
	
	/**
	 * @param preview_sizes The preview_sizes to set.
	 */
	public void setPreview_sizes(int[] preview_sizes)
	{
		this.preview_sizes = preview_sizes;
	}
	
	/**
	 * @return Returns the preview_steps.
	 */
	public String[] getPreview_steps()
	{
		return preview_steps;
	}
	
	/**
	 * @param preview_steps The preview_steps to set.
	 */
	public void setPreview_steps(String[] preview_steps)
	{
		this.preview_steps = preview_steps;
	}
	
	/**
	 * @return Returns the transMeta.
	 */
	public TransMeta getTransMeta()
	{
		return transMeta;
	}
	
	/**
	 * @param transMeta The transMeta to set.
	 */
	public void setTransMeta(TransMeta transMeta)
	{
		this.transMeta = transMeta;
	}
	
	/**
	 * @return Returns the currentDate.
	 */
	public Date getCurrentDate()
	{
		return currentDate;
	}
	
	/**
	 * @return Returns the depDate.
	 */
	public Date getDepDate()
	{
		return depDate;
	}
	
	/**
	 * @return Returns the logDate.
	 */
	public Date getLogDate()
	{
		return logDate;
	}
	
	/**
	 * @return Returns the rowsets.
	 */
	public ArrayList getRowsets()
	{
		return rowsets;
	}
	
	/**
	 * @return Returns the steps.
	 */
	public ArrayList getSteps()
	{
		return steps;
	}

	public String toString()
	{
        if (transMeta==null || transMeta.getName()==null) return getClass().getName();
		return transMeta.getName();
	}

    public MappingInput findMappingInput()
    {
        // Look in threads and find the MappingInput step thread...
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi smdc = (StepMetaDataCombi) steps.get(i);
            StepInterface step = smdc.step;
            if (step.getStepID().equalsIgnoreCase("MappingInput"))
                return (MappingInput)step;
        }
        return null;
    }

    public MappingOutput findMappingOutput()
    {
        // Look in threads and find the MappingInput step thread...
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi smdc = (StepMetaDataCombi) steps.get(i);
            StepInterface step = smdc.step;
            if (step.getStepID().equalsIgnoreCase("MappingOutput"))
                return (MappingOutput)step;
        }
        return null;
    }

    /**
     * Return the preview rows buffer of a step 
     * @param stepname the name of the step to preview
     * @param copyNr the step copy number
     * @return an ArrayList of rows
     */
    public ArrayList getPreviewRows(String stepname, int copyNr)
    {
        BaseStep baseStep = getRunThread(stepname, copyNr);
        if (baseStep!=null)
        {
            return baseStep.previewBuffer;
        }
        return null;
    }
    
    /**
     * Find the StepInterface (thread) by looking it up using the name
     * @param stepname The name of the step to look for
     * @param copy the copy number of the step to look for
     * @return the StepInterface or null if nothing was found.
     */
    public StepInterface getStepInterface(String stepname, int copy)
    {
        // Now start all the threads...
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
            if (sid.stepname.equalsIgnoreCase(stepname) && sid.copy==copy)
            {
                return sid.step;
            }
        }

        return null;
    }

}


