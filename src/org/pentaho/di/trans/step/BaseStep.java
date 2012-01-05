/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.step;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.SlaveStepCopyPartitionDistribution;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.steps.mapping.Mapping;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.di.www.SocketRepository;

public class BaseStep implements VariableSpace, StepInterface, LoggingObjectInterface
{
	private static Class<?> PKG = BaseStep.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private VariableSpace variables = new Variables();
	
    private TransMeta                    transMeta;

    private StepMeta                     stepMeta;

    private String                       stepname;

    protected LogChannelInterface        log;
    
    private LogLevel                     logLevel;

    private String containerObjectId;

    private Trans                        trans;

    private Object statusCountersLock = new Object();
    
    /**  nr of lines read from previous step(s)
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesRead;
    
    /** nr of lines written to next step(s)
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesWritten;
    
    /** nr of lines read from file or database
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesInput;
    
    /** nr of lines written to file or database
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesOutput;
    
    /** nr of updates in a database table or file
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesUpdated;
    
    /** nr of lines skipped
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesSkipped;
    
    /** total sleep time in ns caused by an empty input buffer (previous step is slow)
     * @deprecated please use the supplied getters, setters and increment/decrement methods 
     */
    public long                          linesRejected;

    
    private boolean                      distributed;

    private long                         errors;

    private StepMeta                     nextSteps[];

    private StepMeta                     prevSteps[];

    private int                          currentInputRowSetNr, currentOutputRowSetNr;

    /** The rowsets on the input, size() == nr of source steps */
    private List<RowSet> inputRowSets;

    /** the rowsets on the output, size() == nr of target steps */
    private List<RowSet> outputRowSets;
    
    /** The remote input steps. */
    private List<RemoteStep> remoteInputSteps;

    /** The remote output steps. */
    private List<RemoteStep> remoteOutputSteps;

    /** the rowset for the error rows */
    private RowSet errorRowSet;

    private AtomicBoolean                 running;

    private AtomicBoolean                 stopped;

    private AtomicBoolean                 paused;
    
    private boolean                       init;

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
    private Map<String,BlockingRowSet>                         partitionTargets;
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

    private static int NR_OF_ROWS_IN_BLOCK = 500;

    private int blockPointer;
    
    
    /**
     * A flag to indicate that clustered partitioning was not yet initialized
     */
    private boolean clusteredPartitioningFirst;
    
    /**
     * A flag to determine whether or not we are doing local or clustered (remote) par
     */
    private boolean clusteredPartitioning;

	private boolean usingThreadPriorityManagment;
	
	private List<StepListener> stepListeners;
	
	/** The socket repository to use when opening server side sockets in clustering mode */
	private SocketRepository socketRepository;
	
	/** The upper buffer size boundary after which we manage the thread priority a little bit to prevent excessive locking */
	private int upperBufferBoundary;
	
	/** The lower buffer size boundary after which we manage the thread priority a little bit to prevent excessive locking */
	private int lowerBufferBoundary;

	/** maximum number of errors to allow */
	private Long maxErrors = -1L;
	
	/** maximum percent of errors to allow */
	private int maxPercentErrors = -1;
	
	/** minumum number of rows to process before using maxPercentErrors in calculation */
	private long minRowsForMaxErrorPercent = -1L;
	
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
        this.stepMeta = stepMeta;
        this.stepDataInterface = stepDataInterface;
        this.stepcopy = copyNr;
        this.transMeta = transMeta;
        this.trans = trans;
        this.stepname = stepMeta.getName();
        this.socketRepository = trans.getSocketRepository();
        
        // Set the name of the thread
        if (stepMeta.getName() == null)
        {
            throw new RuntimeException("A step in transformation [" + transMeta.toString() + "] doesn't have a name.  A step should always have a name to identify it by.");
        }

        log = new LogChannel(this, trans);
        logLevel = log.getLogLevel();
        
        first = true;
        clusteredPartitioningFirst=true;
        
        running = new AtomicBoolean(false);
        stopped = new AtomicBoolean(false);
        paused = new AtomicBoolean(false);
        
        init = false;

        synchronized(statusCountersLock) {
            linesRead = 0L; // new AtomicLong(0L); // Keep some statistics!
            linesWritten = 0L; // new AtomicLong(0L);
            linesUpdated = 0L; // new AtomicLong(0L);
            linesSkipped = 0L; // new AtomicLong(0L);
            linesRejected = 0L; // new AtomicLong(0L);
            linesInput = 0L; // new AtomicLong(0L);
            linesOutput = 0L; //new AtomicLong(0L);
        }
        
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
            logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.DistributionActivated")); //$NON-NLS-1$
        else
            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.DistributionDeactivated")); //$NON-NLS-1$

        rowListeners = new ArrayList<RowListener>();
        resultFiles = new Hashtable<String,ResultFile>();

        repartitioning = StepPartitioningMeta.PARTITIONING_METHOD_NONE;
        partitionTargets = new Hashtable<String,BlockingRowSet>();

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
	    
	    stepListeners = new ArrayList<StepListener>();
        
        dispatch();
        
        upperBufferBoundary = (int)(transMeta.getSizeRowset() * 0.99);
        lowerBufferBoundary = (int)(transMeta.getSizeRowset() * 0.01);
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepExecutionStatus.STATUS_INIT);
        
        String slaveNr = transMeta.getVariable(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER);
        String clusterSize = transMeta.getVariable(Const.INTERNAL_VARIABLE_CLUSTER_SIZE);
        boolean master = "Y".equalsIgnoreCase(transMeta.getVariable(Const.INTERNAL_VARIABLE_CLUSTER_MASTER));
        
        if (!Const.isEmpty(slaveNr) && !Const.isEmpty(clusterSize) && !master)
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
        uniqueStepCountAcrossSlaves = this.clusterSize<=1 ? getStepMeta().getCopies() : this.clusterSize * getStepMeta().getCopies();
        if (uniqueStepCountAcrossSlaves==0) uniqueStepCountAcrossSlaves = 1;
        
        setVariable(Const.INTERNAL_VARIABLE_STEP_UNIQUE_NUMBER, Integer.toString(uniqueStepNrAcrossSlaves));
        setVariable(Const.INTERNAL_VARIABLE_STEP_UNIQUE_COUNT, Integer.toString(uniqueStepCountAcrossSlaves));
        setVariable(Const.INTERNAL_VARIABLE_STEP_COPYNR, Integer.toString(stepcopy));
        
        // Now that these things have been done, we also need to start a number of server sockets.
        // One for each of the remote output steps that we're going to write to.
        // 
        try
        {
			// If this is on the master, separate logic applies.
			//
			// boolean isMaster = "Y".equalsIgnoreCase(getVariable(Const.INTERNAL_VARIABLE_CLUSTER_MASTER));

        	remoteOutputSteps = new ArrayList<RemoteStep>();
	        for (int i=0;i<stepMeta.getRemoteOutputSteps().size();i++) {
	        	RemoteStep remoteStep = stepMeta.getRemoteOutputSteps().get(i);
	        	
	        	// If the step run in multiple copies, we only want to open every socket once.
	        	// 
				if (getCopy()==remoteStep.getSourceStepCopyNr()) { 
		        	// Open a server socket to allow the remote output step to connect.
		        	// 
		        	RemoteStep copy = (RemoteStep) remoteStep.clone();
		        	try {
		        		if (log.isDetailed()) logDetailed("Selected remote output step ["+copy+"] to open a server socket to remote step ["+copy.getTargetStep()+"]."+copy.getTargetStepCopyNr()+" on port "+copy.getPort());
		        		copy.openServerSocket(this);
		        		if (log.isDetailed()) logDetailed("Opened a server socket connection to "+copy);
		        	}
		        	catch(Exception e) {
		            	logError("Unable to open server socket during step initialisation: "+copy.toString(), e);
		            	throw e;
		        	}
		        	remoteOutputSteps.add(copy);
				}
	        }
        }
        catch(Exception e) {
	        for (RemoteStep remoteStep : remoteOutputSteps) {
	        	if (remoteStep.getServerSocket()!=null) {
					try {
						ServerSocket serverSocket = remoteStep.getServerSocket();
						getTrans().getSocketRepository().releaseSocket(serverSocket.getLocalPort());
					} catch (IOException e1) {
			        	logError("Unable to close server socket after error during step initialisation", e);
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
        	
        	if ((stepMeta.isPartitioned()  && getClusterSize()>1) || stepMeta.getCopies() > 1) {
        		// If the step is partitioned or has multiple copies and clustered, we only want to take one remote input step per copy.
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
        	logError("Unable to initialize remote input steps during step initialisation", e);
        	return false;
        }
        
        //  Getting ans setting the error handling values
        //    first, get the step meta
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
        if (stepErrorMeta !=null) { 
      
           //  do an environment substitute for stepErrorMeta.getMaxErrors(), stepErrorMeta.getMinPercentRows()
           //  and stepErrorMeta.getMaxPercentErrors()
           //  Catch NumberFormatException since the user can enter anything in the dialog- the value
           //  they enter must be a number or a variable set to a number
           
           //  We will use a boolean to indicate failure so that we can log all errors - not just the first one caught
           boolean envSubFailed = false;
           try {
              maxErrors = (!Const.isEmpty(stepErrorMeta.getMaxErrors()) ? Long.valueOf(trans.environmentSubstitute(stepErrorMeta.getMaxErrors())) : -1L);
           }
           catch (NumberFormatException nfe) {
              log.logError(BaseMessages.getString(PKG, "BaseStep.Log.NumberFormatException", 
                           BaseMessages.getString(PKG, "BaseStep.Property.MaxErrors.Name"), 
                           this.stepname, (stepErrorMeta.getMaxErrors()!=null?stepErrorMeta.getMaxErrors():"")));
              envSubFailed = true;
           }
           
           try {
              minRowsForMaxErrorPercent = (!Const.isEmpty(stepErrorMeta.getMinPercentRows()) ? Long.valueOf(trans.environmentSubstitute(stepErrorMeta.getMinPercentRows())) : -1L);
           }
           catch (NumberFormatException nfe) {
              log.logError(BaseMessages.getString(PKG, "BaseStep.Log.NumberFormatException", 
                           BaseMessages.getString(PKG, "BaseStep.Property.MinRowsForErrorsPercentCalc.Name"), 
                           this.stepname, (stepErrorMeta.getMinPercentRows()!=null?stepErrorMeta.getMinPercentRows():"")));
              envSubFailed = true;
           }
           
           try {
              maxPercentErrors = (!Const.isEmpty(stepErrorMeta.getMaxPercentErrors()) ? Integer.valueOf(trans.environmentSubstitute(stepErrorMeta.getMaxPercentErrors())) : -1);
           }
           catch (NumberFormatException nfe) {
              log.logError(BaseMessages.getString(PKG, "BaseStep.Log.NumberFormatException",
                           BaseMessages.getString(PKG, "BaseStep.Property.MaxPercentErrors.Name"), 
                           this.stepname, (stepErrorMeta.getMaxPercentErrors()!=null?stepErrorMeta.getMaxPercentErrors():"")));
              envSubFailed = true;
           }
           
           //  if we failed and environment subsutitue
           if (envSubFailed) {
              return false;
           }
        }
        
        return true;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        sdi.setStatus(StepExecutionStatus.STATUS_DISPOSED);
    }

    public void cleanup() {
      for (ServerSocket serverSocket : serverSockets) {
        try {
  
          socketRepository.releaseSocket(serverSocket.getLocalPort());
  
          logDetailed("Released server socket on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
          logError("Cleanup: Unable to release server socket (" + serverSocket.getLocalPort() + ")", e);
        }
      }
      
      for (RemoteStep remoteStep : getRemoteInputSteps()) {
        remoteStep.cleanup();
      }
      for (RemoteStep remoteStep : getRemoteOutputSteps()) {
        remoteStep.cleanup();
      }
    }

    public long getProcessed()
    {
    	if (getLinesRead()>getLinesWritten()) {
    		return getLinesRead();
    	} else {
    		return getLinesWritten();
    	}
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
     * @return Returns the number of lines read from previous steps
     */
    public long getLinesRead()
    {
        synchronized(statusCountersLock) {
            return linesRead;
        }
    }
    
    /**
     * Increments the number of lines read from previous steps by one
     * @return Returns the new value
     */
    public long incrementLinesRead()
    {
        synchronized(statusCountersLock) {
            return ++linesRead;
        }
    }
    
    
    /**
     * Decrements the number of lines read from previous steps by one
     * @return Returns the new value
     */
    public long decrementLinesRead()
    {
        synchronized(statusCountersLock) {
            return --linesRead;
        }
    }
    
    /**
     * @param newLinesReadValue the new number of lines read from previous steps
     */
    public void setLinesRead(long newLinesReadValue)
    {
        synchronized(statusCountersLock) {
            linesRead = newLinesReadValue;
        }
    }
    
    /**
     * @return Returns the number of lines read from an input source: database, file, socket, etc.
     */
    public long getLinesInput()
    {
        synchronized(statusCountersLock) {
            return linesInput;
        }
    }
    
    /**
     * Increments the number of lines read from an input source: database, file, socket, etc.
     * @return the new incremented value
     */
    public long incrementLinesInput()
    {
        synchronized(statusCountersLock) {
            return ++linesInput;
        }
    }
    
    /**
     * @param newLinesInputValue the new number of lines read from an input source: database, file, socket, etc.
     */
    public void setLinesInput(long newLinesInputValue)
    {
        synchronized(statusCountersLock) {
            linesInput = newLinesInputValue;
        }
    }

    /**
     * @return Returns the number of lines written to an output target: database, file, socket, etc.
     */
    public long getLinesOutput()
    {
        synchronized(statusCountersLock) {
            return linesOutput;
        }
    }
    
    /**
     * Increments the number of lines written to an output target: database, file, socket, etc.
     * @return the new incremented value
     */
    public long incrementLinesOutput()
    {
        synchronized(statusCountersLock) {
            return ++linesOutput;
        }
    }
    
    /**
     * @param newLinesOutputValue the new number of lines written to an output target: database, file, socket, etc.
     */
    public void setLinesOutput(long newLinesOutputValue)
    {
        synchronized(statusCountersLock) {
            linesOutput = newLinesOutputValue;
        }
    }

    /**
     * @return Returns the linesWritten.
     */
    public long getLinesWritten()
    {
        synchronized(statusCountersLock) {
            return linesWritten;
        }
    }
    
    /**
     * Increments the number of lines written to next steps by one
     * @return Returns the new value
     */
    public long incrementLinesWritten()
    {
        synchronized(statusCountersLock) {
            return ++linesWritten;
        }
    }

    /**
     * Decrements the number of lines written to next steps by one
     * @return Returns the new value
     */
    public long decrementLinesWritten()
    {
        synchronized(statusCountersLock) {
            return --linesWritten;
        }
    }

    /**
     * @param newLinesWrittenValue the new number of lines written to next steps
     */
    public void setLinesWritten(long newLinesWrittenValue)
    {
        synchronized(statusCountersLock) {
            linesWritten = newLinesWrittenValue;
        }
    }

    /**
     * @return Returns the number of lines updated in an output target: database, file, socket, etc.
     */
    public long getLinesUpdated()
    {
        synchronized(statusCountersLock) {
            return linesUpdated;
        }
    }
    
    /**
     * Increments the number of lines updated in an output target: database, file, socket, etc.
     * @return the new incremented value
     */
    public long incrementLinesUpdated()
    {
        synchronized(statusCountersLock) {
            return ++linesUpdated;
        }
    }
    
    /**
     * @param newLinesOutputValue the new number of lines updated in an output target: database, file, socket, etc.
     */
    public void setLinesUpdated(long newLinesUpdatedValue)
    {
        synchronized(statusCountersLock) {
            linesUpdated = newLinesUpdatedValue;
        }
    }

    /**
     * @return the number of lines rejected to an error handling step
     */
    public long getLinesRejected()
    {
        synchronized(statusCountersLock) {
            return linesRejected;
        }
    }
    
    /**
     * Increments the number of lines rejected to an error handling step
     * @return the new incremented value
     */
    public long incrementLinesRejected()
    {
        synchronized(statusCountersLock) {
            return ++linesRejected;
        }
    }

    /**
     * @param newLinesRejectedValue lines number of lines rejected to an error handling step
     */
    public void setLinesRejected(long newLinesRejectedValue)
    {
        synchronized(statusCountersLock) {
            linesRejected = newLinesRejectedValue;
        }
    }

    /**
     * @return the number of lines skipped
     */
    public long getLinesSkipped()
    {
        synchronized(statusCountersLock) {
            return linesSkipped;
        }
    }
    
    /**
     * Increments the number of lines skipped
     * @return the new incremented value
     */
    public long incrementLinesSkipped()
    {
        synchronized(statusCountersLock) {
            return ++linesSkipped;
        }
    }

    /**
     * @param newLinesSkippedValue lines number of lines skipped
     */
    public void setLinesSkipped(long newLinesSkippedValue)
    {
        synchronized(statusCountersLock) {
            linesSkipped = newLinesSkippedValue;
        }
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
        return getStatus().getDescription();
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
          Thread.sleep(1);
        } catch (InterruptedException e) {
          throw new KettleStepException(e);
        }
      }
  
      // Right after the pause loop we have to check if this thread is stopped or
      // not.
      //
      if (stopped.get()) {
        if (log.isDebug())
          logDebug(BaseMessages.getString(PKG, "BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
        stopAll();
        return;
      }
  
      // Have all threads started?
      // Are we running yet? If not, wait a bit until all threads have been
      // started.
      //
      if (this.checkTransRunning == false) {
        while (!trans.isRunning() && !stopped.get()) {
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
          }
        }
        this.checkTransRunning = true;
      }
  
      // call all row listeners...
      //
      synchronized(rowListeners) {
        for (int i = 0; i < rowListeners.size(); i++) {
          RowListener rowListener = rowListeners.get(i);
          rowListener.rowWrittenEvent(rowMeta, row);
        }
      }
  
      // Keep adding to terminator_rows buffer...
      //
      if (terminator && terminator_rows != null) {
        try {
          terminator_rows.add(rowMeta.cloneRow(row));
        } catch (KettleValueException e) {
          throw new KettleStepException("Unable to clone row while adding rows to the terminator rows.", e);
        }
      }
  
      if (outputRowSets.isEmpty()) {
        // No more output rowsets!
        // Still update the nr of lines written.
        //
        incrementLinesWritten();
  
        return; // we're done here!
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
              
      	    // To reduce stress on the locking system we are NOT going to allow
      	    // the buffer to grow to its full capacity.
      	    //
              if (isUsingThreadPriorityManagment() && !rs.isDone() && rs.size()>= upperBufferBoundary && !isStopped())
              {
              	try { Thread.sleep(0,1); } catch (InterruptedException e) { }
              }
              
              // Loop until we find room in the target rowset
              //
              while (!rs.putRow(rowMeta, row) && !isStopped()) 
              	;
              incrementLinesWritten();

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
                  
          	    // To reduce stress on the locking system we are NOT going to allow
          	    // the buffer to grow to its full capacity.
          	    //
                  if (isUsingThreadPriorityManagment() && !rs.isDone() && rs.size()>= upperBufferBoundary && !isStopped())
                  {
                  	try { Thread.sleep(0,1); } catch (InterruptedException e) { }
                  }

                  try
                  {
                      // Loop until we find room in the target rowset
                      //
                      while (!rs.putRow(rowMeta, rowMeta.cloneRow(row)) && !isStopped()) 
                      	;
                      incrementLinesWritten();
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
              incrementLinesWritten();
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
          		List<StepMeta> nextSteps = transMeta.findNextSteps(stepMeta);
                  if (nextSteps.size()>0) {
                  	nextStepPartitioningMeta = nextSteps.get(0).getStepPartitioningMeta();
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
	                if (partitionNr<partitionNrRowSetList.length) {
	                	selectedRowSet = partitionNrRowSetList[partitionNr];
	                } else {
	                	String rowsets = "";
	                	for (RowSet rowSet : partitionNrRowSetList) {
	                		rowsets+="["+rowSet.toString()+"] ";
	                	}
	                	throw new KettleStepException("Internal error: the referenced partition nr '"+partitionNr+"' is higher than the maximum of '"+(partitionNrRowSetList.length-1)+".  The available row sets are: {"+rowsets+"}");
	                }
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
                incrementLinesWritten();
                
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
   * @param rowMeta
   *          The row meta-data to put to the destination RowSet.
   * @param row
   *          the data to put in the RowSet
   * @param rowSet
   *          the RoWset to put the row into.
   * @throws KettleStepException
   *           In case something unexpected goes wrong
   */
  public void putRowTo(RowMetaInterface rowMeta, Object[] row, RowSet rowSet) throws KettleStepException {
    
    // Are we pausing the step? If so, stall forever...
    //
    while (paused.get() && !stopped.get()) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        throw new KettleStepException(e);
      }
    }

    // call all row listeners...
    //
    synchronized(rowListeners) {
      for (int i = 0; i < rowListeners.size(); i++) {
        RowListener rowListener = rowListeners.get(i);
        rowListener.rowWrittenEvent(rowMeta, row);
      }
    }

    // Keep adding to terminator_rows buffer...
    if (terminator && terminator_rows != null) {
      try {
        terminator_rows.add(rowMeta.cloneRow(row));
      } catch (KettleValueException e) {
        throw new KettleStepException("Unable to clone row while adding rows to the terminator buffer", e);
      }
    }

    if (stopped.get()) {
      if (log.isDebug())
        logDebug(BaseMessages.getString(PKG, "BaseStep.Log.StopPuttingARow")); //$NON-NLS-1$
      stopAll();
      return;
    }

    // Don't distribute or anything, only go to this rowset!
    //
    while (!rowSet.putRow(rowMeta, row) && !isStopped())
      ;
    incrementLinesWritten();
  }

  public void putError(RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes) throws KettleStepException {
    if (trans.isSafeModeEnabled()) {
      if (rowMeta.size() > row.length) {
        throw new KettleStepException(BaseMessages.getString(PKG, "BaseStep.Exception.MetadataDoesntMatchDataRowSize", Integer.toString(rowMeta.size()), Integer.toString(row != null ? row.length : 0)));
      }
    }

    StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();

    if (errorRowMeta == null) {
      errorRowMeta = rowMeta.clone();

      RowMetaInterface add = stepErrorMeta.getErrorRowMeta(nrErrors, errorDescriptions, fieldNames, errorCodes);
      errorRowMeta.addRowMeta(add);
    }

    Object[] errorRowData = RowDataUtil.allocateRowData(errorRowMeta.size());
    if (row != null) {
      System.arraycopy(row, 0, errorRowData, 0, rowMeta.size());
    }

    // Also add the error fields...
    stepErrorMeta.addErrorRowData(errorRowData, rowMeta.size(), nrErrors, errorDescriptions, fieldNames, errorCodes);

    // call all row listeners...
    synchronized(rowListeners) {
      for (int i = 0; i < rowListeners.size(); i++) {
        RowListener rowListener = rowListeners.get(i);
        rowListener.errorRowWrittenEvent(rowMeta, row);
      }
    }

    if (errorRowSet != null) {
      while (!errorRowSet.putRow(errorRowMeta, errorRowData) && !isStopped())
        ;
      incrementLinesRejected();
    }

    verifyRejectionRates();
  }

    private void verifyRejectionRates()
    {
        StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
        if (stepErrorMeta==null) return; // nothing to verify.
      
        
        // Was this one error too much?
        if (maxErrors>0 && getLinesRejected()>maxErrors)
        {
            logError(BaseMessages.getString(PKG, "BaseStep.Log.TooManyRejectedRows", Long.toString(maxErrors), Long.toString(getLinesRejected())));
            setErrors(1L);
            stopAll();
        }

        if ( maxPercentErrors>0 && getLinesRejected()>0 &&
            ( minRowsForMaxErrorPercent<=0 || getLinesRead()>=minRowsForMaxErrorPercent)
            )
        {
            int pct = (int) (100 * getLinesRejected() / getLinesRead() );
            if (pct>maxPercentErrors)
            {
                logError(BaseMessages.getString(PKG, "BaseStep.Log.MaxPercentageRejectedReached", Integer.toString(pct) ,Long.toString(getLinesRejected()), Long.toString(getLinesRead())));
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
   * Wait until the transformation is completely running and all threads have been started.
   */
  protected void waitUntilTransformationIsStarted() {
    // Have all threads started?
    // Are we running yet? If not, wait a bit until all threads have been
    // started.
    //
    if (this.checkTransRunning == false) {
      while (!trans.isRunning() && !stopped.get()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
      }
      this.checkTransRunning = true;
    }
  }

  /**
   * In case of getRow, we receive data from previous steps through the input
   * rowset. In case we split the stream, we have to copy the data to the
   * alternate splits: rowsets 1 through n.
   */
  public Object[] getRow() throws KettleException {

    // Are we pausing the step? If so, stall forever...
    //
    while (paused.get() && !stopped.get()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new KettleStepException(e);
      }
    }

    if (stopped.get()) {
      if (log.isDebug())
        logDebug(BaseMessages.getString(PKG, "BaseStep.Log.StopLookingForMoreRows")); //$NON-NLS-1$
      stopAll();
      return null;
    }

    // Small startup check
    //
    waitUntilTransformationIsStarted();

    // See if we need to open sockets to remote input steps...
    //
    openRemoteInputStepSocketsOnce();

    // If everything is finished, we can stop immediately!
    //
    if (inputRowSets.isEmpty()) {
      return null;
    }

    RowSet inputRowSet = null;
    Object[] row = null;

    // Do we need to switch to the next input stream?
    if (blockPointer >= NR_OF_ROWS_IN_BLOCK) {

      // Take a peek at the next input stream.
      // If there is no data, process another NR_OF_ROWS_IN_BLOCK on the next
      // input stream.
      //
      for (int r = 0; r < inputRowSets.size() && row == null; r++) {
        nextInputStream();
        inputRowSet = currentInputStream();
        row = inputRowSet.getRowImmediate();
      }
      if (row != null) {
        incrementLinesRead();
      }
    } else {
      // What's the current input stream?
      inputRowSet = currentInputStream();
    }

    // To reduce stress on the locking system we are going to allow
    // The buffer to grow beyond "a few" entries.
    // We'll only do that if the previous step has not ended...
    //
    if (isUsingThreadPriorityManagment() && !inputRowSet.isDone() && inputRowSet.size() <= lowerBufferBoundary && !isStopped()) {
      try {
        Thread.sleep(0, 1);
      } catch (InterruptedException e) {
      }
    }

    // See if this step is receiving partitioned data...
    // In that case it might be the case that one input row set is receiving
    // all data and
    // the other rowsets nothing. (repartitioning on the same key would do
    // that)
    //
    // We never guaranteed that the input rows would be read one by one
    // alternatively.
    // So in THIS particular case it is safe to just read 100 rows from one
    // rowset, then switch to another etc.
    // We can use timeouts to switch from one to another...
    // 
    while (row == null && !isStopped()) {
      // Get a row from the input in row set ...
      // Timeout immediately if nothing is there to read.
      // We will then switch to the next row set to read from...
      //
      row = inputRowSet.getRowWait(1, TimeUnit.MILLISECONDS);
      if (row != null) {
        incrementLinesRead();
        blockPointer++;
      } else {
        // Try once more...
        // If row is still empty and the row set is done, we remove the row
        // set from
        // the input stream and move on to the next one...
        //
        if (inputRowSet.isDone()) {
          row = inputRowSet.getRowWait(1, TimeUnit.MILLISECONDS);
          if (row == null) {
            inputRowSets.remove(currentInputRowSetNr);
            if (inputRowSets.isEmpty())
              return null; // We're completely done.
          } else {
            incrementLinesRead();
          }
        }
        nextInputStream();
        inputRowSet = currentInputStream();
      }
    }

    // This rowSet is perhaps no longer giving back rows?
    //
    while (row == null && !stopped.get()) {
      // Try the next input row set(s) until we find a row set that still has
      // rows...
      // The getRowFrom() method removes row sets from the input row sets
      // list.
      //
      if (inputRowSets.isEmpty())
        return null; // We're done.

      nextInputStream();
      inputRowSet = currentInputStream();
      row = getRowFrom(inputRowSet);
    }

    // Also set the meta data on the first occurrence.
    //
    if (inputRowMeta == null) {
      inputRowMeta = inputRowSet.getRowMeta();
    }

    if (row != null) {
      // OK, before we return the row, let's see if we need to check on mixing
      // row compositions...
      // 
      if (trans.isSafeModeEnabled()) {
        safeModeChecking(inputRowSet.getRowMeta(), inputRowMeta); // Extra
        // checking
        if (row.length < inputRowMeta.size()) {
          throw new KettleException("Safe mode check noticed that the length of the row data is smaller (" + row.length + ") than the row metadata size (" + inputRowMeta.size() + ")");
        }
      }

      synchronized(rowListeners) {
        for (int i = 0; i < rowListeners.size(); i++) {
          RowListener rowListener = rowListeners.get(i);
          rowListener.rowReadEvent(inputRowMeta, row);
        }
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
						BlockingRowSet rowSet = remoteStep.openReaderSocket(this);
						inputRowSets.add(rowSet);
					} catch (Exception e) {
						throw new KettleStepException("Error opening reader socket to remote step '"+remoteStep+"'", e);
					}
        		}
        		remoteInputStepsInitialized = true;
        	}
        }
	}
    
    /**
     * Opens socket connections to the remote output steps of this step.
     * <br>This method is called in method initBeforeStart() because it needs to connect to the server sockets (remote steps) as soon as possible to avoid time-out situations.
     * <br>This action is executed only once.
     * @throws KettleStepException
     */
    protected void openRemoteOutputStepSocketsOnce() throws KettleStepException {
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
				for (int i=0;i<remoteOutputSteps.size();i++) {
					RemoteStep remoteStep = remoteOutputSteps.get(i);
					try {
						if (remoteStep.getTargetSlaveServerName()==null) {
		    				throw new KettleStepException("The target slave server name is not defined for remote output step: "+remoteStep);
						}
						BlockingRowSet rowSet = remoteStep.openWriterSocket();
						if (log.isDetailed()) logDetailed("Opened a writer socket to remote step: "+remoteStep);
						outputRowSets.add(rowSet);
					} catch (IOException e) {
						throw new KettleStepException("Error opening writer socket to remote step '"+remoteStep+"'", e);
					}
				}
				
				remoteOutputStepsInitialized = true;
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
                    throw new KettleRowException(BaseMessages.getString(PKG, "BaseStep.SafeMode.Exception.DoubleFieldnames", fieldnames[i]));
                }
            }
        }
        else
        {
            safeModeChecking(inputReferenceRow, row);
        }
    }
	
	public void identifyErrorOutput() {
		if (stepMeta.isDoingErrorHandling()) {
            StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
            boolean stop=false;
            for (int rowsetNr=0;rowsetNr<outputRowSets.size() && !stop;rowsetNr++)
            {
                RowSet outputRowSet = outputRowSets.get(rowsetNr);
                if (outputRowSet.getDestinationStepName().equalsIgnoreCase(stepErrorMeta.getTargetStep().getName()))
                {
                    // This is the rowset to move!
                	//
                    errorRowSet = outputRowSet;
                    outputRowSets.remove(rowsetNr);
                    stop=true;
                }
            }
		}
	}
	

    public static void safeModeChecking(RowMetaInterface referenceRowMeta, RowMetaInterface rowMeta) throws KettleRowException
    {
        // See if the row we got has the same layout as the reference row.
        // First check the number of fields
    	//
        if (referenceRowMeta.size() != rowMeta.size())
        {
            throw new KettleRowException(BaseMessages.getString(PKG, "BaseStep.SafeMode.Exception.VaryingSize", ""+referenceRowMeta.size(), ""+rowMeta.size(), rowMeta.toString()));
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
                    throw new KettleRowException(BaseMessages.getString(PKG, "BaseStep.SafeMode.Exception.MixingLayout", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));
                }

                if (referenceValue.getType()!=compareValue.getType())
                {
                    throw new KettleRowException(BaseMessages.getString(PKG, "BaseStep.SafeMode.Exception.MixingTypes", ""+(i+1), referenceValue.getName()+" "+referenceValue.toStringMeta(), compareValue.getName()+" "+compareValue.toStringMeta()));               
                }
            }
        }
    }
    
  public Object[] getRowFrom(RowSet rowSet) throws KettleStepException {
    // Are we pausing the step? If so, stall forever...
    //
    while (paused.get() && !stopped.get()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new KettleStepException(e);
      }
    }

    // Have all threads started?
    // Are we running yet? If not, wait a bit until all threads have been
    // started.
    if (this.checkTransRunning == false) {
      while (!trans.isRunning() && !stopped.get()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
        }
      }
      this.checkTransRunning = true;
    }
    Object[] rowData = null;

    // To reduce stress on the locking system we are going to allow
    // The buffer to grow beyond "a few" entries.
    // We'll only do that if the previous step has not ended...
    //
    if (isUsingThreadPriorityManagment() && !rowSet.isDone() && rowSet.size() <= lowerBufferBoundary && !isStopped()) {
      try {
        Thread.sleep(0, 1);
      } catch (InterruptedException e) {
      }
    }

    // Grab a row... If nothing received after a timeout, try again.
    //
    rowData = rowSet.getRow();
    while (rowData == null && !rowSet.isDone() && !stopped.get()) {
      rowData = rowSet.getRow();
    }

    // Still nothing: no more rows to be had?
    //
    if (rowData == null && rowSet.isDone()) {
      // Try one more time to get a row to make sure we don't get a
      // race-condition between the get and the isDone()
      //
      rowData = rowSet.getRow();
    }

    if (stopped.get()) {
      if (log.isDebug())
        logDebug(BaseMessages.getString(PKG, "BaseStep.Log.StopLookingForMoreRows")); //$NON-NLS-1$
      stopAll();
      return null;
    }

    if (rowData == null && rowSet.isDone()) {
      // Try one more time...
      //
      rowData = rowSet.getRow();
      if (rowData == null) {
        inputRowSets.remove(rowSet);
        return null;
      }
    }
    incrementLinesRead();

    // call all rowlisteners...
    //
    synchronized(rowListeners) {
      for (int i = 0; i < rowListeners.size(); i++) {
        RowListener rowListener = rowListeners.get(i);
        rowListener.rowReadEvent(rowSet.getRowMeta(), rowData);
      }
    }

    return rowData;
  }
    
    public RowSet findInputRowSet(String sourceStep) throws KettleStepException {
    	// Check to see that "sourceStep" only runs in a single copy
    	// Otherwise you'll see problems during execution.
    	//
    	StepMeta sourceStepMeta = transMeta.findStep(sourceStep);
    	if (sourceStepMeta==null) {
    		throw new KettleStepException(BaseMessages.getString(PKG, "BaseStep.Exception.SourceStepToReadFromDoesntExist", sourceStep));
    	}
    	
    	if (sourceStepMeta.getCopies()>1) {
    		throw new KettleStepException(BaseMessages.getString(PKG, "BaseStep.Exception.SourceStepToReadFromCantRunInMultipleCopies", sourceStep, Integer.toString(sourceStepMeta.getCopies())));
    	}
    	
    	return findInputRowSet(sourceStep, 0, getStepname(), getCopy());
    }

	public RowSet findInputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        for (RowSet rs : inputRowSets)
        {
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return rs;
        }
        
        // See if the rowset is part of the output of a mapping source step...
        //
        // Lookup step "From"
        //
        StepMeta mappingStep = transMeta.findStep(from);
        
        // See if it's a mapping
        //
        if (mappingStep!=null && mappingStep.isMapping()) {
        
        	// In this case we can cast the step thread to a Mapping...
        	//
        	List<StepInterface> baseSteps = trans.findBaseSteps(from);
        	if (baseSteps.size()==1) {
	        	Mapping mapping = (Mapping) baseSteps.get(0);
	        	
	        	// Find the appropriate rowset in the mapping...
	        	// The rowset in question has been passed over to a Mapping Input step inside the Mapping transformation. 
	            //
	        	MappingOutput[] outputs = mapping.getMappingTrans().findMappingOutput();
	        	for (MappingOutput output: outputs) {
	        		for (RowSet rs : output.getOutputRowSets()) {
	        			// The destination is what counts here...
	        			//
	        			if (rs.getDestinationStepName().equalsIgnoreCase(to)) return rs;
	        		}
	        	}
        	}
        }
        
        return null;
    }
	
	public RowSet findOutputRowSet(String targetStep) throws KettleStepException {
		
    	// Check to see that "targetStep" only runs in a single copy
    	// Otherwise you'll see problems during execution.
    	//
    	StepMeta targetStepMeta = transMeta.findStep(targetStep);
    	if (targetStepMeta==null) {
    		throw new KettleStepException(BaseMessages.getString(PKG, "BaseStep.Exception.TargetStepToWriteToDoesntExist", targetStep));
    	}
    	
    	if (targetStepMeta.getCopies()>1) {
    		throw new KettleStepException(BaseMessages.getString(PKG, "BaseStep.Exception.TargetStepToWriteToCantRunInMultipleCopies", targetStep, Integer.toString(targetStepMeta.getCopies())));
    	}
    	

		return findOutputRowSet(getStepname(), getCopy(), targetStep, 0);
	}

	/**
	 * Find an output rowset in a running transformation.  It will also look at the "to" step to see if this is a mapping.
	 * If it is, it will find the appropriate rowset in that transformation.  
	 * @param from
	 * @param fromcopy
	 * @param to
	 * @param tocopy
	 * @return The rowset or null if none is found.
	 */
    public RowSet findOutputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        for (RowSet rs : outputRowSets)
        {
            if (rs.getOriginStepName().equalsIgnoreCase(from) && rs.getDestinationStepName().equalsIgnoreCase(to)
                    && rs.getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy) return rs;
        }
        
        // See if the rowset is part of the input of a mapping target step...
        //
        // Lookup step "To"
        //
        StepMeta mappingStep = transMeta.findStep(to);
        
        // See if it's a mapping
        //
        if (mappingStep!=null && mappingStep.isMapping()) {
        
        	// In this case we can cast the step thread to a Mapping...
        	//
        	List<StepInterface> baseSteps = trans.findBaseSteps(to);
        	if (baseSteps.size()==1) {
	        	Mapping mapping = (Mapping) baseSteps.get(0);
	        	
	        	// Find the appropriate rowset in the mapping...
	        	// The rowset in question has been passed over to a Mapping Input step inside the Mapping transformation. 
	            //
	        	MappingInput[] inputs= mapping.getMappingTrans().findMappingInput();
	        	for (MappingInput input : inputs) {
	        		for (RowSet rs : input.getInputRowSets()) {
	        			// The source step is what counts in this case...
	        			//
	                    if (rs.getOriginStepName().equalsIgnoreCase(from)) return rs;
	        		}
	        	}
        	}
        }
        
        // Still nothing found!
        //
        return null;
    }

    //
    // We have to tell the next step we're finished with
    // writing to output rowset(s)!
    //
    public void setOutputDone()
    {
        if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "BaseStep.Log.OutputDone", String.valueOf(outputRowSets.size()))); //$NON-NLS-1$ //$NON-NLS-2$
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

        if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.StartingBuffersAllocation")); //$NON-NLS-1$

        // How many next steps are there? 0, 1 or more??
        // How many steps do we send output to?
        List<StepMeta> previousSteps = transMeta.findPreviousSteps(stepMeta, true);
        List<StepMeta> succeedingSteps = transMeta.findNextSteps(stepMeta);
        
        int nrInput = previousSteps.size();
        int nrOutput = succeedingSteps.size();

        inputRowSets = new ArrayList<RowSet>();
        outputRowSets = new ArrayList<RowSet>();
        errorRowSet = null;
        prevSteps = new StepMeta[nrInput];
        nextSteps = new StepMeta[nrOutput];

        currentInputRowSetNr = 0; // we start with input[0];

        if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.StepInfo", String.valueOf(nrInput), String.valueOf(nrOutput))); //$NON-NLS-1$ //$NON-NLS-2$

        for (int i = 0; i < previousSteps.size(); i++)
        {
            prevSteps[i] = previousSteps.get(i);
            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.GotPreviousStep", stepname, String.valueOf(i), prevSteps[i].getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            // Looking at the previous step, you can have either 1 rowset to look at or more then one.
            int prevCopies = prevSteps[i].getCopies();
            int nextCopies = stepMeta.getCopies();
            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.InputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

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
                        	if (stepMeta.isPartitioned() && !prevSteps[i].isPartitioned()) {
                                dispatchType = Trans.TYPE_DISP_N_M;
                                nrCopies = nextCopies;
                        	} else {
	                            dispatchType = Trans.TYPE_DISP_N_N;
	                            nrCopies = 1;
                        	}
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
                    if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.FoundInputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                	if (!prevSteps[i].isMapping() && !stepMeta.isMapping()) {
	                    logError(BaseMessages.getString(PKG, "BaseStep.Log.UnableToFindInputRowset")); //$NON-NLS-1$
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
            nextSteps[i] = succeedingSteps.get(i);

            int prevCopies = stepMeta.getCopies();
            int nextCopies = nextSteps[i].getCopies();

            if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.OutputRowInfo", String.valueOf(prevCopies), String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$

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
                        	if (!stepMeta.isPartitioned() && nextSteps[i].isPartitioned()) {
                                dispatchType = Trans.TYPE_DISP_N_M;
                                nrCopies = nextCopies;
                        	} else {
	                            dispatchType = Trans.TYPE_DISP_N_N;
	                            nrCopies = 1;
                        	}
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
                    if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.FoundOutputRowset", rowSet.getName())); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                	if (!stepMeta.isMapping() && !nextSteps[i].isMapping()) {
	                    logError(BaseMessages.getString(PKG, "BaseStep.Log.UnableToFindOutputRowset")); //$NON-NLS-1$
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

        if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.FinishedDispatching")); //$NON-NLS-1$
    }

    public boolean isBasic() { return log.isBasic(); }
    public boolean isDetailed() { return log.isDetailed(); }
    public boolean isDebug() { return log.isDebug(); }
    public boolean isRowLevel() { return log.isRowLevel(); }
    public void logMinimal(String message) { log.logMinimal(message); }
    public void logMinimal(String message, Object...arguments) { log.logMinimal(message, arguments); }
    public void logBasic(String message) { log.logBasic(message); }
    public void logBasic(String message, Object...arguments) { log.logBasic(message, arguments); }
    public void logDetailed(String message) { log.logDetailed(message); }
    public void logDetailed(String message, Object...arguments) { log.logDetailed(message, arguments); }
    public void logDebug(String message) { log.logDebug(message); }
    public void logDebug(String message, Object...arguments) { log.logDebug(message, arguments); }
    public void logRowlevel(String message) { log.logRowlevel(message); }
    public void logRowlevel(String message, Object...arguments) { log.logRowlevel(message, arguments); }
    public void logError(String message) { log.logError(message); } 
    public void logError(String message, Throwable e) { log.logError(message, e); }
    public void logError(String message, Object...arguments) { log.logError(message, arguments); }

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
    
    public boolean isRunning()
    {
        return running.get();
    }
    
    public boolean isPaused()
    {
        return paused.get();
    }

	public void setStopped(boolean stopped) {
		this.stopped.set(stopped);
	}

	public void setRunning(boolean running) {
		this.running.set(running);
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
        
        // Here we are completely done with the transformation.
        // Call all the attached listeners and notify the outside world that the step has finished.
        //
        for (StepListener stepListener : stepListeners) {
        	stepListener.stepFinished(trans, stepMeta, this);
        }
        
        // We're finally completely done with this step.
        //
        setRunning(false);
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
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Stepname"), ValueMetaInterface.TYPE_STRING)); //$NON-NLS-1$
        data[nr]=sname; 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Copy"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(copynr); 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesReaded"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_read); 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesWritten"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_written); 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesUpdated"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_updated); 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesSkipped"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        data[nr]=new Double(lines_skipped); 
        nr++;
        
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Errors"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
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
        ValueMetaInterface sname = new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Stepname"), ValueMetaInterface.TYPE_STRING); //$NON-NLS-1$ //$NON-NLS-2$
        sname.setLength(256);
        r.addValueMeta(sname);

        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Copy"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesReaded"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesWritten"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesUpdated"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.LinesSkipped"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.Errors"), ValueMetaInterface.TYPE_NUMBER)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.StartDate"), ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$
        r.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "BaseStep.ColumnName.EndDate"), ValueMetaInterface.TYPE_DATE)); //$NON-NLS-1$

        for (int i = 0; i < r.size(); i++)
        {
            r.getValueMeta(i).setOrigin(comm);
        }

        return r;
    }

    public String toString()
    {
    	StringBuffer string = new StringBuffer();
    	
    	// If the step runs in a mapping (and as such has a "parent transformation", we are going to print the name of the transformation during logging
    	//
    	//
    	if (!Const.isEmpty(getTrans().getMappingStepName())) {
    		string.append('[').append(trans.toString()).append(']').append('.'); // Name of the mapping transformation
    	}
    	
    	if (!Const.isEmpty(partitionID)) {
    		string.append(stepname).append('.').append(partitionID);  //$NON-NLS-1$
    	}
    	else if (clusterSize>1) {
    		string.append(stepname).append('.').append(slaveNr).append('.').append(Integer.toString(getCopy())); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	else {
    		string.append(stepname).append('.').append(Integer.toString(getCopy())); //$NON-NLS-1$
    	}
    	
    	return string.toString();
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
        synchronized(statusCountersLock) {
            long li = getLinesInput();
            long lo = getLinesOutput();
            long lr = getLinesRead();
            long lw = getLinesWritten();
            long lu = getLinesUpdated();
            long lj = getLinesRejected();
            if (li > 0 || lo > 0 || lr > 0 || lw > 0 || lu > 0 || lj > 0 || errors > 0)
                logBasic(BaseMessages.getString(PKG, "BaseStep.Log.SummaryInfo", String.valueOf(li), String.valueOf(lo), String.valueOf(lr), String.valueOf(lw), String.valueOf(lw), String.valueOf(errors+lj)));
            else
                logDetailed(BaseMessages.getString(PKG, "BaseStep.Log.SummaryInfo", String.valueOf(li), String.valueOf(lo), String.valueOf(lr), String.valueOf(lw), String.valueOf(lw), String.valueOf(errors+lj)));
        }
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
    public void setInputRowSets(List<RowSet> inputRowSets)
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
    public void setOutputRowSets(List<RowSet> outputRowSets)
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

    public StepExecutionStatus getStatus()
    {
    	// Is this thread alive or not?
    	//
    	if (isRunning()) {
    		if (isStopped()) {
    			return StepExecutionStatus.STATUS_HALTING;
    		} else {
    			if (isPaused()) {
    				return StepExecutionStatus.STATUS_PAUSED;
    			} else {
    				return StepExecutionStatus.STATUS_RUNNING;
    			}
    		}
    	} 
    	else
    	{
    		// Step is not running... What are we doing?
    		//
			// An init thread is running...
    		//
    		if (trans.isInitializing()) {
        		if (isInitialising()) {
        			return StepExecutionStatus.STATUS_INIT;
        		} else {
        			// Done initializing, but other threads are still busy.
        			// So this step is idle
        			//
        			return StepExecutionStatus.STATUS_IDLE;
        		}
    		}
    		else {
    			// It's not running, it's not initializing, so what is it doing?
    			//
        		if (isStopped()) {
        	    	return StepExecutionStatus.STATUS_STOPPED;
        		} else {
        			// To be sure (race conditions and all), get the rest in StepDataInterface object:
        			// 
        			StepDataInterface sdi = trans.getStepDataInterface(stepname, stepcopy);
        			if (sdi != null)
        			{
        				if (sdi.getStatus() == StepExecutionStatus.STATUS_DISPOSED) {
        					return StepExecutionStatus.STATUS_FINISHED;
        				} else {
        					return sdi.getStatus();
        				}
        			}
        			return StepExecutionStatus.STATUS_EMPTY;
        		}
    		}
    	}
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
    public Map<String,BlockingRowSet> getPartitionTargets()
    {
        return partitionTargets;
    }

    /**
     * @param partitionTargets the partitionTargets to set
     */
    public void setPartitionTargets(Map<String,BlockingRowSet> partitionTargets)
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

	/**
	 * @param usingThreadPriorityManagment set to true to actively manage priorities of step threads
	 */
	public void setUsingThreadPriorityManagment(boolean usingThreadPriorityManagment) {
		this.usingThreadPriorityManagment = usingThreadPriorityManagment;
	}

	/**
	 * @return true if we are actively managing priorities of step threads
	 */
	public boolean isUsingThreadPriorityManagment() {
		return usingThreadPriorityManagment;
	}

	/**
	 * This method is executed by Trans right before the threads start and right after initialization.
	 * 
	 * More to the point: here we open remote output step sockets. 
	 * 
	 * @throws KettleStepException In case there is an error
	 */
	public void initBeforeStart() throws KettleStepException {
		openRemoteOutputStepSocketsOnce();
	}

	/**
	 * @return the stepListeners
	 */
	public List<StepListener> getStepListeners() {
		return stepListeners;
	}

	/**
	 * @param stepListeners the stepListeners to set
	 */
	public void setStepListeners(List<StepListener> stepListeners) {
		this.stepListeners = stepListeners;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		return false;
	}
	
	public boolean canProcessOneRow() {
		switch(inputRowSets.size()) {
		case 0: return false;
		case 1:
			RowSet set = inputRowSets.get(0);
			if (set.isDone()) return false;
			return set.size()>0;
		default: 
			boolean allDone=true;
			for (RowSet rowSet : inputRowSets) {
				if (!rowSet.isDone()) allDone=false;
				if (rowSet.size()>0) {
					return true;
				}
			}
			return !allDone;
		}
	}
	
	public void addStepListener(StepListener stepListener) {
		stepListeners.add(stepListener);
	}
	
	public boolean isMapping() {
		return stepMeta.isMapping();
	}

	/**
	 * @return the socketRepository
	 */
	public SocketRepository getSocketRepository() {
		return socketRepository;
	}

	/**
	 * @param socketRepository the socketRepository to set
	 */
	public void setSocketRepository(SocketRepository socketRepository) {
		this.socketRepository = socketRepository;
	}
	
	public String getObjectName() {
		return getStepname();
	}

	public LogChannelInterface getLogChannel() {
		return log;
	}

	public String getFilename() {
		return null;
	}

	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	public ObjectId getObjectId() {
		if (stepMeta==null) return null;
		return stepMeta.getObjectId();
	}

	public ObjectRevision getObjectRevision() {
		return null;
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.STEP;
	}

	public LoggingObjectInterface getParent() {
		return trans;
	}

	public RepositoryDirectory getRepositoryDirectory() {
		return null;
	}
	
	public String getObjectCopy() {
		return Integer.toString(stepcopy);
	}

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
    log.setLogLevel(logLevel);
  }
  
  public static void closeQuietly(Closeable cl) {
    if (cl != null) {
      try {
        cl.close();
      } catch (IOException ignored) {
        // Ignore IOException on close
      }
    }
  }
 
  /**
   * @return the containerObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the containerObjectId to set
   */
  public void setCarteObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }
  
  @Override
  public void batchComplete() throws KettleException {
  }
 
  public List<RemoteStep> getRemoteInputSteps() {
    return remoteInputSteps;
  }
  
  public List<RemoteStep> getRemoteOutputSteps() {
    return remoteOutputSteps;
  }
  
  /**
   * Stub
   */
  public Date getRegistrationDate() {
    return null;
  }
}