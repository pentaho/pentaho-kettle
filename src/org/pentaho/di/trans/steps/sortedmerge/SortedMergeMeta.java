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
 
package org.pentaho.di.trans.steps.sortedmerge;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/*
 * Created on 02-jun-2003
 *
 */

public class SortedMergeMeta extends BaseStepMeta implements StepMetaInterface
{
    /** order by which fields? */
    private String  fieldName[];
    /** false : descending, true=ascending */
    private boolean ascending[];

	public SortedMergeMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<? extends SharedObjectInterface> databases, Hashtable counters) throws KettleXMLException
	{
		readData(stepnode);
	}

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields]; // order by
        ascending = new boolean[nrfields];
    }
    
    public void setDefault()
    {       
        int nrfields = 0;
        
        allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            fieldName[i]="field"+i;
        }
    }
     
    public Object clone()
    {
        SortedMergeMeta retval = (SortedMergeMeta)super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.fieldName   [i] = fieldName[i];
            retval.ascending[i] = ascending[i]; 
        }
        
        return retval;
    }
	
    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields = XMLHandler.countNodes(fields, "field");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

                fieldName[i] = XMLHandler.getTagValue(fnode, "name");
                String asc = XMLHandler.getTagValue(fnode, "ascending");
                if (asc.equalsIgnoreCase("Y"))
                    ascending[i] = true;
                else
                    ascending[i] = false;
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

        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<fieldName.length;i++)
        {
            retval.append("      <field>"+Const.CR);
            retval.append("        "+XMLHandler.addTagValue("name",      fieldName[i]));
            retval.append("        "+XMLHandler.addTagValue("ascending", ascending[i]));
            retval.append("        </field>"+Const.CR);
        }
        retval.append("      </fields>"+Const.CR);

        return retval.toString();   
    }

    public void readRep(Repository rep, long id_step, List<? extends SharedObjectInterface> databases, Hashtable counters) throws KettleException
    {
        try
        {
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
                ascending[i] = rep.getStepAttributeBoolean(id_step, i, "field_ascending");
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
            for (int i = 0; i < fieldName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_ascending", ascending[i]);
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info) throws KettleStepException
    {
        // Set the sorted properties: ascending/descending
        for (int i=0;i<fieldName.length;i++)
        {
            int idx = inputRowMeta.indexOfValue(fieldName[i]);
            if (idx>=0)
            {
                ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(idx);
                valueMeta.setSortedDescending(!ascending[i]);
                
                // TODO: add case insensivity
            }
        }
        
    }
	
    public void check(List<CheckResult> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SortedMergeMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i< fieldName.length;i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx<0)
                {
                    error_message+="\t\t"+fieldName[i]+Const.CR;
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=Messages.getString("SortedMergeMeta.CheckResult.SortKeysNotFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length>0)
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SortedMergeMeta.CheckResult.AllSortKeysFound"), stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SortedMergeMeta.CheckResult.NoSortKeysEntered"), stepMeta);
                    remarks.add(cr);
                }
            }
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SortedMergeMeta.CheckResult.NoFields"), stepMeta);
            remarks.add(cr);
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SortedMergeMeta.CheckResult.ExpectedInputOk"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SortedMergeMeta.CheckResult.ExpectedInputError"), stepMeta);
            remarks.add(cr);
        }
    }

	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new SortedMergeDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new SortedMerge(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SortedMergeData();
	}

    /**
     * @return the ascending
     */
    public boolean[] getAscending()
    {
        return ascending;
    }

    /**
     * @param ascending the ascending to set
     */
    public void setAscending(boolean[] ascending)
    {
        this.ascending = ascending;
    }

    /**
     * @return the fieldName
     */
    public String[] getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }


}
