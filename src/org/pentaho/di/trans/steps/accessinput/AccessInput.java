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
 

package org.pentaho.di.trans.steps.accessinput;

import java.io.File;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read all Access files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class AccessInput extends BaseStep implements StepInterface
{
	private AccessInputMeta meta;
	private AccessInputData data;
	
	public AccessInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		data.rownr=0;
		
		
		 if (data.filenr >= data.files.size())
        {
            setOutputDone();
            return false;
        }
        
        if (first)
        {
            first = false;
            // Create the output row meta-data
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
            
            
            // Create convert meta-data objects that will contain Date & Number formatters
            //
            data.convertRowMeta = data.outputRowMeta.clone();
            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);

            
            
            // For String to <type> conversions, we allocate a conversion meta data row as well...
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
				data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);           
            
			}
        }
	
		
		for (int i=0;i<data.files.size();i++)
		{	
			if ((meta.getRowLimit()>0 &&  data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0) 
			{		
				data.file = (FileObject) data.files.get(i);
		    			    	
				logBasic(Messages.getString("AccessInput.Log.OpeningFile", data.file.toString()));
		    	
				// Fetch files and process each one
				Processfile(data.file);					
				
				if (log.isDetailed()) logDetailed(Messages.getString("AccessInput.Log.FileOpened", data.file.toString()));
			}
	    	
			
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment(Messages.getString("AccessInput.Log.FileAddedResult"));
			addResultFile(resultFile);
	    	
			// Move file pointer ahead!
			data.filenr++;
			
			if (meta.resetRowNumber())
			{
				// Reset Row number for each file
				data.rownr=0;
			}
			else
			{
				// Move Row number pointer ahead
				data.rownr++;
			}
							
		}		
		
		linesInput++;
		
		//setOutputDone();  // signal end to receiver(s)
		//return false;     // This is the end of this step. 

        if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0) logBasic("linenr " + linesInput);

        return true;
	}		
	
	private void Processfile(FileObject file)
	{
		Database d = null;
		Object[] outputRowData = null;
	
		
		try 
		{	
			// Read mdb file
        	d = Database.open(new File(KettleVFS.getFilename(data.file)));	
        	// Get table
			Table t=d.getTable(environmentSubstitute(meta.getTableName()));

			
			Map<String,Object> rw;
			
			
			// Fetch all rows from the table 
			while (((meta.getRowLimit()>0 &&  data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0)  && ((rw = t.getNextRow()) != null))  
			{
				// Create new row				
				outputRowData = buildEmptyRow();
						
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{

					// Get field value
					Object obj = rw.get(meta.getInputFields()[i].getColumn());	
					String value=String.valueOf(obj);
					
					// DO Trimming!
					switch (meta.getInputFields()[i].getTrimType())
					{
					case AccessInputField.TYPE_TRIM_LEFT:
						value = Const.ltrim(value);
						break;
					case AccessInputField.TYPE_TRIM_RIGHT:
						value = Const.rtrim(value);
						break;
					case AccessInputField.TYPE_TRIM_BOTH:
						value = Const.trim(value);
						break;
					default:
						break;
					}
						      
					
					// DO CONVERSIONS...
					//
					ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
					ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(i);
					outputRowData[i] = targetValueMeta.convertData(sourceValueMeta, value);
					
					// Do we need to repeat this field if it is null?
					if (meta.getInputFields()[i].isRepeated())
					{
						if (data.previousRow!=null && Const.isEmpty(value))
						{
							outputRowData[i] = data.previousRow[i];
						}
					}
		            
		    
		 			
				}    // End of loop over fields...
		    
				int rowIndex = meta.getInputFields().length;
				
				// See if we need to add the filename to the row...
				if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) {
					outputRowData[rowIndex++] = KettleVFS.getFilename(data.file);
				}
				
				// See if we need to add the table name to the row...
				if ( meta.includeTablename() && !Const.isEmpty(meta.getTableName()) ) {
					outputRowData[rowIndex++] = environmentSubstitute(meta.getTableName());
				}
				
		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            outputRowData[rowIndex++] = new Long(data.rownr);
		        }
		        
				
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
				// surely the next step doesn't change it in between...
				data.rownr++;
	    		      
	           
				putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
			}			       
		} 
		catch(Exception e)
		{
			logError(Messages.getString("AccessInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
		} 
		finally
	    {
	        // Don't forget to close the bugger.
	        try
	        {
	             if (d!=null) d.close();
	        }
	        catch(Exception e)
	        {}
	    }
	}
	
	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessInputMeta)smi;
		data=(AccessInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles(this).getFiles();
			if (data.files==null || data.files.size()==0)
			{
				logError(Messages.getString("AccessInput.Log.NoFiles"));
				return false;
			}
            
			data.rownr = 1L;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessInputMeta)smi;
		data=(AccessInputData)sdi;

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	//
	public void run()
	{			    
		try
		{
			logBasic(Messages.getString("AccessInput.Log.StartingRun"));		
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : ");
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}