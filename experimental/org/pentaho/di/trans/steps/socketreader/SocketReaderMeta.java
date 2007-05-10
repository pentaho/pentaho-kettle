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
 
package org.pentaho.di.trans.steps.socketreader;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;



/*
 * Created on 02-jun-2003
 *
 */

public class SocketReaderMeta extends BaseStepMeta implements StepMetaInterface
{
    private String hostname;
    private String port;
    private String bufferSize;
    private boolean compressed;
    
	public SocketReaderMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
    public String getXML()
    {
        StringBuffer xml = new StringBuffer();
        
        xml.append("     "+XMLHandler.addTagValue("hostname", hostname));
        xml.append("     "+XMLHandler.addTagValue("port", port));
        xml.append("     "+XMLHandler.addTagValue("buffer_size", bufferSize));
        xml.append("     "+XMLHandler.addTagValue("compressed", compressed));

        return xml.toString();
    }
    
	private void readData(Node stepnode)
	{
        hostname      = XMLHandler.getTagValue(stepnode, "hostname");
        port          = XMLHandler.getTagValue(stepnode, "port");
        bufferSize    = XMLHandler.getTagValue(stepnode, "buffer_size");
        compressed    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "compressed") );
	}

	public void setDefault()
	{
        bufferSize = "3000";
        compressed = true;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
	{
        hostname      = rep.getStepAttributeString(id_step, "hostname");
        port          = rep.getStepAttributeString(id_step, "port");
        bufferSize    = rep.getStepAttributeString(id_step, "buffer_size");
        compressed    = rep.getStepAttributeBoolean(id_step, "compressed");
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
        rep.saveStepAttribute(id_transformation, id_step, "hostname", hostname);
        rep.saveStepAttribute(id_transformation, id_step, "port", port);
        rep.saveStepAttribute(id_transformation, id_step, "buffer_size", bufferSize);
        rep.saveStepAttribute(id_transformation, id_step, "compressed", compressed);
	}
	
	public void check(ArrayList remarks, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("SocketReaderMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SocketReaderMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SocketReaderMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SocketReaderMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new SocketReaderDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new SocketReader(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SocketReaderData();
	}

    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    /**
     * @return the port
     */
    public String getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port)
    {
        this.port = port;
    }

    public String getBufferSize()
    {
        return bufferSize;
    }
    
    public void setBufferSize(String bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    /**
     * @return the compressed
     */
    public boolean isCompressed()
    {
        return compressed;
    }

    /**
     * @param compressed the compressed to set
     */
    public void setCompressed(boolean compressed)
    {
        this.compressed = compressed;
    }
}
