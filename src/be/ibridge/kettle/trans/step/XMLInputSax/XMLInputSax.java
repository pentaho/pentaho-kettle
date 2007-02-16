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
 

package be.ibridge.kettle.trans.step.XMLInputSax;

//import org.w3c.dom.Node;

//import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
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
public class XMLInputSax extends BaseStep implements StepInterface
{
	private XMLInputSaxMeta meta;
	private XMLInputSaxData data;
	
	public XMLInputSax(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
		
		logRowlevel("Read row: "+row.toString());
        
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
        Row row;
        
        if ( data.document==null ) // finished reading the file, read the next file!
        {
            data.filename=null;
        }
        else if ( !data.document.hasNext())
        {
        	data.filename=null;
        }
        
        // First, see if we need to open a new file
        if (data.filename==null)
        {
            if (!openNextFile())
            {
                return null;
            }
        }
        
        row=data.document.getNext();	

        //Node itemNode = XMLHandler.getSubNodeByNr(data.section, data.itemElement, data.itemPosition);
        //data.itemPosition++;

        // See if we need to add the filename to the row...  
        if (meta.includeFilename() && meta.getFilenameField()!=null && meta.getFilenameField().length()>0)
        {
            Value fn = new Value( meta.getFilenameField(), data.filename );
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
        
        return row;
    }



	private boolean openNextFile()
	{
		try
		{
            if (data.filenr>=data.files.length) // finished processing!
            {
                logDetailed("Finished processing files.");
                return false;
            }
            
		    // Is this the last file?
			data.last_file = ( data.filenr==data.files.length-1);
			data.filename = data.files[data.filenr];
			
			logBasic("Opening file: "+data.filename);
			
			// Move file pointer ahead!
			data.filenr++;
            
            // Open the XML document
            data.document = new XMLInputSaxDataRetreiver(data.filename,meta,data);
            data.document.runExample();
            
		}
		catch(Exception e)
		{
			logError("Couldn't open file #"+data.filenr+" : "+data.filename+" --> "+e.toString()+e.getMessage()+e.getLocalizedMessage());
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputSaxMeta)smi;
		data=(XMLInputSaxData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles();
			if (data.files==null || data.files.length==0)
			{
				logError("No file(s) specified! Stop processing.");
				return false;
			}
            
            data.rownr = 1L;
			
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XMLInputSaxMeta)smi;
		data=(XMLInputSaxData)sdi;

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