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
	private String  xsdfilename;
	private String  fieldname;
	private String  resultfieldname;
	private boolean internxsd;
	private String resultfieldformat;
	private String xsdvalidetext;
	private String xsdnovalidetext;
	private boolean invalidmsg;
	private String invalidmsgfield;
	private String xsdfilefield;
	private boolean xsdfilefielduse;

	
	public XsltMeta()
	{
		super(); // allocate BaseStepMeta
	}
	

    
    /**
     * @return Returns the XSD filename.
     */
    public String getXsdFilename()
    {
        return xsdfilename;
    }
   
    public void setXsdValideText(String validetext)
    {
        xsdvalidetext=validetext;
    }
    
    public void setInvalidMsgField(String invalidmsgfieldin)
    {
    	invalidmsgfield=invalidmsgfieldin;
    }
    
    
       
    
    
    
    public void setXSDFileField(String xsdfilefieldin)
    {
    	xsdfilefield=xsdfilefieldin;
    }
    
    
    public String getXSDFileField()
    {
        return xsdfilefield;
    }
    
    
    public String getInvalidMsgField()
    {
        return invalidmsgfield;
    }
    
    public String getXsdValideText()
    {
        return xsdvalidetext;
    }
    
    public String getRealInvalidMsgField()
    {
        return StringUtil.environmentSubstitute(getInvalidMsgField());
    }
      
    
    
    
    public String getRealXsdValideText()
    {
        return StringUtil.environmentSubstitute(getXsdValideText());
    }
    
    
    
    public void setXsdNoValideText(String novalidetext)
    {
        xsdnovalidetext=novalidetext;
    }
    
    public String getXsdNoValideText()
    {
        return xsdnovalidetext;
    }
    
    public String getRealXsdNoValideText()
    {
        return StringUtil.environmentSubstitute(getXsdNoValideText());
    }
    
 
    
    public String getResultfieldname()
    {
    	return resultfieldname;
    }
    
    
    /**
     * @return Returns the real XSD Filename.
     */
    public String getRealResultfieldname()
    {
        return StringUtil.environmentSubstitute(getResultfieldname());
    }
    
    
    /**
     * @return Returns the realscript.
     */
    public String getRealXsdFilename()
    {
        return StringUtil.environmentSubstitute(getXsdFilename());
    }
   
    
    public String getFieldname()
    {
    	return fieldname;
    }
    
    /**
     * @param script The script to set.
     */
    public void setXsdFilename(String xsdfilenamein)
    {
        this.xsdfilename = xsdfilenamein;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultfieldname = resultfield;
    }
    
    public void setResultfieldFormat(String format)
    {
        this.resultfieldformat = format;
    }
    
    public String getResultfieldFormat()
    {
    	return resultfieldformat;
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
	 /**
     * @return Returns the use var flag.
     */
    public boolean useInternXSD()
    {
        return internxsd;
    }
    
   
    public boolean useInvalidMsg()
    {
        return invalidmsg;
    }
    
    
    public boolean useXSDFileFieldUse()
    {
        return xsdfilefielduse;
    }
    
      
    public void setXSDFileFieldUse(boolean xsdfilefieldusein)
    {
        this.xsdfilefielduse = xsdfilefieldusein;
    }
    
      
    public void setInvalidMsg(boolean invalidmsgin)
    {
        this.invalidmsg = invalidmsgin;
    }
    
    
   
    
    /**
     * @param useVar The useVar flag to set.
     */
    public void setUseInternXSD(boolean internxsdin)
    {
        this.internxsd = internxsdin;
    }
 
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{		
			xsdfilename     = XMLHandler.getTagValue(stepnode, "xsdfilename"); //$NON-NLS-1$
			fieldname     = XMLHandler.getTagValue(stepnode, "fieldname"); //$NON-NLS-1$
			resultfieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			internxsd = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "internxsd"));
			resultfieldformat     = XMLHandler.getTagValue(stepnode, "resultfieldformat"); //$NON-NLS-1$
			
			xsdnovalidetext     = XMLHandler.getTagValue(stepnode, "xsdnovalidetext"); //$NON-NLS-1$
			xsdvalidetext     = XMLHandler.getTagValue(stepnode, "xsdvalidetext"); //$NON-NLS-1$
			
			invalidmsg = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "invalidmsg"));
			invalidmsgfield     = XMLHandler.getTagValue(stepnode, "invalidmsgfield");
			xsdfilefield     = XMLHandler.getTagValue(stepnode, "xsdfilefield");
			
			xsdfilefielduse = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "xsdfilefielduse"));	
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("XsltMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		xsdfilename = null; //$NON-NLS-1$
		fieldname = null;
		resultfieldname="result";
		internxsd = false;
		resultfieldformat = "Boolean";
		xsdnovalidetext=null;
		xsdvalidetext=null;
		invalidmsg=false;
		invalidmsgfield=null;
		xsdfilefield=null;
		xsdfilefielduse=false;		
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
            
            // XSD Invalid message Field
            if (useInvalidMsg())
            {
            	 v = new Value(getRealInvalidMsgField(), Value.VALUE_TYPE_STRING);
            	 v.setOrigin(name);
            	 row.addValue(v);          	 
            	 
            }
            
        }

        return row;
    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("xsdfilename", xsdfilename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("fieldname", fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("internxsd",  internxsd));
		retval.append("    "+XMLHandler.addTagValue("resultfieldformat", resultfieldformat)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    "+XMLHandler.addTagValue("xsdnovalidetext", xsdnovalidetext)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("xsdvalidetext", xsdvalidetext)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    "+XMLHandler.addTagValue("invalidmsg",  invalidmsg)); 
		retval.append("    "+XMLHandler.addTagValue("invalidmsgfield", invalidmsgfield));
		
		retval.append("    "+XMLHandler.addTagValue("xsdfilefield", xsdfilefield));
		
		retval.append("    "+XMLHandler.addTagValue("xsdfilefielduse",  xsdfilefielduse));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			xsdfilename     = rep.getStepAttributeString(id_step, "xsdfilename"); //$NON-NLS-1$
			fieldname     = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			internxsd    =      rep.getStepAttributeBoolean(id_step, "internxsd"); 
			
			resultfieldformat     = rep.getStepAttributeString(id_step, "resultfieldformat"); //$NON-NLS-1$
			xsdvalidetext     = rep.getStepAttributeString(id_step, "xsdvalidetext"); //$NON-NLS-1$
			xsdnovalidetext     = rep.getStepAttributeString(id_step, "xsdnovalidetext"); //$NON-NLS-1$
			
			invalidmsg    =      rep.getStepAttributeBoolean(id_step, "invalidmsg"); 
			invalidmsgfield     = rep.getStepAttributeString(id_step, "invalidmsgfield");
			
			xsdfilefield     = rep.getStepAttributeString(id_step, "xsdfilefield");
			xsdfilefielduse    =      rep.getStepAttributeBoolean(id_step, "xsdfilefielduse"); 

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
			rep.saveStepAttribute(id_transformation, id_step, "xsdfilename", xsdfilename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "internxsd",  internxsd);
			
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldformat", resultfieldformat); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "xsdvalidetext", xsdvalidetext); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "xsdnovalidetext", xsdnovalidetext); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "invalidmsg",  invalidmsg);
			rep.saveStepAttribute(id_transformation, id_step, "invalidmsgfield", invalidmsgfield);
			
			rep.saveStepAttribute(id_transformation, id_step, "xsdfilefield", xsdfilefield);
			
			rep.saveStepAttribute(id_transformation, id_step, "xsdfilefielduse",  xsdfilefielduse);

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
			  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ErrorMatcherMissing"), stepinfo); //$NON-NLS-1$
	          remarks.add(cr);
		
		}
		
			
		
		// Check XSD Invalid message Field		
	      if (useInvalidMsg() && Const.isEmpty(getInvalidMsgField()))
          {
	    	  cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.NoXSDInvalidMessageField"), stepinfo); //$NON-NLS-1$
	          remarks.add(cr);
          }
	      
	      // Check Output Field format
			if (getResultfieldFormat().equals("String"))
				{
					if(getXsdValideText()==null)
					{
		
						 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ResultFiedFormatXSDValide"), stepinfo); //$NON-NLS-1$
				         remarks.add(cr);
						
					}
					
					if(getXsdNoValideText()==null)
					{
						
						 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XsltMeta.CheckResult.ResultFiedFormatXSDNoValide"), stepinfo); //$NON-NLS-1$
				         remarks.add(cr);
						
					}
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
