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

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.AbstractNode;
import java.io.InputStream;
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
 * Read XML files, parse them and convert them to rows and writes these to one or more output 
 * streams.
 * 
 * @author Samatar,Brahim
 * @since 20-06-2007
 */
public class GetXMLData extends BaseStep implements StepInterface  
{
	private static Class<?> PKG = GetXMLDataMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private GetXMLDataMeta meta;
	private GetXMLDataData data;
  private Object[] prevRow = null; // A pre-allocated spot for the previous row

	
	public GetXMLData(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	

   protected boolean setDocument(String StringXML,FileObject file,boolean IsInXMLField,boolean readurl) throws KettleException {
	   
     this.prevRow = buildEmptyRow(); // pre-allocate previous row
     
	   try{
			SAXReader reader = new SAXReader();
			data.stopPruning=false;
	
			// Validate XML against specified schema?
			if(meta.isValidating())
			{
				reader.setValidation(true);
				reader.setFeature("http://apache.org/xml/features/validation/schema", true);
			}
			
			// Ignore comments?
			if(meta.isIgnoreComments())	reader.setIgnoreComments(true);

			if(data.prunePath!=null) { 
				// when pruning is on: reader.read() below will wait until all is processed in the handler
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.Activated"));
		    if ( data.PathValue.equals(data.prunePath) )  {
          // Edge case, but if true, there will only ever be one item in the list
		      data.an = new ArrayList<AbstractNode>(1); // pre-allocate array and sizes
		      data.an.add(null);
		    }
				reader.addHandler( data.prunePath, 
				    new ElementHandler() {
				        public void onStart(ElementPath path) {
				            // do nothing here...    
				        }
				        public void onEnd(ElementPath path) {
				        	if(isStopped()) {
				        		// when a large file is processed and it should be stopped it is still reading the hole thing
				        		// the only solution I see is to prune / detach the document and this will lead into a 
				        		// NPE or other errors depending on the parsing location - this will be treated in the catch part below
				        		// any better idea is welcome
				        		if (log.isBasic()) logBasic(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.Stopped"));
				        		data.stopPruning=true;
				        		path.getCurrent().getDocument().detach();  // trick to stop reader
				        		return;
				        	}
				    		
				            // process a ROW element
				        	if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.StartProcessing"));
				            Element row = path.getCurrent();
				            try {
				              // Pass over the row instead of just the document. If
				              // if there's only one row, there's no need to
				              // go back to the whole document.
				            	processStreaming(row);
				            }
				            catch (Exception e ) {
				            	// catch the KettleException or others and forward to caller, e.g. when applyXPath() has a problem
				            	throw new RuntimeException(e);
				            }
				            // prune the tree
				            row.detach();
				            if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.EndProcessing"));
				        }
				    }
				);
			}
			
			if (IsInXMLField)
			{
				//read string to parse
				data.document= reader.read(new StringReader(StringXML));	
			}
			else if (readurl)
			{
				// read url as source
				data.document= reader.read(new URL(StringXML));	
			}
			else
			{	
				// get encoding. By default UTF-8
				String encoding="UTF-8";
				if (!Const.isEmpty(meta.getEncoding())) encoding=meta.getEncoding();
        InputStream is = KettleVFS.getInputStream(file);
        try {
          data.document = reader.read( is, encoding);
        } finally {
          BaseStep.closeQuietly(is);
        }
			}

			if(meta.isNamespaceAware())	prepareNSMap(data.document.getRootElement());	    
	   }catch (Exception e)
	   {
		   if (data.stopPruning) {
			   // ignore error when pruning
			   return false;
		   } else {
			   throw new KettleException(e);
		   }
	   }
	   return true;        
   }
   
	/**
	 * Process chunk of data in streaming mode.
	 * Called only by the handler when pruning is true.
	 * Not allowed in combination with meta.getIsInFields(), but could be redesigned later on.
	 * 
	 */
   private void processStreaming(Element row) throws KettleException  {
	   	data.document = row.getDocument();
	   	
		if(meta.isNamespaceAware())	prepareNSMap(data.document.getRootElement());
	   	if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.ApplyXPath"));
	  // If the prune path and the path are the same, then
	  // we're processing one row at a time through here.
   	if ( data.PathValue.equals(data.prunePath) )  {
   	  data.an.set(0,(AbstractNode)row);
      data.nodesize=1; // it's always just one row.
      data.nodenr=0;
      if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.ProcessingRows"));
      Object[] r=getXMLRowPutRowWithErrorhandling();
      if (! data.errorInRowButContinue) { // do not put out the row but continue
        putRowOut(r);  //false when limit is reached, functionality is there but we can not stop reading the hole file (slow but works)
      }
      data.nodesize=0;
      data.nodenr=0;
      return;
   	} else {
		if(!applyXPath())
		{
			throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableApplyXPath"));
		}
    }
		// main loop through the data until limit is reached or transformation is stopped
		// similar functionality like in BaseStep.runStepThread
		if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.ProcessingRows"));
		boolean cont=true;
		while (data.nodenr<data.nodesize && cont && !isStopped())
		{
			Object[] r=getXMLRowPutRowWithErrorhandling();
			if (data.errorInRowButContinue) continue; // do not put out the row but continue
			cont=putRowOut(r);  //false when limit is reached, functionality is there but we can not stop reading the hole file (slow but works)
		} 		
		if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "GetXMLData.Log.StreamingMode.FreeMemory"));
		// free allocated memory
		data.an.clear();
		data.nodesize=data.an.size();
		data.nodenr=0;
   }
   
   @SuppressWarnings("unchecked")
   public void prepareNSMap(Element l) 
   {
		for (Namespace ns : (List<Namespace>) l.declaredNamespaces()) 
		{
			if (ns.getPrefix().trim().length() == 0) 
			{
				data.NAMESPACE.put("pre" + data.NSPath.size(),ns.getURI());
				String path = "";
				Element element = l;
				while (element != null) 
				{
					if (element.getNamespacePrefix() != null && element.getNamespacePrefix().length() > 0) 
					{
						path = GetXMLDataMeta.N0DE_SEPARATOR + element.getNamespacePrefix()
								+ ":" + element.getName() + path;
					} else {
						path = GetXMLDataMeta.N0DE_SEPARATOR + element.getName() + path;
					}
					element = element.getParent();
				}
				data.NSPath.add(path);
			} else {
				data.NAMESPACE.put(ns.getPrefix(), ns.getURI());
			}
		}
		for (Element e : (List<Element>) l.elements()) {
			prepareNSMap(e);
		}
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
			logError(BaseMessages.getString(PKG, "GetXMLData.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "GetXMLData.Log.RequiredFiles", message));

			throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Log.RequiredFilesMissing",message));
		}

		List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
		if (nonAccessibleFiles.size() != 0)
		{
			String message = FileInputList.getRequiredFilesDescription(nonAccessibleFiles);
			logError(BaseMessages.getString(PKG, "GetXMLData.Log.RequiredFilesTitle"), BaseMessages.getString(PKG, "GetXMLData.Log.RequiredNotAccessibleFiles",message));

				throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Log.RequiredNotAccessibleFilesMissing",message));
		}
	}
   private boolean ReadNextString()
   {
	   
	   try{
		   data.readrow= getRow();  // Grab another row ...
		   
		   if (data.readrow==null) // finished processing!
           {
           	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.FinishedProcessing"));
               return false;
           }

		   if(first)
			{
			    first=false;
			    
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
					logError(BaseMessages.getString(PKG, "GetXMLData.Log.NoField"));
					throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Log.NoField"));
				}
				
				// cache the position of the field			
				if (data.indexOfXmlField<0)
				{	
					data.indexOfXmlField =getInputRowMeta().indexOfValue(meta.getXMLField());
					if (data.indexOfXmlField<0)
					{
						// The field is unreachable !
						logError(BaseMessages.getString(PKG, "GetXMLData.Log.ErrorFindingField", meta.getXMLField())); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Exception.CouldnotFindField",meta.getXMLField())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}

		   
		   
		   if(meta.isInFields())
		   {
			   // get XML field value
			   String Fieldvalue= getInputRowMeta().getString(data.readrow,data.indexOfXmlField);
				
			   if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.XMLStream", meta.getXMLField(),Fieldvalue));

			   if(meta.getIsAFile())
			   {
				   FileObject file=null;
					try
					{
						// XML source is a file.
						file=  KettleVFS.getFileObject(Fieldvalue, getTransMeta());
						//Open the XML document
						if(!setDocument(null,file,false,false)) 
						{
							throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableCreateDocument"));
						}
						
						if(!applyXPath())
						{
							throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableApplyXPath"));
						}

						addFileToResultFilesname(file);
			            
			            if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.LoopFileOccurences",""+data.nodesize,file.getName().getBaseName()));
						
					}
					catch (Exception e)
					{
						throw new KettleException (e);
					}finally{try {if(file!=null) file.close();}catch (Exception e){}
					}
			   }
			   else
			   {
				   boolean url=false;
				   boolean xmltring=true;
				    if(meta.isReadUrl())
				    {
				    	url=true;
				    	xmltring=false;
				    }
				    	
					//Open the XML document
					if(!setDocument(Fieldvalue,null,xmltring,url)) 
					{
						throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableCreateDocument"));
					}
	
					// Apply XPath and set node list
					if(!applyXPath())
					{
						throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableApplyXPath"));
					}
					if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.LoopFileOccurences",""+data.nodesize));		
			    }
		   }
	   }
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "GetXMLData.Log.UnexpectedError", e.toString()));
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
			resultFile.setComment(BaseMessages.getString(PKG, "GetXMLData.Log.FileAddedResult"));
			addResultFile(resultFile);
       }
   }
   public String addNSPrefix(String path, String loopPath) {
		if (data.NSPath.size() > 0) 
		{
			String fullPath = loopPath;
			if (!path.equals(fullPath)) {
				for (String tmp : path.split(GetXMLDataMeta.N0DE_SEPARATOR)) 
				{
					if (tmp.equals("..")) 
					{
						fullPath = fullPath.substring(0, fullPath.lastIndexOf(GetXMLDataMeta.N0DE_SEPARATOR));
					} else {
						fullPath += GetXMLDataMeta.N0DE_SEPARATOR + tmp;
					}
				}
			}
			int[] indexs = new int[fullPath.split(GetXMLDataMeta.N0DE_SEPARATOR).length - 1];
			java.util.Arrays.fill(indexs, -1);
			int length = 0;
			for (int i = 0; i < data.NSPath.size(); i++) 
			{
				if (data.NSPath.get(i).length() > length
						&& fullPath
								.startsWith(data.NSPath.get(i))) 
				{
					java.util.Arrays.fill(indexs, data.NSPath.get(i).split(GetXMLDataMeta.N0DE_SEPARATOR).length - 2,
							indexs.length, i);
					length = data.NSPath.get(i).length();
				}
			}

			StringBuilder newPath = new StringBuilder();
			String[] pathStrs = path.split(GetXMLDataMeta.N0DE_SEPARATOR);
			for (int i = 0; i < pathStrs.length; i++) 
			{
				String tmp = pathStrs[i];
				if (newPath.length() > 0) 
				{
					newPath.append(GetXMLDataMeta.N0DE_SEPARATOR);
				}
				if (tmp.length() > 0 && tmp.indexOf(":") == -1
						&& tmp.indexOf(".") == -1
						&& tmp.indexOf(GetXMLDataMeta.AT) == -1) {
					int index = indexs[i + indexs.length
							- pathStrs.length];
					if (index >= 0) 
					{
						newPath.append("pre").append(index).append(
								":").append(tmp);
					} else 
					{
						newPath.append(tmp);
					}
				} else 
				{
					newPath.append(tmp);
				}
			}
			return newPath.toString();
		}
		return path;
	}

   @SuppressWarnings("unchecked")
   private boolean applyXPath()
   {
	   try{
	       XPath xpath = data.document.createXPath(data.PathValue);
	       if(meta.isNamespaceAware())
	       {
	    	   xpath = data.document.createXPath(addNSPrefix(data.PathValue,	data.PathValue));
	    	   xpath.setNamespaceURIs(data.NAMESPACE);
	       }
	       // get nodes list
		   data.an =  (List<AbstractNode>) xpath.selectNodes(data.document);
		   data.nodesize=data.an.size();
		   data.nodenr=0;
	   }catch (Exception e)
	   {
		   logError(BaseMessages.getString(PKG, "GetXMLData.Log.ErrorApplyXPath",e.getMessage()));
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
            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.FinishedProcessing"));
                return false;
            }

			data.file = (FileObject) data.files.getFile(data.filenr);
			
			// Move file pointer ahead!
			data.filenr++;
            
			if(meta.isIgnoreEmptyFile() && data.file.getContent().getSize()==0)
			{
				// log only basic as a warning (was before logError)
				logBasic(BaseMessages.getString(PKG, "GetXMLData.Error.FileSizeZero", ""+data.file.getName()));
				openNextFile();
				
			}else
			{
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.OpeningFile", data.file.toString()));
	            
				//Open the XML document
				if(!setDocument(null,data.file,false,false)) 
				{
					if(data.stopPruning) return false; // ignore error when stopped while pruning
					throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableCreateDocument"));
				}

				// Apply XPath and set node list
				if(data.prunePath==null) { // this was already done in processStreaming()
					if(!applyXPath())
					{
						throw new KettleException (BaseMessages.getString(PKG, "GetXMLData.Log.UnableApplyXPath"));
					}
				}
				
				addFileToResultFilesname(data.file);
	
	            if (log.isDetailed()) 
	            {
	            	logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.FileOpened", data.file.toString()));
	               logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.LoopFileOccurences",""+data.nodesize,data.file.getName().getBaseName()));
	            }   
	         }
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "GetXMLData.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if(first && !meta.isInFields())
		{
			first = false;
			
			data.files = meta.getFiles(this);
			
		
			if(!meta.isdoNotFailIfNoFile() && data.files.nrOfFiles()==0)
			{
				throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Log.NoFiles"));
			}

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
		 // Grab a row
		Object[] r=getXMLRow();
		if (data.errorInRowButContinue) return true; //continue without putting the row out
		if (r==null)
	    {
			setOutputDone();  // signal end to receiver(s)
			return false; // end of data or error.
	    }
		 
		return putRowOut(r);
		
	}
	
	private boolean putRowOut(Object[] r) throws KettleException
	{		 
		 if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetXMLData.Log.ReadRow", data.outputRowMeta.getString(r)));
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

		if(!meta.isInFields())
		{
			while ((data.nodenr>=data.nodesize ||  data.file==null))
			{
		        if (!openNextFile())
		        {
		        	data.errorInRowButContinue=false; //stop in all cases
		            return null;
		        }
			} 
		}
		return getXMLRowPutRowWithErrorhandling();
	}

	private Object[] getXMLRowPutRowWithErrorhandling()  throws KettleException
	{
		 // Build an empty row based on the meta-data		  
		 Object[] r=null;
		 data.errorInRowButContinue=false;
		 try{	
			 if(meta.isInFields())
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
			 	
			if(meta.isInFields())
				r= processPutRow(data.readrow,(AbstractNode)data.an.get(data.nodenr));
			else
				r= processPutRow(null,(AbstractNode)data.an.get(data.nodenr));
		 }
		 catch (Exception e)
		 {
			throw new KettleException(BaseMessages.getString(PKG, "GetXMLData.Error.UnableReadFile"), e);
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
			if(row!=null) { 
			  outputRowData = row.clone();
			}
			// Read fields...
			for (int i=0;i<data.nrInputFields;i++)
			{	
				// Get field
				GetXMLDataField xmlDataField = meta.getInputFields()[i];
				// Get the Path to look for
				String XPathValue = xmlDataField.getXPath();

				if(meta.isuseToken())
				{
					// See if user use Token inside path field
					// The syntax is : @_Fieldname-
					// PDI will search for Fieldname value and replace it
					// Fieldname must be defined before the current node
					XPathValue=substituteToken(XPathValue, outputRowData);
					if (isDetailed() ) 	logDetailed(toString(),XPathValue);
				}
				
				// Get node value
				String nodevalue =null;
				
				// Handle namespaces
				if(meta.isNamespaceAware())
				{
					XPath xpathField = node.createXPath(addNSPrefix(XPathValue, data.PathValue));
					xpathField.setNamespaceURIs(data.NAMESPACE);
					nodevalue=xpathField.valueOf(node);
				}else
				{
					nodevalue=node.valueOf(XPathValue);
				}
				
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
				
				if(meta.isInFields())
				{
					// Add result field to input stream
	                outputRowData = RowDataUtil.addValueData(outputRowData,data.totalpreviousfields+i, nodevalue);
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
						outputRowData[data.totalpreviousfields+i] = data.previousRow[data.totalpreviousfields+i];
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
			
			if (irow == null) {
			  data.previousRow = outputRowData;
			} else {
			  // clone to previously allocated array to make sure next step doesn't
			  // change it in between...
			  for (int i=0; i<outputRowData.length; i++) {
			    // Clone without re-allocating array
			    this.prevRow[i] = outputRowData[i]; // Direct copy
			  }
        data.previousRow = irow.cloneRow(outputRowData, this.prevRow); // Pick up everything else that needs a real deep clone 
			}
	    
		}
		catch(Exception e)
		{
			 if (getStepMeta().isDoingErrorHandling())
			 {
				 //Simply add this row to the error row
				 putError(data.outputRowMeta, outputRowData, 1, e.toString(), null, "GetXMLData001");
				 data.errorInRowButContinue = true;				 
				 return null;
			 } else {
				logError(e.toString());
				throw new KettleException(e.toString());
			 }
		} 
		return outputRowData;
	}

	public String substituteToken(String aString, Object[] outputRowData)
	{
		if (aString==null) return null;

		StringBuffer buffer = new StringBuffer();

		String rest = aString;

		// search for closing string
		int i = rest.indexOf(data.tokenStart);
		while (i > -1) {
			int j = rest.indexOf(data.tokenEnd, i + data.tokenStart.length());
			// search for closing string
			if (j > -1) {
				String varName = rest.substring(i + data.tokenStart.length(), j);
				Object Value = varName;
				
				for (int k=0;k<data.nrInputFields;k++) {
					GetXMLDataField Tmp_xmlInputField = meta.getInputFields()[k];
					if(Tmp_xmlInputField.getName().equalsIgnoreCase(varName)) {	
						Value="'"+ outputRowData[data.totalpreviousfields+k] +"'";
					}
				}	
				buffer.append(rest.substring(0, i));
				buffer.append(Value);
				rest = rest.substring(j + data.tokenEnd.length());
			} else {
				// no closing tag found; end the search
				buffer.append(rest);
				rest = "";
			}
			// keep searching
			i = rest.indexOf(data.tokenEnd);
		}
		buffer.append(rest);
		return buffer.toString();
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GetXMLDataMeta)smi;
		data=(GetXMLDataData)sdi;				
		
		if (super.init(smi, sdi))
		{
			data.rownr = 1L;
			data.nrInputFields=meta.getInputFields().length;
			
			// correct attribut path if needed
			// do it once
			for(int i=0; i<data.nrInputFields; i++) {
				GetXMLDataField xmlDataField = meta.getInputFields()[i];
				if (xmlDataField.getElementType() == GetXMLDataField.ELEMENT_TYPE_ATTRIBUT){
					// We have an attribut
					// do we need to add leading @
					String XPathValue=environmentSubstitute(xmlDataField.getXPath());
					
					//Only put @ to the last element in path, not in front at all
					int last=XPathValue.lastIndexOf(GetXMLDataMeta.N0DE_SEPARATOR);
					if(last>-1){
						last++;
						String attribut=XPathValue.substring(last, XPathValue.length());
						if(!attribut.startsWith(GetXMLDataMeta.AT)) {
							XPathValue=XPathValue.substring(0, last)+GetXMLDataMeta.AT+attribut;
							xmlDataField.setXPath(XPathValue);
						}
					}else{
						if(!XPathValue.startsWith(GetXMLDataMeta.AT)) {
							XPathValue=GetXMLDataMeta.AT+XPathValue; 
							xmlDataField.setXPath(XPathValue);
						}
					}
				}
			}
			
			data.PathValue=environmentSubstitute(meta.getLoopXPath());
			if(Const.isEmpty(data.PathValue)) {
				logError(BaseMessages.getString(PKG, "GetXMLData.Error.EmptyPath"));
				return false;
			}
			if(!data.PathValue.substring(0,1).equals(GetXMLDataMeta.N0DE_SEPARATOR)) data.PathValue=GetXMLDataMeta.N0DE_SEPARATOR + data.PathValue;
			if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetXMLData.Log.LoopXPath",data.PathValue));
			
			data.prunePath=environmentSubstitute(meta.getPrunePath());
			if(data.prunePath!=null) {
				if(Const.isEmpty(data.prunePath.trim())) {
					data.prunePath=null; 
				} else {
					// ensure a leading slash
					if(!data.prunePath.startsWith(GetXMLDataMeta.N0DE_SEPARATOR)) data.prunePath=GetXMLDataMeta.N0DE_SEPARATOR+data.prunePath;
					// check if other conditions apply that do not allow pruning
					if(meta.isInFields()) data.prunePath=null; // not possible by design, could be changed later on
				}
			}
			
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

}