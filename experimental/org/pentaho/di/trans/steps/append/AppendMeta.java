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

package org.pentaho.di.trans.steps.append;

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

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;

/**
 * @author Sven Boden
 * @since  3-june-2007
 */
public class AppendMeta extends BaseStepMeta implements StepMetaInterface
{
	private String   headStepName;
	private StepMeta headStepMeta;

	private String   tailStepName;  
	private StepMeta tailStepMeta;

	
    public AppendMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
     * @return Returns the hopname to be used as tail
     */
    public String getTailStepName()
    {
		if (tailStepMeta!=null && 
	        tailStepMeta.getName()!=null &&
	        tailStepMeta.getName().length()>0
		   ) 
			return tailStepMeta.getName();
		return null;
   }
 
	/**
     * @return Returns the hopname to be used as head
     */
    public String getHeadStepName()
    {
		if (headStepMeta!=null && 
	        headStepMeta.getName()!=null &&
	        headStepMeta.getName().length()>0
		   ) 
			return headStepMeta.getName();
		return null;
    }
    
    /**
     * @param tailStepname The tailStepname to set.
     */
    public void setTailStepName(String tailStepname)
    {
        this.tailStepName = tailStepname;
    }
    
    /**
     * @param headStepname The headStepname to set.
     */
    public void setHeadStepName(String headStepname)
    {
        this.headStepName = headStepname;
    }
    
    /**
     * @return Returns the tailStep.
     */
    public StepMeta getTailStepMeta()
    {
        return tailStepMeta;
    }
    
    /**
     * @return Returns the headStep.
     */
    public StepMeta getHeadStepMeta()
    {
        return headStepMeta;
    }
    
    /**
     * @param tailStep The tailStep to set.
     */
    public void setTailStepMeta(StepMeta tailStep)
    {
        this.tailStepMeta = tailStep;
    }
	
    /**
     * @param headStep The headStep to set.
     */
    public void setHeadStepMeta(StepMeta headStep)
    {
        this.headStepMeta = headStep;
    }
	
	public Object clone()
	{
		AppendMeta retval = (AppendMeta)super.clone();

        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append(XMLHandler.addTagValue("head_name", getHeadStepName()));	//$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("tail_name", getTailStepName()));	//$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
			headStepName = XMLHandler.getTagValue(stepnode, "head_name");  //$NON-NLS-1$
			tailStepName = XMLHandler.getTagValue(stepnode, "tail_name");  //$NON-NLS-1$			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("MergeRowsMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
	}
    
    public String[] getInfoSteps()
    {
        return new String[] { headStepName, tailStepName }; 
    }

    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
        if (infoSteps!=null && infoSteps.length==2)
        {
            headStepMeta = infoSteps[0];
            tailStepMeta = infoSteps[1];
        }
    }

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			headStepName =  rep.getStepAttributeString (id_step, "head_name");  //$NON-NLS-1$
			tailStepName =  rep.getStepAttributeString (id_step, "tail_name");  //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeRowsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "head_name", getHeadStepName()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "tail_name", getTailStepName()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeRowsMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void searchInfoAndTargetSteps(ArrayList steps)
	{
		headStepMeta = StepMeta.findStep(steps, headStepName);
		tailStepMeta = StepMeta.findStep(steps, tailStepName);
	}

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just take the info fields.
        //
        r.addRow(info);
                
        return r;
    }

	public void check(ArrayList remarks, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		
		if (getHeadStepName()!=null && getTailStepName()!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MergeRowsMeta.CheckResult.SourceStepsOK"), stepMeta);
			remarks.add(cr);
		}
		else if (getHeadStepName()==null && getTailStepName()==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MergeRowsMeta.CheckResult.SourceStepsMissing"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MergeRowsMeta.CheckResult.OneSourceStepMissing"), stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new AppendDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new Append(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AppendData();
	}
}