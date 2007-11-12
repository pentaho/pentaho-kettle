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

/*
 * Created on 9-apr-2003
 *
 */

package org.pentaho.di.trans.step;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.SlaveStepCopyPartitionDistribution;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.StepPluginMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

public class BaseStep extends Thread implements VariableSpace
{
	private VariableSpace variables = new Variables();
	    
    public static StepPluginMeta[] steps = null;
    
    static
    {
    	//TODO: Move this out of this class
    	synchronized(BaseStep.class)
    	{
    		try
	    	{
	    		//annotated classes first
	    		ConfigManager<?> stepsAnntCfg = KettleConfig.getInstance().getManager("steps-annotation-config");
	    		Collection<StepPluginMeta> mainSteps = stepsAnntCfg.loadAs(StepPluginMeta.class);
	    		ConfigManager<?> stepsCfg = KettleConfig.getInstance().getManager("steps-xml-config");
	    		Collection<StepPluginMeta> csteps = stepsCfg.loadAs(StepPluginMeta.class);
	    	
	    		mainSteps.addAll(csteps);
	    
	    		steps = mainSteps.toArray(new StepPluginMeta[mainSteps.size()]);
	    	}
	    	catch(KettleConfigException e)
	    	{
	    		e.printStackTrace();
	    		throw new RuntimeException(e.getMessage());
	    	}
    	}
    }
    
   /* public static final StepPluginMeta[] steps =
      {
      	TODO: port these steps

            new StepPluginMeta(WebServiceMeta.class, "WebServiceLookup", Messages.getString("BaseStep.TypeLongDesc.WebServiceLookup"), Messages.getString("BaseStep.TypeTooltipDesc.WebServiceLookup"), "WSL.png", CATEGORY_EXPERIMENTAL),
            new StepPluginMeta(FormulaMeta.class, "Formula", Messages.getString("BaseStep.TypeLongDesc.Formula"), Messages.getString("BaseStep.TypeTooltipDesc.Formula"), "FRM.png", CATEGORY_EXPERIMENTAL),
                              
      };*/

    public static final String category_order[] =
    {
        StepCategory.INPUT.getName(),
        StepCategory.OUTPUT.getName(),
        StepCategory.LOOKUP.getName(),
        StepCategory.TRANSFORM.getName(),
        StepCategory.JOINS.getName(),
        StepCategory.SCRIPTING.getName(),
        StepCategory.DATA_WAREHOUSE.getName(),
        StepCategory.MAPPING.getName(),
        StepCategory.JOB.getName(),
        StepCategory.INLINE.getName(),
        StepCategory.EXPERIMENTAL.getName(),
        StepCategory.DEPRECATED.getName(),
    };

    public static final String[] statusDesc = { 
    		Messages.getString("BaseStep.status.Empty"),
            Messages.getString("BaseStep.status.Init"), 
            Messages.getString("BaseStep.status.Running"), 
            Messages.getString("BaseStep.status.Idle"),
            Messages.getString("BaseStep.status.Finished"), 
            Messages.getString("BaseStep.status.Stopped"),
            Messages.getString("BaseStep.status.Disposed"), 
            Messages.getString("BaseStep.status.Halted"), 
            Messages.getString("BaseStep.status.Paused"), 
    	};

    private TransMeta                    transMeta;

    private StepMeta                     stepMeta;

    private String                       stepname;

    protected LogWriter                  log;

    private Trans                        trans;

    /**  nr of lines read from previous step(s) */
    public long                          linesRead;
    
    /** nr of lines written to next step(s) */
    public long                          linesWritten;
    
    /** nr of lines read from file or database */
    public long                          linesInput;
    
    /** nr of lines written to file or database */
    public long                          linesOutput;
    
    /** nr of updates in a database table or file */
    public long                          linesUpdated;
    
    /** nr of lines skipped */
    public long                          linesSkipped;
    
    /** total sleep time in ns caused by an empty input buffer (previous step is slow) */
    public long                          linesRejected;

    private boolean                      distributed;

    private long                         errors;

    private StepMeta                     nextSteps[];

    private StepMeta                     prevSteps[];

    private int                          currentInputRowSetNr, currentOutputRowSetNr;

    public List<BaseStep>                thr;

    /** The rowsets on the input, size() == nr of source steps */
    public ArrayList<RowSet> inputRowSets;

    /** the rowsets on the output, size() == nr of target steps */
    public ArrayList<RowSet> outputRowSets;
    
    /** The remote input steps. */
    public List<RemoteStep> remoteInputSteps;

    /** The remote output steps. */
    public List<RemoteStep> remoteOutputSteps;

    /** the rowset for the error rows */
    public RowSet errorRowSet;

    public AtomicBoolean                 stopped;

    public AtomicBoolean                 paused;

    public boolean                       waiting;

    public boolean                       init;

    /** the copy number of this thread */
    private int                          stepcopy;

    private Date                         start_time, stop_time;

    public boolean                       first;

    public boolean                       terminator;

    public List<Object[]>                     terminator_rows;

    private StepMetaInterface            stepMetaInterface;

    private StepDataInterface            stepDataInterface;

    /** The list of RowListener interfaces */
    private List<RowListener>                         rowListeners;

    /**
     * Map of files that are generated or used by this step. After execution, these can be added to result.
     * The entry to the map is the filename
     */
    private Map<String,ResultFile>                          resultFiles;

    /**
     * Set this to true if you want to have extra checking enabled on the rows that are entering this step. All too
     * often people send in bugs when it is really the mixing of different types of rows that is causing the problem.
     */
    private boolean                      safeModeEnabled;

    /**
     * This contains the first row received and will be the reference row. We used it to perform extra checking: see if
     * we don't get rows with "mixed" contents.
     */
    private RowMetaInterface             inputReferenceRow;

    /**
     * This field tells the putRow() method that we are in partitioned mode
     */
    private boolean                      partitioned;

    /**
     * The partition ID at which this step copy runs, or null if this step is not running partitioned.
     */
    private String                       partitionID;

    /**
     * This field tells the putRow() method to re-partition the incoming data, See also StepPartitioningMeta.PARTITIONING_METHOD_*
     */
    private int                          repartitioning;

    /**
     * The partitionID to rowset mapping
     */
    private Map<String,RowSet>                         partitionTargets;
    private RowMetaInterface inputRowMeta;

    /**
     * step partitioning information of the NEXT step
     */
    private StepPartitioningMeta  nextStepPartitioningMeta;
    
    /** The metadata information of the error output row.  There is only one per step so we cache it */
    private RowMetaInterface errorRowMeta = null;
    private RowMetaInterface previewRowMeta;

    private boolean checkTransRunning;

	private int slaveNr;

	private int clusterSize;

	private int uniqueStepNrAcrossSlaves;

	private int uniqueStepCountAcrossSlaves;

	private boolean remoteOutputStepsInitialized;

	private boolean remoteInputStepsInitialized;

	private RowSet[] partitionNrRowSetList;
	
    /** A list of server sockets that need to be closed during transformation cleanup. */
    private List<ServerSocket> serverSockets;

    private static int NR_OF_ROWS_IN_BLOCK = 100;

    private int blockPointer;
    
    
    /**
     * A flag to indicate that clustered partitioning was not yet initialized
     */
    private boolean clusteredPartitioningFirst;
    
    /**
     * A flag to determine whether or not we are doing local or clustered (remote) par
     */
    private boolean clusteredPartitioning;

    /**
     * This is the base step that forms that basis for all steps. You can derive from this class to implement your own
     * steps.
     *
     * @param stepMeta The StepMeta object to run.
     * @param stepDataInterface the data object to store temporary data, database connections, caches, result sets,
     * hashtables etc.
     * @param copyNr The copynumber for this step.
     * @param transMeta The TransInfo of which the step stepMeta is part of.
     * @param trans The (running) transformation to obtain information shared among the steps.
     */
    public BaseStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        log = LogWriter.getInstance();
        this.stepMeta = stepMeta;
        this.stepDataInterface = stepDataInterface;
        this.stepcopy = copyNr;
        this.transMeta = transMeta;
        this.trans = trans;
        this.stepname = stepMeta.getName();

        // Set the name of the thread
        if (stepMeta.getName() != null)
        {
            setName(toString() + " (" + super.getName() + ")");
        }
        else
        {
            throw new RuntimeException("A step in transformation [" + transMeta.toString()
                    + "] doesn't have a name.  A step should always have a name to identify it by.");
        }

        first = true;
        clusteredPartitioningFirst=true;
        
        stopped = new AtomicBoolean(false);;
        paused = new AtomicBoolean(false);;
        init = false;

        linesRead = 0L; // Keep some statistics!
        linesWritten = 0L;
        linesUpdated = 0L;
        linesSkipped = 0L;

        inputRowSets = null;
        outputRowSets = null;
        nextSteps = null;

        terminator = stepMeta.hasTerminator();
        if (terminator)
        {
            terminator_rows = new ArrayList<Object[]>();
        }
        else
        {
            terminator_rows = null;
        }

        // debug="-"; //$NON-NLS-1$

        start_time = null;
        stop_time = null;

        distributed = stepMeta.isDistributes();

        if (distributed) if (log.isDetailed())
            logDetailed(Messages.getString("BaseStep.Log.DistributionActivated")); //$NON-NLS-1$
        else
            if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.DistributionDeactivated")); //$NON-NLS-1$

        rowListeners = new ArrayList<RowListener>();
        resultFiles = new Hashtable<String,ResultFile>();

        repartitioning = StepPartitioningMeta.PARTITIONING_METHOD_NONE;
        partitionTargets = new Hashtable<String,RowSet>();

        serverSockets = new ArrayList<ServerSocket>();
        
        // tuning parameters
	    // putTimeOut = 10; //s
	    // getTimeOut = 500; //s
	    // timeUnit = TimeUnit.MILLISECONDS;
	    // the smaller singleWaitTime, the faster the program run but cost CPU
	    // singleWaitTime = 1; //ms
	    // maxPutWaitCount = putTimeOut*1000/singleWaitTime; 
	    // maxGetWaitCount = getTimeOut*1000/singleWaitTime; 
	    
	    //worker = Executors.newFixedThreadPool(10);
	    checkTransRunning = false;
	    
	    blockPointer = 0; 
        
        dispatch();
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepDataInterface.STATUS_INIT);

        String slaveNr = transMeta.getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER);
        String clusterSize = transMeta.getVariable(Const.INTERNAL_VARIABLE_CLUSTER_SIZE);
        if (!Const.isEmpty(slaveNr) && !Const.isEmpty(clusterSize))
        {
            this.slaveNr = Integer.parseInt(slaveNr);
            this.clusterSize = Integer.parseInt(clusterSize);
            
            if (log.isDetailed()) logDetailed("Running on slave server #"+slaveNr+"/"+clusterSize+"."); 
        }
        else
        {
            this.slaveNr = 0;
            this.clusterSize = 0;
        }

        // Also set the internal variable for the partition
        //
    	SlaveStepCopyPartitionDistribution partitionDistribution = transMeta.getSlaveStepCopyPartitionDistribution();
    	
        if (stepMeta.isPartitioned()) 
        {
        	// See if we are partitioning remotely
        	//
        	if (partitionDistribution!=null && !partitionDistribution.getDistribution().isEmpty())
        	{
	        	String slaveServerName = getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME);
	        	int stepCopyNr = stepcopy;
	        	
	        	// Look up the partition nr...
	        	// Set the partition ID (string) as well as the partition nr [0..size[
	        	//
	        	PartitionSchema partitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
	        	int partitionNr = partitionDistribution.getPartition(slaveServerName, partitionSchema.getName(), stepCopyNr);
	        	if (partitionNr>=0) {
	        		String partitionNrString = new DecimalFormat("000").format(partitionNr);
	        		setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_NR, partitionNrString);
	        		
	        		if (partitionDistribution.getOriginalPartitionSchemas()!=null) {
		        		// What is the partition schema name?
		        		//
		        		String partitionSchemaName = stepMeta.getStepPartitioningMeta().getPartitionSchema().getName();
		
		        		// Search the original partition schema in the distribution...
		        		//
		        		for (PartitionSchema originalPartitionSchema : partitionDistribution.getOriginalPartitionSchemas()) {
		        			String slavePartitionSchemaName = TransSplitter.createSlavePartitionSchemaName(originalPartitionSchema.getName());
		        			if (slavePartitionSchemaName.equals(partitionSchemaName)) {
		        				PartitionSchema schema = (PartitionSchema) originalPartitionSchema.clone();
		        				
		        				// This is the one...
		        				//
		        				if (schema.isDynamicallyDefined()) {
		        					schema.expandPartitionsDynamically(this.clusterSize, this);
		        				}
		        				
		    	        		String partID = schema.getPartitionIDs().get(partitionNr);
		    	        		setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_ID, partID);
		        				break;
		        			}
		        		}
	        		}	
	        	}
        	}
        	else 
        	{
        		// This is a locally partitioned step...
        		//
        		int partitionNr = stepcopy;
        		String partitionNrString = new DecimalFormat("000").format(partitionNr);
        		setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_NR, partitionNrString);
        		String partitionID = stepMeta.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs().get(partitionNr);
        		setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_ID, partitionID);
        	}
        }
        else if (!Const.isEmpty(partitionID))
        {
            setVariable(Const.INTERNAL_VARIABLE_STEP_PARTITION_ID, partitionID);
        }
        
        // Set a unique step number across all slave servers
        //
        //   slaveNr * nrCopies + copyNr
        //
        uniqueStepNrAcrossSlaves = this.slaveNr * getStepMeta().getCopies() + stepcopy;
        uniqueStepCountAcrossSlaves = this.clusterSize==0 ? getStepMeta().getCopies() : this.clusterSize * getStepMeta().getCopies();
        if (uniqueStepCountAcrossSlaves==0) uniqueStepCountAcrossSlaves = 1;
        
        setVariable(Const.INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER, Integer.toString(uniqueStepNrAcrossSlaves));
        setVariable(Const.INTERNAL_VARIABLE_STEP_UNIQUE_COUNT, Integer.toString(uniqueStepCountAcrossSlaves));
        setVariable(Const.INTERNAL_VARIABLE_STEP_COPYNR, Integer.toString(stepcopy));
        
        // Now that these things have been done, we also need to start a number of server sockets.
        // One for each of the remote output steps that we're going to write to.
        // 
        try
        {
        	remoteOutputSteps = new ArrayList<RemoteStep>();
	        for (RemoteStep remoteStep : stepMeta.getRemoteOutputSteps()) {
	        	// Open a server socket to allow the remote output step to connect.
	        	//
	        	RemoteStep copy = (RemoteStep) remoteStep.clone();
	        	try {
	        		copy.openServerSocket(this);
	        		if (log.isDetailed()) logDetailed("Opened a server socket connection to "+copy);
	        	}
	        	catch(Exception e) {
	            	log.logError(toString(), "Unable to open server socket during step initialisation: "+copy.toString(), e);
	            	throw new Exception(e);
	        	}
	        	remoteOutputSteps.add(copy);
	        }
        }
        catch(Exception e) {
	        for (RemoteStep remoteStep : remoteOutputSteps) {
	        	if (remoteStep.getServerSocket()!=null) {
					try {
						remoteStep.getServerSocket().close();
					} catch (IOException e1) {
			        	log.logError(toString(), "Unable to close server socket after error during step initialisation", e);
					} 
	        	}
	        }
        	return false;
        }
        
        // For the remote input steps to read from, we do the same: make a list and initialize what we can...
        //
        try
        {
        	remoteInputSteps = new ArrayList<RemoteStep>();
        	
        	if (stepMeta.isPartitioned() && getClusterSize()>1) {
        		// If the step is partitioned and clustered, we only want to take one remote input step per copy.
        		// This is where we make that selection...
        		//
        		for (int i=0;i<stepMeta.getRemoteInputSteps().size();i++) {
    	        	RemoteStep remoteStep = stepMeta.getRemoteInputSteps().get(i);
    	        	if (remoteStep.getTargetStepCopyNr()==stepcopy) {
	    	        	RemoteStep copy = (RemoteStep) remoteStep.clone();
	    	        	remoteInputSteps.add(copy);
    	        	}
    	        }
        	}
        	else {
    	        for (RemoteStep remoteStep : stepMeta.getRemoteInputSteps()) {
    	        	RemoteStep copy = (RemoteStep) remoteStep.clone();
    	        	remoteInputSteps.add(copy);
    	        }
        	}
        	
        }
        catch(Exception e) {
        	log.logError(toString(), "Unable to initialize remote input steps during step initialisation", e);
        	return false;
        }
        
        return true;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepDataInterface.STATUS_DISPOSED);
    }

    public void cleanup()
    {
		for (ServerSocket serverSocket : serverSockets)
		{
	    	try {
	    		serverSocket.close();
	    	} catch (IOException e) {
	    		log.logError(toString(), "Cleanup: Unable to close server socket ("+serverSocket.getLocalPort()+")", e);
	    	}
		}
    }

    public long getProcessed()
    {
        return linesRead;
    }

    public void setCopy(int cop)
    {
        stepcopy = cop;
    }

    /**
     * @return The steps copy number (default 0)
     */
    public int getCopy()
    {
        return stepcopy;
    }

    public long getErrors()
    {
        return errors;
    }

    public void setErrors(long e)
    {
        errors = e;
    }

    /**
     * @return Returns the linesInput.
     */
    public long getLinesInput()
    {
        return linesInput;
    }

    /**
     * @return Returns the linesOutput.
     */
    public long getLinesOutput()
    {
        return linesOutput;
    }

    /**
     * @return Returns the linesRead.
     */
    public long getLinesRead()
    {
        return linesRead;
    }

    /**
     * @return Returns the linesWritten.
     */
    public long getLinesWritten()
    {
        return linesWritten;
    }

    /**
     * @return Returns the linesUpdated.
     */
    public long getLinesUpdated()
    {
        return linesUpdated;
    }

    public String getStepname()
    {
        return stepname;
    }

    public void setStepname(String stepname)
    {
        this.stepname = stepname;
    }

    public Trans getDispatcher()
    {
        return trans;
    }

    public String getStatusDescription()
    {
        return statusDesc[getStatus()];
    }

    /**
     * @return Returns the stepMetaInterface.
     */
    public StepMetaInterface getStepMetaInterface()
    {
        return stepMetaInterface;
    }

    /**
     * @param stepMetaInterface The stepMetaInterface to set.
     */
    public void setStepMetaInterface(StepMetaInterface stepMetaInterface)
    {
        this.stepMetaInterface = stepMetaInterface;
    }

    /**
     * @return Returns the stepDataInterface.
     */
    public StepDataInterface getStepDataInterface()
    {
        return stepDataInterface;
    }

    /**
     * @param stepDataInterface The stepDataInterface to set.
     */
    public void setStepDataInterface(StepDataInterface stepDataInterface)
    {
        this.stepDataInterface = stepDataInterface;
    }

    /**
     * @return Returns the stepMeta.
     */
    public StepMeta getStepMeta()
    {
        return stepMeta;
    }

    /**
     * @param stepMeta The stepMeta to set.
     */
    public void setStepMeta(StepMeta stepMeta)
    {
        this.stepMeta = stepMeta;
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
     * @return Returns the trans.
     */
    public Trans getTrans()
    {
        return trans;
    }

    /**
     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
     * (synchronized) If distribute is true, a row is copied only once to the output rowsets, otherwise copies are sent
     * to each rowset!
     *
     * @param row The row to put to the destination rowset(s).
     * @throws KettleStepException
     */
    public void putRow(RowMetaInterface rowMeta, Object[] row) throws KettleStepException
    {
    	// Are we pausing the step? If so, stall forever...
    	//
    	while (paused.get() && !stopped.get()) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new KettleStepException(e);
			}
    	}
    	
	    // Have all threads started?
	    // Are we running yet?  If not, wait a bit until all threads have been started.
    	//
	    if(this.checkTransRunning == false){
	    	while (!trans.isRunning() && !stopped.get())
	        {
	            try { Thread.sleep(1); } catch (InterruptedException e) { }
	        }
	    	this.checkTransRunning = true;
	    }

        // call all row listeners...
        //
	    synchronized (this) {
	        for (int i = 0; i < rowListeners.size(); i++)
	        {
	            RowListener rowListener = (RowListener) rowListeners.get(i);
	            rowListener.rowWrittenEvent(rowMeta, row);
	        }
		}

        // Keep adding to terminator_rows buffer...
        //
        if (terminator && terminator_rows != null)
        {
            try
            {
                terminator_rows.add(rowMeta.cloneRow(row));
            }
            catch (KettleValueException e)
            {
                throw new KettleStepException("Unable to clone row while adding rows to the terminator rows.", e);
            }
        }
        
        // Check the remote output sets.  Do we need to initialize open any connections there?
        //
        if (!remoteOutputSteps.isEmpty()) {
        	if (!remoteOutputStepsInitialized) {
        		
        		// Set the current slave target name on all the current output steps (local)
        		//
        		for (int c=0;c<outputRowSets.size();c++) {
        			RowSet rowSet = outputRowSets.get(c);
        			rowSet.setRemoteSlaveServerName(getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME));
        			if (getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME)==null) {
        				throw new KettleStepException("Variable '"+Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME+"' is not defined.");
        			}
        		}
        		
        		// Start threads: one per remote step to funnel the data through...
        		//
        		for (RemoteStep remoteStep : remoteOutputSteps) {
        			try {
        				if (remoteStep.getTargetSlaveServerName()==null) {
            				throw new KettleStepException("The target slave server name is not defined for remote output step: "+remoteStep);
        				}
						RowSet rowSet = remoteStep.openWriterSocket();
						if (log.isDetailed()) logDetailed("Opened a writer socket to remote step: "+remoteStep);
						outputRowSets.add(rowSet);
					} catch (IOException e) {
						throw new KettleStepException("Error opening writer socket to remote step '"+remoteStep+"'", e);
					}
        		}
        		
        		remoteOutputStepsInitialized = true;
        	}
        }

	    if (outputRowSets.isEmpty())
	    {
	        // No more output rowsets!
	    	// Still update the nr of lines written.
	    	//
	    	linesWritten++;
	    	
	        return; // we're done here!
	    }

        if (stopped.get())
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
            stopAll();
            return;
        }

        // Repartitioning happens when the current step is not partitioned, but the next one is.
        // That means we need to look up the partitioning information in the next step..
        // If there are multiple steps, we need to look at the first (they should be all the same)
        // 
        switch(repartitioning)
        {
        case StepPartitioningMeta.PARTITIONING_METHOD_NONE:
        {
            if (distributed)
            {
                // Copy the row to the "next" output rowset.
                // We keep the next one in out_handling
            	//
                RowSet rs = outputRowSets.get(currentOutputRowSetNr);
                
                // Loop until we find room in the target rowset
                //
                while (!rs.putRow(rowMeta, row) && !isStopped()) 
                	;
                linesWritten++;

                // Now determine the next output rowset!
                // Only if we have more then one output...
                //
                if (outputRowSets.size() > 1)
                {
                    currentOutputRowSetNr++;
                    if (currentOutputRowSetNr >= outputRowSets.size()) currentOutputRowSetNr = 0;
                }
            }
            else
            	
            // Copy the row to all output rowsets
            //
            {
                // Copy to the row in the other output rowsets...
                for (int i = 1; i < outputRowSets.size(); i++) // start at 1
                {
                    RowSet rs = outputRowSets.get(i);
                    try
                    {
                        // Loop until we find room in the target rowset
                        //
                        while (!rs.putRow(rowMeta, rowMeta.cloneRow(row)) && !isStopped()) 
                        	;
                        linesWritten++;
                    }
                    catch (KettleValueException e)
                    {
                        throw new KettleStepException("Unable to clone row while copying rows to multiple target steps", e);
                    }
                }

                // set row in first output rowset
                //
                RowSet rs = outputRowSets.get(0);
                while (!rs.putRow(rowMeta, row) && !isStopped()) 
                	;
                linesWritten++;
            }
        }
        break;

        case StepPartitioningMeta.PARTITIONING_METHOD_SPECIAL:
            {
            	if( nextStepPartitioningMeta == null )
            	{
            		// Look up the partitioning of the next step.
            		// This is the case for non-clustered partitioning...
            		//
            		StepMeta[] nextSteps = transMeta.getNextSteps(stepMeta);
                    if (nextSteps.length>0) {
                    	nextStepPartitioningMeta = nextSteps[0].getStepPartitioningMeta();
                    }
                    
                    // TODO: throw exception if we're not partitioning yet. 
                    // For now it throws a NP Exception.
            	}
            	
                int partitionNr;
                try
                {
                	partitionNr = nextStepPartitioningMeta.getPartition(rowMeta, row);
                }
                catch (KettleException e)
                {
                    throw new KettleStepException("Unable to convert a value to integer while calculating the partition number", e);
                }

                RowSet selectedRowSet = null;
                
                if (clusteredPartitioningFirst) {
                	clusteredPartitioningFirst=false;
                	
                	// We are only running remotely if both the distribution is there AND if the distribution is actually contains something.
                	//
                	clusteredPartitioning = transMeta.getSlaveStepCopyPartitionDistribution()!=null && !transMeta.getSlaveStepCopyPartitionDistribution().getDistribution().isEmpty();
                }
                
        		// OK, we have a SlaveStepCopyPartitionDistribution in the transformation...
        		// We want to pre-calculate what rowset we're sending data to for which partition...
                // It is only valid in clustering / partitioning situations.
                // When doing a local partitioning, it is much simpler.
        		//
                if (clusteredPartitioning) {
                	
                	// This next block is only performed once for speed...
                	//
	                if (partitionNrRowSetList==null) {
	        			partitionNrRowSetList = new RowSet[outputRowSets.size()];
	        			
	        			// The distribution is calculated during transformation split
	        			// The slave-step-copy distribution is passed onto the slave transformation
	        			//
		        		SlaveStepCopyPartitionDistribution distribution = transMeta.getSlaveStepCopyPartitionDistribution();
		        		
		        		String nextPartitionSchemaName = TransSplitter.createPartitionSchemaNameFromTarget( nextStepPartitioningMeta.getPartitionSchema().getName() );
		        		
		        		for (RowSet outputRowSet : outputRowSets) {
		        			try
		        			{
		        				// Look at the pre-determined distribution, decided at "transformation split" time.
			        			//
				        		int partNr = distribution.getPartition(outputRowSet.getRemoteSlaveServerName(), nextPartitionSchemaName, outputRowSet.getDestinationStepCopy());
			        			
			        			if (partNr<0) {
			        				throw new KettleStepException("Unable to find partition using rowset data, slave="+outputRowSet.getRemoteSlaveServerName()+", partition schema="+nextStepPartitioningMeta.getPartitionSchema().getName()+", copy="+outputRowSet.getDestinationStepCopy());
			        			}
			        			partitionNrRowSetList[partNr] = outputRowSet;
		        			}
		        			catch(NullPointerException e) {
		        				throw(e);
		        			}
		        		}
	                }
                
	                // OK, now get the target partition based on the partition nr...
	                // This should be very fast
                	//
	                selectedRowSet = partitionNrRowSetList[partitionNr];
                }
                else {
                	// Local partitioning...
	                // Put the row forward to the next step according to the partition rule.
	                //
	                selectedRowSet = outputRowSets.get(partitionNr);
                }
                
                if (selectedRowSet==null) {
                	logBasic("Target rowset is not available for target partition, partitionNr="+partitionNr);
                }
                
                // logBasic("Putting row to partition #"+partitionNr);
                
                while (!selectedRowSet.putRow(rowMeta, row) && !isStopped()) 
                	;
                linesWritten++;
                
                if (log.isRowLevel())
					try {
						logRowlevel("Partitioned #"+partitionNr+" to "+selectedRowSet+", row="+rowMeta.getString(row));
					} catch (KettleValueException e) {
						throw new KettleStepException(e);
					}
            }
            break;
        case StepPartitioningMeta.PARTITIONING_METHOD_MIRROR:
            {
                // Copy always to all target steps/copies.
                // 
                for (int r = 0; r < outputRowSets.size(); r++)
                {
                    RowSet rowSet = outputRowSets.get(r);
                    while (!rowSet.putRow(rowMeta, row) && !isStopped()) 
                    	;
                }
            }
            break;
        default:
        	throw new KettleStepException("Internal error: invalid repartitioning type: " + repartitioning);
        }
    }

    /**
     * putRowTo is used to put a row in a certain specific RowSet. 
     * 
     * @param rowMeta The row meta-data to put to the destination RowSet.
     * @param row the data to put in the RowSet
     * @param rowSet the RoWset to put the row into.
     * @throws KettleStepException In case something unexpected goes wrong
     */
    public void putRowTo(RowMetaInterface rowMeta, Object[] row, RowSet rowSet) throws KettleStepException
    {
    	// Are we pausing the step? If so, stall forever...
    	//
    	while (paused.get() && !stopped.get()) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new KettleStepException(e);
			}
    	}
    	
        // call all row listeners...
        //
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = rowListeners.get(i);
            rowListener.rowWrittenEvent(rowMeta, row);
        }

        // Keep adding to terminator_rows buffer...
        if (terminator && terminator_rows != null)
        {
            try
            {
                terminator_rows.add(rowMeta.cloneRow(row));
            }
            catch (KettleValueException e)
            {
                throw new KettleStepException("Unable to clone row while adding rows to the terminator buffer", e);
            }
        }

        if (stopped.get())
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
            stopAll();
            return;
        }

        // Don't distribute or anything, only go to this rowset!
        //
        while (!rowSet.putRow(rowMeta, row) && !isStopped()) 
        	;
        linesWritten++;
    }

    public void putError(RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes) throws KettleStepException
    {
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();

        if (errorRowMeta==null)
        {
            errorRowMeta = rowMeta.clone();
            
            RowMetaInterface add = stepErrorMeta.getErrorRowMeta(nrErrors, errorDescriptions, fieldNames, errorCodes);
            errorRowMeta.addRowMeta(add);
        }
        
        Object[] errorRowData = RowDataUtil.allocateRowData(errorRowMeta.size());
        System.arraycopy(row, 0, errorRowData, 0, rowMeta.size());
        
        // Also add the error fields...
        stepErrorMeta.addErrorRowData(errorRowData, rowMeta.size(), nrErrors, errorDescriptions, fieldNames, errorCodes);
        
        // call all rowlisteners...
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.errorRowWrittenEvent(rowMeta, row);
        }

        if (errorRowSet!=null) 
        {
        	while (!errorRowSet.putRow(errorRowMeta, errorRowData) && !isStopped()) 
        		;
        	linesRejected++;
        }

        verifyRejectionRates();
    }

    private void verifyRejectionRates()
    {
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
        if (stepErrorMeta==null) return; // nothing to verify.

        // Was this one error too much?
        if (stepErrorMeta.getMaxErrors()>0 && linesRejected>stepErrorMeta.getMaxErrors())
        {
            logError(Messages.getString("BaseStep.Log.TooManyRejectedRows", Long.toString(stepErrorMeta.getMaxErrors()), Long.toString(linesRejected)));
            setErrors(1L);
            stopAll();
        }

        if ( stepErrorMeta.getMaxPercentErrors()>0 && linesRejected>0 &&
            ( stepErrorMeta.getMinPercentRows()<=0 || linesRead>=stepErrorMeta.getMinPercentRows())
            )
        {
            int pct = (int) (100 * linesRejected / linesRead );
            if (pct>stepErrorMeta.getMaxPercentErrors())
            {
                logError(Messages.getString("BaseStep.Log.MaxPercentageRejectedReached", Integer.toString(pct) ,Long.toString(linesRejected), Long.toString(linesRead)));
                setErrors(1L);
                stopAll();
            }
        }
    }

    private RowSet currentInputStream()
    {
        return inputRowSets.get(currentInputRowSetNr);
    }

    /**
     * Find the next not-finished input-stream... in_handling says which one...
     */
    private void nextInputStream()
    {
    	synchronized(inputRowSets) {
    		blockPointer=0;

	        int streams = inputRowSets.size();
	
	        // No more streams left: exit!
	        if (streams == 0) return;
	
	        // Just the one rowSet (common case)
	        if (streams == 1) currentInputRowSetNr = 0;
	        
	        // If we have some left: take the next!
	        currentInputRowSetNr++;
	        if (currentInputRowSetNr >= inputRowSets.size()) currentInputRowSetNr = 0;
    	}
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    public Object[] getRow() throws KettleException
    {
    	// Are we pausing the step? If so, stall forever...
    	//
    	while (paused.get() && !stopped.get()) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new KettleStepException(e);
			}
    	}
    	
	    if (stopped.get())
	    {
	        if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopLookingForMoreRows")); //$NON-NLS-1$
	        stopAll();
	        return null;
	    }
	    
	    // Have all threads started?
	    // Are we running yet?  If not, wait a bit until all threads have been started.
	    if (this.checkTransRunning == false) {
	    	while (!trans.isRunning() && !stopped.get())
	        {
	            try { Thread.sleep(1); } catch (InterruptedException e) { }
	        }
	    	this.checkTransRunning = true;
	    }
	    
	    // See if we need to open sockets to remote input steps...
	    //
	    openRemoteInputStepSocketsOnce();
	    
	    // If everything is finished, we can stop immediately!
	    //
	    if (inputRowSets.isEmpty())
	    {
	        return null;
	    }

	    // Do we need to switch to the next input stream?
    	if (blockPointer>=NR_OF_ROWS_IN_BLOCK) {
    		nextInputStream();
    	}
	    
        // What's the current input stream?
        RowSet inputRowSet = currentInputStream();
        
        // See if this step is receiving partitioned data...
        // In that case it might be the case that one input row set is receiving all data and
        // the other rowsets nothing. (repartitioning on the same key would do that)
        //
        // We never guaranteed that the input rows would be read one by one alternatively.
        // So in THIS particular case it is safe to just read 100 rows from one rowset, then switch to another etc.
        // We can use timeouts to switch from one to another...
        // 
        Object[] row=null;
        
    	while (row==null && !isStopped()) {
        	// Get a row from the input in row set ...
    		// Timeout almost immediately if nothing is there to read.
    		// We will then switch to the next row set to read from...
    		//
        	row = inputRowSet.getRowWait(2, TimeUnit.MILLISECONDS);
        	if (row!=null) {
        		linesRead++;
        		blockPointer++;
        	}
        	else {
        		// Try once more...
        		// If row is still empty and the row set is done, we remove the row set from
        		// the input stream and move on to the next one...
        		//
        		if (inputRowSet.isDone()) {
        			row = inputRowSet.getRowWait(2, TimeUnit.MILLISECONDS);
        			if (row==null) {
        				inputRowSets.remove(currentInputRowSetNr);
        				if (inputRowSets.isEmpty()) return null; // We're completely done.
        			}
        			else {
        				linesRead++;
        			}
        		}
        		nextInputStream();
            	inputRowSet = currentInputStream();
        	}
    	}
        
         // This rowSet is perhaps no longer giving back rows?
        //
        while (row==null && !stopped.get()) {
        	// Try the next input row set(s) until we find a row set that still has rows...
        	// The getRowFrom() method removes row sets from the input row sets list.
        	//
            if (inputRowSets.isEmpty()) return null; // We're done.
        	
        	nextInputStream();
            inputRowSet = currentInputStream();
            row = getRowFrom(inputRowSet);
        }
        
        // Also set the meta data on the first occurrence.
        //
        if (inputRowMeta==null) {
        	inputRowMeta=inputRowSet.getRowMeta();
        }
        
        if ( row != null )
        {
            // OK, before we return the row, let's see if we need to check on mixing row compositions...
            // 
            if (safeModeEnabled)
            {
                safeModeChecking(inputRowSet.getRowMeta(), inputRowMeta); // Extra checking 
                if (row.length<inputRowMeta.size()) {
                	throw new KettleException("Safe mode check noticed that the length of the row data is smaller ("+row.length+") than the row metadata size ("+inputRowMeta.size()+")");
                }
            } 
            
            for (int i = 0; i < rowListeners.size(); i++)
            {
                RowListener rowListener = (RowListener) rowListeners.get(i);
                rowListener.rowReadEvent(inputRowMeta, row);
            }
        }                

        // Check the rejection rates etc. as well.
        verifyRejectionRates();

        return row;
    }

    /**
     * Opens socket connections to the remote input steps of this step.
     * <br>This method should be used by steps that don't call getRow() first in which it is executed automatically.
     * <br><b>This method should be called before any data is read from previous steps.</b>
     * <br>This action is executed only once.
     * @throws KettleStepException
     */
    protected void openRemoteInputStepSocketsOnce() throws KettleStepException {
        if (!remoteInputSteps.isEmpty()) {
        	if (!remoteInputStepsInitialized) {
        		// Loop over the remote steps and open client sockets to them 
        		// Just be careful in case we're dealing with a partitioned clustered step.
        		// A partitioned clustered step has only one. (see dispatch())
        		// 
        		for (RemoteStep remoteStep : remoteInputSteps) {
        			try {
						RowSet rowSet = remoteStep.openReaderSocket(this);
						inputRowSets.add(rowSet);
					} catch (Exception e) {
						throw new KettleStepException("Error opening reader socket to remote step '"+remoteStep+"'", e);
					}
        		}
        		remoteInputStepsInitialized = true;
        	}
        }
	}

	protected void safeModeChecking(RowMetaInterface row) throws KettleRowException
    {
    	if (row==null) {
    		return;
    	}
    	
        if (inputReferenceRow == null)
        {
            inputReferenceRow = row.clone(); // copy it!
            
            // Check for double field names.
            // 
            String[] fieldnames = row.getFieldNames();
            Arrays.sort(fieldnames);
            for (int i=0;i<fieldnames.length-1;i++)
            {
                if (fieldnames[i].equals(fieldnames[i+1]))
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.DoubleFieldnames", fieldnames[i]));
                }
            }
        }
        else
        {
            safeModeChecking(inputReferenceRow, row);
        }
    }

    public static void safeModeChecking(RowMetaInterface referenceRowMeta, RowMetaInterface rowMeta) throws KettleRowException
    {
        // See if the row we got has the same layout as the reference row.
        // First check the number of fields
    	//
        if (referenceRowMeta.size() != rowMeta.size())
        {
            throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.VaryingSize", ""+referenceRowMeta.size(), ""+rowMeta.size(), rowMeta.toString()));
        }
        else
        {
            // Check field by field for the position of the names...
            for (int i = 0; i < referenceRowMeta.size(); i++)
            {
                ValueMetaInterface referenceValue = referenceRowMeta.getValueMeta(i);
                ValueMetaInterface compareValue = rowMeta.getValueMeta(i);

                if (!referenceValue.getName().equalsIgnoreCase(compareValue.getName()))
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.MixingLayout", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));
                }

                if (referenceValue.getType()!=compareValue.getType())
                {
                    throw new KettleRowException(Messages.getString("BaseStep.SafeMode.Exception.MixingTypes", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));               
                }
            }
        }
    }
    
    public Object[] getRowFrom(RowSet rowSet) throws KettleStepException {
        
    	// Are we pausing the step? If so, stall forever...
    	//
    	while (paused.get() && !stopped.get()) {
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new KettleStepException(e);
			}
    	}
    	
        Object[] rowData = rowSet.getRow();
        while (rowData==null && !rowSet.isDone() && !stopped.get())
        {
        	rowData=rowSet.getRow();
        }
        
        if (rowData==null && rowSet.isDone()) {
        	// Try one more time to get a row to make sure we don't get a race-condition between the get and the isDone()
        	//
        	rowData = rowSet.getRow();
        }
        
        if (stopped.get())
        {
            if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.StopLookingForMoreRows")); //$NON-NLS-1$
            stopAll();
            return null;
        }

        if (rowData==null && rowSet.isDone())
        {
        	// Try one more time...
        	//
        	rowData = rowSet.getRow();
        	if (rowData==null) {
	            inputRowSets.remove(rowSet);
	            return null;
        	}
        }
		
        linesRead++;

        // call all rowlisteners...
        //
        for (int i = 0; i < rowListeners.size(); i++)
        {
            RowListener rowListener = (RowListener) rowListeners.get(i);
            rowListener.rowReadEvent(rowSet.getRowMeta(), rowData);
        }

        return rowData;
	}
    
    public RowSet findInputRowSet(String sourceStep) {
    	return findInputRowSet(sourceStep, 0, getStepname(), getCopy());
    }

	public RowSet findInputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        for (RowSet rs : inputRowSets)
        {
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return rs;
        }
        return null;
    }
	
	public RowSet findOutputRowSet(String targetStep) {
		return findOutputRowSet(getStepname(), getCopy(), targetStep, 0);
	}

    public RowSet findOutputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        for (RowSet rs : outputRowSets)
        {
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return rs;
        }
        return null;
    }

    //
    // We have to tell the next step we're finished with
    // writing to output rowset(s)!
    //
    public void setOutputDone()
    {
        if (log.isDebug()) logDebug(Messages.getString("BaseStep.Log.OutputDone", String.valueOf(outputRowSets.size()))); //$NON-NLS-1$ //$NON-NLS-2$
        synchronized(outputRowSets)
        {
            for (int i = 0; i < outputRowSets.size(); i++)
            {
                RowSet rs = outputRowSets.get(i);
                rs.setDone();
            }
            if (errorRowSet!=null) errorRowSet.setDone();
        }
    }

    /**
     * This method finds the surrounding steps and rowsets for this base step. This steps keeps it's own list of rowsets
     * (etc.) to prevent it from having to search every time.
     */
    public void dispatch()
    {
        if (transMeta == null) { // for preview reasons, no dispatching is done!
        	return; 
        }

        StepMeta stepMeta = transMeta.findStep(stepname);

        if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.StartingBuffersAllocation")); //$NON-NLS-1$

        // How many next steps are there? 0, 1 or more??
        // How many steps do we send output to?
        int nrInput = transMeta.findNrPrevSteps(stepMeta, true);
        int nrOutput = transMeta.findNrNextSteps(stepMeta);

        inputRowSets = new ArrayList<RowSet>();
        outputRowSets = new ArrayList<RowSet>();
        errorRowSet = null;
        prevSteps = new StepMeta[nrInput];
        nextSteps = new StepMeta[nrOutput];

        currentInputRowSetNr = 0; // we start with input[0];

        if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.StepInfo", String.valueOf(nrInput), String.valueOf(nrOutput))); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < nrInput; i++)
        {
            prevSteps[i] = transMeta.findPrevStep(stepMeta, i, true); // sir.getHopFromWithTo(stepname, i);
            if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.GotPreviousStep", stepname, String.valueOf(i), prevSteps[i].getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            // Looking at the previous step, you can have either 1 rowset to look at or more then one.
            int prevCopies = prevSteps[i].getCopies();
            int nextCopies = stepMeta.getCopies();
            if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.InputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

            int nrCopies;
            int dispatchType;

            if (prevCopies == 1 && nextCopies == 1)
            {
                dispatchType = Trans.TYPE_DISP_1_1;
                nrCopies = 1;
            }
            else
            {
                if (prevCopies == 1 && nextCopies > 1)
                {
                    dispatchType = Trans.TYPE_DISP_1_N;
                    nrCopies = 1;
                }
                else
                {
                    if (prevCopies > 1 && nextCopies == 1)
                    {
                        dispatchType = Trans.TYPE_DISP_N_1;
                        nrCopies = prevCopies;
                    }
                    else
                    {
                        if (prevCopies == nextCopies)
                        {
                            dispatchType = Trans.TYPE_DISP_N_N;
                            nrCopies = 1;
                        } // > 1!
                        else
                        {
                            dispatchType = Trans.TYPE_DISP_N_M;
                            nrCopies = prevCopies;
                        }
                    }
                }
            }

            for (int c = 0; c < nrCopies; c++)
            {
                RowSet rowSet = null;
                switch (dispatchType)
                {
                case Trans.TYPE_DISP_1_1:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), 0, stepname, 0);
                    break;
                case Trans.TYPE_DISP_1_N:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), 0, stepname, getCopy());
                    break;
                case Trans.TYPE_DISP_N_1:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), c, stepname, 0);
                    break;
                case Trans.TYPE_DISP_N_N:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), getCopy(), stepname, getCopy());
                    break;
                case Trans.TYPE_DISP_N_M:
                    rowSet = trans.findRowSet(prevSteps[i].getName(), c, stepname, getCopy());
                    break;
                }
                if (rowSet != null)
                {
                    inputRowSets.add(rowSet);
                    if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.FoundInputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                	if (!prevSteps[i].isMapping() && !stepMeta.isMapping()) {
	                    logError(Messages.getString("BaseStep.Log.UnableToFindInputRowset")); //$NON-NLS-1$
	                    setErrors(1);
	                    stopAll();
	                    return;
                	}
                }
            }
        }
        // And now the output part!
        for (int i = 0; i < nrOutput; i++)
        {
            nextSteps[i] = transMeta.findNextStep(stepMeta, i);

            int prevCopies = stepMeta.getCopies();
            int nextCopies = nextSteps[i].getCopies();

            if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.OutputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

            int nrCopies;
            int dispatchType;

            if (prevCopies == 1 && nextCopies == 1)
            {
                dispatchType = Trans.TYPE_DISP_1_1;
                nrCopies = 1;
            }
            else
            {
                if (prevCopies == 1 && nextCopies > 1)
                {
                    dispatchType = Trans.TYPE_DISP_1_N;
                    nrCopies = nextCopies;
                }
                else
                {
                    if (prevCopies > 1 && nextCopies == 1)
                    {
                        dispatchType = Trans.TYPE_DISP_N_1;
                        nrCopies = 1;
                    }
                    else
                    {
                        if (prevCopies == nextCopies)
                        {
                            dispatchType = Trans.TYPE_DISP_N_N;
                            nrCopies = 1;
                        } // > 1!
                        else
                        {
                            dispatchType = Trans.TYPE_DISP_N_M;
                            nrCopies = nextCopies;
                        }
                    }
                }
            }

            for (int c = 0; c < nrCopies; c++)
            {
                RowSet rowSet = null;
                switch (dispatchType)
                {
                case Trans.TYPE_DISP_1_1:
                    rowSet = trans.findRowSet(stepname, 0, nextSteps[i].getName(), 0);
                    break;
                case Trans.TYPE_DISP_1_N:
                    rowSet = trans.findRowSet(stepname, 0, nextSteps[i].getName(), c);
                    break;
                case Trans.TYPE_DISP_N_1:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), 0);
                    break;
                case Trans.TYPE_DISP_N_N:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), getCopy());
                    break;
                case Trans.TYPE_DISP_N_M:
                    rowSet = trans.findRowSet(stepname, getCopy(), nextSteps[i].getName(), c);
                    break;
                }
                if (rowSet != null)
                {
                    outputRowSets.add(rowSet);
                    if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.FoundOutputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                	if (!stepMeta.isMapping() && !nextSteps[i].isMapping()) {
	                    logError(Messages.getString("BaseStep.Log.UnableToFindOutputRowset")); //$NON-NLS-1$
	                    setErrors(1);
	                    stopAll();
	                    return;
                	}
                }
            }
        }
        
        if (stepMeta.getTargetStepPartitioningMeta()!=null) {
        	nextStepPartitioningMeta = stepMeta.getTargetStepPartitioningMeta();
        }

        if (log.isDetailed()) logDetailed(Messages.getString("BaseStep.Log.FinishedDispatching")); //$NON-NLS-1$
    }

    public void logMinimal(String s)
    {
        log.println(LogWriter.LOG_LEVEL_MINIMAL, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logBasic(String s)
    {
        log.println(LogWriter.LOG_LEVEL_BASIC, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logError(String s)
    {
        log.println(LogWriter.LOG_LEVEL_ERROR, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logError(String s, Throwable e)
    {
    	log.logError(stepname + "." + stepcopy, s, e); //$NON-NLS-1$
    }

    public void logDetailed(String s)
    {
        log.println(LogWriter.LOG_LEVEL_DETAILED, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logDebug(String s)
    {
        log.println(LogWriter.LOG_LEVEL_DEBUG, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public void logRowlevel(String s)
    {
        log.println(LogWriter.LOG_LEVEL_ROWLEVEL, stepname + "." + stepcopy, s); //$NON-NLS-1$
    }

    public int getNextClassNr()
    {
        int ret = trans.class_nr;
        trans.class_nr++;

        return ret;
    }

    public boolean outputIsDone()
    {
        int nrstopped = 0;

        for (RowSet rs : outputRowSets)
        {
            if (rs.isDone()) nrstopped++;
        }
        return nrstopped >= outputRowSets.size();
    }

    public void stopAll()
    {
        stopped.set(true);
        trans.stopAll();
    }

    public boolean isStopped()
    {
        return stopped.get();
    }

    public boolean isPaused()
    {
        return paused.get();
    }

	public void setStopped(boolean stopped) {
		this.stopped.set(stopped);
	}

	public void setStopped(AtomicBoolean stopped) {
		this.stopped = stopped;
	}
	
	public void pauseRunning() {
		setPaused(true);
	}
	
	public void resumeRunning() {
		setPaused(false);
	}
	
	public void setPaused(boolean paused) {
		this.paused.set(paused);
	}

	public void setPaused(AtomicBoolean paused) {
		this.paused = paused;
	}

    public boolean isInitialising()
    {
        return init;
    }

    public void markStart()
    {
        Calendar cal = Calendar.getInstance();
        start_time = cal.getTime();
                
        setInternalVariables();
    }

    public void setInternalVariables()
    {
        setVariable(Const.INTERNAL_VARIABLE_STEP_NAME, stepname);
        setVariable(Const.INTERNAL_VARIABLE_STEP_COPYNR, Integer.toString(getCopy()));
    }

    public void markStop()
    {
        Calendar cal = Calendar.getInstance();
        stop_time = cal.getTime();
    }

    public long getRuntime()
    {
        long lapsed;
        if (start_time != null && stop_time == null)
        {
            Calendar cal = Calendar.getInstance();
            long now = cal.getTimeInMillis();
            long st = start_time.getTime();
            lapsed = now - st;
        }
        else
            if (start_time != null && stop_time != null)
            {
                lapsed = stop_time.getTime() - start_time.getTime();
            }
            else
            {
                lapsed = 0;
            }

        return lapsed;
    }

    public RowMetaAndData buildLog(String sname, int copynr, long lines_read, long lines_written, long lines_updated, long lines_skipped, long errors, Date start_date, Date end_date)
    {
        RowMetaInterface r = new RowMeta();
        Object[] data = new Object[9];
        int nr=0;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.Stepname"), ValueMetaInterface.TYPE_STRING)); //$NON-NLS-1$
        data[nr]=sname; 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.Copy"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(copynr); 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesReaded"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_read); 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesWritten"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_written); 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesUpdated"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_updated); 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesSkipped"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_skipped); 
        nr++;
        
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.Errors"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(errors); 
        nr++;
        
        r.addValueMeta(new ValueMeta("start_date", ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$
        data[nr]=start_date; 
        nr++;
        
        r.addValueMeta(new ValueMeta("end_date", ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$
        data[nr]=end_date; 
        nr++;
        
        return new RowMetaAndData(r, data);
    }

    public static final RowMetaInterface getLogFields(String comm)
    {
        RowMetaInterface r = new RowMeta();
        ValueMetaInterface sname = new ValueMeta(Messages.getString("BaseStep.ColumnName.Stepname"), ValueMetaInterface.TYPE_STRING); //$NON-NLS-1$ //$NON-NLS-2$
        sname.setLength(256);
        r.addValueMeta(sname);

        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.Copy"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesReaded"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesWritten"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesUpdated"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.LinesSkipped"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.Errors"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.StartDate"), ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(Messages.getString("BaseStep.ColumnName.EndDate"), ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$

        for (int i = 0; i < r.size(); i++)
        {
            r.getValueMeta(i).setOrigin(comm);
        }

        return r;
    }

    public String toString()
    {
    	if (!Const.isEmpty(partitionID)) {
    		return stepname + "." + partitionID;  //$NON-NLS-1$
    	}
    	else if (clusterSize>1) {
    		return stepname + "." + slaveNr+"."+getCopy(); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else {
    		return stepname + "." + getCopy(); //$NON-NLS-1$
    	}
    }

    public Thread getThread()
    {
        return this;
    }

    public int rowsetOutputSize()
    {
        int size = 0;
        int i;
        for (i = 0; i < outputRowSets.size(); i++)
        {
            size += outputRowSets.get(i).size();
        }

        return size;
    }

    public int rowsetInputSize()
    {
        int size = 0;
        int i;
        for (i = 0; i < inputRowSets.size(); i++)
        {
            size += inputRowSets.get(i).size();
        }

        return size;
    }

    /**
     * Create a new empty StepMeta class from the steploader
     *
     * @param stepplugin The step/plugin to use
     * @param steploader The StepLoader to load from
     * @return The requested class.
     */
    public static final StepMetaInterface getStepInfo(StepPlugin stepplugin, StepLoader steploader) throws KettleStepLoaderException
    {
        return steploader.getStepClass(stepplugin);
    }

    public static final String getIconFilename(int steptype)
    {
        return steps[steptype].getImageFileName();
    }

    /**
     * Perform actions to stop a running step. This can be stopping running SQL queries (cancel), etc. Default it
     * doesn't do anything.
     *
     * @param stepDataInterface The interface to the step data containing the connections, resultsets, open files, etc.
     * @throws KettleException in case something goes wrong
     *
     */
    public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException
    {
    }

    /**
     * Stops running operations This method is deprecated, please use the method specifying the metadata and data
     * interfaces.
     *
     * @deprecated
     */
    public void stopRunning()
    {
    }

    public void logSummary()
    {
        logBasic(Messages.getString("BaseStep.Log.SummaryInfo", String.valueOf(linesInput), String.valueOf(linesOutput), String.valueOf(linesRead), String.valueOf(linesWritten), String.valueOf(linesUpdated), String.valueOf(errors+linesRejected))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }

    public String getStepID()
    {
        if (stepMeta != null) return stepMeta.getStepID();
        return null;
    }

    /**
     * @return Returns the inputRowSets.
     */
    public List<RowSet> getInputRowSets()
    {
        return inputRowSets;
    }

    /**
     * @param inputRowSets The inputRowSets to set.
     */
    public void setInputRowSets(ArrayList<RowSet> inputRowSets)
    {
        this.inputRowSets = inputRowSets;
    }

    /**
     * @return Returns the outputRowSets.
     */
    public List<RowSet> getOutputRowSets()
    {
        return outputRowSets;
    }

    /**
     * @param outputRowSets The outputRowSets to set.
     */
    public void setOutputRowSets(ArrayList<RowSet> outputRowSets)
    {
        this.outputRowSets = outputRowSets;
    }

    /**
     * @return Returns the distributed.
     */
    public boolean isDistributed()
    {
        return distributed;
    }

    /**
     * @param distributed The distributed to set.
     */
    public void setDistributed(boolean distributed)
    {
        this.distributed = distributed;
    }

    public void addRowListener(RowListener rowListener)
    {
        rowListeners.add(rowListener);
    }

    public void removeRowListener(RowListener rowListener)
    {
        rowListeners.remove(rowListener);
    }

    public List<RowListener> getRowListeners()
    {
        return rowListeners;
    }

    public void addResultFile(ResultFile resultFile)
    {
        resultFiles.put(resultFile.getFile().toString(), resultFile);
    }

    public Map<String,ResultFile> getResultFiles()
    {
        return resultFiles;
    }

    /**
     * @return Returns true is this step is running in safe mode, with extra checking enabled...
     */
    public boolean isSafeModeEnabled()
    {
        return safeModeEnabled;
    }

    /**
     * @param safeModeEnabled set to true is this step has to be running in safe mode, with extra checking enabled...
     */
    public void setSafeModeEnabled(boolean safeModeEnabled)
    {
        this.safeModeEnabled = safeModeEnabled;
    }

    public int getStatus()
    {
        if (isPaused()) return StepDataInterface.STATUS_PAUSED;
        if (isAlive()) return StepDataInterface.STATUS_RUNNING;
        if (isStopped()) return StepDataInterface.STATUS_STOPPED;

        // Get the rest in StepDataInterface object:
        StepDataInterface sdi = trans.getStepDataInterface(stepname, stepcopy);
        if (sdi != null)
        {
            if (sdi.getStatus() == StepDataInterface.STATUS_DISPOSED && !isAlive()) return StepDataInterface.STATUS_FINISHED;
            return sdi.getStatus();
        }
        return StepDataInterface.STATUS_EMPTY;
    }

    /**
     * @return the partitionID
     */
    public String getPartitionID()
    {
        return partitionID;
    }

    /**
     * @param partitionID the partitionID to set
     */
    public void setPartitionID(String partitionID)
    {
        this.partitionID = partitionID;
    }

    /**
     * @return the partitionTargets
     */
    public Map<String,RowSet> getPartitionTargets()
    {
        return partitionTargets;
    }

    /**
     * @param partitionTargets the partitionTargets to set
     */
    public void setPartitionTargets(Map<String,RowSet> partitionTargets)
    {
        this.partitionTargets = partitionTargets;
    }

    /**
     * @return the repartitioning type
     */
    public int getRepartitioning()
    {
        return repartitioning;
    }

    /**
     * @param repartitioning the repartitioning type to set
     */
    public void setRepartitioning(int repartitioning)
    {
        this.repartitioning = repartitioning;
    }

    /**
     * @return the partitioned
     */
    public boolean isPartitioned()
    {
        return partitioned;
    }

    /**
     * @param partitioned the partitioned to set
     */
    public void setPartitioned(boolean partitioned)
    {
        this.partitioned = partitioned;
    }

    protected boolean checkFeedback(long lines)
    {
        return getTransMeta().isFeedbackShown() && (lines > 0) && (getTransMeta().getFeedbackSize() > 0)
                && (lines % getTransMeta().getFeedbackSize()) == 0;
    }

    /**
     * @return the linesRejected
     */
    public long getLinesRejected()
    {
        return linesRejected;
    }

    /**
     * @param linesRejected the linesRejected to set
     */
    public void setLinesRejected(long linesRejected)
    {
        this.linesRejected = linesRejected;
    }

    /**
     * @return the rowMeta
     */
    public RowMetaInterface getInputRowMeta()
    {
        return inputRowMeta;
    }

    /**
     * @param rowMeta the rowMeta to set
     */
    public void setInputRowMeta(RowMetaInterface rowMeta)
    {
        this.inputRowMeta = rowMeta;
    }

    /**
     * @return the errorRowMeta
     */
    public RowMetaInterface getErrorRowMeta()
    {
        return errorRowMeta;
    }

    /**
     * @param errorRowMeta the errorRowMeta to set
     */
    public void setErrorRowMeta(RowMetaInterface errorRowMeta)
    {
        this.errorRowMeta = errorRowMeta;
    }

    /**
     * @return the previewRowMeta
     */
    public RowMetaInterface getPreviewRowMeta()
    {
        return previewRowMeta;
    }

    /**
     * @param previewRowMeta the previewRowMeta to set
     */
    public void setPreviewRowMeta(RowMetaInterface previewRowMeta)
    {
        this.previewRowMeta = previewRowMeta;
    }    
    
	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}	

	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}
	
	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;		
	}

	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}
  
  /**
   * Support for CheckResultSourceInterface
   */
  public String getTypeId() {
    return this.getStepID();
  }

	/**
	 * @return the unique slave number in the cluster
	 */
	public int getSlaveNr() {
		return slaveNr;
	}

	/**
	 * @return the cluster size
	 */
	public int getClusterSize() {
		return clusterSize;
	}

	/**
	 * @return a unique step number across all slave servers: slaveNr * nrCopies + copyNr
	 */
	public int getUniqueStepNrAcrossSlaves() {
		return uniqueStepNrAcrossSlaves;
	}

	/**
	 * @return the number of unique steps across all slave servers
	 */
	public int getUniqueStepCountAcrossSlaves() {
		return uniqueStepCountAcrossSlaves;
	}

	/**
	 * @return the serverSockets
	 */
	public List<ServerSocket> getServerSockets() {
		return serverSockets;
	}

	/**
	 * @param serverSockets the serverSockets to set
	 */
	public void setServerSockets(List<ServerSocket> serverSockets) {
		this.serverSockets = serverSockets;
	}



  
}