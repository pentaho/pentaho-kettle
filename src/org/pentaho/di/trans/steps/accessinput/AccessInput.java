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

import com.healthmarketscience.jackcess.Database;

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
	
		Object[] r=null;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;
		 
		try{
			 // Grab one row
			 Object[] outputRowData=getOneRow();
			 if (outputRowData==null)
		     {
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.
		     }
	 
			 putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);

			  if (meta.getRowLimit()>0 && data.rownr>meta.getRowLimit())  // limit has been reached: stop now.
		      {
		            setOutputDone();
		            return false;
		      }	
		}catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
			else
			{
				logError(Messages.getString("AccessInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, null, "AccessInput001");
	         }
		}
		 return true;
	}		
	private Object[] getOneRow() throws KettleException
	{
		try{
			if(meta.isFileField())
			{
				 while ((data.readrow==null || ((data.rw = data.t.getNextRow())==null)))
				 { 
					if (!openNextFile()) return null;
				 }	
			}
			else
			{
				while ((data.file==null || ((data.rw = data.t.getNextRow())==null)))
				{
			        if (!openNextFile()) return null;
				}
			}
			
			
		} catch (Exception IO)
		{
			return null;
		}

		 // Build an empty row based on the meta-data		  
		 Object[] r=buildEmptyRow();
		 
		 // Create new row	or clone
		 if(meta.isFileField())	 r = data.readrow.clone();

		 try{	
			 
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{

					// Get field value
					Object obj = data.rw.get(meta.getInputFields()[i].getColumn());	
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
					ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(data.totalpreviousfields+i);
					ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.totalpreviousfields+i);
					r[data.totalpreviousfields+i] = targetValueMeta.convertData(sourceValueMeta, value);
					
					// Do we need to repeat this field if it is null?
					if (meta.getInputFields()[i].isRepeated())
					{
						if (data.previousRow!=null && Const.isEmpty(value))
						{
							r[data.totalpreviousfields+i] = data.previousRow[data.totalpreviousfields+i];
						}
					}	
				}    // End of loop over fields...
		    
				int rowIndex = meta.getInputFields().length;
				
				// See if we need to add the filename to the row...
				if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) {
					r[data.totalpreviousfields+rowIndex++] = KettleVFS.getFilename(data.file);
				}
				
				// See if we need to add the table name to the row...
				if ( meta.includeTablename() && !Const.isEmpty(meta.getTableName()) ) {
					r[data.totalpreviousfields+rowIndex++] = environmentSubstitute(meta.getTableName());
				}
				
		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            r[data.totalpreviousfields+rowIndex++] = new Long(data.rownr);
		        }
		        
				
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?r:(Object[])irow.cloneRow(r); // copy it to make
				// surely the next step doesn't change it in between...
				
				 linesInput++;
				 data.rownr++;
			
		 }
		 catch (Exception e)
		 {
			 
			throw new KettleException("Unable to read row from Access file", e);
			
		 }
		 
		return r;
	}
	private boolean openNextFile()
	{
		try
		{
			if(!meta.isFileField())
			{
	            if (data.filenr>=data.files.nrOfFiles()) // finished processing!
	            {
	            	if (log.isDetailed()) logDetailed(Messages.getString("AccessInput.Log.FinishedProcessing"));
	                return false;
	            }
	            
			    // Is this the last file?
				data.last_file = ( data.filenr==data.files.nrOfFiles()-1);
				data.file = (FileObject) data.files.getFile(data.filenr);
				
				// Move file pointer ahead!
				data.filenr++;
			}else
			{
				data.readrow=getRow();     // Get row from input rowset & set row busy!
				if (data.readrow==null)
			    {
					if (log.isDetailed()) logDetailed(Messages.getString("AccessInput.Log.FinishedProcessing"));
			         return false;
			    }
				
				if (first)
		        {
		            first = false;
		            
	            	data.inputRowMeta = getInputRowMeta();
		            data.outputRowMeta = data.inputRowMeta.clone();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		            
		            // Get total previous fields
		            data.totalpreviousfields=data.inputRowMeta.size();

					// Create convert meta-data objects that will contain Date & Number formatters
		            data.convertRowMeta = data.outputRowMeta.clone();
		            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);
		  
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
					
					// Check is filename field is provided
					if (Const.isEmpty(meta.getFilename_Field()))
					{
						logError(Messages.getString("AccessInput.Log.NoField"));
						throw new KettleException(Messages.getString("AccessInput.Log.NoField"));
					}
					
					// cache the position of the field			
					if (data.indexOfFilenameField<0)
					{	
						data.indexOfFilenameField =getInputRowMeta().indexOfValue(meta.getFilename_Field());
						if (data.indexOfFilenameField<0)
						{
							// The field is unreachable !
							logError(Messages.getString("AccessInput.Log.ErrorFindingField")+ "[" + meta.getFilename_Field()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(Messages.getString("AccessInput.Exception.CouldnotFindField",meta.getFilename_Field())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
	            	
		            
		        }  // End if first
				
				
				String filename=getInputRowMeta().getString(data.readrow,data.indexOfFilenameField);
				if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("AccessInput.Log.FilenameInStream", meta.getFilename_Field(),filename));

				data.file= KettleVFS.getFileObject(filename);
				// Check if file exists!
			}
			
			// Check if file is empty
			//long fileSize= data.file.getContent().getSize();
			
			if(meta.resetRowNumber()) data.rownr=0;
            
			if (log.isDetailed()) logDetailed(Messages.getString("AccessInput.Log.OpeningFile", data.file.toString()));
    
			if(meta.isAddResultFile())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(Messages.getString("AccessInput.Log.FileAddedResult"));
				addResultFile(resultFile);
			}
			
			// Read mdb file
        	data.d = Database.open(new File(KettleVFS.getFilename(data.file)));	

        	// Get table
			data.t=data.d.getTable(environmentSubstitute(meta.getTableName()));

			if (log.isDetailed()) logDetailed(Messages.getString("AccessInput.Log.FileOpened", data.file.toString()));


		}
		catch(Exception e)
		{
			logError(Messages.getString("AccessInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
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
			if(!meta.isFileField())
			{
				data.files = meta.getFiles(this);
				if (data.files==null || data.files.nrOfFiles()==0)
				{
					logError(Messages.getString("AccessInput.Log.NoFiles"));
					return false;
				}
				try{
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
				catch(Exception e)
				{
					logError("Error initializing step: "+e.toString());
					logError(Const.getStackTracker(e));
					return false;
				}
			} 
			data.rownr = 1L;
			data.totalpreviousfields=0;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessInputMeta)smi;
		data=(AccessInputData)sdi;

		if(data.t!=null) data.t=null;
		if(data.rw!=null) data.rw=null;
		if(data.readrow!=null) data.readrow=null;
		if(data.d!=null) 
		{
			try
			{
				data.d.close();
				data.d=null;
			}catch  (Exception e)
			{
			}
		}
		
		super.dispose(smi, sdi);
	}
	
    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }	
}