/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.trans.steps.getxmldata;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.List;

import org.dom4j.io.SAXReader;
import org.dom4j.XPath;
import org.dom4j.tree.AbstractNode;

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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Read XML files, parse them and convert them to rows and writes these to one or more output 
 * streams.
 * 
 * @author Samatar,Brahim
 * @since 20-06-2007
 */
public class GetXMLData extends BaseStep implements StepInterface  
{
	private GetXMLDataMeta meta;
	private GetXMLDataData data;
	
	public GetXMLData(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	

   protected boolean setDocument(String StringXML,FileObject file,boolean IsInXMLField) throws KettleException {
	   
	   try{
			SAXReader reader = new SAXReader();
	
			// Validate XML against specified schema?
			if(meta.isValidating())
			{
				reader.setValidation(true);
				reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			}

			if (IsInXMLField)
			{
				data.document= reader.read(new StringReader(StringXML));	
			}
			else
			{	
				// get encoding. By default UTF-8
				String encoding="UTF-8";
				if (!Const.isEmpty(meta.getEncoding())) encoding=meta.getEncoding();
				
				data.document = reader.read(new FileInputStream(KettleVFS.getFilename(file)),encoding);				    
		}
			    	    
	   }catch (Exception e)
	   {
		   throw new KettleException(e);
	   }
	   return true;        
   }
   
	/**
	 * Build an empty row based on the meta-data.
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{
       Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

	    return rowData;
	}
	private void handleMissingFiles() throws KettleException
	{
		List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();
	
		if (nonExistantFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonExistantFiles);
			if(log.isBasic()) log.logBasic("Required files", "WARNING: Missing " + message);

			throw new KettleException("Following required files are missing " +message);
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			if(log.isBasic()) log.logBasic("Required files", "WARNING: Not accessible " + message);

				throw new KettleException("Following required files are not accessible " +message);
		}
	}
   private boolean ReadNextString()
   {
	   
	   try{
		   data.readrow= getRow();  // Grab another row ...
		   
		   if (data.readrow==null) // finished processing!
           {
           	if (log.isDetailed()) logDetailed(Messages.getString("GetXMLData.Log.FinishedProcessing"));
               return false;
           }

		   if(first)
			{
			    first=false;
			    
				if(meta.getIsInFields())
				{
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
					
					// Check is XML field is provided
					if (Const.isEmpty(meta.getXMLField()))
					{
						logError(Messages.getString("GetXMLData.Log.NoField"));
						throw new KettleException(Messages.getString("GetXMLData.Log.NoField"));
					}
					
					// cache the position of the field			
					if (data.indexOfXmlField<0)
					{	
						data.indexOfXmlField =getInputRowMeta().indexOfValue(meta.getXMLField());
						if (data.indexOfXmlField<0)
						{
							// The field is unreachable !
							logError(Messages.getString("GetXMLData.Log.ErrorFindingField")+ "[" + meta.getXMLField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
							throw new KettleException(Messages.getString("GetXMLData.Exception.CouldnotFindField",meta.getXMLField())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}	
			}
		   
		   
		   if(meta.getIsInFields())
		   {
			   // get XML field value
			   String Fieldvalue= getInputRowMeta().getString(data.readrow,data.indexOfXmlField);
				
			   if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetXMLData.Log.XMLStream", meta.getXMLField(),Fieldvalue));

			   if(meta.getIsAFile())
			   {
				   FileObject file=null;
					try
					{
						// XML source is a file.
						file=  KettleVFS.getFileObject(Fieldvalue);
						//Open the XML document
						if(!setDocument(null,file,false)) 
						{
							throw new KettleException (Messages.getString("GetXMLData.Log.UnableCreateDocument"));
						}
						
						// Apply XPath and set node list
						if(!applyXPath())
						{
							throw new KettleException (Messages.getString("GetXMLData.Log.UnableApplyXPath"));
						}
						
						addFileToResultFilesname(file);
			            
			            if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetXMLData.Log.LoopFileOccurences",""+data.nodesize,file.getName().getBaseName()));
						
					}
					catch (Exception e)
					{
						throw new KettleException (e);
					}finally{try {if(file!=null) file.close();}catch (Exception e){}
					}
			   }
				else
				{
					//Open the XML document
					if(!setDocument(Fieldvalue,null,true)) 
					{
						throw new KettleException (Messages.getString("GetXMLData.Log.UnableCreateDocument"));
					}
	
					// Apply XPath and set node list
					if(!applyXPath())
					{
						throw new KettleException (Messages.getString("GetXMLData.Log.UnableApplyXPath"));
					}
					if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetXMLData.Log.LoopFileOccurences",""+data.nodesize));
						
				}

		   }
		
	   }
		catch(Exception e)
		{
			logError(Messages.getString("GetXMLData.Log.UnexpectedError", e.toString()));
			stopAll();
			logError(Const.getStackTracker(e));
			setErrors(1);
			return false;
		}
		return true;
	   
   }
   private void addFileToResultFilesname(FileObject file) throws Exception
   {
       if(meta.addResultFile())
       {
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, file, getTransMeta().getName(), getStepname());
			resultFile.setComment("File was read by an get XML Data step");
			addResultFile(resultFile);
       }
   }
   
   @SuppressWarnings("unchecked")
   private boolean applyXPath()
   {
	   try{
	       XPath xpath = data.document.createXPath(data.PathValue);
		   data.an =  (List<AbstractNode>) xpath.selectNodes(data.document);
		  
		   data.nodesize=data.an.size();
		   data.nodenr=0;
	   }catch (Exception e)
	   {
		   log.logError(toString(),Messages.getString("GetXMLData.Log.ErrorApplyXPath",e.getMessage()));
		   return false;
	   }
	   return true;
   }
	private boolean openNextFile()
	{
		try
		{
            if (data.filenr>=data.files.nrOfFiles()) // finished processing!
            {
            	if (log.isDetailed()) logDetailed(Messages.getString("GetXMLData.Log.FinishedProcessing"));
                return false;
            }
            
		    // Is this the last file?
			data.last_file = ( data.filenr==data.files.nrOfFiles()-1);
			data.file = (FileObject) data.files.getFile(data.filenr);
			
			// Check if file is empty
			long fileSize= data.file.getContent().getSize();
			
			// Move file pointer ahead!
			data.filenr++;
            
			if(meta.isIgnoreEmptyFile() && fileSize==0)
			{
				log.logError(toString(),Messages.getString("GetXMLData.Error.FileSizeZero", ""+data.file.getName()));
				openNextFile();
				
			}else
			{
				if (log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetXMLData.Log.OpeningFile", data.file.toString()));
	            
				//Open the XML document
				if(!setDocument(null,data.file,false)) 
				{
					throw new KettleException (Messages.getString("GetXMLData.Log.UnableCreateDocument"));
				}
				
				// Apply XPath and set node list
				if(!applyXPath())
				{
					throw new KettleException (Messages.getString("GetXMLData.Log.UnableApplyXPath"));
				}
				
				addFileToResultFilesname(data.file);
	
	            if (log.isDetailed()) 
	            {
	            	logDetailed(Messages.getString("GetXMLData.Log.FileOpened", data.file.toString()));
	               log.logDetailed(toString(),Messages.getString("GetXMLData.Log.LoopFileOccurences",""+data.nodesize,data.file.getName().getBaseName()));
	            }   
	         }

		}
		catch(Exception e)
		{
			logError(Messages.getString("GetXMLData.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		 // Grab a row
		 Object[] r=getXMLRow();
		 if (r==null)
	     {
	        setOutputDone();  // signal end to receiver(s)
	        return false; // end of data or error.
	     }
		 
		 return putRowOut(r);
		
	}
	
	private boolean putRowOut(Object[] r) throws KettleException
	{
		 if (r==null)
	     {
	        setOutputDone();  // signal end to receiver(s)
	        return false; // end of data or error.
	     }
		 
		 if (log.isRowLevel()) logRowlevel(Messages.getString("GetXMLData.Log.ReadRow", r.toString()));
		 incrementLinesInput();
		 data.rownr++;
		 putRow(data.outputRowMeta, r);  // copy row to output rowset(s);
		 
		  if (meta.getRowLimit()>0 && data.rownr>meta.getRowLimit())  // limit has been reached: stop now.
	      {
	            setOutputDone();
	            return false;
	      }	
		  
		  return true;
	}
	
	
	private Object[] getXMLRow()  throws KettleException
	{

		if(!meta.getIsInFields())
		{
			while ((data.nodenr>=data.nodesize ||  data.file==null))
			{
		        if (!openNextFile())
		        {
		            return null;
		        }
			} 
		}

		 // Build an empty row based on the meta-data		  
		 Object[] r=null;
		 boolean sendToErrorRow=false;
		 String errorMessage = null;
		 try{	
			 if(meta.getIsInFields())
			 {
			    
				while ((data.nodenr>=data.nodesize || data.readrow==null))
				{
					if(!ReadNextString())
					{
						return null;
					}
					if(data.readrow==null)
					{
						return null;
					}
				}
			 }
			 	
				if(meta.getIsInFields())
					r= processPutRow(data.readrow,(AbstractNode)data.an.get(data.nodenr));
				else
					r= processPutRow(null,(AbstractNode)data.an.get(data.nodenr));
				
			
		 }
		 catch (Exception e)
		 {
			 
			 if (getStepMeta().isDoingErrorHandling())
				{
			          sendToErrorRow = true;
			          errorMessage = e.toString();
				}
				else
				{

					throw new KettleException("Unable to read row from XML file", e);
				}
				if (sendToErrorRow)
				{
				   // Simply add this row to the error row
				   putError(getInputRowMeta(), r, 1, errorMessage, null, "GetXMLData001");
				}
		 }
		 
		return r;
	}
	 
		
	private Object[] processPutRow(Object[] row,AbstractNode node) throws KettleException
	{

		// Create new row...
		Object[] outputRowData = buildEmptyRow();
		
		try
		{
			data.nodenr++; 
			if(row!=null) outputRowData = row.clone();
			Object extraData[] = new Object[data.nrInputFields];
			// Read fields...
			for (int i=0;i<data.nrInputFields;i++)
			{	
				// Get field
				GetXMLDataField xmlDataField = meta.getInputFields()[i];
				// Get the Path to look for
				String XPathValue = environmentSubstitute(xmlDataField.getXPath());
				// Get the path type
				String Element_Type = xmlDataField.getElementTypeCode();
				
				if(meta.isuseToken())
				{
					
					// See if user use Token inside path field
					// The syntax is : @_Fieldname-
					// PDI will search for Fieldname value and replace it
					// Fieldname must be defined before the current node
					int indexvarstart=XPathValue.indexOf(data.tokenStart);
					int indexvarend=XPathValue.indexOf(data.tokenEnd);

					if(indexvarstart>=0 && indexvarend>=0)
					{
						String NameVarInputField = XPathValue.substring(indexvarstart+2, indexvarend);
						for (int k=0;k<meta.getInputFields().length;k++)
						{
							GetXMLDataField Tmp_xmlInputField = meta.getInputFields()[k];
							if(Tmp_xmlInputField.getName().equalsIgnoreCase(NameVarInputField))
							{		
								XPathValue = XPathValue.replaceAll(data.tokenStart+NameVarInputField+data.tokenEnd,"'"+ outputRowData[k] +"'");
								if ( log.isDetailed() )
								{
								   if(log.isDetailed()) log.logDetailed(toString(),XPathValue);
								   
								}
							}
						}	
						
					}
				}
				
				// Get node value
				String nodevalue =null;
				
				if (!Element_Type.equals("node")) XPathValue='@'+XPathValue;
				
				// Get node	value
				nodevalue=node.valueOf(XPathValue);
				
				// Do trimming
				switch (xmlDataField.getTrimType())
				{
				case GetXMLDataField.TYPE_TRIM_LEFT:
					nodevalue = Const.ltrim(nodevalue);
					break;
				case GetXMLDataField.TYPE_TRIM_RIGHT:
					nodevalue = Const.rtrim(nodevalue);
					break;
				case GetXMLDataField.TYPE_TRIM_BOTH:
					nodevalue = Const.trim(nodevalue);
					break;
				default:
					break;
				}
				
				if(meta.getIsInFields())
				{
					// Add result field to input stream
					extraData[i]=nodevalue;
	                outputRowData = RowDataUtil.addRowData(outputRowData,data.totalpreviousfields, extraData);
	                
				}
				// Do conversions
				//
				ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(data.totalpreviousfields+i);
				ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.totalpreviousfields+i);
				outputRowData[data.totalpreviousfields+i] = targetValueMeta.convertData(sourceValueMeta, nodevalue);

				// Do we need to repeat this field if it is null?
				if (meta.getInputFields()[i].isRepeated())
				{
					if (data.previousRow!=null && Const.isEmpty(nodevalue))
					{
						outputRowData[i] = data.previousRow[i];
					}
				}
				
			}// End of loop over fields...	
			
			int rowIndex = data.nrInputFields;
			
			// See if we need to add the filename to the row...
			if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) {
				outputRowData[rowIndex++] = KettleVFS.getFilename(data.file);
			}
			 // See if we need to add the row number to the row...  
	        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
	        {
	            outputRowData[rowIndex++] = new Long(data.rownr);
	        }
			
			RowMetaInterface irow = getInputRowMeta();
			
			data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
			// surely the next step doesn't change it in between...
	    
		}
		catch(Exception e)
		{
			log.logError(toString(), e.toString());
			throw new KettleException(e.toString());

		} 
		
			return outputRowData;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GetXMLDataMeta)smi;
		data=(GetXMLDataData)sdi;				
		
		if (super.init(smi, sdi))
		{
			if(!meta.getIsInFields())
			{
				// We process given file list
				data.files = meta.getFiles(this);
				if (data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() == 0)
				{
					logError(Messages.getString("GetXMLData.Log.NoFiles"));
					return false;
				}
				try{
					handleMissingFiles();
					
					// Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
	
					meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
					
					// Create convert meta-data objects that will contain Date & Number formatters
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
			data.nrInputFields=meta.getInputFields().length;
			data.PathValue=environmentSubstitute(meta.getLoopXPath());
			if(!data.PathValue.substring(0,1).equals("/")) data.PathValue="/" + data.PathValue;
			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("GetXMLData.Log.LoopXPath",data.PathValue));
				
			return true;
		}
		return false;		
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (GetXMLDataMeta) smi;
		data = (GetXMLDataData) sdi;
		if(data.file!=null) 
		{
			try{
			data.file.close();
			}catch (Exception e){}
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