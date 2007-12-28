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

package org.pentaho.di.trans.steps.xsdvalidator;


import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
 * Created on 14-08-2007
 *
 */

public class XsdValidatorMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  xdsfilename;
	private String  xmlstream;
	private String  resultfieldname;
	private boolean addvalidationmsg;
	private String validationmsgfield;
	private boolean outputstringfield;
	private String ifxmlvalid;
	private String ifxmlunvalid;
	private boolean xmlsourcefile;
	private String xsddefinedfield;
	
	private String xsdsource;
	
	public String SPECIFY_FILENAME="filename";
	public String SPECIFY_FIELDNAME="fieldname";
	public String NO_NEED="noneed";
	
	
	public void setXSDSource(String xsdsourcein)
	{
		this.xsdsource=xsdsourcein;
	}
	
	public String getXSDSource()
	{
		return xsdsource;
	}
	
	public void setXSDDefinedField(String xsddefinedfieldin)
	{
		this.xsddefinedfield=xsddefinedfieldin;
	}
	
	public String getXSDDefinedField()
	{
		return xsddefinedfield;
	}
	
	public boolean getXMLSourceFile()
	{
		return xmlsourcefile;
	}
	
	public void setXMLSourceFile(boolean xmlsourcefilein)
	{
		this.xmlsourcefile=xmlsourcefilein;
	}
	
	public String getifXMLValid()
	{
		return ifxmlvalid;
	}
	
	public String getifXMLUnValid()
	{
		return ifxmlunvalid;
	}
	
	public void setifXMLValid(String ifxmlvalidin)
	{
		this.ifxmlvalid=ifxmlvalidin;
	}
	
	
	public void setifXMLUnValid(String ifxmlunvalidin)
	{
		this.ifxmlunvalid=ifxmlunvalidin;
	}
	
	
	public boolean getOutputStringField()
	{
		return outputstringfield;
	}
	
	
	public void setOutputStringField(boolean outputstringfieldin)
	{
		this.outputstringfield=outputstringfieldin;
	}
	
	public String getValidationMsgField()
	{
		return validationmsgfield;
	}
	
	public void setValidationMsgField(String validationmsgfieldin)
	{
		this.validationmsgfield=validationmsgfieldin;
	}
	
	public boolean useAddValidationMsg()
	{
		return addvalidationmsg;
	}
	
	
	public void setAddValidationMsg(boolean addvalidationmsgin)
	{
		this.addvalidationmsg=addvalidationmsgin;
	}
	
	public XsdValidatorMeta()
	{
		super(); // allocate BaseStepMeta
	}
	

	    
    /**
     * @return Returns the XSD filename.
     */
    public String getXSDFilename()
    {
        return xdsfilename;
    }
    
    public String getResultfieldname()
    {
    	return resultfieldname;
    }
    

    public String getXMLStream()
    {
    	return xmlstream;
    }
    
    /**
     * @param script The XSD filename to set.
     */
    public void setXSDfilename(String xdsfilenamein)
    {
        this.xdsfilename = xdsfilenamein;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultfieldname = resultfield;
    }
    

    
    public void setXMLStream(String xmlstreamin)
    {
        this.xmlstream =  xmlstreamin;
    }
    

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
	readData(stepnode);
	}
	

	public Object clone()
	{
		XsdValidatorMeta retval = (XsdValidatorMeta)super.clone();
		

		return retval;
	}
	
   
    
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{

			
			xdsfilename     = XMLHandler.getTagValue(stepnode, "xdsfilename"); //$NON-NLS-1$
			xmlstream     = XMLHandler.getTagValue(stepnode, "xmlstream"); //$NON-NLS-1$
			resultfieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			xsddefinedfield     = XMLHandler.getTagValue(stepnode, "xsddefinedfield");
			xsdsource     = XMLHandler.getTagValue(stepnode, "xsdsource");
			
			
			addvalidationmsg = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addvalidationmsg"));
			
			validationmsgfield     = XMLHandler.getTagValue(stepnode, "validationmsgfield"); //$NON-NLS-1$
			ifxmlvalid     = XMLHandler.getTagValue(stepnode, "ifxmlvalid");
			ifxmlunvalid     = XMLHandler.getTagValue(stepnode, "ifxmlunvalid");
			outputstringfield = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "outputstringfield"));
			xmlsourcefile = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "xmlsourcefile"));
			
	
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("XsdValidatorMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		xdsfilename = ""; //$NON-NLS-1$
		xmlstream = "";
		resultfieldname="result";
		addvalidationmsg=false;
		validationmsgfield="ValidationMsgField";
		ifxmlvalid="";
		ifxmlunvalid="";
		outputstringfield=false;
		xmlsourcefile=false;
		xsddefinedfield="";
		xsdsource=SPECIFY_FILENAME;
		

	}
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
	   if (!Const.isEmpty(resultfieldname))
        {
		   if (outputstringfield)
	        {
	            // Output field (String)	
	            ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getResultfieldname()), ValueMeta.TYPE_STRING);
	            inputRowMeta.addValueMeta(v);
	        }
		   else
	        {
	         	
	            // Output field (boolean)	
	            ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getResultfieldname()), ValueMeta.TYPE_BOOLEAN);
	            inputRowMeta.addValueMeta(v);
	        }  

        }
	   // Add String Field that contain validation message (most the time, errors)
        if(addvalidationmsg && !Const.isEmpty(validationmsgfield))
        {
	          ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(validationmsgfield), ValueMeta.TYPE_STRING);
	          inputRowMeta.addValueMeta(v);
        }

    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("xdsfilename", xdsfilename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("xmlstream", xmlstream)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname",resultfieldname));
		retval.append("    "+XMLHandler.addTagValue("addvalidationmsg",addvalidationmsg));
		retval.append("    "+XMLHandler.addTagValue("validationmsgfield", validationmsgfield));
		retval.append("    "+XMLHandler.addTagValue("ifxmlunvalid", ifxmlunvalid));
		retval.append("    "+XMLHandler.addTagValue("ifxmlvalid", ifxmlvalid));
		
		retval.append("    "+XMLHandler.addTagValue("outputstringfield",outputstringfield));
		retval.append("    "+XMLHandler.addTagValue("xmlsourcefile",xmlsourcefile));
		retval.append("    "+XMLHandler.addTagValue("xsddefinedfield", xsddefinedfield));
		retval.append("    "+XMLHandler.addTagValue("xsdsource", xsdsource));
		
		return retval.toString();
	}


	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			xdsfilename     = rep.getStepAttributeString(id_step, "xdsfilename"); //$NON-NLS-1$
			xmlstream     = rep.getStepAttributeString(id_step, "xmlstream"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			
			xmlsourcefile    =      rep.getStepAttributeBoolean(id_step, "xmlsourcefile"); 
			addvalidationmsg    =      rep.getStepAttributeBoolean(id_step, "addvalidationmsg"); 
			validationmsgfield     = rep.getStepAttributeString(id_step, "validationmsgfield");
			ifxmlvalid     = rep.getStepAttributeString(id_step, "ifxmlvalid");
			ifxmlunvalid     = rep.getStepAttributeString(id_step, "ifxmlunvalid");
			
			outputstringfield    =      rep.getStepAttributeBoolean(id_step, "outputstringfield"); 
			xsddefinedfield     = rep.getStepAttributeString(id_step, "xsddefinedfield");
			xsdsource     = rep.getStepAttributeString(id_step, "xsdsource");
			
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsdValidatorMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "xdsfilename", xdsfilename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "xmlstream", xmlstream); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname",resultfieldname);
			rep.saveStepAttribute(id_transformation, id_step, "xmlsourcefile",  xmlsourcefile);
			rep.saveStepAttribute(id_transformation, id_step, "addvalidationmsg",  addvalidationmsg);
			rep.saveStepAttribute(id_transformation, id_step, "validationmsgfield", validationmsgfield); 
			rep.saveStepAttribute(id_transformation, id_step, "ifxmlvalid", ifxmlvalid); 
			rep.saveStepAttribute(id_transformation, id_step, "ifxmlunvalid", ifxmlunvalid); 
			rep.saveStepAttribute(id_transformation, id_step, "outputstringfield",  outputstringfield);
			rep.saveStepAttribute(id_transformation, id_step, "xsddefinedfield", xsddefinedfield);
			rep.saveStepAttribute(id_transformation, id_step, "xsdsource", xsdsource);
			
			
			
		
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XsdValidatorMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
			RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		// Check XML stream field
		if(Const.isEmpty(xmlstream))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.XMLStreamFieldEmpty"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}else{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsdValidatorMeta.CheckResult.XMLStreamFieldOK"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// Check result fieldname
		if(Const.isEmpty(resultfieldname))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.ResultFieldEmpty"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}else{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsdValidatorMeta.CheckResult.ResultFieldOK"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		if(xsdsource.equals(SPECIFY_FILENAME))
		{
			if(Const.isEmpty(xdsfilename))
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.XSDFieldEmpty"), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}else{
				// Check if file exists
				java.io.File fileXSD= new java.io.File(xdsfilename);
				if(!fileXSD.exists())
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.CanNotFindXSDFile",xdsfilename), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
				}else{
					if(!fileXSD.isFile())
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.NotFileXSDFile",xdsfilename), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
						remarks.add(cr);
					}
				}
			}
		}
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XsdValidatorMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
        else
        {	
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
		
		
			
		  // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("XsdValidatorMeta.CheckResult.ExpectedInputOk"), stepinfo);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("XsdValidatorMeta.CheckResult.ExpectedInputError"), stepinfo);
            remarks.add(cr);
        }
	}


	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new XsdValidator(stepMeta, stepDataInterface, cnr, transMeta, trans);
        
	}

	public StepDataInterface getStepData()
	{
		return new XsdValidatorData();
	}
    
 

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
