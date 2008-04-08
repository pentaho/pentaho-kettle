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
 

package be.ibridge.kettle.trans.step.accessinput;

import java.io.File;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

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
	    	
			//	Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment(Messages.getString("AccessInput.Log.FileAddedResult"));
			addResultFile(resultFile);
	    	
			// Move file pointer ahead!
			data.filenr++;				
		}		
		setOutputDone();  // signal end to receiver(s)
		return false;     // This is the end of this step.   
	}		
	
	private void Processfile(FileObject file)
	{
		Database d = null;
		
		try 
		{			
        	d = Database.open(new File(KettleVFS.getFilename(data.file)));	
        	
        	
			Table t=d.getTable(meta.getRealTableName());

			Map rw;
			
			int rowCount = 0;
			while ((rowCount++ < Long.MAX_VALUE) && ((rw = t.getNextRow()) != null)) 
			{
				// Create new row
				Row row = buildEmptyRow();
						
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					AccessInputField accessInputField = meta.getInputFields()[i];
					
					Object obj = rw.get(meta.getInputFields()[i].getAttribute());	
				
					// OK, we have the string...
					Value v = row.getValue(i);
					v.setValue(String.valueOf(obj));
										
					// DO Trimming!
					switch(accessInputField.getTrimType())
					{
						case AccessInputField.TYPE_TRIM_LEFT  : v.ltrim(); break;
						case AccessInputField.TYPE_TRIM_RIGHT : v.rtrim(); break;
						case AccessInputField.TYPE_TRIM_BOTH  : v.trim(); break;
						default: break;
					}
					
					// DO CONVERSIONS...
					switch(accessInputField.getType())
					{
						case Value.VALUE_TYPE_STRING:
							break;
						case Value.VALUE_TYPE_NUMBER:
							// System.out.println("Convert value to Number :"+v);
							if (accessInputField.getFormat()!=null && accessInputField.getFormat().length()>0)
							{
								if (accessInputField.getDecimalSymbol()!=null && accessInputField.getDecimalSymbol().length()>0)
								{
									if (accessInputField.getGroupSymbol()!=null && accessInputField.getGroupSymbol().length()>0)
									{
										if (accessInputField.getCurrencySymbol()!=null && accessInputField.getCurrencySymbol().length()>0)
										{
											v.str2num(accessInputField.getFormat(), accessInputField.getDecimalSymbol(), accessInputField.getGroupSymbol(), accessInputField.getCurrencySymbol());
										}
										else
										{
											v.str2num(accessInputField.getFormat(), accessInputField.getDecimalSymbol(), accessInputField.getGroupSymbol());
										}
									}
									else
									{
										v.str2num(accessInputField.getFormat(), accessInputField.getDecimalSymbol());
									}
								}
								else
								{
									v.str2num(accessInputField.getFormat()); // just a format mask
								}
							}
							else
							{
								v.str2num();
							}
							v.setLength(accessInputField.getLength(), accessInputField.getPrecision());
							break;
						case Value.VALUE_TYPE_INTEGER:
							// System.out.println("Convert value to integer :"+v);
							v.setValue(v.getInteger());
							v.setLength(accessInputField.getLength(), accessInputField.getPrecision());
							break;
						case Value.VALUE_TYPE_BIGNUMBER:
							// System.out.println("Convert value to BigNumber :"+v);
							v.setValue(v.getBigNumber());
							v.setLength(accessInputField.getLength(), accessInputField.getPrecision());
							break;
						case Value.VALUE_TYPE_DATE:
							// System.out.println("Convert value to Date :"+v);

							if (accessInputField.getFormat()!=null && accessInputField.getFormat().length()>0)
							{
								v.str2dat(accessInputField.getFormat());
							}
							else
							{
								v.setValue(v.getDate());
							}
							break;
						case Value.VALUE_TYPE_BOOLEAN:
							v.setValue(v.getBoolean());
							break;
						default: break;
					}
		            
		            // Do we need to repeat this field if it is null?
		            if (meta.getInputFields()[i].isRepeated())
		            {
		                if (v.isNull() && data.previousRow!=null)
		                {
		                    Value previous = data.previousRow.getValue(i);
		                    v.setValue(previous);
		                }
		            }
		 			
				}    // End of loop over fields...
		            
				// See if we need to add the filename to the row...  
		        if (meta.includeFilename() && meta.getFilenameField()!=null && meta.getFilenameField().length()>0)
		        {
		            Value fn = new Value( meta.getFilenameField(), KettleVFS.getFilename(data.file));
		            row.addValue(fn);
		        }
		        
 		 	    // See if we need to add the tablename to the row...  
		        if (meta.includeTablename() && meta.gettablenameField()!=null && meta.gettablenameField().length()>0)
		        {
		            Value fn = new Value( meta.gettablenameField(), meta.getRealTableName());
		            row.addValue(fn);
		        }		        
		        
		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && meta.getRowNumberField()!=null && meta.getRowNumberField().length()>0)
		        {
		            Value fn = new Value( meta.getRowNumberField(), data.rownr );
		            row.addValue(fn);
		        }
		        
				data.previousRow = new Row(row); // copy it to make sure the next step doesn't change it in between... 
				data.rownr++;
		        
	    		
				if (log.isRowLevel()) logRowlevel(Messages.getString("AccessInput.Log.ReadRow", row.toString()));        
	            
				putRow(row);
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
	 * @return
	 */
	private Row buildEmptyRow()
	{
		Row row = new Row();
        
		AccessInputField fields[] = meta.getInputFields();
		for (int i=0;i<fields.length;i++)
		{
			AccessInputField field = fields[i];
            
			Value value = new Value(StringUtil.environmentSubstitute(field.getName()), field.getType());
			value.setLength(field.getLength(), field.getPrecision());
			value.setNull();
            
			row.addValue(value);
		}
        
		return row;
	}

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessInputMeta)smi;
		data=(AccessInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
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
		if(data.file!=null)
		{
			try {
				data.file.close();
			}catch (Exception e){}
		}
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
			logError("Unexpected error : "+e.toString());
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