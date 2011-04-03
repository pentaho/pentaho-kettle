 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.writetolog;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/*
 * Created on 30-06-2008
 *
 */


public class WriteToLogMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = WriteToLogMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** by which fields to display? */
    private String  fieldName[];
	
    public static String logLevelCodes[] = 
	{			
		"log_level_nothing",
		"log_level_error",
		"log_level_minimal",
		"log_level_basic",
		"log_level_detailed",
		"log_level_debug",
		"log_level_rowlevel"
	};
    
    private boolean displayHeader;
    
    private String logmessage;
    
    private String loglevel;
    
	public WriteToLogMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void setLogLevel(int i)
	{
		loglevel=logLevelCodes[i];
	}

	public LogLevel getLogLevelByDesc()
	{
		if(loglevel==null) return LogLevel.BASIC;
		LogLevel retval;
		if(loglevel.equals(logLevelCodes[0]))
			retval=LogLevel.NOTHING;
		else if(loglevel.equals(logLevelCodes[1]))
			retval=LogLevel.ERROR;
		else if(loglevel.equals(logLevelCodes[2]))
			retval=LogLevel.MINIMAL;
		else if(loglevel.equals(logLevelCodes[3]))
			retval=LogLevel.BASIC;
		else if(loglevel.equals(logLevelCodes[4]))
			retval=LogLevel.DETAILED;
		else if(loglevel.equals(logLevelCodes[5]))
			retval=LogLevel.DEBUG;
		else
			retval=LogLevel.ROWLEVEL;
		return retval;
	}
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{	
        WriteToLogMeta retval = (WriteToLogMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
        }
		return retval;
	}
	   public void allocate(int nrfields)
	    {
	        fieldName = new String[nrfields]; 
	    }
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    public boolean isdisplayHeader()
    {
    	return displayHeader;
    }
    public void setdisplayHeader(boolean displayheader)
    {
    	this.displayHeader=displayheader;
    }
    
	public String getLogMessage()
	{
		if (logmessage == null)
		{
			logmessage="";
		}
		return logmessage;
	}
	public void setLogMessage(String s)
	{
		logmessage=s;
	}

    
	private void readData(Node stepnode)  throws KettleXMLException
	{
	  try
	    {
		  loglevel = XMLHandler.getTagValue(stepnode, "loglevel");
		  displayHeader = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "displayHeader"));
		  
		  logmessage = XMLHandler.getTagValue(stepnode, "logmessage");
		  
		  Node fields = XMLHandler.getSubNode(stepnode, "fields");
          int nrfields = XMLHandler.countNodes(fields, "field");

          allocate(nrfields);

          for (int i = 0; i < nrfields; i++)
          {
              Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
              fieldName[i] = XMLHandler.getTagValue(fnode, "name");
          }
	    }
      catch (Exception e)
      {
          throw new KettleXMLException("Unable to load step info from XML", e);
      }
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("      " + XMLHandler.addTagValue("loglevel", loglevel));
        retval.append("      " + XMLHandler.addTagValue("displayHeader", displayHeader));
        
        retval.append("      " + XMLHandler.addTagValue("logmessage", logmessage));
        
        retval.append("    <fields>" + Const.CR);
        for (int i = 0; i < fieldName.length; i++)
        {
            retval.append("      <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", fieldName[i]));
            retval.append("        </field>" + Const.CR);
        }
        retval.append("      </fields>" + Const.CR);

        return retval.toString();
    }
	public void setDefault()
	{
		loglevel=logLevelCodes[3];
		displayHeader=true;
		logmessage = "";
		
        int nrfields = 0;

        allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i;
        }
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
	throws KettleException
	{
	        try
	        {
	        	loglevel = rep.getStepAttributeString(id_step, "loglevel");
	        	displayHeader = rep.getStepAttributeBoolean(id_step, "displayHeader");
	        	
	        	logmessage = rep.getStepAttributeString(id_step, "logmessage");
	        	
	            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

	            allocate(nrfields);

	            for (int i = 0; i < nrfields; i++)
	            {
	                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unexpected error reading step information from the repository", e);
	        }
	    }
	
	    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	    {
	        try
	        {
	        	rep.saveStepAttribute(id_transformation, id_step, "loglevel", loglevel);
	        	rep.saveStepAttribute(id_transformation, id_step, "displayHeader", displayHeader);
	        	
	        	rep.saveStepAttribute(id_transformation, id_step, "logmessage", logmessage);
	        	
	            for (int i = 0; i < fieldName.length; i++)
	            {
	                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
	        }
	    }
	
	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String error_message = "";
	        boolean error_found = false;

            // Starting from selected fields in ...
            for (int i = 0; i < fieldName.length; i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx < 0)
                {
                    error_message += "\t\t" + fieldName[i] + Const.CR;
                    error_found = true;
                }
            }
            if (error_found)
            {
                error_message = BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.FieldsFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.AllFieldsFound"), stepinfo);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.NoFieldsEntered"), stepinfo);
                    remarks.add(cr);
                }
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "WriteToLogMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new WriteToLog(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new WriteToLogData();
	}


}
