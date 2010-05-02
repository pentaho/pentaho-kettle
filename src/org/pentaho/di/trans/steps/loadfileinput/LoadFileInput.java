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
 

package org.pentaho.di.trans.steps.loadfileinput;

import org.apache.commons.vfs.FileObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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
 * Read files, parse them and convert them to rows and writes these to one or more output 
 * streams.
 * 
 * @author Samatar
 * @since 20-06-2007
 */
public class LoadFileInput extends BaseStep implements StepInterface  
{
	private static Class<?> PKG = LoadFileInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private LoadFileInputMeta meta;
	private LoadFileInputData data;
	
	public LoadFileInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	

   private void addFileToResultFilesname(FileObject file) throws Exception
   {
       if(meta.addResultFile())
       {
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
			resultFile.setComment("File was read by a LoadFileInput step");
			addResultFile(resultFile);
       }
   }
	private boolean openNextFile()
	{
		try
		{
			if(meta.getIsInFields())
			{
			   data.readrow= getRow();  // Grab another row ...
			   
			   if (data.readrow==null) // finished processing!
	           {
	           	if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "LoadFileInput.Log.FinishedProcessing"));
	               return false;
	           }
			   
			   if(first)
				{
				    first=false;
				    
	            	data.inputRowMeta = getInputRowMeta();
		            data.outputRowMeta = data.inputRowMeta.clone();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
				    
					// Create convert meta-data objects that will contain Date & Number formatters
		            data.convertRowMeta = data.outputRowMeta.clone();
		            
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
		            
					if(meta.getIsInFields())
					{
						// Check is filename field is provided
						if (Const.isEmpty(meta.getDynamicFilenameField()))
						{
							logError(BaseMessages.getString(PKG, "LoadFileInput.Log.NoField"));
							throw new KettleException(BaseMessages.getString(PKG, "LoadFileInput.Log.NoField"));
						}
						
						// cache the position of the field			
						if (data.indexOfFilenameField<0)
						{	
							data.indexOfFilenameField =data.inputRowMeta.indexOfValue(meta.getDynamicFilenameField());
							if (data.indexOfFilenameField<0)
							{
								// The field is unreachable !
								logError(BaseMessages.getString(PKG, "LoadFileInput.Log.ErrorFindingField")+ "[" + meta.getDynamicFilenameField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
								throw new KettleException(BaseMessages.getString(PKG, "LoadFileInput.Exception.CouldnotFindField",meta.getDynamicFilenameField())); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
						// Get the number of previous fields
						data.totalpreviousfields=data.inputRowMeta.size();

					}	
				}// end if first
			   
			   // get field value
			   String Fieldvalue= data.inputRowMeta.getString(data.readrow, data.indexOfFilenameField);
				
			   if(isDetailed()) log.logDetailed(toString(),BaseMessages.getString(PKG, "LoadFileInput.Log.Stream", meta.getDynamicFilenameField(),Fieldvalue));

			   FileObject file=null;
				try
				{
					// Source is a file.
					data.file=  KettleVFS.getFileObject(Fieldvalue);
				}
				catch (Exception e)
				{
					throw new KettleException (e);
				}finally{try {if(file!=null) file.close();}catch (Exception e){}
				}
			}else
			{
	            if (data.filenr>=data.files.nrOfFiles()) // finished processing!
	            {
	            	if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "LoadFileInput.Log.FinishedProcessing"));
	                return false;
	            }
	            
			    // Is this the last file?
				data.last_file = ( data.filenr==data.files.nrOfFiles()-1);
				data.file = (FileObject) data.files.getFile(data.filenr);
			}
			
			// Check if file is empty
			data.fileSize= data.file.getContent().getSize();
			// Move file pointer ahead!
			data.filenr++;
            
			if(meta.isIgnoreEmptyFile() && data.fileSize==0)
			{
				log.logError(toString(),BaseMessages.getString(PKG, "LoadFileInput.Error.FileSizeZero", ""+data.file.getName()));
				openNextFile();
				
			}else
			{
				if (isDetailed()) log.logDetailed(toString(),BaseMessages.getString(PKG, "LoadFileInput.Log.OpeningFile", data.file.toString()));
				
				// get File content
				getFileContent();
				
				addFileToResultFilesname(data.file);
	
	            if (isDetailed()) 
	            {
	            	logDetailed(BaseMessages.getString(PKG, "LoadFileInput.Log.FileOpened", data.file.toString()));
	            }
	          }

		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "LoadFileInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		try {
			 // Grab a row
			 Object[] outputRowData=getOneRow();
			 if (outputRowData==null)
		     {
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.
		     }
			 
			 if (isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "LoadFileInput.Log.ReadRow",data.outputRowMeta.getString(outputRowData)));

			 putRow(data.outputRowMeta, outputRowData);
			 
			 if (meta.getRowLimit()>0 && data.rownr>meta.getRowLimit())  // limit has been reached: stop now.
		     {
		           setOutputDone();
		           return false;
		     }	
		}catch(KettleException e)
		{
			logError(BaseMessages.getString(PKG, "LoadFileInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		 return true;
		
	}
	private void getFileContent() throws KettleException
	{
		try{
			data.filecontent=getTextFileContent(data.file.toString(), meta.getEncoding());
		} catch(java.lang.OutOfMemoryError o) {
			log.logError(toString(), "There is no enaugh memory to load the content of the file ["+data.file.getName()+"]");
			throw new KettleException(o);
		} catch(Exception e) {
			throw new KettleException(e);
		}
	}
    /**
     * Read a text file.
     * 
     * @param vfsFilename the filename or URL to read from
     * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
     * @return The content of the file as a String
     * @throws IOException
     */
    public static String getTextFileContent(String vfsFilename, String encoding) throws KettleException
    {
    	InputStream inputStream =null;
    	InputStreamReader reader=null;
    	 
    	String retval=null;
    	try {
	        inputStream = KettleVFS.getInputStream(vfsFilename);
	        
			if (!Const.isEmpty(encoding)) {
				reader = new InputStreamReader(new BufferedInputStream(inputStream), encoding);
			}else {
		        reader = new InputStreamReader(new BufferedInputStream(inputStream));
			}
	
	        int c;
	        StringBuffer stringBuffer = new StringBuffer();
	        while ( (c=reader.read())!=-1) stringBuffer.append((char)c);
	        
	        retval=stringBuffer.toString();
    	}catch(Exception e) {
    		throw new KettleException(BaseMessages.getString(PKG, "LoadFileInput.Error.GettingFileContent", vfsFilename, e.toString()));
    	}finally {
    		if(reader!=null) try {reader.close();}catch(Exception e){};
    		if(inputStream!=null) try {inputStream.close();}catch(Exception e){};
    	}
        
        return retval;
    }
	

	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			logError(BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredFilesTitle"), 
					BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredFiles", message));

			throw new KettleException(BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredFilesMissing",message));
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			logError(BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredNotAccessibleFiles",message));
			throw new KettleException(BaseMessages.getString(PKG, "LoadFileInput.Log.RequiredNotAccessibleFilesMissing",message));
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

	private Object[] getOneRow()  throws KettleException
	{
        if (!openNextFile())
        {
            return null;
        }
		
		// Build an empty row based on the meta-data		  
		Object[] outputRowData =buildEmptyRow();

		 try{	
			 // Create new row	or clone
			 if(meta.getIsInFields()) System.arraycopy(data.readrow, 0, outputRowData, 0, data.readrow.length);
				
			// Read fields...
			for (int i=0;i<data.nrInputFields;i++)
			{	
				// Get field
				LoadFileInputField loadFileInputField = meta.getInputFields()[i];
	            
				String o=null;
				switch (loadFileInputField.getElementType())
				{
					case LoadFileInputField.ELEMENT_TYPE_FILECONTENT:

						// DO Trimming!
						switch(loadFileInputField.getTrimType())
						{
							case LoadFileInputField.TYPE_TRIM_LEFT  : data.filecontent=Const.ltrim(data.filecontent); break;
							case LoadFileInputField.TYPE_TRIM_RIGHT : data.filecontent=Const.rtrim(data.filecontent); break;
							case LoadFileInputField.TYPE_TRIM_BOTH  : data.filecontent=Const.trim(data.filecontent); break;
							default: break;
						}
						o=data.filecontent;
					break;
					case LoadFileInputField.ELEMENT_TYPE_FILESIZE:
						o=String.valueOf(data.fileSize);
					break;
					default:
					break;
				}
	            
				int indexField=data.totalpreviousfields+i;
				// Do conversions
				//
				ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(indexField);
				ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(indexField);
				outputRowData[indexField] = targetValueMeta.convertData(sourceValueMeta, o);

				
				// Do we need to repeat this field if it is null?
				if (loadFileInputField.isRepeated())
				{
					if (data.previousRow!=null && o==null)
					{
						outputRowData[indexField] = data.previousRow[indexField];
					}
				}	
			}// End of loop over fields...	
			int rowIndex = data.totalpreviousfields+data.nrInputFields;

			// See if we need to add the filename to the row...  
	        if (meta.includeFilename() && meta.getFilenameField()!=null && meta.getFilenameField().length()>0)
	        {
	        	outputRowData[rowIndex++] = KettleVFS.getFilename(data.file);
	        }
	
	        // See if we need to add the row number to the row...  
	        if (meta.includeRowNumber() && meta.getRowNumberField()!=null && meta.getRowNumberField().length()>0)
	        {
	        	outputRowData[rowIndex++] =new Long(data.rownr);
	        }
			
			RowMetaInterface irow = getInputRowMeta();
			
			data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
			// surely the next step doesn't change it in between...
			
			 incrementLinesInput();
			 data.rownr++;
	    
		 } catch (Exception e) {
			throw new KettleException("Impossible de charger le fichier", e);
		 }
		 
		return outputRowData;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LoadFileInputMeta)smi;
		data=(LoadFileInputData)sdi;				
		
		if (super.init(smi, sdi))
		{
			if(!meta.getIsInFields())
			{
				try{
					data.files = meta.getFiles(this);
					handleMissingFiles();
					 // Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
				
					// Create convert meta-data objects that will contain Date & Number formatters
		            data.convertRowMeta = data.outputRowMeta.clone();
		            
		            // For String to <type> conversions, we allocate a conversion meta data row as well...
					//
					data.convertRowMeta = data.outputRowMeta.clone();
					for (int i=0;i<data.convertRowMeta.size();i++) {
						data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
					}
				}
				catch(Exception e)
				{
					logError("Error at step initialization: "+e.toString());
					logError(Const.getStackTracker(e));
					return false;
				}
			}	
			data.rownr = 1L;
			data.nrInputFields=meta.getInputFields().length;
				
			return true;
		}
		return false;		
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (LoadFileInputMeta) smi;
		data = (LoadFileInputData) sdi;
		if(data.file!=null) 
		{
			try{
			data.file.close();
			}catch (Exception e){}
		}
		super.dispose(smi, sdi);
	}

}