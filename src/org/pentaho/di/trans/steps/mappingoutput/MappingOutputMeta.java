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

package org.pentaho.di.trans.steps.mappingoutput;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;




/*
 * Created on 02-jun-2003
 * 
 */

public class MappingOutputMeta extends BaseStepMeta implements StepMetaInterface
{
    private String  fieldName[];

    private int     fieldType[];

    private int     fieldLength[];

    private int     fieldPrecision[];
    
    private boolean fieldAdded[];
    
    private volatile String[] oldName;
    private volatile String[] newName;

    public MappingOutputMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the fieldLength.
     */
    public int[] getFieldLength()
    {
        return fieldLength;
    }

    /**
     * @param fieldLength The fieldLength to set.
     */
    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
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

    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }

    /**
     * @param fieldPrecision The fieldPrecision to set.
     */
    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }

    /**
     * @return Returns the fieldType.
     */
    public int[] getFieldType()
    {
        return fieldType;
    }

    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    public boolean[] getFieldAdded()
    {
        return fieldAdded;
    }
    
    public void setFieldAdded(boolean[] fieldAdded)
    {
        this.fieldAdded = fieldAdded;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        MappingOutputMeta retval = (MappingOutputMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.fieldType[i] = fieldType[i];
            retval.fieldLength[i] = fieldLength[i];
            retval.fieldPrecision[i] = fieldPrecision[i];
            retval.fieldAdded[i] = fieldAdded[i];
        }

        return retval;
    }

    public void allocate(int nrfields)
    {
        fieldName      = new String[nrfields];
        fieldType      = new int[nrfields];
        fieldLength    = new int[nrfields];
        fieldPrecision = new int[nrfields];
        fieldAdded     = new boolean[nrfields];
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

                fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
                String slength = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
                String sprecision = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$

                fieldLength[i] = Const.toInt(slength, -1);
                fieldPrecision[i] = Const.toInt(sprecision, -1);

                fieldAdded[i] = "Y".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "added") ); //$NON-NLS-1$ //$NON-NLS-2$
}
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("MappingOutputMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    <fields>" + Const.CR); //$NON-NLS-1$
        for (int i = 0; i < fieldName.length; i++)
        {
            if (fieldName[i] != null && fieldName[i].length() != 0)
            {
                retval.append("      <field>" + Const.CR); //$NON-NLS-1$
                retval.append("        " + XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("length", fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("added", fieldAdded[i]?"Y":"N")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                retval.append("        </field>" + Const.CR); //$NON-NLS-1$
            }
        }
        retval.append("      </fields>" + Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    public void setDefault()
    {
        int i, nrfields = 0;

        allocate(nrfields);

        for (i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i; //$NON-NLS-1$
            fieldType[i] = ValueMeta.TYPE_STRING;
            fieldLength[i] = 30;
            fieldPrecision[i] = -1;
            fieldAdded[i] = true;
        }
    }

    public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
    	// TODO it's best that this method doesn't change anything by itself.
    	// Eventually it's the Mapping step that's going to tell this step how to behave metadata wise.
    	// So what we'll have is the mapping step tell the mapping output step what fields to rename.
    	// 
    	if (oldName!=null && newName!=null) {
    		for (int i=0;i<oldName.length;i++) {
    			int index = r.indexOfValue(oldName[i]);
    			if (index>=0) {
    				r.getValueMeta(index).setName(newName[i]);
    			}
    		}
    	}
    	
    	// For now, that's all there is really...
    	
    	/*
        //
        // Always overwrite and ignore input values
        // Catch mismatches in check.
        // It's probably better like this instead of recursively going down: this takes a long time!
        // 
        for (int i = 0; i < fieldName.length; i++)
        {
            if (fieldName[i] != null && fieldName[i].length() != 0)
            {
                ValueMetaInterface v = new ValueMeta(fieldName[i], fieldType[i]);
                v.setLength(fieldLength[i]);
                v.setPrecision(fieldPrecision[i]);
                v.setOrigin(name);
                
                if (fieldAdded[i])
                {
                    // Before adding, see it it's not already there...
                    //
                    int idx = r.indexOfValue(fieldName[i]);
                    if (idx>=0)
                    {
                        // Replace this version
                        r.setValueMeta(idx, v);
                    }
                    else
                    {
                        // Just add it!
                        r.addValueMeta(v);
                    }
                }
                else
                {
                    int idx = r.indexOfValue(fieldName[i]);
                    if (idx>=0)
                    {
                        r.removeValueMeta(idx); // Remove the value from the stream.
                    }
                }
            }
        }
        */
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "field_type") ); //$NON-NLS-1$
                fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
                fieldAdded[i] = rep.getStepAttributeBoolean(id_step, i, "field_added"); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingOutputMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] != null && fieldName[i].length() != 0)
                {
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_type", ValueMeta.getTypeDesc(fieldType[i])); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_added", fieldAdded[i]); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingOutputMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResult> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        if (prev == null || prev.size() == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MappingOutputMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingOutputMeta.CheckResult.StepReceivingDatasOK",prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingOutputMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingOutputMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new MappingOutputDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
    {
        return new MappingOutput(stepMeta, stepDataInterface, cnr, tr, trans);
    }

    public StepDataInterface getStepData()
    {
        return new MappingOutputData();
    }
    
    public RowMetaInterface getRequiredFields() throws KettleException {
    	RowMetaInterface row = new RowMeta();
		for (int i = 0; i < fieldName.length; i++) 
        {
            ValueMetaInterface valueMeta = new ValueMeta(fieldName[i], fieldType[i]);
            valueMeta.setLength(fieldLength[i]);
            valueMeta.setPrecision(fieldPrecision[i]);
			row.addValueMeta(valueMeta);
		}
		return row;
    }

	/**
	 * @return the newName
	 */
	public String[] getNewName() {
		return newName;
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String[] newName) {
		this.newName = newName;
	}

	/**
	 * @return the oldName
	 */
	public String[] getOldName() {
		return oldName;
	}

	/**
	 * @param oldName the oldName to set
	 */
	public void setOldName(String[] oldName) {
		this.oldName = oldName;
	}
}
