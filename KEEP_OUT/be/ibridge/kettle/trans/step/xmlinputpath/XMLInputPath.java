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
 

package be.ibridge.kettle.trans.step.xmlinputpath;

import org.apache.commons.vfs.FileObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
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
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class XMLInputPath extends BaseStep implements StepInterface
{
	private XMLInputPathMeta meta;
	private XMLInputPathData data;
	
	public XMLInputPath(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
		    	
				logBasic("-----------------------------------");
		    	
				logBasic(Messages.getString("XMLInputPath.Log.OpeningFile", data.file.toString()));
		    	
				// Fetch files and process each one
				Processfile(data.file);	
				
				
				if (log.isDetailed()) logDetailed(Messages.getString("XMLInputPath.Log.FileOpened", data.file.toString()));
		        		
	
			}
	    	
			//	Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment(Messages.getString("XMLInputPath.Log.FileAddedResult"));
			addResultFile(resultFile);
	    	
			// 	 Move file pointer ahead!
			data.filenr++;	
			
		}
		
		setOutputDone();  // signal end to receiver(s)
		return false;     // This is the end of this step.
   
	}
		
	private String getValueXML(NodeList widgetNodes,int itFileInputXML_1,javax.xml.xpath.XPath xpath, String xpathvalue, String element_type)
	{
		String valueNode=null;
	
		try
		{
			Node widgetNode = widgetNodes.item(itFileInputXML_1);
			
			if (element_type.equals("node"))
			{
				// Get Node value
				Node resultNode = (Node) xpath.evaluate(xpathvalue,widgetNode, javax.xml.xpath.XPathConstants.NODE);
			
				//widgetNode
				if (resultNode != null) valueNode=XMLHandler.getNodeValue( resultNode ); // resultNode.getTextContent();
			}
			else
			{
				// Get attribute value
                NamedNodeMap attributes = widgetNode.getAttributes();
                if (attributes!=null)
                {
                    Node namedItem = widgetNode.getAttributes().getNamedItem(xpathvalue);
                    if (namedItem!=null)
                    {
                        valueNode=XMLHandler.getNodeValue(namedItem); // namedItem.getTextContent();
                    }
                }
			}
		} 
		catch (Exception e) 
		{
			logError("Error : " + e.toString());
			stopAll();
			setErrors(1);		
		}
			
		return valueNode;
	}
	
	private void Processfile(FileObject file)
	{
		
		try 
		{
			// get encoding. By default UTF-8
			String encodage="UTF-8";
			if (!Const.isEmpty(meta.getEncoding()))
			{
				encodage=meta.getEncoding();
			}
			javax.xml.parsers.DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document document = builder.parse(new org.xml.sax.InputSource(new java.io.InputStreamReader(new java.io.FileInputStream(KettleVFS.getFilename(file)), encodage)));        
	    	
			javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
			NodeList widgetNodes = (NodeList) xpath.evaluate(meta.getRealLoopXPath(), document,javax.xml.xpath.XPathConstants.NODESET);
	        
			if (log.isDetailed()) logDetailed(widgetNodes.getLength() + Messages.getString("XMLInputPath.Log.LoopOccurences1") + 
									  KettleVFS.getFilename(file) + Messages.getString("XMLInputPath.Log.LoopOccurences2") );
			
	        
	        
			for (int itFileInputXML_1 = 0; itFileInputXML_1 < widgetNodes.getLength(); itFileInputXML_1++) 
			{
				if ((meta.getRowLimit()>0 && data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0)  
				{
					// Create new row
					Row row = buildEmptyRow();
					
					// Read from the Node...
					for (int i=0;i<meta.getInputFields().length;i++)
					{
						XMLInputPathField xmlInputField = meta.getInputFields()[i];
						// Get the Path to look for
						String XPathValue = xmlInputField.getRealXPath();
						String Element_Type = xmlInputField.getElementTypeCode();
			        
			        	
						String valueNode = getValueXML(widgetNodes,itFileInputXML_1,xpath, XPathValue,Element_Type);        	 
			        	
			        	
						// OK, we have the string...
						Value v = row.getValue(i);
						v.setValue(valueNode);
			 			
						// DO Trimming!
						switch(xmlInputField.getTrimType())
						{
							case XMLInputPathField.TYPE_TRIM_LEFT  : v.ltrim(); break;
							case XMLInputPathField.TYPE_TRIM_RIGHT : v.rtrim(); break;
							case XMLInputPathField.TYPE_TRIM_BOTH  : v.trim(); break;
							default: break;
						}
			      	            
			            
						// DO CONVERSIONS...
						switch(xmlInputField.getType())
						{
							case Value.VALUE_TYPE_STRING:
								break;
							case Value.VALUE_TYPE_NUMBER:
								// System.out.println("Convert value to Number :"+v);
								if (xmlInputField.getFormat()!=null && xmlInputField.getFormat().length()>0)
								{
									if (xmlInputField.getDecimalSymbol()!=null && xmlInputField.getDecimalSymbol().length()>0)
									{
										if (xmlInputField.getGroupSymbol()!=null && xmlInputField.getGroupSymbol().length()>0)
										{
											if (xmlInputField.getCurrencySymbol()!=null && xmlInputField.getCurrencySymbol().length()>0)
											{
												v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol(), xmlInputField.getGroupSymbol(), xmlInputField.getCurrencySymbol());
											}
											else
											{
												v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol(), xmlInputField.getGroupSymbol());
											}
										}
										else
										{
											v.str2num(xmlInputField.getFormat(), xmlInputField.getDecimalSymbol());
										}
									}
									else
									{
										v.str2num(xmlInputField.getFormat()); // just a format mask
									}
								}
								else
								{
									v.str2num();
								}
								v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
								break;
							case Value.VALUE_TYPE_INTEGER:
								// System.out.println("Convert value to integer :"+v);
								v.setValue(v.getInteger());
								v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
								break;
							case Value.VALUE_TYPE_BIGNUMBER:
								// System.out.println("Convert value to BigNumber :"+v);
								v.setValue(v.getBigNumber());
								v.setLength(xmlInputField.getLength(), xmlInputField.getPrecision());
								break;
							case Value.VALUE_TYPE_DATE:
								// System.out.println("Convert value to Date :"+v);
	
								if (xmlInputField.getFormat()!=null && xmlInputField.getFormat().length()>0)
								{
									v.str2dat(xmlInputField.getFormat());
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
						if (xmlInputField.isRepeated())
						{
							if (v.isNull() && data.previousRow!=null)
							{
								Value previous = data.previousRow.getValue(i);
								v.setValue(previous);
							}
						}
			      
					} // End of loop over fields...
					   	
			        
					// See if we need to add the filename to the row...  
					if (meta.includeFilename() && meta.getFilenameField()!=null && meta.getFilenameField().length()>0)
					{
						Value fn = new Value( meta.getRealFilenameField(), KettleVFS.getFilename(file));
						row.addValue(fn);
			            
					}
			        
					// See if we need to add the row number to the row...  
					if (meta.includeRowNumber() && meta.getRowNumberField()!=null && meta.getRowNumberField().length()>0)
					{
						Value fn = new Value( meta.getRealRowNumberField(), data.rownr );
						row.addValue(fn);
					}
			        
					data.previousRow = new Row(row); // copy it to make sure the next step doesn't change it in between... 
					data.rownr++;
			        
		    		
					if (log.isRowLevel()) logRowlevel(Messages.getString("XMLInputPath.Log.ReadRow", row.toString()));        
		            
					putRow(row);
				}
			}        
		} 
		catch(Exception e)
		{
			logError(Messages.getString("XMLInputPath.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
		}    
    		
		
	}
	

	/**
	 * Build an empty row based on the meta-data...
	 * @return
	 */
	private Row buildEmptyRow()
	{
		Row row = new Row();
        
		XMLInputPathField fields[] = meta.getInputFields();
		for (int i=0;i<fields.length;i++)
		{
			XMLInputPathField field = fields[i];
            
			Value value = new Value(StringUtil.environmentSubstitute(field.getName()), field.getType());
			value.setLength(field.getLength(), field.getPrecision());
			value.setNull();
            
			row.addValue(value);
		}
        
		return row;
	}

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputPathMeta)smi;
		data=(XMLInputPathData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
			if (data.files==null || data.files.size()==0)
			{
				logError(Messages.getString("XMLInputPath.Log.NoFiles"));
				return false;
			}
            
			data.rownr = 1L;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputPathMeta)smi;
		data=(XMLInputPathData)sdi;

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	//
	//
	public void run()
	{			    
		try
		{
			logBasic(Messages.getString("XMLInputPath.Log.StartingRun"));		
			
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