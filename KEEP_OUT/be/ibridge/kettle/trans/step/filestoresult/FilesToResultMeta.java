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


package be.ibridge.kettle.trans.step.filestoresult;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * 
 * @author matt
 * @since 26-may-2006
 *
 */

public class FilesToResultMeta extends BaseStepMeta implements StepMetaInterface
{
	private String filenameField;
	
	private int fileType;
	
	/**
	 * @return Returns the fieldname that contains the filename.
	 */
	public String getFilenameField()
	{
		return filenameField;
	}

	/**
	 * @param filenameField set the fieldname that contains the filename.
	 */
	public void setFilenameField(String filenameField)
	{
		this.filenameField = filenameField;
	}

	/**
	 * @return Returns the fileType.
	 * @see ResultFile
	 */
	public int getFileType()
	{
		return fileType;
	}

	/**
	 * @param fileType The fileType to set.
	 * @see ResultFile
	 */
	public void setFileType(int fileType)
	{
		this.fileType = fileType;
	}

	
	
	public FilesToResultMeta()
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
		
		xml.append(XMLHandler.addTagValue("filename_field", filenameField));  // $NON-NLS-1
		xml.append(XMLHandler.addTagValue("file_type", ResultFile.getTypeCode(fileType))); //$NON-NLS-1$
		
		return xml.toString();
	}
	
	private void readData(Node stepnode)
	{
		filenameField = XMLHandler.getTagValue(stepnode, "filename_field"); //$NON-NLS-1$
		fileType = ResultFile.getType( XMLHandler.getTagValue(stepnode, "file_type") ); //$NON-NLS-1$
	}

	public void setDefault()
	{
		filenameField=null;
		fileType = ResultFile.FILE_TYPE_GENERAL;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		filenameField = rep.getStepAttributeString(id_step, "filename_field"); //$NON-NLS-1$
		fileType = ResultFile.getType( rep.getStepAttributeString(id_step, "file_type") ); //$NON-NLS-1$
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		rep.saveStepAttribute(id_transformation, id_step, "filename_field", filenameField); //$NON-NLS-1$
		rep.saveStepAttribute(id_transformation, id_step, "file_type", ResultFile.getTypeCode(fileType)); //$NON-NLS-1$
	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FilesToResultMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FilesToResultMeta.CheckResult.NoInputReceivedError"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new FilesToResultDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new FilesToResult(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new FilesToResultData();
	}


}
