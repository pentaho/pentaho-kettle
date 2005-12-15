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
 
package be.ibridge.kettle.trans.step.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Groups informations based on aggregation rules. (sum, count, ...)
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class GroupBy extends BaseStep implements StepInterface
{
	private GroupByMeta meta;
	private GroupByData data;
	
	public GroupBy(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(GroupByMeta)getStepMeta().getStepMetaInterface();
		data=(GroupByData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		debug="processRow";
		
		Row r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
            if (meta.passAllRows())  // ALL ROWS
            {
                if (data.previous!=null)
                {
                    calcAggregate(data.previous);
                    addToBuffer(data.previous);
                }
                data.groupResult = getAggregateResult();

                Row row = getRowFromBuffer();
                while (row!=null)
                {
                    row.addRow(data.groupResult);
                    putRow(row);
                    row = getRowFromBuffer();
                }
                closeInput();
            }
            else   // JUST THE GROUP + AGGREGATE
            {
    			// Don't forget the last set of rows...
    			if (data.previous!=null) 
    			{
    				calcAggregate(data.previous);
    				
    				Row result = buildResult(data.previous);
    				putRow(result);
    			} 
    		}
			setOutputDone();
			return false;
		}
		
		//System.out.println("r = "+r);
		
		if (first)
		{
			data.counts     = new long[meta.getSubjectField().length];
			data.subjectnrs = new int[meta.getSubjectField().length];
			for (int i=0;i<meta.getSubjectField().length;i++)
			{
				data.subjectnrs[i] = r.searchValueIndex(meta.getSubjectField()[i]);
				if (data.subjectnrs[i]<0)
				{
					logError("Aggregate subject field ["+meta.getSubjectField()[i]+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
			data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = r.searchValueIndex(meta.getGroupField()[i]);
				if (data.groupnrs[i]<0)
				{
					logError("Grouping field ["+meta.getGroupField()[i]+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}				
			}

			data.previous=new Row(r); // copy the row to previous
			newAggregate(r);         // Create a new group aggregate (init)
			
			// System.out.println("FIRST, agg="+agg);

			first=false;
		}
		else
		{
			calcAggregate(data.previous);
			//System.out.println("After calc, agg="+agg);

			if (meta.passAllRows())
			{
                addToBuffer(data.previous);
			}
		}
				
		// System.out.println("Check for same group...");
        
		if (!sameGroup(data.previous, r))
		{
            // System.out.println("Different group!");
            
            if (meta.passAllRows())
            {
                // System.out.println("Close output...");
                
                // Not the same group: close output (if any)
                closeOutput();

                // System.out.println("getAggregateResult()");

                // Get all rows from the buffer!
                data.groupResult = getAggregateResult();

                // System.out.println("dump rows from the buffer");

                Row row = getRowFromBuffer();
                while (row!=null)
                {
                    row.addRow(data.groupResult);
                    putRow(row);
                    row = getRowFromBuffer();
                }
                closeInput();
            }
            else
            {
    			Row result = buildResult(data.previous);
    			putRow(result);        // copy row to possible alternate rowset(s).
            }
            newAggregate(r);       // Create a new group aggregate (init)
		}

		data.previous=new Row(r);
        

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}
	
	// Is the row r of the same group as previous?
	private boolean sameGroup(Row previous, Row r)
	{
		debug="sameGroup";
		
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value prev = previous.getValue(data.groupnrs[i]);
			Value curr = r.getValue(data.groupnrs[i]);
			
			if (!prev.equals(curr)) return false;
		}
		
		return true;
	}
	
	// Calculate the aggregates in the row...
	private void calcAggregate(Row r)
	{
		debug="calcAggregate start";
		
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			debug="calcAggregate start loop";
			Value subj = r.getValue(data.subjectnrs[i]);
			Value value = data.agg.getValue(i);
			
			//System.out.println("  calcAggregate value, i="+i+", agg.size()="+agg.size()+", subj="+subj+", value="+value);
			
			debug="calcAggregate switch";
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            :
					value.plus(subj);
					break; 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
					value.plus(subj);
				data.counts[i]++;
					break; 
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      :
					data.counts[i]++;
					break;
				case GroupByMeta.TYPE_GROUP_MIN            :
					if (subj.compare(value)<0) value.setValue(subj);
					break; 
				case GroupByMeta.TYPE_GROUP_MAX            : 
					if (subj.compare(value)>0) value.setValue(subj);
					break; 
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    if (!subj.isNull()) 
                    {
                        if (value.getStringLength()>0) value.setValue(value.getString()+", ");
                        value.setValue(value.getString()+subj.getString());
                    }
                    break; 
				default: break;
			}
		}
		debug="calcAggregate end";
	}

	// Initialize a group..
	private void newAggregate(Row r)
	{
		debug="newAggregate";
		
		// Put all the counters at 0
		for (int i=0;i<data.counts.length;i++) data.counts[i]=0;
		
		data.agg = new Row();
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Value subj = r.getValue(data.subjectnrs[i]);
			Value v=null;
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            : 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      :
                    v = new Value(meta.getAggregateField()[i], subj.isNumeric()?subj.getType():Value.VALUE_TYPE_NUMBER);
                    switch(subj.getType())
                    {
                    case Value.VALUE_TYPE_BIGNUMBER: v.setValue(new BigDecimal(0)); break;
                    case Value.VALUE_TYPE_INTEGER:   v.setValue(0L); break;
                    case Value.VALUE_TYPE_NUMBER:    
                    default:                         v.setValue(0.0); break;
                    }
					break; 
				case GroupByMeta.TYPE_GROUP_MIN            : 
				case GroupByMeta.TYPE_GROUP_MAX            : 
					v = new Value(meta.getAggregateField()[i], subj.getType());
					v.setValue(subj);
					break;
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    v = new Value(meta.getAggregateField()[i], Value.VALUE_TYPE_STRING);
                    v.setValue("");
                    break; 
				default: break;
			}
            
            if (meta.getAggregateType()[i]!=GroupByMeta.TYPE_GROUP_COUNT_ALL)
            {
                v.setLength(subj.getLength(), subj.getPrecision());
            }
			if (v!=null) data.agg.addValue(v);
		}
	}
	
	private Row buildResult(Row r) throws KettleValueException
	{
		debug="buildResult";
		
		Row result = new Row();
		debug="buildResult 1";
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value gr = r.getValue(data.groupnrs[i]);
			result.addValue(gr);
		}
		debug="buildResult 2";
        
        result.addRow(getAggregateResult());
        
		debug="buildResult end";
		return result;
	}
    
    private Row getAggregateResult() throws KettleValueException
    {
        Row result = new Row();

        for (int i=0;i<data.subjectnrs.length;i++)
        {
            Value ag = data.agg.getValue(i);
            switch(meta.getAggregateType()[i])
            {
                case GroupByMeta.TYPE_GROUP_SUM            : break; 
                case GroupByMeta.TYPE_GROUP_AVERAGE        : ag.divide(new Value("c", data.counts[i])); break; 
                case GroupByMeta.TYPE_GROUP_COUNT_ALL      : ag.setValue(data.counts[i]); break;
                case GroupByMeta.TYPE_GROUP_MIN            : break; 
                case GroupByMeta.TYPE_GROUP_MAX            : break; 
                default: break;
            }
            result.addValue(ag);
        }

        return result;

    }

    /*
	private Row buildRegular(Row r)
	{
		debug="buildRegular start";
		
		Row result = new Row();

        // Group
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value gr = r.getValue(data.groupnrs[i]);
			result.addValue(gr);
		}

        // Subjects
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Value ag = r.getValue(data.subjectnrs[i]);
			// Rename to the new aggregate names!
			ag.setName(meta.getAggregateField()[i]);
			result.addValue(ag);
		}

		debug="buildRegular end";
		return result;
	}
    */
		
    private void addToBuffer(Row row) throws KettleFileException
    {
        //System.out.println("Add to buffer: "+row);
        
        data.bufferList.add(row);
        if (data.bufferList.size()>5000)
        {
            if (data.rowsOnFile==0)
            {
                try
                {
                    data.tempFile = File.createTempFile(meta.getPrefix(), ".tmp", new File(Const.replEnv(meta.getDirectory())));
                    data.fos=new FileOutputStream(data.tempFile);
                    data.dos=new DataOutputStream(data.fos);
                    data.firstRead = true;
                }
                catch(IOException e)
                {
                    throw new KettleFileException("Unable to create temporary file", e);
                }
            }
            // OK, save the oldest rows to disk!
            Row oldest = (Row) data.bufferList.get(0);
            oldest.write(data.dos);
            data.bufferList.remove(0);
            data.rowsOnFile++;
        }
    }
    
    private Row getRowFromBuffer() throws KettleFileException
    {
        if (data.rowsOnFile>0)
        {
            if (data.firstRead)
            {
                // Open the inputstream first...
                try
                {
                    data.fis=new FileInputStream( data.tempFile );
                    data.dis=new DataInputStream( data.fis );
                    data.firstRead = false;
                }
                catch(IOException e)
                {
                    throw new KettleFileException("Unable to read back row from temporary file!", e);
                }
            }
            
            // Read one row from the file!
            Row row = new Row(data.dis);
            data.rowsOnFile--;
            
            return row;
        }
        else
        {
            if (data.bufferList.size()>0)
            {
                Row row = (Row)data.bufferList.get(0);
                data.bufferList.remove(0);
                return row;
            }
            else
            {
                return null; // Nothing left!
            }
        }
    }
    
    private void closeOutput() throws KettleFileException
    {
        try
        {
            if (data.dos!=null) { data.dos.close(); data.dos=null; }
            if (data.fos!=null) { data.fos.close(); data.fos=null; }
            data.firstRead = true;
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to close input stream!", e);
        }
    }
    
    private void closeInput() throws KettleFileException
    {
        try
        {
            if (data.fis!=null) { data.fis.close(); data.fis=null; }
            if (data.dis!=null) { data.dis.close(); data.dis=null; }
        }
        catch(IOException e)
        {
            throw new KettleFileException("Unable to close input stream!", e);
        }
    }

    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GroupByMeta)smi;
		data=(GroupByData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.bufferList = new ArrayList();
            
            data.rowsOnFile = 0;
            
            return true;
		}
		return false;
	}

	
	//
	// Run is were the action happens!
	public void run()
	{		
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
            if (data.tempFile!=null) data.tempFile.delete();
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
