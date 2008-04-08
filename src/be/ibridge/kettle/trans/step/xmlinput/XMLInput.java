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
 

package be.ibridge.kettle.trans.step.xmlinput;

import org.apache.commons.vfs.FileObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
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
public class XMLInput extends BaseStep implements StepInterface
{
	private XMLInputMeta meta;
	private XMLInputData data;
	
	public XMLInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Row row = getRowFromXML();
		if (row==null) 
		{
		    setOutputDone();  // signal end to receiver(s)
		    return false;     // This is the end of this step.
		}
		
		if (log.isRowLevel()) logRowlevel(Messages.getString("XMLInput.Log.ReadRow", row.toString()));
        
        linesInput++;
        
        putRow(row);
        
        if (meta.getRowLimit()>0 && data.rownr>=meta.getRowLimit())  // limit has been reached: stop now.
        {
            setOutputDone();
            return false;
        }

		return true;
	}
		
	private Row getRowFromXML() throws KettleValueException
    {
        while (data.itemPosition>=data.itemCount || data.file==null) // finished reading the file, read the next file!
        {
            data.file=null;
            if (!openNextFile())
            {
                return null;
            }
        }
        
        Row row = buildEmptyRow();
        
        // Get the item in the XML file...
        
        // First get the appropriate node
        
        Node itemNode;
        if (meta.getInputPosition().length>1)
        {
            itemNode = XMLHandler.getSubNodeByNr(data.section, data.itemElement, data.itemPosition);
        }
        else
        {
            itemNode = data.section; // Only the root node, 1 element to read in the whole document.
        }
        data.itemPosition++;

        // Read from the Node...
        for (int i=0;i<meta.getInputFields().length;i++)
        {
            Node node = itemNode;
            
            XMLInputField xmlInputField = meta.getInputFields()[i];

            String value = null;
            
            for (int p=0; (value==null) && node!=null && p<xmlInputField.getFieldPosition().length;p++)
            {
                XMLInputFieldPosition pos = xmlInputField.getFieldPosition()[p];

                switch(pos.getType())
                {
                case XMLInputFieldPosition.XML_ELEMENT :
                    {
                        if (pos.getElementNr()<=1)
                        {
                            Node subNode = XMLHandler.getSubNode(node, pos.getName());
                            if (subNode!=null)
                            {
                                if (p==xmlInputField.getFieldPosition().length-1) // last level
                                {
                                    value = XMLHandler.getNodeValue(subNode);
                                }
                            }
                            else
                            {
                            	if (log.isDebug()) logDebug(Messages.getString("XMLInput.Log.UnableToFindPosition", pos.toString(), node.toString()));
                            }
                            node=subNode;
                        }
                        else // Multiple possible values: get number pos.getElementNr()!
                        {
                            Node subNode = XMLHandler.getSubNodeByNr(node, pos.getName(), pos.getElementNr()-1, false);
                            if (subNode!=null)
                            {
                                if (p==xmlInputField.getFieldPosition().length-1) // last level
                                {
                                    value = XMLHandler.getNodeValue(subNode);
                                }
                            }
                            else
                            {
                            	if (log.isDebug()) logDebug(Messages.getString("XMLInput.Log.UnableToFindPosition", pos.toString(), node.toString()));
                            }
                            node=subNode;
                        }
                    }
                    break;
                    
                case XMLInputFieldPosition.XML_ATTRIBUTE:
                    {
                        value = XMLHandler.getTagAttribute(node, pos.getName());
                    }
                    break;
                case XMLInputFieldPosition.XML_ROOT:
                    {
                        value = XMLHandler.getNodeValue(node);
                    }
                    break;
                default: break;
                }
                
            }
            
            // OK, we have the string...
            Value v = row.getValue(i);
            
            if (value!=null) v.setValue(value); else v.setNull();
            
            // DO Trimming!
            switch(xmlInputField.getTrimType())
            {
            case XMLInputField.TYPE_TRIM_LEFT  : v.ltrim(); break;
            case XMLInputField.TYPE_TRIM_RIGHT : v.rtrim(); break;
            case XMLInputField.TYPE_TRIM_BOTH  : v.trim(); break;
            default: break;
            }
            
            // System.out.println("after trim, field #"+i+" : "+v);
            
            // DO CONVERSIONS...
            switch(xmlInputField.getType())
            {
            case Value.VALUE_TYPE_STRING:
                // System.out.println("Convert value to String :"+v);
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
            if (meta.getInputFields()[i].isRepeated())
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
            Value fn = new Value( meta.getFilenameField(), KettleVFS.getFilename(data.file));
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
        
        // Throw away the information in the item?
        NodeList nodeList = itemNode.getChildNodes();
        for (int i=0;i<nodeList.getLength();i++) itemNode.removeChild(nodeList.item(i));
        
        return row;
    }

    /**
     * Build an empty row based on the meta-data...
     * @return
     */
    private Row buildEmptyRow()
    {
        Row row = new Row();
        
        XMLInputField fields[] = meta.getInputFields();
        for (int i=0;i<fields.length;i++)
        {
            XMLInputField field = fields[i];
            
            Value value = new Value(field.getName(), field.getType());
            value.setLength(field.getLength(), field.getPrecision());
            value.setNull();
            
            row.addValue(value);
        }
        
        return row;
    }

	private boolean openNextFile()
	{
		try
		{
            if (data.filenr>=data.files.size()) // finished processing!
            {
            	if (log.isDetailed()) logDetailed(Messages.getString("XMLInput.Log.FinishedProcessing"));
                return false;
            }
            
		    // Is this the last file?
			data.last_file = ( data.filenr==data.files.size()-1);
			data.file = (FileObject) data.files.get(data.filenr);
			
			logBasic(Messages.getString("XMLInput.Log.OpeningFile", data.file.toString()));
			
			// Move file pointer ahead!
			data.filenr++;
            
            // Open the XML document
            data.document = XMLHandler.loadXMLFile(data.file);
            
			// Add this to the result file names...
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
			resultFile.setComment("File was read by an XML input step");
			addResultFile(resultFile);

            if (log.isDetailed()) logDetailed(Messages.getString("XMLInput.Log.FileOpened", data.file.toString()));
            
            // Position in the file...
            data.section = data.document;
            
            for (int i=0;i<meta.getInputPosition().length-1;i++)
            {
               data.section = XMLHandler.getSubNode(data.section, meta.getInputPosition()[i]);
            }
            // Last element gets repeated: what's the name?
            data.itemElement = meta.getInputPosition()[meta.getInputPosition().length-1];
            
            data.itemCount = XMLHandler.countNodes(data.section, data.itemElement);
            data.itemPosition = (int)meta.getNrRowsToSkip();
		}
		catch(Exception e)
		{
			logError(Messages.getString("XMLInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputMeta)smi;
		data=(XMLInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles().getFiles();
			if (data.files==null || data.files.size()==0)
			{
				logError(Messages.getString("XMLInput.Log.NoFiles"));
				return false;
			}
            
            data.rownr = 1L;
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputMeta)smi;
		data=(XMLInputData)sdi;
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
	//
	public void run()
	{			    
		try
		{
			logBasic("Starting to run...");
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