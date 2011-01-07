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
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.healthmarketscience.jackcess.Database;

/**
 * Read all Access files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class AccessInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = AccessInput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
				logError(BaseMessages.getString(PKG, "AccessInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
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
		 if(meta.isFileField())
			 System.arraycopy(data.readrow, 0, r, 0, data.readrow.length);

		 
		 try{	
			 
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					// get field
					AccessInputField field= meta.getInputFields()[i];

					// Get field value
					Object obj = data.rw.get(environmentSubstitute(field.getColumn()));	
						
					if(obj instanceof Date && field.getType()==ValueMetaInterface.TYPE_DATE
						|| obj instanceof byte[] && field.getType()==ValueMetaInterface.TYPE_BINARY)
					{
						r[data.totalpreviousfields+i]=obj;
					}
					else 
					{
						String value=null;
						if (obj!=null) {
							value=String.valueOf(obj);
						}
						
						// DO Trimming!
						switch (field.getTrimType())
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
					}
						
					// Do we need to repeat this field if it is null?
					if (field.isRepeated())
					{
						if (data.previousRow!=null && obj==null)
						{
							r[data.totalpreviousfields+i] = data.previousRow[data.totalpreviousfields+i];
						}
					}	
				}    // End of loop over fields...
		    
				int rowIndex = data.totalpreviousfields+meta.getInputFields().length;
				
				// See if we need to add the filename to the row...
				if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) 
				{
					r[rowIndex++] = AccessInputMeta.getFilename(data.file);
				}
				
				// See if we need to add the table name to the row...
				if ( meta.includeTablename() && !Const.isEmpty(data.t.getName()) ) 
				{
					r[rowIndex++] = data.t.getName();
				}
				
		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            r[rowIndex++] = new Long(data.rownr);
		        }
		        // Possibly add short filename...
				if (meta.getShortFileNameField()!=null && meta.getShortFileNameField().length()>0)
				{
					r[rowIndex++] = data.shortFilename;
				}
				// Add Extension
				if (meta.getExtensionField()!=null && meta.getExtensionField().length()>0)
				{
					r[rowIndex++] = data.extension;
				}
				// add path
				if (meta.getPathField()!=null && meta.getPathField().length()>0)
				{
					r[rowIndex++] = data.path;
				}
				// Add Size
				if (meta.getSizeField()!=null && meta.getSizeField().length()>0)
				{
					r[rowIndex++] = new Long(data.size);
				}
				// add Hidden
				if (meta.isHiddenField()!=null && meta.isHiddenField().length()>0)
				{
					r[rowIndex++] = new Boolean(data.hidden);
				}
				// Add modification date
				if (meta.getLastModificationDateField()!=null && meta.getLastModificationDateField().length()>0)
				{
					r[rowIndex++] = data.lastModificationDateTime;
				}
				// Add Uri
				if (meta.getUriField()!=null && meta.getUriField().length()>0)
				{
					r[rowIndex++] = data.uriName;
				}
				// Add RootUri
				if (meta.getRootUriField()!=null && meta.getRootUriField().length()>0)
				{
					r[rowIndex++] = data.rootUriName;
				}
				
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?r:(Object[])irow.cloneRow(r); // copy it to make
				// surely the next step doesn't change it in between...
				
				 incrementLinesInput();
				 data.rownr++;
			
		 }
		 catch (Exception e)
		 { 
			throw new KettleException(BaseMessages.getString(PKG, "AccessInput.Error.ErrorReadingFile"), e);
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
	            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "AccessInput.Log.FinishedProcessing"));
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
					if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "AccessInput.Log.FinishedProcessing"));
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
		            
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
					
					// Check is filename field is provided
					if (Const.isEmpty(meta.getDynamicFilenameField()))
					{
						logError(BaseMessages.getString(PKG, "AccessInput.Log.NoField"));
						throw new KettleException(BaseMessages.getString(PKG, "AccessInput.Log.NoField"));
					}
					
					// cache the position of the field			
					if (data.indexOfFilenameField<0)
					{	
						data.indexOfFilenameField =getInputRowMeta().indexOfValue(meta.getDynamicFilenameField());
						if (data.indexOfFilenameField<0)
						{
							// The field is unreachable !
							logError(BaseMessages.getString(PKG, "AccessInput.Log.ErrorFindingField")+ "[" + meta.getDynamicFilenameField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(BaseMessages.getString(PKG, "AccessInput.Exception.CouldnotFindField",meta.getDynamicFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}   
		        }  // End if first
				
				
				String filename=getInputRowMeta().getString(data.readrow,data.indexOfFilenameField);
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "AccessInput.Log.FilenameInStream", meta.getDynamicFilenameField(),filename));

				data.file= KettleVFS.getFileObject(filename, getTransMeta());
				// Check if file exists!
			}
			// Add additional fields?
			if (meta.getShortFileNameField()!=null && meta.getShortFileNameField().length()>0)
			{
				data.shortFilename  =  data.file.getName().getBaseName();
			}
			if (meta.getPathField()!=null && meta.getPathField().length()>0)
			{
				data.path = KettleVFS.getFilename(data.file.getParent());
			}
			if (meta.isHiddenField()!=null && meta.isHiddenField().length()>0)
			{
				data.hidden =  data.file.isHidden();
			}
			if (meta.getExtensionField()!=null && meta.getExtensionField().length()>0)
			{
				data.extension =  data.file.getName().getExtension();
			}
			if (meta.getLastModificationDateField()!=null && meta.getLastModificationDateField().length()>0)
			{
				data.lastModificationDateTime =  new Date(data.file.getContent().getLastModifiedTime());
			}
			if (meta.getUriField()!=null && meta.getUriField().length()>0)
			{
				data.uriName = data.file.getName().getURI();
			}
			if (meta.getRootUriField()!=null && meta.getRootUriField().length()>0)
			{
				data.rootUriName = data.file.getName().getRootURI();
			}
			if (meta.getSizeField()!=null && meta.getSizeField().length()>0)
			{
				data.size = new Long( data.file.getContent().getSize());
			}
			
			if(meta.resetRowNumber()) data.rownr=0;
            
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "AccessInput.Log.OpeningFile", data.file.toString()));
    
			if(meta.isAddResultFile())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(BaseMessages.getString(PKG, "AccessInput.Log.FileAddedResult"));
				addResultFile(resultFile);
			}
			
			// Read mdb file
			data.file.getName().getPathDecoded();
			
        	data.d = Database.open(new File(AccessInputMeta.getFilename(data.file)), true);	// Read-only

        	// Get table
        	if(data.isTableSystem)
        		data.t=data.d.getSystemTable(data.tableName);
        	else
        		data.t=data.d.getTable(data.tableName);

			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "AccessInput.Log.FileOpened", data.file.toString()));
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "AccessInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			logError(BaseMessages.getString(PKG, "AccessInput.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "AccessInput.Log.RequiredFiles", message));

			throw new KettleException(BaseMessages.getString(PKG, "AccessInput.Log.RequiredFilesMissing",message));
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			logError(BaseMessages.getString(PKG, "AccessInput.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "AccessInput.Log.RequiredNotAccessibleFiles",message));

				throw new KettleException(BaseMessages.getString(PKG, "AccessInput.Log.RequiredNotAccessibleFilesMissing",message));
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
			// Get table
        	data.tableName=environmentSubstitute(meta.getTableName());
        	// Check tablename
        	if(Const.isEmpty(data.tableName))
        	{
        		logError(BaseMessages.getString(PKG, "AccessInput.Error.TableNameMissing"));
        		return false;
        	}
        	data.isTableSystem=(data.tableName.startsWith(AccessInputMeta.PREFIX_SYSTEM));
			if(!meta.isFileField())
			{
				data.files = meta.getFiles(this);
				try{
					handleMissingFiles();
					
					// Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated

		   		   // Create convert meta-data objects that will contain Date & Number formatters
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);           
					}
				}
				catch(Exception e)
				{
					logError(BaseMessages.getString(PKG, "AccessInput.ErrorInit",e.toString()));
					logError(Const.getStackTracker(e));
					return false;
				}
			} 			
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
		try
		{
			if(data.d!=null) 
			{
				data.d.close();
				data.d=null;
			}
			if(data.file!=null) 
			{
				data.file.close();
				data.file=null;
			}
		} catch  (Exception e){} // ignore this
		super.dispose(smi, sdi);
	}	
}