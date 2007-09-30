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

package be.ibridge.kettle.trans.step.xslt;

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
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/*
 * Created on 2-jun-2003
 *
 */

public class XsltMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  xslfilename;
	private String  fieldname;
	private String  resultfieldname;
	private String  xslfilefield;
	private boolean xslfilefielduse;

	
	public XsltMeta()
	{
		super(); // allocate BaseStepMeta
	}
	

    
    /**
     * @return Returns the XSL filename.
     */
    public String getXslFilename()
    {
        return xslfilename;
    }
  
    public void setXSLFileField(String xslfilefieldin)
    {
    	xslfilefield=xslfilefieldin;
    }
    
    
    public String getXSLFileField()
    {
        return xslfilefield;
    }
    
    
    public String getResultfieldname()
    {
    	return resultfieldname;
    }
    
    
    /**
     * @return Returns the real XSL Filename.
     */
    public String getRealResultfieldname()
    {
        return StringUtil.environmentSubstitute(getResultfieldname());
    }
    
    
    /**
     * @return Returns the real XSL filename.
     */
    public String getRealXslFilename()
    {
        return StringUtil.environmentSubstitute(getXslFilename());
    }
   
    
    public String getFieldname()
    {
    	return fieldname;
    }
    
    /**
     * @param script The Xsl filename to set.
     */
    public void setXslFilename(String xslfilenamein)
    {
        this.xslfilename = xslfilenamein;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultfieldname = resultfield;
    }
    

    
    public void setFieldname(String fieldnamein)
    {
        this.fieldname =  fieldnamein;
    }
    

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	

	public Object clone()
	{
		XsltMeta retval = (XsltMeta)super.clone();
		

		return retval;
	}
	
    
    
    public boolean useXSLFileFieldUse()
    {
        return xslfilefielduse;
    }
    
      
    public void setXSLFileFieldUse(boolean xslfilefieldusein)
    {
        this.xslfilefielduse = xslfilefieldusein;
    }
    
  
    
   
    
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{		
			xslfilename     = XMLHandler.getTagValue(stepnode, "xslfilename"); //$NON-NLS-1$
			fieldname     = XMLHandler.getTagValue(stepnode, "fieldname"); //$NON-NLS-1$
			resultfieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			
			xslfilefield     = XMLHandler.getTagValue(stepnode, "xslfilefield");
			
			xslfilefielduse = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "xslfilefielduse"));	
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("XsltMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		xslfilename = null; //$NON-NLS-1$
		fieldname = null;
		resultfieldname="result";
		xslfilefield=null;
		xslfilefielduse=false;		
	}
	
    public Row getFields(Row r, String name, Row info)
    {
        Row row;
        if (r == null)
            row = new Row(); // give back values
        else
            row = r; // add to the existing row of values...

        if (!Const.isEmpty(getResultfieldname()))
        {
        	Value v = null;

        		 v = new Value(getRealResultfieldname(), Value.VALUE_TYPE_STRING);

            v.setOrigin(name);
            row.addValue(v);
            
           
            
        }

        return row;
    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("xslfilename", xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("fieldname", fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    "+XMLHandler.addTagValue("xslfilefield", xslfilefield));
		
		retval.append("    "+XMLHandler.addTagValue("xslfilefielduse",  xslfilefielduse));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			xslfilename     = rep.getStepAttributeString(id_step, "xslfilename"); //$NON-NLS-1$
			fieldname     = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			
			xslfilefield     = rep.getStepAttributeString(id_step, "xslfilefield");
			xslfilefielduse    =      rep.getStepAttributeBoolean(id_step, "xslfilefielduse"); 

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsltMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "xslfilename", xslfilename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "xslfilefield", xslfilefield);
			
			rep.saveStepAttribute(id_transformation, id_step, "xslfilefielduse",  xslfilefielduse);

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsltMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	//private boolean test(boolean getvars, boolean popup)
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{

		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsltMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
        else
        {
        	
        	
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
		
		
		//	Check if The result field is given
		if (getResultfieldname()==null)
		{
			 // Result Field is missing !
			  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ErrorResultFieldNameMissing"), stepinfo); //$NON-NLS-1$
	          remarks.add(cr);
		
		}
		
			
		

	     
			
	}

	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new XsltDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new Xslt(stepMeta, stepDataInterface, cnr, transMeta, trans);
        
	}

	public StepDataInterface getStepData()
	{
		return new XsltData();
	}
    
 

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
