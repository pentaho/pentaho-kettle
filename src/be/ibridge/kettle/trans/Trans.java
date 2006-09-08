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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleTransException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInitThread;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaDataCombi;
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
    public static final String REPLAY_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$

	private LogWriter log;
	private TransMeta transMeta;

    /** The job that's launching this transformation. This gives us access to the whole chain, including the parent variables, etc. */
    private Job parentJob;

	/**
	 * Indicates that we are running in preview mode...
	 */
	private boolean preview;

	/**
	 * Indicates that we want to monitor the running transformation in a GUI
	 */
	private boolean monitored;

	private Date      startDate, endDate, currentDate, logDate, depDate;
    private Date      jobStartDate, jobEndDate;
    
    private long      batchId;
    
    /** This is the batch ID that is passed from job to job to transformation, if nothing is passed, it's the transformation's batch id */
    private long      passedBatchId;

	/**
	 * An arraylist of all the rowsets
	 */
	private ArrayList rowsets;

	/**
	 * A list of all the steps
	 */
	private ArrayList steps;

	public  int class_nr;

	/**
	 * The replayDate indicates that this transformation is a replay
	 * tarnsformation for a transformation executed on replayDate. If replayDate
	 * is null, the transformation is not a replay.
	 */
	private Date replayDate;

	public final static int TYPE_DISP_1_1    = 1;
	public final static int TYPE_DISP_1_N    = 2;
	public final static int TYPE_DISP_N_1    = 3;
	public final static int TYPE_DISP_N_N    = 4;

	private String preview_steps[];
	private int    preview_sizes[];

	private boolean safeModeEnabled;

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
		this(lw, (String)null, (String)null, new String[] { Messages.getString("Trans.Dialog.Description.NoFileNamePreviewMode") }); //$NON-NLS-1$
		this.transMeta=transMeta;
		preview=true;
		preview_steps=prev_steps;
		preview_sizes=prev_sizes;
		log.logBasic(toString(), Messages.getString("Trans.Log.TransformationIsInPreviewMode")); //$NON-NLS-1$
		log.logDebug(toString(), Messages.getString("Trans.Log.NumberOfStepsToPreview",String.valueOf(transMeta.nrSteps()),String.valueOf(transMeta.nrTransHops()))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * Initialize transformation from transformation defined in memory
	 */
	public Trans(LogWriter lw, TransMeta transMeta)
	{
		this(lw, (String)null, (String)null, new String[] { Messages.getString("Trans.Dialog.Description.NoFileNamePreloadedTransformation") }); //$NON-NLS-1$
		this.transMeta=transMeta;
		preview=false;
		preview_steps=null;
		preview_sizes=null;
		log.logMinimal(toString(), Messages.getString("Trans.Log.TransformationIsPreloaded")); //$NON-NLS-1$
		log.logDebug(toString(), Messages.getString("Trans.Log.NumberOfStepsToRun",String.valueOf(transMeta.nrSteps()) ,String.valueOf(transMeta.nrTransHops()))); //$NON-NLS-1$ //$NON-NLS-2$
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
					throw new KettleException(Messages.getString("Trans.Exception.UnableToLoadTransformation",name,dirname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			else
			{
				transMeta = new TransMeta(filename);
			}
		}
		catch(KettleException e)
		{
			throw new KettleException(Messages.getString("Trans.Exception.UnableToOpenTransformation",name), e); //$NON-NLS-1$ //$NON-NLS-2$
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
     * @return true if the execution preparation went well, false if an error occurred.
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
		if (arguments!=null) transMeta.setArguments(arguments);

		/* OK, see if we need to capture the logging into a String and
		 * then put it in a database field later on.
		 * From here until the execution is finished, the log will be captured.
		 *
		 */
		if (transMeta.isLogfieldUsed())
		{
			log.startStringCapture();
			log.setString(Messages.getString("Trans.Log.Start")+Const.CR); //$NON-NLS-1$
		}

		if (transMeta.getName()==null)
		{
			if (transMeta.getFilename()!=null)
			{
				log.logMinimal(toString(), Messages.getString("Trans.Log.DispacthingStartedForFilename",transMeta.getFilename())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		{
			log.logMinimal(toString(), Messages.getString("Trans.Log.DispacthingStartedForTransformation",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (transMeta.getArguments()!=null)
		{
			log.logMinimal(toString(), Messages.getString("Trans.Log.NumberOfArgumentsDetected", String.valueOf(transMeta.getArguments().length) )); //$NON-NLS-1$
		}

		if (isSafeModeEnabled())
		{
			log.logBasic(toString(), Messages.getString("Trans.Log.SafeModeIsEnabled",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (getReplayDate() != null) {
			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(toString(), Messages.getString("Trans.Log.ThisIsAReplayTransformation") //$NON-NLS-1$
					+ df.format(getReplayDate()));
		} else {
			log.logBasic(toString(), Messages.getString("Trans.Log.ThisIsNotAReplayTransformation")); //$NON-NLS-1$
		}

		steps	 = new ArrayList();
		rowsets	 = new ArrayList();

		//
		// Sort the steps & hops for visual pleasure...
		//
		if (isMonitored() && transMeta.nrSteps()<10)
		{
			transMeta.sortStepsNatural();
			transMeta.sortHopsNatural();
		}

		ArrayList hopsteps=transMeta.getTransHopSteps(false);

		log.logDetailed(toString(), Messages.getString("Trans.Log.FoundDefferentSteps",String.valueOf(hopsteps.size())));	 //$NON-NLS-1$ //$NON-NLS-2$
		log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatingRowsets")); //$NON-NLS-1$

		// First allocate all the rowsets required!
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta stepMeta=(StepMeta)hopsteps.get(i);
			log.logDetailed(toString(), Messages.getString("Trans.Log.AllocateingRowsetsForStep",String.valueOf(i),stepMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$

			nroutput = transMeta.findNrNextSteps(stepMeta);

			for (int n=0;n<nroutput;n++)
			{
				// What's the next step?
				StepMeta nsi = transMeta.findNextStep(stepMeta, n);

				// How many times do we start the target step?
				nextcopies=nsi.getCopies();
				prevcopies=stepMeta.getCopies();
				log.logDetailed(toString(), Messages.getString("Trans.Log.copiesInfo",String.valueOf(prevcopies),String.valueOf(nextcopies))); //$NON-NLS-1$ //$NON-NLS-2$
				int disptype;
				     if (prevcopies==1 && nextcopies==1) { disptype=TYPE_DISP_1_1; nrcopies = 1; }
				else if (prevcopies==1 && nextcopies >1) { disptype=TYPE_DISP_1_N; nrcopies = nextcopies; }
				else if (prevcopies >1 && nextcopies==1) { disptype=TYPE_DISP_N_1; nrcopies = prevcopies; }
				else if (prevcopies==nextcopies)         { disptype=TYPE_DISP_N_N; nrcopies = nextcopies; } // > 1!
				else
				{
					log.logError(toString(), Messages.getString("Trans.Log.AllowedRelationships")); //$NON-NLS-1$
					log.logError(toString(), Messages.getString("Trans.Log.CannotHaveXYRelationships")); //$NON-NLS-1$
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
					log.logDetailed(toString(), Messages.getString("Trans.TransformationAllocatedNewRowset",rs.toString())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatedRowsets",String.valueOf(rowsets.size()),String.valueOf(i),stepMeta.getName())+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatingStepsAndStepData")); //$NON-NLS-1$
        
		// Allocate the steps & the data...
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta stepMeta=(StepMeta)hopsteps.get(i);
			String stepid = stepMeta.getStepID();

			log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationIsToAllocateStep",stepMeta.getName(),stepid)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// How many copies are launched of this step?
			nrcopies=stepMeta.getCopies();

            if (log.isDebug()) log.logDebug(toString(), Messages.getString("Trans.Log.StepHasNumberRowCopies",String.valueOf(nrcopies))); //$NON-NLS-1$

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

					// Possibly, enable safe mode in the steps...
					((BaseStep)step).setSafeModeEnabled(safeModeEnabled);

                    // Create the kettle variables...
                    LocalVariables.getInstance().createKettleVariables(((BaseStep)step).getName(), Thread.currentThread().getName(), true);

					// Save the step too
					combi.step = step;

					// Add to the bunch...
					steps.add(combi);

					log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationHasAllocatedANewStep",stepMeta.getName(),String.valueOf(c))); //$NON-NLS-1$ //$NON-NLS-2$
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

        log.logBasic(toString(), Messages.getString("Trans.Log.InitialisingSteps", String.valueOf(steps.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        StepInitThread initThreads[] = new StepInitThread[steps.size()];
        Thread[] threads = new Thread[steps.size()];

        // Initialize all the threads...
		for (int i=0;i<steps.size();i++)
		{
			final StepMetaDataCombi sid=(StepMetaDataCombi)steps.get(i);
            
            // Do the init code in the background!
            // Init all steps at once, but ALL steps need to finish before we can continue properly!
			initThreads[i] = new StepInitThread(sid, log);
            
            // Put it in a separate thread!
			threads[i] = new Thread(initThreads[i]);
            threads[i].setName("init of "+sid.stepname+"."+sid.copy+" ("+threads[i].getName()+")");
            threads[i].start();
		}
        
        for (int i=0; i < threads.length;i++)
        {
            try {
                threads[i].join();
            } catch(Exception ex) {
                ex.printStackTrace();
                log.logError("Error with init thread: " + ex.getMessage(), ex.getMessage());
            }
        }
        
        boolean ok = true;
        
        // All step are initialized now: see if there was one that didn't do it correctly!
        for (int i=0;i<initThreads.length;i++)
        {
            StepMetaDataCombi combi = initThreads[i].getCombi();
            if (!initThreads[i].isOk()) 
            {
                log.logError(toString(), Messages.getString("Trans.Log.StepFailedToInit", combi.stepname+"."+combi.copy));
                combi.data.setStatus(StepDataInterface.STATUS_STOPPED);
                ok=false;
            }
            else
            {
                combi.data.setStatus(StepDataInterface.STATUS_IDLE);
                log.logDetailed(toString(), Messages.getString("Trans.Log.StepInitialized", combi.stepname+"."+combi.copy));
            }
        }
        
		if (!ok)
		{
			log.logError(toString(), Messages.getString("Trans.Log.FailToInitializeAtLeastOneStep")); //$NON-NLS-1$

            // Halt the other threads as well, signal end-of-the line to the outside world...
            //
            for (int i=0;i<initThreads.length;i++)
            {
                StepMetaDataCombi combi = initThreads[i].getCombi();
                if (initThreads[i].isOk()) 
                {
                    combi.data.setStatus(StepDataInterface.STATUS_HALTED);
                }
            }
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

        log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationHasAllocated",String.valueOf(steps.size()),String.valueOf(rowsets.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

	public void logSummary(StepInterface si)
	{
		log.logBasic(si.getStepname(), Messages.getString("Trans.Log.FinishedProcessing",String.valueOf(si.getLinesInput()),String.valueOf(si.getLinesOutput()),String.valueOf(si.getLinesRead()))+Messages.getString("Trans.Log.FinishedProcessing2",String.valueOf(si.getLinesWritten()),String.valueOf(si.getLinesUpdated()),String.valueOf(si.getErrors()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
				log.logMinimal(toString(), Messages.getString("Trans.Log.TransformationEnded")); //$NON-NLS-1$
			}
			else
			{
				log.logMinimal(toString(), Messages.getString("Trans.Log.TransformationDetectedErrors")+errors+" steps with errors!"); //$NON-NLS-1$ //$NON-NLS-2$
				log.logMinimal(toString(), Messages.getString("Trans.Log.TransformationIsKillingTheOtherSteps")); //$NON-NLS-1$
				killAll();
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), Messages.getString("Trans.Log.TransformationError")+e.toString()); //$NON-NLS-1$
            log.logError(toString(), Const.getStackTracker(e)); //$NON-NLS-1$
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
		if (errors>0) log.logError(toString(), Messages.getString("Trans.Log.TransformationErrorsDetected")); //$NON-NLS-1$

		return errors;
	}

	public int getEnded()
	{
		int nrEnded=0;

		if (steps==null) return 0;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
			
            BaseStep thr=(BaseStep)sid.step;
            StepDataInterface data = sid.data;
            
			if ((thr!=null && !thr.isAlive()) ||  // Should normally not be needed anymore, status is kept in data.
                    data.getStatus()==StepDataInterface.STATUS_FINISHED || // Finished processing 
                    data.getStatus()==StepDataInterface.STATUS_HALTED ||   // Not launching because of init error
                    data.getStatus()==StepDataInterface.STATUS_STOPPED     // Stopped because of an error
                    )
            {
                nrEnded++;
            }
		}

		return nrEnded;
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

			log.logBasic(toString(), Messages.getString("Trans.Log.LookingAtStep")+thr.getStepname()); //$NON-NLS-1$
			while (thr.isAlive())
			{
				thr.stopAll();
				try
				{
					Thread.sleep(20);
				}
				catch(Exception e)
				{
					log.logError(toString(), Messages.getString("Trans.Log.TransformationErrors")+e.toString()); //$NON-NLS-1$
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

		log.logBasic(toString(), " "); //$NON-NLS-1$
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
					log.logBasic(toString(), Messages.getString("Trans.Log.ProcessSuccessfullyInfo",thr.getStepname(),"'."+thr.getCopy(),String.valueOf(proc),String.valueOf((proc/seconds)))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(toString(), Messages.getString("Trans.Log.ProcessErrorInfo",thr.getStepname(),"'."+thr.getCopy(),String.valueOf(thr.getErrors()),String.valueOf(proc),String.valueOf(proc/seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
			}
			else
			{
				if (thr.getErrors()==0)
				{
					log.logBasic(toString(), Messages.getString("Trans.Log.ProcessSuccessfullyInfo",thr.getStepname(),"'."+thr.getCopy(),String.valueOf(proc),seconds!=0 ? String.valueOf((proc/seconds)) : "-")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(toString(), Messages.getString("Trans.Log.ProcessErrorInfo2",thr.getStepname(),"'."+thr.getCopy(),String.valueOf(thr.getErrors()),String.valueOf(proc),String.valueOf(seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
	public void beginProcessing() throws KettleTransException
	{
		try
		{
			// if (preview) return true;

			currentDate = new Date();
			logDate     = new Date();
			startDate   = Const.MIN_DATE;
			endDate     = currentDate;
			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(toString(), Messages.getString("Trans.Log.TransformationCanBeReplayed") + df.format(currentDate)); //$NON-NLS-1$

            Database ldb = null;
            try
            {
				DatabaseMeta logcon = transMeta.getLogConnection();
    			if (logcon!=null)
    			{
    			    ldb = new Database(logcon);
				    log.logDetailed(toString(), Messages.getString("Trans.Log.OpeningLogConnection",""+transMeta.getLogConnection())); //$NON-NLS-1$ //$NON-NLS-2$
					ldb.connect();

					//
					// Get the date range from the logging table: from the last end_date to now. (currentDate)
					//
					Row lastr = ldb.getLastLogDate(transMeta.getLogTable(), transMeta.getName(), false, Messages.getString("Trans.Row.Status.End")); //$NON-NLS-1$
					if (lastr!=null && lastr.size()>0)
					{
						Value last = lastr.getValue(0); // #0: last enddate
						if (last!=null && !last.isNull())
						{
							startDate = last.getDate();
						    log.logDetailed(toString(), Messages.getString("Trans.Log.StartDateFound")+startDate); //$NON-NLS-1$
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
						log.logDetailed(toString(), Messages.getString("Trans.Log.LookingForMaxdateConnection",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
						DatabaseMeta maxcon = transMeta.getMaxDateConnection();
						if (maxcon!=null)
						{
							Database maxdb = new Database(maxcon);
							try
							{
							    log.logDetailed(toString(), Messages.getString("Trans.Log.OpeningMaximumDateConnection")); //$NON-NLS-1$
								maxdb.connect();

								//
								// Determine the endDate by looking at a field in a table...
								//
								String sql = "SELECT MAX("+transMeta.getMaxDateField()+") FROM "+transMeta.getMaxDateTable(); //$NON-NLS-1$ //$NON-NLS-2$
								Row r1 = maxdb.getOneRow(sql);
								if (r1!=null)
								{
									// OK, we have a value, what's the offset?
									Value maxvalue = r1.getValue(0);
									if (maxvalue!=null && !maxvalue.isNull() && maxvalue.getDate()!=null)
									{
									    log.logDetailed(toString(), Messages.getString("Trans.Log.LastDateFoundOnTheMaxdateConnection")+r1); //$NON-NLS-1$
										endDate.setTime( (long)( maxvalue.getDate().getTime() + ( transMeta.getMaxDateOffset()*1000 ) ));
									}
								}
								else
								{
								    log.logDetailed(toString(), Messages.getString("Trans.Log.NoLastDateFoundOnTheMaxdateConnection")); //$NON-NLS-1$
								}
							}
							catch(KettleException e)
							{
								throw new KettleTransException(Messages.getString("Trans.Exception.ErrorConnectingToDatabase",""+transMeta.getMaxDateConnection()), e); //$NON-NLS-1$ //$NON-NLS-2$
							}
							finally
							{
								maxdb.disconnect();
							}
						}
						else
						{
							throw new KettleTransException(Messages.getString("Trans.Exception.MaximumDateConnectionCouldNotBeFound",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}


					// Determine the last date of all dependend tables...
					// Get the maximum in depdate...
					if (transMeta.nrDependencies()>0)
					{
						log.logDetailed(toString(), Messages.getString("Trans.Log.CheckingForMaxDependencyDate")); //$NON-NLS-1$
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

									String sql = "SELECT MAX("+td.getFieldname()+") FROM "+td.getTablename(); //$NON-NLS-1$ //$NON-NLS-2$
									Row r1 = depdb.getOneRow(sql);
									if (r1!=null)
									{
										// OK, we have a row, get the result!
										Value maxvalue = r1.getValue(0);
										if (maxvalue!=null && !maxvalue.isNull() && maxvalue.getDate()!=null)
										{
											log.logDetailed(toString(), Messages.getString("Trans.Log.FoundDateFromTable",td.getTablename(),"."+td.getFieldname()," = "+maxvalue.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											if ( maxvalue.getDate().getTime() > maxdepdate.getTime())
											{
												maxdepdate=maxvalue.getDate();
											}
										}
										else
										{
											throw new KettleTransException(Messages.getString("Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
										}
									}
									else
									{
										throw new KettleTransException(Messages.getString("Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									}
								}
								catch(KettleException e)
								{
									throw new KettleTransException(Messages.getString("Trans.Exception.ErrorInDatabase",""+td.getDatabase()), e); //$NON-NLS-1$ //$NON-NLS-2$
								}
								finally
								{
									depdb.disconnect();
								}
							}
							else
							{
								throw new KettleTransException(Messages.getString("Trans.Exception.ConnectionCouldNotBeFound",""+td.getDatabase())); //$NON-NLS-1$ //$NON-NLS-2$
							}
							log.logDetailed(toString(), Messages.getString("Trans.Log.Maxdepdate")+(new Value("maxdepdate", maxdepdate)).toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
					Value id_batch = new Value("ID_BATCH", (long)1); //$NON-NLS-1$
					if (transMeta.isBatchIdUsed())
					{
						ldb.getNextValue(transMeta.getCounters(), transMeta.getLogTable(), id_batch);
						setBatchId( id_batch.getInteger() );
                        if (getPassedBatchId()<=0) 
                        {
                            setPassedBatchId(id_batch.getInteger());
                        }
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
                               getBatchId(),
                               false,
                               transMeta.getName(),
                               "start",  //$NON-NLS-1$
                               0L, 0L, 0L, 0L, 0L, 0L,
                               startDate, endDate, logDate, depDate,currentDate,
                               null
                             );
                }

            }
			catch(KettleException e)
			{
				throw new KettleTransException(Messages.getString("Trans.Exception.ErrorWritingLogRecordToTable",transMeta.getLogTable()), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			finally
			{
				if (ldb!=null) ldb.disconnect();
			}


		}
		catch(KettleException e)
		{
			throw new KettleTransException(Messages.getString("Trans.Exception.UnableToBeginProcessingTransformation"), e); //$NON-NLS-1$
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
			result.getResultFiles().addAll(rt.getResultFiles());

			if (transMeta.getReadStep()  !=null && rt.getStepname().equals(transMeta.getReadStep().getName()))   result.setNrLinesRead(result.getNrLinesRead()+ rt.linesRead);
			if (transMeta.getInputStep() !=null && rt.getStepname().equals(transMeta.getInputStep().getName()))  result.setNrLinesInput(result.getNrLinesInput() + rt.linesInput);
			if (transMeta.getWriteStep() !=null && rt.getStepname().equals(transMeta.getWriteStep().getName()))  result.setNrLinesWritten(result.getNrLinesWritten()+rt.linesWritten);
			if (transMeta.getOutputStep()!=null && rt.getStepname().equals(transMeta.getOutputStep().getName())) result.setNrLinesOutput(result.getNrLinesOutput()+rt.linesOutput);
			if (transMeta.getUpdateStep()!=null && rt.getStepname().equals(transMeta.getUpdateStep().getName())) result.setNrLinesUpdated(result.getNrLinesUpdated()+rt.linesUpdated);
		}

		result.setRows( transMeta.getResultRows() );

		return result;
	}

	//
	// Handle logging at end
	//
	public boolean endProcessing(String status) throws KettleException
	{
		if (preview) return true;

		Result result = getResult();

		logDate     = new Date();

		// Change the logging back to stream...
		String log_string = null;
		if (transMeta.isLogfieldUsed())
		{
			log_string = log.getString();
			log_string+=Const.CR+Messages.getString("Trans.Log.Status.End"); //$NON-NLS-1$
			log.setString(""); //$NON-NLS-1$
			log.endStringCapture();
		}

		DatabaseMeta logcon = transMeta.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();

				ldb.writeLogRecord
					(
						transMeta.getLogTable(),
						transMeta.isBatchIdUsed(),
						getBatchId(),
						false,
						transMeta.getName(),
						status,
						result.getNrLinesRead(),
						result.getNrLinesWritten(),
						result.getNrLinesUpdated(),
						result.getNrLinesInput()+result.getNrFilesRetrieved(),
						result.getNrLinesOutput(),
						result.getNrErrors(),
					    startDate, endDate, logDate, depDate,currentDate,
						log_string
					);
			}
			catch(Exception e)
			{
				throw new KettleException(Messages.getString("Trans.Exception.ErrorWritingLogRecordToTable")+transMeta.getLogTable()+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
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
					return false;
				}
			}
		}
		return true;
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
            if (step.getStepID().equalsIgnoreCase("MappingInput")) //$NON-NLS-1$
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
            if (step.getStepID().equalsIgnoreCase("MappingOutput")) //$NON-NLS-1$
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

	public Date getReplayDate() {
		return replayDate;
	}

	public void setReplayDate(Date replayDate) {
		this.replayDate = replayDate;
	}

	/**
	 * Turn on safe mode during running: the transformation will run slower but with more checking enabled.
	 * @param safeModeEnabled true for safe mode
	 */
	public void setSafeModeEnabled(boolean safeModeEnabled)
	{
		this.safeModeEnabled = safeModeEnabled;
	}

	/**
	 * @return Returns true if the safe mode is enabled: the transformation will run slower but with more checking enabled
	 */
	public boolean isSafeModeEnabled()
	{
		return safeModeEnabled;
	}

    /**
     * This adds a row producer to the transformation that just got set up.
     * Preferable run this BEFORE execute() but after prepareExcution()
     *
     * @param stepname The step to produce rows for
     * @param copynr The copynr of the step to produce row for (normally 0 unless you have multiple copies running)
     * @throws KettleException in case the thread/step to produce rows for could not be found.
     */
    public RowProducer addRowProducer(String stepname, int copynr) throws KettleException
    {
        StepInterface stepInterface = getStepInterface(stepname, copynr);
        if (stepInterface==null)
        {
            throw new KettleException("Unable to find thread with name "+stepname+" and copy number "+copynr);
        }

        // We are going to add an extra RowSet to this stepInterface.
        RowSet rowSet = new RowSet(transMeta.getSizeRowset());

        // Add this rowset to the list of active rowsets for the selected step
        stepInterface.getInputRowSets().add(rowSet);

        return new RowProducer(stepInterface, rowSet);
    }

    /**
     * @return Returns the parentJob.
     */
    public Job getParentJob()
    {
        return parentJob;
    }

    /**
     * @param parentJob The parentJob to set.
     */
    public void setParentJob(Job parentJob)
    {
        this.parentJob = parentJob;
    }

    public KettleVariables getKettleVariables()
    {
        KettleVariables vars = KettleVariables.getInstance();
        return vars;
    }

    /**
     * Finds the StepDataInterface (currently) associated with the specified step  
     * @param stepname The name of the step to look for
     * @param stepcopy The copy number (0 based) of the step
     * @return The StepDataInterface or null if non found.
     */
    public StepDataInterface getStepDataInterface(String stepname, int stepcopy)
    {
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
            if (sid.stepname.equals(stepname) && sid.copy==stepcopy) return sid.data;
        }
        return null;
    }

    /**
     * 
     * @return true if one or more steps are halted
     */
    public boolean hasHaltedSteps()
    {
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = (StepMetaDataCombi)steps.get(i);
            if (sid.data.getStatus()==StepDataInterface.STATUS_HALTED) return true;
        }
        return false;
    }

    public Date getJobStartDate()
    {
        return jobStartDate;
    }
    
    public Date getJobEndDate()
    {
        return jobEndDate;
    }

    /**
     * @param jobEndDate the jobEndDate to set
     */
    public void setJobEndDate(Date jobEndDate)
    {
        this.jobEndDate = jobEndDate;
    }

    /**
     * @param jobStartDate the jobStartDate to set
     */
    public void setJobStartDate(Date jobStartDate)
    {
        this.jobStartDate = jobStartDate;
    }

    /**
     * @return the jobBatchId
     */
    public long getPassedBatchId()
    {
        return passedBatchId;
    }

    /**
     * @param jobBatchId the jobBatchId to set
     */
    public void setPassedBatchId(long jobBatchId)
    {
        this.passedBatchId = jobBatchId;
    }

    /**
     * @return the batchId
     */
    public long getBatchId()
    {
        return batchId;
    }

    /**
     * @param batchId the batchId to set
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }
}


