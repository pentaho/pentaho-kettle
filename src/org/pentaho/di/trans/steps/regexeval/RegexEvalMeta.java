 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.regexeval;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
/*
 * Created on 15-08-2007
 *
 */

public class RegexEvalMeta extends BaseStepMeta implements StepMetaInterface
{
	private String  script;
	private String  matcher;
	private String  resultFieldname;
	private boolean usevar;
	private boolean canoneq;
	private boolean caseInsensitive;
	private boolean comment;
	private boolean dotAll;
	private boolean multiLine;
	private boolean unicode;
	private boolean unix;

	

	
	public RegexEvalMeta()
	{
		super();
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
    	return resultFieldname;
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
    
    
    public void setResultFieldname(String resultfield)
    {
        this.resultFieldname = resultfield;
    }
    

    
    public void setMatcher(String matcher)
    {
        this.matcher =  matcher;
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
    
    public boolean caseInsensitive()
    {
        return caseInsensitive;
    }
    public boolean comment()
    {
        return comment;
    }
    public boolean dotAll()
    {
        return dotAll;
    }
    public boolean multiLine()
    {
        return multiLine;
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
    public void setUseVar(boolean useVar)
    {
        this.usevar = useVar;
    }
  
    public void setCanoneq(boolean canoneq)
    {
        this.canoneq = canoneq;
    }
    public void setCaseinsensitive(boolean caseinsensitive)
    {
        this.caseInsensitive = caseinsensitive;
    }
    public void setComment(boolean comment)
    {
        this.comment = comment;
    }
    public void setDotAll(boolean dotall)
    {
        this.dotAll = dotall;
    }
    public void setMultiLine(boolean multiline)
    {
        this.multiLine = multiline;
    }
    public void setUnicode(boolean unicode)
    {
        this.unicode = unicode;
    }
    public void setUnix(boolean unix)
    {
        this.unix = unix;
    }
  

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException    
	{
		try
		{

			
			script     = XMLHandler.getTagValue(stepnode, "script"); //$NON-NLS-1$
			matcher     = XMLHandler.getTagValue(stepnode, "matcher"); //$NON-NLS-1$
			resultFieldname     = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
			usevar = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usevar"));
			canoneq = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "canoneq"));
			caseInsensitive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "caseinsensitive"));
			comment = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "comment"));
			dotAll = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "dotall"));
			multiLine = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "multiline"));
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
		resultFieldname="result";
		usevar = false;
		canoneq = false;
		caseInsensitive = false;
		comment = false;
		dotAll = false;
		multiLine = false;
		unicode = false;
		unix = false;

	}
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
        if (!Const.isEmpty(getResultfieldname()))
        {
            ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(getResultfieldname()), ValueMeta.TYPE_BOOLEAN);
            inputRowMeta.addValueMeta(v);
        }
    }
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)	throws KettleXMLException
	{
		readData(stepnode, databases);
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("script", script)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("matcher", matcher)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("resultfieldname", resultFieldname)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("usevar",  usevar));
		retval.append("    "+XMLHandler.addTagValue("canoneq", canoneq));
		retval.append("    "+XMLHandler.addTagValue("caseinsensitive",  caseInsensitive));
		retval.append("    "+XMLHandler.addTagValue("comment", comment));
		retval.append("    "+XMLHandler.addTagValue("dotall", dotAll));
		retval.append("    "+XMLHandler.addTagValue("multiline",  multiLine));
		retval.append("    "+XMLHandler.addTagValue("unicode",unicode));
		retval.append("    "+XMLHandler.addTagValue("unix",unix));

	
		return retval.toString();
	}
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	
	{
		try
		{
			script     = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
			matcher     = rep.getStepAttributeString(id_step, "matcher"); //$NON-NLS-1$
			resultFieldname     = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
			usevar    =      rep.getStepAttributeBoolean(id_step, "usevar"); 
			canoneq    =      rep.getStepAttributeBoolean(id_step, "canoneq"); 
			caseInsensitive    =      rep.getStepAttributeBoolean(id_step, "caseinsensitive"); 
			comment    =      rep.getStepAttributeBoolean(id_step, "comment"); 
			multiLine    =      rep.getStepAttributeBoolean(id_step, "multiline"); 
			dotAll    =      rep.getStepAttributeBoolean(id_step, "dotall");
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
			rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultFieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "usevar",  usevar);
			rep.saveStepAttribute(id_transformation, id_step, "canoneq",  canoneq);
			rep.saveStepAttribute(id_transformation, id_step, "caseinsensitive",  caseInsensitive);
			rep.saveStepAttribute(id_transformation, id_step, "comment",  comment);
			rep.saveStepAttribute(id_transformation, id_step, "dotall",  dotAll);
			rep.saveStepAttribute(id_transformation, id_step, "multiline",  multiLine);
			rep.saveStepAttribute(id_transformation, id_step, "unicode",  unicode);
			rep.saveStepAttribute(id_transformation, id_step, "unix",  unix);
			
		
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("RegexEvalMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
	
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.ConnectedStepOK",String.valueOf(prev.size())), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
        else
        {
        	
        	
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
		
		// Check Field to evaluate
		 if (!Const.isEmpty(getMatcher()))
		 {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.MatcherOK"), stepMeta);
			remarks.add(cr);
		 }
		 else
		 {
			 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoMatcher"), stepMeta);
	         remarks.add(cr); 
		 
		 }
		
		// Check Result Field name
		 if (!Const.isEmpty(getResultfieldname()))
		 {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("RegexEvalMeta.CheckResult.ResultFieldnameOK"), stepMeta);
			remarks.add(cr);
		 }
		 else
		 {
			 cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("RegexEvalMeta.CheckResult.NoResultFieldname"), stepMeta);
	         remarks.add(cr); 
		 
		 }
			
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
