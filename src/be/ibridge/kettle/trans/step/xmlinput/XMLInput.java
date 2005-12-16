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

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
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
		debug="start of processRow()";
		
		Row row = getRowFromXML();
		if (row==null) 
		{
		    setOutputDone();  // signal end to receiver(s)
		    return false;     // This is the end of this step.
		}
		
		logRowlevel("Read row: "+row.toString());
		putRow(row);

		return true;
	}
		
	private Row getRowFromXML()
    {
        // First, see if we need to open a new file
        if (data.filename==null)
        {
            openNextFile();
            positionInFile();
        }
        
        Row row = buildEmptyRow();
        
        // Get the item in the XML file...
        
        // First get the appropriate node
        
        Node node = XMLHandler.getSubNodeByNr(data.section, data.itemElement, data.itemPosition);
        data.itemPosition++;
        
        // Read from the Node...
        for (int i=0;i<meta.getInputFields().length;i++)
        {
            XMLInputField xmlInputField = meta.getInputFields()[i];
            
            String value = null;
            
            for (int p=0; (value==null) && p<xmlInputField.getXmlInputFieldPositions().length;p++)
            {
                XMLInputFieldPosition pos = xmlInputField.getXmlInputFieldPositions()[i];
                if (pos.getType()==XMLInputFieldPosition.XML_ELEMENT)
                {
                    node = XMLHandler.getSubNode(node, pos.getName());
                    if (p==xmlInputField.getXmlInputFieldPositions().length-1) // last level
                    {
                        value = XMLHandler.getNodeValue(node);
                    }
                }
                else
                {
                    value = XMLHandler.getTagAttribute(node, pos.getName());
                }
            }
            
            // OK, we have the string...
            Value v = row.getValue(i);
            v.setValue(value);
        }
        
        return row;
    }

    private void positionInFile()
    {
        
        
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
		    // Is this the last file?
			data.last_file = ( data.filenr==data.files.length-1);
			data.filename = data.files[data.filenr];
			
			logBasic("Opening file: "+data.filename);
			
			// Move file pointer ahead!
			data.filenr++;
            
            // Open the XML document
            data.document = XMLHandler.loadXMLFile(data.filename);
            
            // Position in the file...
            data.section = data.document;
            
            for (int i=0;i<meta.getInputPosition().length-1;i++)
            {
               data.section = XMLHandler.getSubNode(data.section, meta.getInputPosition()[i]);
            }
            // Last element gets repeated: what's the name?
            data.itemElement = meta.getInputPosition()[meta.getInputPosition().length-1];
            
            data.itemCount = XMLHandler.countNodes(data.section, data.itemElement);
            data.itemPosition = 0;
		}
		catch(Exception e)
		{
			logError("Couldn't open file #"+data.filenr+" : "+data.filename+" --> "+e.toString());
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
			data.files = meta.getFiles();
			if (data.files==null || data.files.length==0)
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputMeta)smi;
		data=(XMLInputData)sdi;

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
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
