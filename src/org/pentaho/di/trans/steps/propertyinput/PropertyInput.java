/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

/*
 * Created on 24-03-2008
 *
 */

package org.pentaho.di.trans.steps.propertyinput;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.ini4j.Wini;
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


/**
 * Read all Properties files (& INI files)
 * , convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 24-03-2008
 */
public class PropertyInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = PropertyInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private PropertyInputMeta meta;
	private PropertyInputData data;
	
	public PropertyInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if(first && !meta.isFileField())
		{
			data.files = meta.getFiles(this);
			if (data.files==null || data.files.nrOfFiles()==0)
					throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Log.NoFiles"));
	
			handleMissingFiles();
			
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
				logError(BaseMessages.getString(PKG, "PropertyInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, null, "PropertyInput001");
	         }
		}
		 return true;
	}	
	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			logError(BaseMessages.getString(PKG, "PropertyInput.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "PropertyInput.Log.RequiredFiles", message));

			throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Log.RequiredFilesMissing",message));
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			logError(BaseMessages.getString(PKG, "PropertyInput.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "PropertyInput.Log.RequiredNotAccessibleFiles",message));

				throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Log.RequiredNotAccessibleFilesMissing",message));
		}
	}
	private Object[] getOneRow() throws KettleException
	{	
		try{
			if(meta.isFileField())
			{
				 while ((data.readrow==null) 
						 || ((data.propfiles && !data.it.hasNext()) ||(!data.propfiles && !data.iniIt.hasNext())))
				 {  
					//if (!openNextFile()) return null;
					 
					// In case we read all sections
					// maybe we have to change section for ini files...
					 if(!data.propfiles && data.realSection==null && data.readrow!=null &&  data.itSection.hasNext())
					 {
						 data.iniSection =  data.wini.get(data.itSection.next().toString());
						 data.iniIt = data.iniSection.keySet().iterator();
					 }else
					 {
						 if (!openNextFile()) return null;
					 }
					 
				 }		 
			}
			else
			{
				while ((data.file==null) 
						|| ((data.propfiles && !data.it.hasNext()) ||(!data.propfiles && !data.iniIt.hasNext())))
				{
					// In case we read all sections
					// maybe we have to change section for ini files...
					 if(!data.propfiles && data.realSection==null && data.file!=null &&  data.itSection.hasNext())
					 {
						 data.iniSection =  data.wini.get(data.itSection.next().toString());
						 data.iniIt = data.iniSection.keySet().iterator();
					 }else
					 {
						 if (!openNextFile()) return null;
					 }
				}
			}
		} catch (Exception IO)
		{
		    logError("Unable to read row from file : " + IO.getMessage());
			return null;
		}
		 // Build an empty row based on the meta-data		  
		 Object[] r=buildEmptyRow();
		 
		 // Create new row	or clone
		 if(meta.isFileField())	 r = data.readrow.clone();

		 try{	
			   String key=null;
			   if(data.propfiles)
				   key=data.it.next().toString();
			   else
				   key=data.iniIt.next().toString();
			   
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					// Get field value
					String value=null;
					
					if(meta.getInputFields()[i].getColumnCode().equals(PropertyInputField.ColumnCode[PropertyInputField.COLUMN_KEY]))
						value=key;
					else
					{
						if(meta.isResolveValueVariable())
						{
							if(data.propfiles)
								value= environmentSubstitute(data.pro.getProperty(key));
							else
								value=environmentSubstitute(data.iniSection.fetch(key));  // for INI files
						}
						else
						{
							if(data.propfiles)
								value= data.pro.getProperty(key);
							else
								value=data.iniSection.fetch(key);  // for INI files
						}
					}

					// DO Trimming!
					switch (meta.getInputFields()[i].getTrimType())
					{
					case PropertyInputField.TYPE_TRIM_LEFT:
						value = Const.ltrim(value);
						break;
					case PropertyInputField.TYPE_TRIM_RIGHT:
						value = Const.rtrim(value);
						break;
					case PropertyInputField.TYPE_TRIM_BOTH:
						value = Const.trim(value);
						break;
					default:
						break;
					}
					
					if(meta.isFileField())
					{
						// Add result field to input stream
		                r = RowDataUtil.addValueData(r,data.totalpreviousfields+i, value);
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
				
		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            r[data.totalpreviousfields+rowIndex++] = new Long(data.rownr);
		        }
		        
		        // See if we need to add the section for INI files ...  
		        if (meta.includeIniSection() && !Const.isEmpty(meta.getINISectionField()))
		        {
		        	 r[data.totalpreviousfields+rowIndex++]= environmentSubstitute(data.iniSection.getName());
		        }
		        
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?r:(Object[])irow.cloneRow(r); // copy it to make
				// surely the next step doesn't change it in between...
				
				incrementLinesInput();
				data.rownr++;
		 }
		 catch (Exception e)
		 {
			throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Error.CanNotReadFromFile",data.file.toString()), e);
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
	            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "PropertyInput.Log.FinishedProcessing"));
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
				if (data.readrow==null) {
					if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "PropertyInput.Log.FinishedProcessing"));
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
					if (Const.isEmpty(meta.getDynamicFilenameField()))
					{
						logError(BaseMessages.getString(PKG, "PropertyInput.Log.NoField"));
						throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Log.NoField"));
					}
					
					// cache the position of the field			
					if (data.indexOfFilenameField<0)
					{	
						data.indexOfFilenameField =getInputRowMeta().indexOfValue(meta.getDynamicFilenameField());
						if (data.indexOfFilenameField<0)
						{
							// The field is unreachable !
							logError(BaseMessages.getString(PKG, "PropertyInput.Log.ErrorFindingField")+ "[" + meta.getDynamicFilenameField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Exception.CouldnotFindField",meta.getDynamicFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} 
		        }  // End if first
				
				
				String filename=getInputRowMeta().getString(data.readrow,data.indexOfFilenameField);
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "PropertyInput.Log.FilenameInStream", meta.getDynamicFilenameField(),filename));

				data.file= KettleVFS.getFileObject(filename, getTransMeta());
				// Check if file exists!
			}
			
			// Check if file is empty
			//long fileSize= data.file.getContent().getSize();
			
			if(meta.resetRowNumber()) data.rownr=0;
            
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "PropertyInput.Log.OpeningFile", data.file.toString()));
    
			if(meta.isAddResultFile())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(BaseMessages.getString(PKG, "PropertyInput.Log.FileAddedResult"));
				addResultFile(resultFile);
			}

			 File f = new File(KettleVFS.getFilename(data.file));
			 if(data.propfiles)
			 {
				 // load properties file
				 data.pro=new Properties();
		         data.pro.load(new FileInputStream(f));
		         
		         data.it = data.pro.keySet().iterator();
			 }else
			 {
	         
		         // load INI file
		         data.wini = new Wini(f);
		         if(!Const.isEmpty(data.realEncoding))
		        	 data.wini.getConfig().setFileEncoding(Charset.forName(data.realEncoding));
		         if(data.realSection!=null)
		         {
		        	 // just one section
		        	 data.iniSection = data.wini.get(data.realSection); 
		        	 if(data.iniSection==null) throw new KettleException(BaseMessages.getString(PKG, "PropertyInput.Error.CanNotFindSection",data.realSection, ""+data.file.getName()));
		         }
		         else
		         {
		         	// We need to fetch all sections
		        	data.itSection=data.wini.keySet().iterator();
		         	data.iniSection =  data.wini.get( data.itSection.next().toString());
		         }
		         data.iniIt = data.iniSection.keySet().iterator();
			 }
	         
	         
	         if (log.isDetailed()) 
	         {
	        	 logDetailed(BaseMessages.getString(PKG, "PropertyInput.Log.FileOpened", data.file.toString()));
	        	 logDetailed(BaseMessages.getString(PKG, "PropertyInput.log.TotalKey", 
	        			 ""+(data.propfiles?data.pro.size():data.iniSection.size()), KettleVFS.getFilename(data.file)));
	         }
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "PropertyInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
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
		meta=(PropertyInputMeta)smi;
		data=(PropertyInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			String realEncoding=environmentSubstitute(meta.getEncoding());
			if(!Const.isEmpty(realEncoding)) data.realEncoding=realEncoding;
			String realSection=environmentSubstitute(meta.getSection());
			if(!Const.isEmpty(realSection)) data.realSection=realSection;
			data.propfiles=(PropertyInputMeta.getFileTypeByDesc(meta.getFileType())==PropertyInputMeta.FILE_TYPE_PROPERTY);
			data.rownr = 1L;
			data.totalpreviousfields=0;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(PropertyInputMeta)smi;
		data=(PropertyInputData)sdi;

		if(data.readrow!=null) data.readrow=null;
		if(data.iniSection!=null) data.iniSection.clear();data.iniSection=null;
		if(data.itSection!=null) data.itSection=null;
		if(data.file!=null) 
		{
			try
			{
				data.file.close();
				data.file=null;
			}catch  (Exception e){} // ignore this
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