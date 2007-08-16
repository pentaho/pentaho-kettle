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

package be.ibridge.kettle.trans.step.regexeval;

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
 * Created on 2-07-2007
 *
 */

public class RegexEvalMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  script;
	private String  matcher;
	private String  resultfieldname;
	private boolean usevar;
	private boolean canoneq;
	private boolean caseinsensitive;
	private boolean comment;
	private boolean dotall;
	private boolean multiline;
	private boolean unicode;
	private boolean unix;

	

	
	public RegexEvalMeta()
	{
		super(); // allocate BaseStepMeta
	}
	

    
    /**
     * @return Returns the script.
     */
    public String getScript()
    {
        return script;
    }
    
    public String getResultfieldname()
    {
    	return resultfieldname;
    }
    
    
    /**
     * @return Returns the realscript.
     */
    public String getRealResultfieldname()
    {
        return StringUtil.environmentSubstitute(getResultfieldname());
    }
    
    
    /**
     * @return Returns the realscript.
     */
    public String getRealScript()
    {
        return StringUtil.environmentSubstitute(getScript());
    }
   
    
    public String getMatcher()
    {
    	return matcher;
    }
    
    /**
     * @param script The script to set.
     */
    public void setScript(String script)
    {
        this.script = script;
    }
    
    
    public void setResultfieldname(String resultfield)
    {
        this.resultfieldname = resultfield;
    }
    

    
    public void setMatcher(String matcher)
    {
        this.matcher =  matcher;
    }
    

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	

	public Object clone()
	{
		RegexEvalMeta retval = (RegexEvalMeta)super.clone();
		

		return retval;
	}
	 /**
     * @return Returns the use var flag.
     */
    public boolean useVar()
    {
        return usevar;
    }
    
    /**
     * @return Returns the canon eq flag.
     */
    public boolean canoeq()
    {
        return canoneq;
    }
    
    public boolean caseinsensitive()
    {
        return caseinsensitive;
    }
    public boolean comment()
    {
        return comment;
    }
    public boolean dotall()
    {
        return dotall;
    }
    public boolean multiline()
    {
        return multiline;
    }
    
    public boolean unicode()
    {
        return unicode;
    }
    public boolean unix()
    {
        return unix;
    }
    
  
  
    
    /**
     * @param useVar The useVar flag to set.
     */
    public void setuseVar(boolean usevar)
    {
        this.usevar = usevar;
    }
  
    public void setcanoneq(boolean canoneq)
    {
        this.canoneq = canoneq;
    }
    public void setcaseinsensitive(boolean caseinsensitive)
    {
        this.caseinsensitive = caseinsensitive;
    }
    public void setcomment(boolean comment)
    {
        this.comment = comment;
    }
    public void setdotall(boolean dotall)
    {
        this.dotall = dotall;
    }
    public void setmultiline(boolean multiline)
    {
        this.multiline = multiline;
    }
    public void setunicode(boolean unicode)
    {
        this.unicode = unicode;
    }
    public void setunix(boolean unix)
    {
        this.unix = unix;
    }
  

    
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{

			
			script     = XMLHandler.getTagValue(stepnode, "script"); //$NON-NLS-1$
			matcher     = XMLHandler.getTagValue(stepnode, "matcher"); //$NON-NLS-1$
			resultfieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			usevar = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usevar"));
			canoneq = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "canoneq"));
			caseinsensitive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "caseinsensitive"));
			comment = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "comment"));
			dotall = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "dotall"));
			multiline = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "multiline"));
			unicode = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "unicode"));
			unix = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "unix"));
				
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("RegexEvalMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		script = ""; //$NON-NLS-1$
		matcher = "";
		resultfieldname="result";
		usevar = false;
		canoneq = false;
		caseinsensitive = false;
		comment = false;
		dotall = false;
		multiline = false;
		unicode = false;
		unix = false;

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
            Value v = new Value(getRealResultfieldname(), Value.VALUE_TYPE_BOOLEAN);
            v.setOrigin(name);
            row.addValue(v);
        }

        return row;
    }

	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("script", script)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("matcher", matcher)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("usevar",  usevar));
		retval.append("    "+XMLHandler.addTagValue("canoneq", canoneq));
		retval.append("    "+XMLHandler.addTagValue("caseinsensitive",  caseinsensitive));
		retval.append("    "+XMLHandler.addTagValue("comment", comment));
		retval.append("    "+XMLHandler.addTagValue("dotall", dotall));
		retval.append("    "+XMLHandler.addTagValue("multiline",  multiline));
		retval.append("    "+XMLHandler.addTagValue("unicode",unicode));
		retval.append("    "+XMLHandler.addTagValue("unix",unix));

	
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			script     = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
			matcher     = rep.getStepAttributeString(id_step, "matcher"); //$NON-NLS-1$
			resultfieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			usevar    =      rep.getStepAttributeBoolean(id_step, "usevar"); 
			canoneq    =      rep.getStepAttributeBoolean(id_step, "canoneq"); 
			caseinsensitive    =      rep.getStepAttributeBoolean(id_step, "caseinsensitive"); 
			comment    =      rep.getStepAttributeBoolean(id_step, "comment"); 
			multiline    =      rep.getStepAttributeBoolean(id_step, "multiline"); 
			dotall    =      rep.getStepAttributeBoolean(id_step, "dotall");
			unicode    =      rep.getStepAttributeBoolean(id_step, "unicode"); 
			unix    =      rep.getStepAttributeBoolean(id_step, "unix"); 
	
	
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("RegexEvalMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "script", script); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "matcher", matcher); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "usevar",  usevar);
			rep.saveStepAttribute(id_transformation, id_step, "canoneq",  canoneq);
			rep.saveStepAttribute(id_transformation, id_step, "caseinsensitive",  caseinsensitive);
			rep.saveStepAttribute(id_transformation, id_step, "comment",  comment);
			rep.saveStepAttribute(id_transformation, id_step, "dotall",  dotall);
			rep.saveStepAttribute(id_transformation, id_step, "multiline",  multiline);
			rep.saveStepAttribute(id_transformation, id_step, "unicode",  unicode);
			rep.saveStepAttribute(id_transformation, id_step, "unix",  unix);
			
		
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("RegexEvalMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	//private boolean test(boolean getvars, boolean popup)
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{

		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepinfo); 
			remarks.add(cr);
		}
        else
        {
        	
        	
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoInputReceived"), stepinfo);
            remarks.add(cr);
        }
		
		// Check Field to evaluate
		 if (!Const.isEmpty(getMatcher()))
		 {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.MatcherOK"), stepinfo);
			remarks.add(cr);
		 }
		 else
		 {
			 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoMatcher"), stepinfo);
	         remarks.add(cr); 
		 
		 }
		
		// Check Result Field name
		 if (!Const.isEmpty(getResultfieldname()))
		 {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.ResultFieldnameOK"), stepinfo);
			remarks.add(cr);
		 }
		 else
		 {
			 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoResultFieldname"), stepinfo);
	         remarks.add(cr); 
		 
		 }
			
	}

	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new RegexEvalDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new RegexEval(stepMeta, stepDataInterface, cnr, transMeta, trans);
        
	}

	public StepDataInterface getStepData()
	{
		return new RegexEvalData();
	}
    
 

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
