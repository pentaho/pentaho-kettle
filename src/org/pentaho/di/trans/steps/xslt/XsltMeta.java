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
import java.io.File;
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
	private String  xslFilename;
	private String  fieldName;
	private String  resultFieldname;
	private String  xslFileField;
	private boolean xslFileFieldUse;
	private String xslFactory;

	
	public XsltMeta()
	{
		super(); // allocate BaseStepMeta
	}
	

    
    /**
     * @return Returns the XSL filename.
     */
    public String getXslFilename()
    {
        return xslFilename;
    }
  
    public void setXSLFileField(String xslfilefieldin)
    {
    	xslFileField=xslfilefieldin;
    }
    public void setXSLFactory(String xslfactoryin)
    {
    	xslFactory=xslfactoryin;
    }
    /**
     * @return Returns the XSL factory type.
     */
    public String getXSLFactory()
    {
        return xslFactory;
    }
  
    public String getXSLFileField()
    {
        return xslFileField;
    }
    
    
    public String getResultfieldname()
    {
    	return resultFieldname;
    }
    
    
    public String getFieldname()
    {
    	return fieldName;
    }
    
    /**
     * @param xslFilename The Xsl filename to set.
     */
    public void setXslFilename(String xslFilename)
    {
        this.xslFilename = xslFilename;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultFieldname = resultfield;
    }
    

    
    public void setFieldname(String fieldnamein)
    {
        this.fieldName =  fieldnamein;
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
        return xslFileFieldUse;
    }
    
      
    public void setXSLFileFieldUse(boolean xslfilefieldusein)
    {
        this.xslFileFieldUse = xslfilefieldusein;
    }
    
  
    
   
    
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{		
			xslFilename     = XMLHandler.getTagValue(stepnode, "xslfilename"); //$NON-NLS-1$
			fieldName     = XMLHandler.getTagValue(stepnode, "fieldname"); //$NON-NLS-1$
			resultFieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			xslFileField     = XMLHandler.getTagValue(stepnode, "xslfilefield");
			xslFileFieldUse = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "xslfilefielduse"));
			xslFactory     = XMLHandler.getTagValue(stepnode, "xslfactory"); 
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("XsltMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		xslFilename = null; //$NON-NLS-1$
		fieldName = null;
		resultFieldname="result";
		xslFactory="JAXP";
		xslFileField=null;
		xslFileFieldUse=false;		
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
		
		retval.append("    "+XMLHandler.addTagValue("xslfilename", xslFilename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("fieldname", fieldName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultFieldname)); //$NON-NLS-1$ //$NON-NLS-2$	
		retval.append("    "+XMLHandler.addTagValue("xslfilefield", xslFileField));
		retval.append("    "+XMLHandler.addTagValue("xslfilefielduse",  xslFileFieldUse));
		retval.append("    "+XMLHandler.addTagValue("xslfactory", xslFactory)); 
		
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			xslFilename     = rep.getStepAttributeString(id_step, "xslfilename"); //$NON-NLS-1$
			fieldName     = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
			resultFieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1
			xslFileField     = rep.getStepAttributeString(id_step, "xslfilefield");
			xslFileFieldUse    =      rep.getStepAttributeBoolean(id_step, "xslfilefielduse"); 
			xslFactory     = rep.getStepAttributeString(id_step, "xslfactory");
			

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
			rep.saveStepAttribute(id_transformation, id_step, "xslfilename", xslFilename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultFieldname); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "xslfilefield", xslFileField);
			
			rep.saveStepAttribute(id_transformation, id_step, "xslfilefielduse",  xslFileFieldUse);
			rep.saveStepAttribute(id_transformation, id_step, "xslfactory", xslFactory);

		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsltMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsltMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
		 // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("XsltMeta.CheckResult.ExpectedInputOk"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ExpectedInputError"), stepMeta);
            remarks.add(cr);
        }
		
		//	Check if The result field is given
		if (getResultfieldname()==null)
		{
			 // Result Field is missing !
			  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ErrorResultFieldNameMissing"), stepMeta); //$NON-NLS-1$
	          remarks.add(cr);
		
		}
		
		// Check if XSL Filename field is provided
		if(xslFileFieldUse)
		{
			if (getXSLFileField()==null)
			{
				 // Result Field is missing !
				  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ErrorResultXSLFieldNameMissing"), stepMeta); //$NON-NLS-1$
		          remarks.add(cr);
			}
			else
			{
				 // Result Field is provided !
				  cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsltMeta.CheckResult.ErrorResultXSLFieldNameOK"), stepMeta); //$NON-NLS-1$
		          remarks.add(cr);
			}
		}else{
			if(xslFilename==null)
			{
				 // Result Field is missing !
				  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ErrorXSLFileNameMissing"), stepMeta); //$NON-NLS-1$
		          remarks.add(cr);

			}else{
				// Check if it's exist and it's a file
				String RealFilename=transMeta.environmentSubstitute(xslFilename);
				File f=new File(RealFilename);
				
				if (f.exists())
	            {
	                if (f.isFile())
	                {
	                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("XsltMeta.CheckResult.FileExists", RealFilename),stepMeta);
	                    remarks.add(cr);
	                }
	                else
	                {
	                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ExistsButNoFile",	RealFilename), stepMeta);
	                    remarks.add(cr);
	                }
	            }
	            else
	            {
	                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.FileNotExists", RealFilename),
	                        stepMeta);
	                remarks.add(cr);
	            }
			}
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
