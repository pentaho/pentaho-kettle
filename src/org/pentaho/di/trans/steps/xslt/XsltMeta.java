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

package org.pentaho.di.trans.steps.xslt;


import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/*
 * Created on 15-Oct-2007
 *
 */

public class XsltMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  xslfilename;
	private String  fieldname;
	private String  resultfieldname;
	private String  xslfilefield;
	private boolean xslfilefielduse;
	private String xslfactory;

	
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
    public void setXSLFactory(String xslfactoryin)
    {
    	xslfactory=xslfactoryin;
    }
    /**
     * @return Returns the XSL factory type.
     */
    public String getXSLFactory()
    {
        return xslfactory;
    }
  
    public String getXSLFileField()
    {
        return xslfilefield;
    }
    
    
    public String getResultfieldname()
    {
    	return resultfieldname;
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
    

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
			xslfactory     = XMLHandler.getTagValue(stepnode, "xslfactory"); 
			
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
		xslfactory="JAXP";
		xslfilefield=null;
		xslfilefielduse=false;		
	}
	
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{    	
        // Output field (String)	
        ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getResultfieldname()), ValueMeta.TYPE_STRING);
        inputRowMeta.addValueMeta(v);

    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("xslfilename", xslfilename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("fieldname", fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$	
		retval.append("    "+XMLHandler.addTagValue("xslfilefield", xslfilefield));
		retval.append("    "+XMLHandler.addTagValue("xslfilefielduse",  xslfilefielduse));
		retval.append("    "+XMLHandler.addTagValue("xslfactory", xslfactory)); 
		
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			xslfilename     = rep.getStepAttributeString(id_step, "xslfilename"); //$NON-NLS-1$
			fieldname     = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1
			xslfilefield     = rep.getStepAttributeString(id_step, "xslfilefield");
			xslfilefielduse    =      rep.getStepAttributeBoolean(id_step, "xslfilefielduse"); 
			xslfactory     = rep.getStepAttributeString(id_step, "xslfactory");
			

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
			rep.saveStepAttribute(id_transformation, id_step, "xslfactory", xslfactory);

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsltMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
			RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
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
