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

package be.ibridge.kettle.trans.step.mappinginput;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
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
 * Created on 02-jun-2003
 * 
 */

public class MappingInputMeta extends BaseStepMeta implements StepMetaInterface
{
    private String fieldName[];

    private int    fieldType[];

    private int    fieldLength[];

    private int    fieldPrecision[];

    public MappingInputMeta()
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
    
    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        MappingInputMeta retval = (MappingInputMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.fieldType[i] = fieldType[i];
            fieldLength[i] = fieldLength[i];
            fieldPrecision[i] = fieldPrecision[i];
        }

        return retval;
    }

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields];
        fieldType = new int[nrfields];
        fieldLength = new int[nrfields];
        fieldPrecision = new int[nrfields];
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
                fieldType[i] = Value.getType(XMLHandler.getTagValue(fnode, "type")); //$NON-NLS-1$
                String slength = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
                String sprecision = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$

                fieldLength[i] = Const.toInt(slength, -1);
                fieldPrecision[i] = Const.toInt(sprecision, -1);
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("MappingInputMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
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
                retval.append("        " + XMLHandler.addTagValue("type", Value.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("length", fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        " + XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
            fieldType[i] = Value.VALUE_TYPE_STRING;
            fieldLength[i] = 30;
            fieldPrecision[i] = -1;
        }
    }
    
    public Row getFields(Row row, String name, Row info) throws KettleStepException
    {
        for (int i=0;i<fieldName.length;i++)
        {
            if (fieldName[i]!=null && fieldName[i].length()!=0)
            {
                Value v=new Value(fieldName[i], fieldType[i]);
                if (v.getType()==Value.VALUE_TYPE_NONE) v.setType(Value.VALUE_TYPE_STRING);
                v.setLength(fieldLength[i], fieldPrecision[i]);
                v.setOrigin(name);
                row.addValue(v);
            }
        }
        return row;
    }
    

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
                fieldType[i] = Value.getType( rep.getStepAttributeString(id_step, i, "field_type") ); //$NON-NLS-1$
                fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingInputMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
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
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_type", Value.getTypeDesc(fieldType[i])); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
                }
            }
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("MappingInputMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;
        if (prev == null || prev.size() == 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MappingInputMeta.CheckResult.NotReceivingFieldsError"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingInputMeta.CheckResult.StepReceivingDatasFromPreviousOne", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MappingInputMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK , Messages.getString("MappingInputMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new MappingInputDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
    {
        return new MappingInput(stepMeta, stepDataInterface, cnr, tr, trans);
    }

    public StepDataInterface getStepData()
    {
        return new MappingInputData();
    }

}
