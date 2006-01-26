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
 
/*
 * Created on 9-apr-2003
 *
 */

package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.RowSet;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.addsequence.AddSequenceMeta;
import be.ibridge.kettle.trans.step.aggregaterows.AggregateRowsMeta;
import be.ibridge.kettle.trans.step.calculator.CalculatorMeta;
import be.ibridge.kettle.trans.step.combinationlookup.CombinationLookupMeta;
import be.ibridge.kettle.trans.step.constant.ConstantMeta;
import be.ibridge.kettle.trans.step.cubeinput.CubeInputMeta;
import be.ibridge.kettle.trans.step.cubeoutput.CubeOutputMeta;
import be.ibridge.kettle.trans.step.databasejoin.DatabaseJoinMeta;
import be.ibridge.kettle.trans.step.databaselookup.DatabaseLookupMeta;
import be.ibridge.kettle.trans.step.dbproc.DBProcMeta;
import be.ibridge.kettle.trans.step.denormaliser.DenormaliserMeta;
import be.ibridge.kettle.trans.step.dimensionlookup.DimensionLookupMeta;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;
import be.ibridge.kettle.trans.step.excelinput.ExcelInputMeta;
import be.ibridge.kettle.trans.step.fieldsplitter.FieldSplitterMeta;
import be.ibridge.kettle.trans.step.filterrows.FilterRowsMeta;
import be.ibridge.kettle.trans.step.flattener.FlattenerMeta;
import be.ibridge.kettle.trans.step.groupby.GroupByMeta;
import be.ibridge.kettle.trans.step.insertupdate.InsertUpdateMeta;
import be.ibridge.kettle.trans.step.joinrows.JoinRowsMeta;
import be.ibridge.kettle.trans.step.mapping.MappingMeta;
import be.ibridge.kettle.trans.step.mappinginput.MappingInputMeta;
import be.ibridge.kettle.trans.step.mappingoutput.MappingOutputMeta;
import be.ibridge.kettle.trans.step.mergerows.MergeRowsMeta;
import be.ibridge.kettle.trans.step.normaliser.NormaliserMeta;
import be.ibridge.kettle.trans.step.nullif.NullIfMeta;
import be.ibridge.kettle.trans.step.rowgenerator.RowGeneratorMeta;
import be.ibridge.kettle.trans.step.rowsfromresult.RowsFromResultMeta;
import be.ibridge.kettle.trans.step.rowstoresult.RowsToResultMeta;
import be.ibridge.kettle.trans.step.scriptvalues.ScriptValuesMeta;
import be.ibridge.kettle.trans.step.selectvalues.SelectValuesMeta;
import be.ibridge.kettle.trans.step.sortrows.SortRowsMeta;
import be.ibridge.kettle.trans.step.sql.ExecSQLMeta;
import be.ibridge.kettle.trans.step.streamlookup.StreamLookupMeta;
import be.ibridge.kettle.trans.step.systemdata.SystemDataMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;
import be.ibridge.kettle.trans.step.tableoutput.TableOutputMeta;
import be.ibridge.kettle.trans.step.textfileinput.TextFileInputMeta;
import be.ibridge.kettle.trans.step.textfileoutput.TextFileOutputMeta;
import be.ibridge.kettle.trans.step.uniquerows.UniqueRowsMeta;
import be.ibridge.kettle.trans.step.update.UpdateMeta;
import be.ibridge.kettle.trans.step.xbaseinput.XBaseInputMeta;
import be.ibridge.kettle.trans.step.xmlinput.XMLInputMeta;
import be.ibridge.kettle.trans.step.xmloutput.XMLOutputMeta;


public class BaseStep extends Thread 
{
	public static final Class type_classname[] = 
		{
		 	null,
			TextFileInputMeta.class,
			TextFileOutputMeta.class,
			TableInputMeta.class,
			TableOutputMeta.class,
			SelectValuesMeta.class,
			FilterRowsMeta.class,
			DatabaseLookupMeta.class,
			SortRowsMeta.class,
			StreamLookupMeta.class,  
			AddSequenceMeta.class,
			DimensionLookupMeta.class,
			CombinationLookupMeta.class,
			DummyTransMeta.class,
			JoinRowsMeta.class,
			AggregateRowsMeta.class,
			SystemDataMeta.class,
			RowGeneratorMeta.class,
			ScriptValuesMeta.class,
			DBProcMeta.class,               
			InsertUpdateMeta.class,
			UpdateMeta.class,
			NormaliserMeta.class,         
			FieldSplitterMeta.class,
			UniqueRowsMeta.class,
			GroupByMeta.class,
			RowsFromResultMeta.class,
			RowsToResultMeta.class,
			CubeInputMeta.class,
			CubeOutputMeta.class,
			DatabaseJoinMeta.class,
			XBaseInputMeta.class,
			ExcelInputMeta.class,
			NullIfMeta.class,
            CalculatorMeta.class,
            ExecSQLMeta.class,
            MappingMeta.class,
            MappingInputMeta.class,
            MappingOutputMeta.class,
            XMLInputMeta.class,
            XMLOutputMeta.class,
            MergeRowsMeta.class,
            ConstantMeta.class,
            DenormaliserMeta.class,
            FlattenerMeta.class
		};
	
	public static final String type_desc[] = 
		{
			null,
			"TextFileInput",
			"TextFileOutput",
			"TableInput",
			"TableOutput",
			"SelectValues",
			"FilterRows",
			"DBLookup",
			"SortRows",              
			"StreamLookup",  
			"Sequence",
			"DimensionLookup",
			"CombinationLookup",
			"Dummy",
			"JoinRows",
			"AggregateRows",
			"SystemInfo",
			"RowGenerator",
			"ScriptValue",
			"DBProc",               
			"InsertUpdate",
			"Update",
			"Normaliser",         
			"FieldSplitter",
			"Unique",
			"GroupBy",
			"RowsFromResult",
			"RowsToResult",
			"CubeInput",
			"CubeOutput",
			"DBJoin",
			"XBaseInput",
			"ExcelInput",
			"NullIf",
            "Calculator",
            "ExecSQL",
            "Mapping",
            "MappingInput",
            "MappingOutput",
            "XMLInput",
            "XMLOutput",
            "MergeRows",
            "Constant",
            "Denormaliser",
            "Flattener"
		};

	public static final String type_long_desc[] = 
		{
			null,
			"Text file input",
			"Text file output",
			"Table input",
			"Table output",
			"Select values",
			"Filter rows",
			"Database lookup",
			"Sort rows",
			"Stream lookup",             
			"Add sequence",              
			"Dimension update/lookup",
			"Combination update/lookup",
			"Dummy (do nothing)",
			"Join Rows (cartesian product)",
			"Aggregate Rows",
			"Get System Info",
			"Generate Rows",
			"Java Script Value",
			"Call DB Procedure",
			"Insert / Update",
			"Update",
			"Row Normaliser",           
			"Split Fields",
			"Unique rows",
			"Group by",
			"Get rows from result",
			"Copy rows to result",
			"Cube input",
			"Cube output",
			"Database join",
			"XBase input",
			"Excel Input",
			"Null if...",
            "Calculator",
            "Execute SQL script",
            "Mapping (sub-transformation)",
            "Mapping input specification",
            "Mapping output specification",
            "XML Input",
            "XML Output",
            "Merge Rows",
            "Add constants",
            "Row denormaliser",
            "Row flattener"
		};

	public static final String type_tooltip_desc[] = 
		{
			null,
			"Read data from a text file in several formats."+Const.CR+"This data can then be passed on to the next step(s)...",
			"Write rows to a text file.",
			"Read information from a database table.",
			"Write information to a database table",
			"Select or remove fields in a row."+Const.CR+"Optionally, set the field meta-data: type, length and precision.",
			"Filter rows using simple equations",
			"Look up values in a database using field values",
			"Sort rows based upon field values (ascending or descending)",        
			"Look up values coming from another stream in the transformation.",             
			"Get the next value from an sequence",              // 10
			"Update a slowly changing dimension in a data warehouse"+Const.CR+"Alternatively, look up information in this dimension.",
			"Update a junk dimension in a data warehouse"+Const.CR+"Alternatively, look up information in this dimension."+Const.CR+"The primary key of a junk dimension are all the fields.",
			"This step type doesn't do anything."+Const.CR+"It's useful however when testing things or in certain situations where you want to split streams.",
			"This output of this step is the cartesian product of the input streams."+Const.CR+"The number of rows is the multiplication of the number of rows in the input streams.",
			"This step type allows you to aggregate rows."+Const.CR+"It can't do groupings.",
			"Get information from the system like system date, arguments, etc.",
			"Generate a number of empty or equal rows.",
			"Create java scripts to calculate new fields, alter existing ones or manipulate a row.",
			"Get back information by calling a database procedure.", // 20
			"Update or insert rows in a database based upon keys.",  
			"Update data in a database table based upon keys",
			"De-normalised information can be normalised using this step type.",            
			"When you want to split a single field into more then one, use this step type.",
			"Remove double rows and leave only unique occurences."+Const.CR+"This works only on a sorted input."+Const.CR+"If the input is not sorted, only double consecutive rows are handled correctly.",
			"Builds aggregates in a group by fashion."+Const.CR+"This works only on a sorted input."+Const.CR+"If the input is not sorted, only double consecutive rows are handled correctly.",
			"This allows you to read rows from a previous entry in a job.",
			"Use this step to write rows to the executing job."+Const.CR+"The information will then be passed to the next entry in this job.",
			"Read rows of data from a data cube.",
			"Write rows of data to a data cube",                            // 30
			"Execute a database query using stream values as parameters",   
			"Reads records from an XBase type of database file (DBF)",
			"Read data from a Microsoft Excel Workbook.  This works with Excel sheets of Excel 95, 97 and 2000.",
			"Sets a field value to null if it is equal to a constant value",
            "Create new fields by performing simple calculations",
            "Execute an SQL script, optionally parameterized using input rows",
            "Run a mapping (sub-transformation), use MappingInput and MappingOutput to specify the fields interface",
            "Specify the input interface of a mapping",
            "Specify the output interface of a mapping",
            "Read data from an XML file",
            "Wite data to an XML file",
            "Merge two streams of rows, sorted on a certain key.  The two streams are compared and the equals, changed, deleted and new rows are flagged.",
            "Add one or more constants to the input rows",
            "Denormalises rows by looking up key-value pairs and by assigning them to new fields in the output rows."+Const.CR+"This method aggregates and needs the input rows to be sorted on the grouping fields",
            "Flattens consequetive rows based on the order in which they appear in the input stream"
		};

	public static final String image_filename[] =
		{
		 	null,
			"TFI.png",
			"TFO.png",
			"TIP.png",
			"TOP.png",
			"SEL.png",
			"FLT.png",
			"DLU.png",
			"SRT.png",
			"SLU.png",
			"SEQ.png",
			"DIM.png",
			"CMB.png",
			"DUM.png",
			"JRW.png",
			"AGG.png",
			"SYS.png",
			"GEN.png",
			"SCR.png",
			"PRC.png",
			"INU.png",
			"UPD.png",
			"NRM.png",
			"SPL.png",
			"UNQ.png",
			"GRP.png",
			"FCH.png",
			"TCH.png",
			"CIP.png",
			"COP.png",
			"DBJ.png",
			"XBI.png",
			"XLI.png",
			"NUI.png",
            "CLC.png",
            "SQL.png",
            "MAP.png",
            "MPI.png",
            "MPO.png",
            "XIN.png",
            "XOU.png",
            "MRG.png",
            "CST.png",
            "UNP.png",
            "FLA.png"
		};
	
	public static final String category[] = 
		{
			null,
			"Input", 		    // "TextFileInput",
			"Output", 		    // "TextFileOutput",
			"Input", 		    // "TableInput",
			"Output", 		    // "TableOutput",
			"Transform", 	    // "SelectValues",
			"Transform", 	    // "FilterRows",
			"Lookup", 		    // "DBLookup",
			"Transform", 	    // "SortRows",              
			"Lookup", 		    // "StreamLookup",  
			"Transform", 	    // "Sequence",
			"Data Warehouse",   // "DimensionLookup",
			"Data Warehouse",   // "CombinationLookup",
			"Transform", 	    // "Dummy",
			"Transform", 	    // "JoinRows",
			"Transform", 	    // "AggregateRows",
			"Input", 		    // "SystemInfo",
			"Input", 		    // "RowGenerator",
			"Transform", 	    // "ScriptValue",
			"Lookup", 		    // "DBProc",               
			"Output", 		    // "InsertUpdate",
			"Output",           // "Update"
			"Transform", 	    // "Normaliser",         
			"Extra", 	        // "FieldSplitter",
			"Transform", 	    // "Unique",
			"Transform", 	    // "GroupBy",
			"Extra", 		    // "RowsFromResult",
			"Extra", 		    // "RowsToResult",
			"Extra", 		    // "CubeInput",
			"Extra", 		    // "CubeOutput",
			"Lookup", 		    // "DBJoin",
			"Input", 		    // "XBaseInput"
			"Input",            // "ExcelInput"
			"Extra",            // "NullIf"
            "Transform",        // "Calculator"
            "Extra",            // "ExecSQL"
            "Mapping",          // "Mapping"
            "Mapping",          // "MappingInput"
            "Mapping",          // "MappingOutput"
            "Input",            // "XMLInput"
            "Output",           // "XMLOutut"
            "Transform",        // "MergRows"
            "Transform",        // "Constant"
            "Transform",        // "Denormaliser"
            "Transform"         // "Flattener"
		};

    public static final String category_order[] = { "Input", "Output", "Lookup", "Transform", "Data Warehouse", "Extra", "Mapping", "Experimental" };
    
	private static final int MIN_PRIORITY    =  1;
	private static final int LOW_PRIORITY    =  3;
	private static final int NORMAL_PRIORITY =  5;
	private static final int HIGH_PRIORITY   =  7;
	private static final int MAX_PRIORITY    = 10;
	
	private    TransMeta transMeta;
	private    StepMeta  stepMeta;
	private    String stepname;
	protected  LogWriter log;
	private    Trans trans;
	public     String debug;
	public     ArrayList previewBuffer;
	public     int       previewSize;
	
	public  long linesRead;    // # lines read from previous step(s)
	public  long linesWritten; // # lines written to next step(s)
	public  long linesInput;   // # lines read from file or database
	public  long linesOutput;  // # lines written to file or database
	public  long linesUpdated; // # lines updated in database (dimension)
	public  long linesSkipped; // # lines passed without alteration (dimension)
	
	public  long get_sleeps;    // # total get sleep time in nano-seconds
	public  long put_sleeps;    // # total put sleep time in nano-seconds

	private boolean distributed;
	private long errors;
	
	private StepMeta next[];
	private StepMeta prev[];
	private int     in_handling, out_handling;
	public    ArrayList thr;
    
	protected ArrayList inputRowSets;
	protected ArrayList outputRowSets;
	
	public boolean stopped;
	public boolean waiting;
	public boolean init;
	
	private int stepcopy; // The copy number of THIS thread.
	
	private int output_rowset_nr;  // for fixed input channel: StreamLookup
	private Date start_time, stop_time;
	
	public boolean first;
	
	public boolean   terminator;
	public ArrayList terminator_rows;
	
	private StepMetaInterface stepMetaInterface;
	private StepDataInterface stepDataInterface;
    
    private List rowListeners; // List of RowListener interfaces
	
	/**
	 * This is the base step that forms that basis for all steps.  You can derive from this class to implement your own steps.
	 * 
	 * @param stepMeta The StepMeta object to run.
	 * @param stepDataInterface the data object to store temporary data, database connections, caches, result sets, hashtables etc.
	 * @param copyNr The copynumber for this step.
	 * @param transMeta The TransInfo of which the step stepMeta is part of.
	 * @param trans The (running) transformation to obtain information shared among the steps.
	 */
	public BaseStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super();
		
		log = LogWriter.getInstance();
		this.stepMeta=stepMeta;
		this.stepDataInterface = stepDataInterface;
		this.stepcopy+=copyNr;
		this.transMeta=transMeta;
		this.trans=trans;
		
		first=true;
		
		stepname=stepMeta.getName();
		stopped = false;
		init    = false;
		
		linesRead    = 0L;  // Keep some statistics!
  		linesWritten = 0L;
		linesUpdated = 0L;
		linesSkipped = 0L;
				
		get_sleeps=0L;
		put_sleeps=0L;
		
		inputRowSets=null;
		outputRowSets=null;
		next=null;
		
		terminator      = stepMeta.hasTerminator();
		if (terminator)
		{
			terminator_rows = new ArrayList();
		}
		else
		{
			terminator_rows = null;
		}
		
		debug="-";
		
		output_rowset_nr=-1;
		start_time = null;
		stop_time  = null;
		
		distributed = stepMeta.distributes;
		
		if (distributed)	logDetailed("distribution activated");
		else 			logDetailed("distribution de-activated");
		
        rowListeners = new ArrayList();
        
		dispatch();
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		sdi.setStatus(StepDataInterface.STATUS_INIT);
		return true;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		sdi.setStatus(StepDataInterface.STATUS_DISPOSED);
	}
		
	public long getProcessed()
	{
		return linesRead;
	}
	
	public void setCopy(int cop)
	{
		stepcopy=cop;
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
		errors=e;
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
	
	public String getStatus()
	{
		String retval;
		
		if (isAlive())
		{
			retval="running";
		}
		else
		{
			if (isInitialising())
			{
				retval="init";
			}
			else
			{
				if (isStopped())
				{
					retval="stopped";
				}
				else
				{
					retval="finished";
				}
			}
		}
		return retval;
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
	 * putRow is used to copy a row, to the alternate rowset(s)
	 * This should get priority over everything else! (synchronized)
	 * If distribute is true, a a row is copied only once to a single output rowset!
	 * 
	 * @param row The row to put to the destination rowsets.
	 */
	public synchronized void putRow(Row row)
	{
        if (previewSize>0 && previewBuffer.size()<previewSize) 
		{
            previewBuffer.add(new Row(row));
		}
        
        // call all rowlisteners...
        for (int i=0;i<rowListeners.size();i++)
        {
            RowListener rowListener = (RowListener)rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }
		
		// Keep adding to terminator_rows buffer...
		if (terminator && terminator_rows!=null)
		{
			terminator_rows.add(new Row(row));
		}
		
		if (outputRowSets.size()==0) 
        {
            // No more output rowsets!
            return; // we're done here!
        }
		
		//logDebug("putRow() start, output:"+output.size()+", line="+lines_read);

		// Before we copy this row to output, wait for room...
		for (int i=0;i<outputRowSets.size();i++)  // Wait for all rowsets: keep synchronised!
		{
			int sleeptime=transMeta.getSleepTimeFull();
			RowSet rs=(RowSet)outputRowSets.get(i);

			try
			{
				rs.setPriorityFrom(calcPutPriority(rs));
			}
			catch(Exception e)
			{
				logError("Error occured setting priorityFrom");
				setErrors(1);
				stopAll();
				return;
			}
			
			while(rs.isFull() && !stopped) 
			{			
				try{ sleep(sleeptime); } 
				catch(Exception e) 
				{
					logError("Interupted while trying to put a new row in a buffer: "+e.toString()); 
					setErrors(1); 
					stopAll(); 
					return; 
				}
				put_sleeps+=sleeptime;
				if (sleeptime<500) sleeptime*=1.2; else sleeptime=500;
			}
		}
        
		if (stopped)
		{
			logDebug("Stopped while putting a row on the buffer");
			stopAll();
			return;
		}
		
		if (distributed)
		{
			// Copy the row to the "next" output rowset.
			// We keep the next one in out_handling
			RowSet rs=(RowSet)outputRowSets.get(out_handling);
			rs.putRow(row);
			linesWritten++;
			
			// Now determine the next output rowset!
			// Only if we have more then one output...
			if (outputRowSets.size()>1)
			{
				out_handling++;
				if (out_handling>=outputRowSets.size()) out_handling=0;
			}
		}
		else // Copy the row to all output rowsets!
		{
            // set row in first output rowset
			RowSet rs=(RowSet)outputRowSets.get(0);
			rs.putRow(row);
			linesWritten++;
			
			// Copy to the row in the other output rowsets...		
			for (int i=1;i<outputRowSets.size();i++)  // start at 1, 0==input rowset
			{
				rs=(RowSet)outputRowSets.get(i);
				rs.putRow(new Row(row));
			}
		}
	}
	
	/**
     * This version of getRow() only takes data from certain rowsets We select
     * these rowsets that have name = step Otherwise it's the same as the other
     * one.
     * @param row the row to send to the destination step
     * @param to  the name of the step to send the row to
     */
    public synchronized void putRowTo(Row row, String to) throws KettleStepException
    {
        output_rowset_nr = findOutputRowSetNumber(stepname, getCopy(), to, 0);
        if (output_rowset_nr < 0) 
        { 
            //
            // No rowset found: normally it can't happen:
            // we deleted the rowset because it was
            // finished
            //
            throw new KettleStepException("Unable to find rowset for target step ["+to+"]"); 
        }

        putRowTo(row, output_rowset_nr);
    }

	/**
	 * putRow is used to copy a row, to the alternate rowset(s)
	 * This should get priority over everything else! (synchronized)
	 * If distribute is true, a a row is copied only once to a single output rowset!
	 * 
	 * @param row The row to put to the destination rowsets.
	 * @param output_rowset_nr the number of the rowset to put the row to.
	 */
	public synchronized void putRowTo(Row row, int output_rowset_nr)
	{
		int sleeptime;
		
		if (previewSize>0 && previewBuffer.size()<previewSize) 
		{
			previewBuffer.add(new Row(row));
		}
        
        // call all rowlisteners...
        for (int i=0;i<rowListeners.size();i++)
        {
            RowListener rowListener = (RowListener)rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }
		
		// Keep adding to terminator_rows buffer...
		if (terminator && terminator_rows!=null)
		{
			terminator_rows.add(new Row(row));
		}
		
		if (outputRowSets.size()==0) return; // nothing to do here!
	
		RowSet rs = (RowSet) outputRowSets.get(output_rowset_nr);
		
		sleeptime=transMeta.getSleepTimeFull();
		while(rs.isFull() && !stopped) 
		{			
			try{ sleep(sleeptime); } 
			catch(Exception e) 
			{
				logError("Interupted while trying to put a new row in a buffer: "+e.toString()); 
				setErrors(1); 
				stopAll(); 
				return; 
			}
			put_sleeps+=sleeptime;
			if (sleeptime<500) sleeptime*=1.2; else sleeptime=500;
		}
		if (stopped)
		{
			logDebug("Stopped while putting a row on the buffer");
			stopAll();
			return;
		}
		
		// Don't distribute or anything, only go to this rowset!
		rs.putRow(row);
		linesWritten++;
	}

    
	private synchronized RowSet currentInputStream()
	{
		return (RowSet)inputRowSets.get(in_handling);
	}
	
	/**
	  Find the next not-finished input-stream...
	  in_handling says which one...
	**/
	private synchronized void nextInputStream()
	{
		int streams=inputRowSets.size();
		
		// No more streams left: exit!
		if (streams==0) return;
		
		// If we have some left: take the next!
		in_handling++;
		if (in_handling >= inputRowSets.size()) in_handling=0;		
		//logDebug("nextInputStream advanced to in_handling="+in_handling);	
	}
	
	/**
		In case of getRow, we receive data from previous steps through the input rowset.
	 	In case we split the stream, we have to copy the data to the alternate splits: rowsets 1 through n.
	**/
	public synchronized Row getRow()
	{
		int sleeptime;
		int switches;
		
		// If everything is finished, we can stop immediately!
		//if (input.size()==0) return null;

		// What's the current input stream?
		RowSet in=currentInputStream();
		switches=0;
		sleeptime=transMeta.getSleepTimeEmpty();
		while (in.isEmpty() && !stopped)
		{
			// in : empty
			if (/*in.isEmpty() &&*/ in.isDone()) // nothing more here: remove it from input
			{
				inputRowSets.remove(in_handling);
				if (inputRowSets.size()==0) // nothing more to be found! 
				{
					return null;
				}
			}
			nextInputStream();
			in=currentInputStream();
			switches++;
			if (switches>=inputRowSets.size()) // every n looks, wait a bit! Don't use too much CPU!
			{
				switches=0;
				try { sleep(0, sleeptime); } catch(Exception e) 
				{ 
					logError("Sleep interupted! Stopping: "+e.toString());
					setErrors(1); 
					stopAll(); 
					return null; 
				}
				if (sleeptime<5000) sleeptime*=1.2; else sleeptime=5000; 
				get_sleeps+=sleeptime;
			}
		}
		if (stopped) 
		{
			logDebug("Stopped looking for more rows."); 
			stopAll(); 
			return null; 
		} 
		
		// Set the appropriate priority depending on the amount of data in the rowset:
		in.setPriorityTo(calcGetPriority(in));
		
		// Get this row!
		Row row=in.getRow();
		linesRead++;
        
        // Notify all rowlisteners...
        for (int i=0;i<rowListeners.size();i++)
        {
            RowListener rowListener = (RowListener)rowListeners.get(i);
            rowListener.rowReadEvent(row);
        }
		
		nextInputStream(); // Look for the next input stream to get row from.

		return row;
	}

	/**
	    This version of getRow() only takes data from certain rowsets
	    We select these rowsets that have name = step
	    Otherwise it's the same as the other one.
	**/
	public synchronized Row getRowFrom(String from)
	{
		output_rowset_nr = findInputRowSetNumber(from, 0, stepname, 0);
		if (output_rowset_nr<0) // No rowset found: normally it can't happen: we deleted the rowset because it was finished 
		{
			return null;
		} 
		
		return getRowFrom(output_rowset_nr);
	}
	
	public synchronized Row getRowFrom(int input_rowset_nr)
	{
		// Read from one specific rowset
		//
		int sleeptime=transMeta.getSleepTimeEmpty();

		RowSet in=(RowSet)inputRowSets.get(input_rowset_nr);
		while (in.isEmpty() && !in.isDone() && !stopped) 
		{
			try { sleep(0, sleeptime); } catch(Exception e) 
			{
				logError("Sleep interupted while looking for more rows from step ["+in.getOriginStepName()+"] --> "+e.toString());
				setErrors(1);
				stopAll();
				return null;
			}
			get_sleeps+=sleeptime;
		}  
		
		if (stopped)
		{
			logError("Interupted while looking for more rows from step ["+in.getOriginStepName()+"]");
			stopAll();
			return null;
		}

		if (in.isEmpty() && in.isDone())
		{
			inputRowSets.remove(input_rowset_nr);
			return null;
		}

		Row row=in.getRow();  // Get this row!
		linesRead++;
        
        // call all rowlisteners...
        for (int i=0;i<rowListeners.size();i++)
        {
            RowListener rowListener = (RowListener)rowListeners.get(i);
            rowListener.rowWrittenEvent(row);
        }
		
		return row;
	}
	
	private synchronized int findInputRowSetNumber(String from, int fromcopy, String to, int tocopy)
	{
		int i;
		for (i=0; i<inputRowSets.size();i++)
		{
			RowSet rs = (RowSet)inputRowSets.get(i);
			if (rs.getOriginStepName().equalsIgnoreCase(from) &&
				rs.getDestinationStepName().equalsIgnoreCase(to) &&
				rs.getOriginStepCopy() == fromcopy && 
				rs.getDestinationStepCopy() == tocopy
				)
			return i;
		}
		return -1;
	}

	
	private synchronized int findOutputRowSetNumber(String from, int fromcopy, String to, int tocopy)
	{
		int i;
		for (i=0; i<outputRowSets.size();i++)
		{
			RowSet rs = (RowSet)outputRowSets.get(i);
			if (rs.getOriginStepName().equalsIgnoreCase(from) &&
				rs.getDestinationStepName().equalsIgnoreCase(to) &&
				rs.getOriginStepCopy() == fromcopy && 
				rs.getDestinationStepCopy()   == tocopy
				)
			return i;
		}
		return -1;
	}

	//
	// We have to tell the next step we're finished with 
	// writing to output rowset(s)!
	//
	public void setOutputDone()
	{
		logDebug("Signaling 'output done' to "+outputRowSets.size()+" output rowsets.");
		for (int i=0;i<outputRowSets.size();i++)
		{
			RowSet rs=(RowSet)outputRowSets.get(i);
			rs.setDone();
		}
	}

	/**
	 * This method finds the surrounding steps and rowsets for this base step.
	 * This steps keeps it's own list of rowsets (etc.) to prevent it from having to search every time.
	 */
	public void dispatch()
	{
		int i,c;
		RowSet rs;
		int nrinput, nroutput;
		int nrcopies, prevcopies, nextcopies;
		int disptype;
        
        if (transMeta==null) // for preview reasons, no dispatching is done!
        {
            return;
        }
        
		StepMeta stepMeta = transMeta.findStep(stepname);

		logDetailed("Starting allocation of buffers & new threads...");
		
		// How many next steps are there? 0, 1 or more??
		// How many steps do we send output to?
		nrinput  = transMeta.findNrPrevSteps(stepMeta, true);
		nroutput = transMeta.findNrNextSteps(stepMeta);
		
		inputRowSets   = new ArrayList(); // new RowSet[nrinput];
		outputRowSets  = new ArrayList(); // new RowSet[nroutput+out_copies];
		prev    = new StepMeta[nrinput];
		next    = new StepMeta[nroutput];

		in_handling = 0;  // we start with input[0];

		logDetailed("Step info: nrinput="+nrinput+" nroutput="+nroutput);
				
		for (i=0;i<nrinput;i++)
		{
			prev[i]=transMeta.findPrevStep(stepMeta, i, true); // sir.getHopFromWithTo(stepname, i);
			logDetailed("Got previous step from ["+stepname+"] #"+i+" --> "+prev[i].getName());
			
			// Looking at the previous step, you can have either 1 rowset to look at or more then one.
			prevcopies = prev[i].getCopies();
			nextcopies = stepMeta.getCopies(); 
			logDetailed("input rel is  "+prevcopies+":"+nextcopies);
	
			if      (prevcopies==1 && nextcopies==1) { disptype=Trans.TYPE_DISP_1_1; nrcopies = 1; } 
			else if (prevcopies==1 && nextcopies >1) { disptype=Trans.TYPE_DISP_1_N; nrcopies = 1; } 
			else if (prevcopies >1 && nextcopies==1) { disptype=Trans.TYPE_DISP_N_1; nrcopies = prevcopies; } 
			else if (prevcopies==nextcopies)         { disptype=Trans.TYPE_DISP_N_N; nrcopies = 1; } // > 1!
			else 
			{
				log.logError(toString(), "Only 1-1, 1-n, n-1 and n-n relationships are allowed!");
				log.logError(toString(), "This means you can't have x-y relationships!");
				setErrors(1);
				stopAll();
				return;
			}
			for (c=0;c<nrcopies;c++)
			{
				rs=null;
				switch(disptype)
				{
				case Trans.TYPE_DISP_1_1: rs=trans.findRowSet(prev[i].getName(),         0, stepname, 0        ); break;
				case Trans.TYPE_DISP_1_N: rs=trans.findRowSet(prev[i].getName(),         0, stepname, getCopy()); break;
				case Trans.TYPE_DISP_N_1: rs=trans.findRowSet(prev[i].getName(),         c, stepname, 0        ); break;
				case Trans.TYPE_DISP_N_N: rs=trans.findRowSet(prev[i].getName(), getCopy(), stepname, getCopy()); break;
				}
				if (rs!=null) 
				{
					inputRowSets.add(rs);
					logDetailed("Found input rowset ["+rs.getName()+"]");
				} 
				else
				{
					logError("Unable to find input rowset!");
					setErrors(1);
					stopAll();
					return;
				} 
			}
		}
		// And now the output part!
		for (i=0;i<nroutput;i++)
		{
			next[i]= transMeta.findNextStep(stepMeta, i);
			
			prevcopies = stepMeta.getCopies();
			nextcopies = next[i].getCopies();

			logDetailed("output rel. is  "+prevcopies+":"+nextcopies);

			if      (prevcopies==1 && nextcopies==1) { disptype=Trans.TYPE_DISP_1_1; nrcopies = 1;          } 
			else if (prevcopies==1 && nextcopies >1) { disptype=Trans.TYPE_DISP_1_N; nrcopies = nextcopies; } 
			else if (prevcopies >1 && nextcopies==1) { disptype=Trans.TYPE_DISP_N_1; nrcopies = 1;          } 
			else if (prevcopies==nextcopies)         { disptype=Trans.TYPE_DISP_N_N; nrcopies = 1;          } // > 1!
			else 
			{
				log.logError(toString(), "Only 1-1, 1-n, n-1 and n-n relationships are allowed!");
				log.logError(toString(), "This means you can't have x-y relationships!");
				setErrors(1);
				stopAll();
				return;
			}
			for (c=0;c<nrcopies;c++)
			{
				rs=null;
				switch(disptype)
				{
				case Trans.TYPE_DISP_1_1: rs=trans.findRowSet(stepname,         0, next[i].getName(),         0); break;
				case Trans.TYPE_DISP_1_N: rs=trans.findRowSet(stepname,         0, next[i].getName(),         c); break;
				case Trans.TYPE_DISP_N_1: rs=trans.findRowSet(stepname, getCopy(), next[i].getName(),         0); break;
				case Trans.TYPE_DISP_N_N: rs=trans.findRowSet(stepname, getCopy(), next[i].getName(), getCopy()); break;
				}
				if (rs!=null) 
				{
					outputRowSets.add(rs);
					logDetailed("Found output rowset ["+rs.getName()+"]");
				} 
				else
				{
					logError("Unable to find output rowset!");
					setErrors(1);
					stopAll();
					return;
				} 
			}
		}

		logDetailed("Finished dispatching");
	}
	
	public void logMinimal(String s)
	{
		log.println(LogWriter.LOG_LEVEL_MINIMAL, stepname+"."+stepcopy, s);
	}
	
	public void logBasic(String s)
	{
		log.println(LogWriter.LOG_LEVEL_BASIC, stepname+"."+stepcopy, s);
	}

	public void logError(String s)
	{
		log.println(LogWriter.LOG_LEVEL_ERROR, stepname+"."+stepcopy, s);
	}

	public void logDetailed(String s)
	{
		log.println(LogWriter.LOG_LEVEL_DETAILED, stepname+"."+stepcopy, s);
	}

	public void logDebug(String s)
	{
		log.println(LogWriter.LOG_LEVEL_DEBUG, stepname+"."+stepcopy, s);
	}

	public void logRowlevel(String s)
	{
		log.println(LogWriter.LOG_LEVEL_ROWLEVEL, stepname+"."+stepcopy, s);
	}
	
	public int getNextClassNr()
	{
		int ret = trans.class_nr;
		trans.class_nr++;
		
		return ret;
	}
	
	public boolean outputIsDone()
	{
		int nrstopped=0;
		RowSet rs;
		int i;
		
		for (i=0;i<outputRowSets.size();i++)
		{
			rs=(RowSet)outputRowSets.get(i);
			if (rs.isDone()) nrstopped++; 
		}
		return nrstopped>=outputRowSets.size();
	}
	
	public void stopAll()
	{
		stopped=true;
	}
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public boolean isInitialising()
	{
		return init;
	}
	
	public void markStart()
	{
		Calendar cal=Calendar.getInstance();
		start_time=cal.getTime();
	}

	public void markStop()
	{
		Calendar cal=Calendar.getInstance();
		stop_time=cal.getTime();
	}
	
	public long getRuntime()
	{
		long lapsed;
		if (start_time!=null && stop_time==null)
		{
			Calendar cal=Calendar.getInstance();
			long now = cal.getTimeInMillis();
			long st  = start_time.getTime();
			lapsed = now - st;
		}
		else
		if (start_time!=null && stop_time!=null)
		{
			lapsed = stop_time.getTime() - start_time.getTime();
		}
		else
		{
			lapsed = 0;
		}
		
		return lapsed;
	}

	public Row buildLog(String sname, int copynr, 
						long lines_read, 
						long lines_written,
						long lines_updated, 
						long lines_skipped, 
						long errors,
	                    Value start_date, Value end_date
	                    )
	{
		Row r = new Row();
		
		r.addValue( new Value("stepname",      sname)          );
		r.addValue( new Value("copy",          (double)copynr) );
		r.addValue( new Value("lines_read",    (double)lines_read)  );
		r.addValue( new Value("lines_written", (double)lines_written)  );
		r.addValue( new Value("lines_updated", (double)lines_updated)  );
		r.addValue( new Value("lines_skipped", (double)lines_skipped)  );
		r.addValue( new Value("errors",        (double)errors) );
		r.addValue( start_date );
		r.addValue( end_date );
		
		return r;
	}
	
	public static final Row getLogFields(String comm)
	{
		Row r = new Row();
		int i;
		Value sname = new Value("stepname",  ""              );
		sname.setLength(256); 
		r.addValue( sname );
		
		r.addValue( new Value("copy",          0.0             ) );
		r.addValue( new Value("lines_read",    0.0             ) );
		r.addValue( new Value("lines_written", 0.0             ) );
		r.addValue( new Value("lines_updated", 0.0             ) );
		r.addValue( new Value("lines_skipped", 0.0             ) );
		r.addValue( new Value("errors",        0.0             ) );
		r.addValue( new Value("start_date",    Const.MIN_DATE  ) );
		r.addValue( new Value("end_date",      Const.MAX_DATE  ) );
		
		for (i=0;i<r.size();i++)
		{
			r.getValue(i).setOrigin(comm);
		}
		
		return r;
	}
	
	public String toString()
	{
		return stepname+"."+getCopy();
	}
	
	private int calcPutPriority(RowSet rs)
	{
		if (rs.size() > transMeta.getSizeRowset() * 0.95) return MIN_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.75) return LOW_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.50) return NORMAL_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.25) return HIGH_PRIORITY;
		return MAX_PRIORITY;
	}

	private int calcGetPriority(RowSet rs)
	{
		if (rs.size() > transMeta.getSizeRowset() * 0.95) return MAX_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.75) return HIGH_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.50) return NORMAL_PRIORITY;
		if (rs.size() > transMeta.getSizeRowset() * 0.25) return LOW_PRIORITY;
		return MIN_PRIORITY;
	}

	public int rowsetOutputSize()
	{
		int size=0;
		int i;
		for (i=0;i<outputRowSets.size();i++)
		{
			size+=((RowSet)outputRowSets.get(i)).size();
		}
		
		return size;
	}

	public int rowsetInputSize()
	{
		int size=0;
		int i;
		for (i=0;i<inputRowSets.size();i++)
		{
			size+=((RowSet)inputRowSets.get(i)).size();
		}
		
		return size;
	}

	/**
	 * Create a new empty StepMeta class from the steploader
	 * @param stepplugin The step/plugin to use
	 * @param steploader The StepLoader to load from
	 * @return The requested class.
	 */
	public static final StepMetaInterface getStepInfo(StepPlugin stepplugin, StepLoader steploader)
		throws KettleStepLoaderException
	{
		return steploader.getStepClass(stepplugin);
	}
	
	public static final String getIconFilename(int steptype)
	{
		return image_filename[steptype];
	}
	
	/**
	 * Perform actions to stop a running step.
	 * This can be stopping running SQL queries (cancel), etc.
	 * Default it doesn't do anything.
	 */
	public void stopRunning()
	{
	    
	}
	
	public void logSummary()
	{
		logBasic("Finished processing (I="+linesInput+", O="+linesOutput+", R="+linesRead+", W="+linesWritten+", U="+linesUpdated+", E="+getErrors());
	}
    
    public String getStepID()
    {
        if (stepMeta!=null) return stepMeta.getStepID();
        return null;
    }

    /**
     * @return Returns the inputRowSets.
     */
    public ArrayList getInputRowSets()
    {
        return inputRowSets;
    }

    /**
     * @param inputRowSets The inputRowSets to set.
     */
    public void setInputRowSets(ArrayList inputRowSets)
    {
        this.inputRowSets = inputRowSets;
    }

    /**
     * @return Returns the outputRowSets.
     */
    public ArrayList getOutputRowSets()
    {
        return outputRowSets;
    }

    /**
     * @param outputRowSets The outputRowSets to set.
     */
    public void setOutputRowSets(ArrayList outputRowSets)
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

    public List getRowListeners()
    {
        return rowListeners;
    }
}
