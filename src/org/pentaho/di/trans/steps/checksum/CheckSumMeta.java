/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan 
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.checksum;

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
 * Created on 30-06-2008
 *
 */


public class CheckSumMeta extends BaseStepMeta implements StepMetaInterface
{
    /** by which fields to display? */
    private String  fieldName[];
    
    private String resultfieldName;
	
    public static String checksumtypeCodes[] = {"CRC32","ADLER32","MD5","SHA-1"};
    
    private String checksumtype;
    
	public CheckSumMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void setCheckSumType(int i)
	{
		checksumtype=checksumtypeCodes[i];
	}

	public int getTypeByDesc()
	{
		if(checksumtype==null) return 0;
		int retval;
		if(checksumtype.equals(checksumtypeCodes[1]))
			retval=1;
		else if(checksumtype.equals(checksumtypeCodes[2]))
			retval=2;
		else if(checksumtype.equals(checksumtypeCodes[3]))
			retval=3;
		else
			retval=0;
		return retval;
	}
	
	public String getCheckSumType()
	{
		return checksumtype;
	}
	
    /**
     * @return Returns the resultfieldName.
     */
    public String getResultFieldName()
    {
        return resultfieldName;
    }

    /**
     * @param resultName The resultfieldName to set.
     */
    public void setResultFieldName(String resultfieldName)
    {
        this.resultfieldName = resultfieldName;
    }
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{	
        CheckSumMeta retval = (CheckSumMeta) super.clone();

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

	private void readData(Node stepnode)  throws KettleXMLException
	{
	  try
	    {
		  checksumtype = XMLHandler.getTagValue(stepnode, "checksumtype");
		  resultfieldName = XMLHandler.getTagValue(stepnode, "resultfieldName");
		  
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
        retval.append("      " + XMLHandler.addTagValue("checksumtype", checksumtype));
        retval.append("      " + XMLHandler.addTagValue("resultfieldName", resultfieldName));
        
        
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
		resultfieldName=null;
		checksumtype=checksumtypeCodes[0];
        int nrfields = 0;

        allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i;
        }
	}

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	        try
	        {
	        	checksumtype = rep.getStepAttributeString(id_step, "checksumtype");
	        	
	        	resultfieldName = rep.getStepAttributeString(id_step, "resultfieldName");
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
	
    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
	        	rep.saveStepAttribute(id_transformation, id_step, "checksumtype", checksumtype);
	        	
	        	rep.saveStepAttribute(id_transformation, id_step, "resultfieldName", resultfieldName);
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
		public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
		{    	
	        // Output field (String)
			 if (!Const.isEmpty(resultfieldName))
		     {
				 ValueMetaInterface v=null;
				 if(checksumtype.equals("CRC32") || checksumtype.equals("ADLER32"))
					 v = new ValueMeta(space.environmentSubstitute(resultfieldName), ValueMeta.TYPE_INTEGER);
				 else
					 v = new ValueMeta(space.environmentSubstitute(resultfieldName), ValueMeta.TYPE_STRING);
				 v.setOrigin(name);
				 inputRowMeta.addValueMeta(v);
				 
		     }
	    }
	
	   public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	   {
		CheckResult cr;
		String error_message="";
		
		if (Const.isEmpty(resultfieldName))
        {
            error_message = Messages.getString("CheckSumMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
        }
        else
        {
            error_message = Messages.getString("CheckSumMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
        }
		remarks.add(cr);
		
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("CheckSumMeta.CheckResult.NotReceivingFields"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CheckSumMeta.CheckResult.StepRecevingData",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
	        boolean error_found = false;
	        error_message="";
	        
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
                error_message = Messages.getString("CheckSumMeta.CheckResult.FieldsFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CheckSumMeta.CheckResult.AllFieldsFound"), stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("CheckSumMeta.CheckResult.NoFieldsEntered"), stepMeta);
                    remarks.add(cr);
                }
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CheckSumMeta.CheckResult.StepRecevingData2"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("CheckSumMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CheckSum(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new CheckSumData();
	}

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
