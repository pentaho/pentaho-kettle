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
 
package be.ibridge.kettle.trans.step.mapping;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Meta-data for the Mapping step: contains name of the (sub-)transformation to execute 
 * 
 * @since 22-nov-2005
 * @author Matt
 *
 */

public class MappingMeta extends BaseStepMeta implements StepMetaInterface
{
    private TransMeta mappingTransMeta;
    
    private String fileName;
    private long   transformationID;
    
	public MappingMeta()
	{
		super(); // allocate BaseStepMeta
	}

    /**
     * @return Returns the mappingTransMeta.
     */
    public TransMeta getMappingTransMeta()
    {
        return mappingTransMeta;
    }

    /**
     * @param mappingTransMeta The mappingTransMeta to set.
     */
    public void setMappingTransMeta(TransMeta mappingTransMeta)
    {
        this.mappingTransMeta = mappingTransMeta;
    }



    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
        try
        {
            readData(stepnode);
        }
        catch(KettleException e)
        {
            throw new KettleXMLException("Error loading transformation step from XML", e);
        }
        
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleException
	{
        transformationID = Const.toLong( XMLHandler.getTagValue(stepnode, "id_transformation"), -1L);
        fileName         = XMLHandler.getTagValue(stepnode, "filename");
        
        loadMappingMeta(null);
	}
    
    public String getXML()
    {
        String retval="";
        
        if (mappingTransMeta!=null)
        {
            retval+="    "+XMLHandler.addTagValue("id_transformation", mappingTransMeta.getID());
            retval+="    "+XMLHandler.addTagValue("filename", mappingTransMeta.getFilename());
        }

        return retval;
    }

	public void setDefault()
	{
        transformationID = -1L;
        fileName = null;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
        transformationID = rep.getStepAttributeInteger(id_step, "id_transformation");
        fileName         = rep.getStepAttributeString(id_step, "filename");
        
        loadMappingMeta(rep);
	}
    
    private void loadMappingMeta(Repository rep) throws KettleException
    {
        // OK, load the meta-data from file...
        // NOTE: id_transformation is saved, but is useless...
        if (fileName!=null && fileName.length()>0)
        {
            mappingTransMeta = new TransMeta(fileName);
        }
        
        // OK, load the meta-data from the repository...
        // NOTE: filename is saved, but is useless in this context...
        if (transformationID>0 && rep!=null)
        {
            Row transRow = rep.getTransformation(transformationID);
            String transName = transRow.getString("NAME", null);
            long directoryID = transRow.getInteger("ID_DIRECTORY", -1L);
            RepositoryDirectory repositoryDirectory = rep.getDirectoryTree().findDirectory(directoryID);
            
            mappingTransMeta = new TransMeta(rep, transName, repositoryDirectory);
        }
    }
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
        if (mappingTransMeta!=null)
        {
            rep.saveStepAttribute(id_transformation, id_step, "id_transformation", mappingTransMeta.getID());
            rep.saveStepAttribute(id_transformation, id_step, "filename", mappingTransMeta.getFilename());
        }
	}
	
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
		}

        // See if we have input streams leading to this step!
        if (mappingTransMeta!=null)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "A mapping (transformation) is specified.", stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No mapping is specified!", stepinfo);
            remarks.add(cr);
        }

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new MappingDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Mapping(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new MappingData();
	}


}
