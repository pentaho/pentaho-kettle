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

package be.ibridge.kettle.trans.step.denormaliser;

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

/**
 * The Denormaliser transformation step meta-data
 * 
 * @since 17-jan-2006
 * @author Matt
 */

public class DenormaliserMeta extends BaseStepMeta implements StepMetaInterface
{
    /** Fields to group over */
    private String                    groupField[];

    /** The key field */
    private String                    keyField;

    /** The fields to unpivot */
    private DenormaliserTargetField[] denormaliserTargetField;

    public DenormaliserMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the keyField.
     */
    public String getKeyField()
    {
        return keyField;
    }

    /**
     * @param keyField The keyField to set.
     */
    public void setKeyField(String keyField)
    {
        this.keyField = keyField;
    }

    /**
     * @return Returns the groupField.
     */
    public String[] getGroupField()
    {
        return groupField;
    }

    /**
     * @param groupField The groupField to set.
     */
    public void setGroupField(String[] groupField)
    {
        this.groupField = groupField;
    }

    /**
     * @return Returns the pivotField.
     */
    public DenormaliserTargetField[] getDenormaliserTargetField()
    {
        return denormaliserTargetField;
    }

    /**
     * @param pivotField The pivotField to set.
     */
    public void setDenormaliserTargetField(DenormaliserTargetField[] pivotField)
    {
        this.denormaliserTargetField = pivotField;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public void allocate(int sizegroup, int nrfields)
    {
        groupField = new String[sizegroup];
        denormaliserTargetField = new DenormaliserTargetField[nrfields];
    }

    public Object clone()
    {
        Object retval = super.clone();
        return retval;
    }

    public void setDefault()
    {
        int sizegroup = 0;
        int nrfields = 0;

        allocate(sizegroup, nrfields);
    }

    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        Row row = r;

        // Remove the key value (there will be different entries for each output row)
        //
        if (keyField != null && keyField.length() > 0)
        {
            int idx = row.searchValueIndex(keyField);
            if (idx < 0) { throw new KettleStepException("Unable to locate [" + keyField + "] in the input fields"); }
            row.removeValue(idx);
        }
        else
        {
            throw new KettleStepException("The key field is not specified");
        }

        // Remove all field value(s) (there will be different entries for each output row)
        //
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            String fieldname = denormaliserTargetField[i].getFieldName();
            if (fieldname != null && fieldname.length() > 0)
            {
                int idx = row.searchValueIndex(fieldname);
                if (idx >= 0)
                {
                    row.removeValue(idx);
                }
            }
            else
            {
                throw new KettleStepException("The fieldname of target field #" + (i + 1) + " is not specified.");
            }
        }

        // Re-add the target fields
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            DenormaliserTargetField field = denormaliserTargetField[i];
            Value target = new Value(field.getTargetName(), field.getTargetType());
            target.setLength(field.getTargetLength(), field.getTargetPrecision());
            target.setOrigin(name);
            row.addValue(target);
        }

        return row;
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            keyField = XMLHandler.getTagValue(stepnode, "key_field");

            Node groupn = XMLHandler.getSubNode(stepnode, "group");
            Node fields = XMLHandler.getSubNode(stepnode, "fields");

            int sizegroup = XMLHandler.countNodes(groupn, "field");
            int nrfields = XMLHandler.countNodes(fields, "field");

            allocate(sizegroup, nrfields);

            for (int i = 0; i < sizegroup; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(groupn, "field", i);
                groupField[i] = XMLHandler.getTagValue(fnode, "name");
            }

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
                denormaliserTargetField[i] = new DenormaliserTargetField();
                denormaliserTargetField[i].setFieldName(XMLHandler.getTagValue(fnode, "field_name"));
                denormaliserTargetField[i].setKeyValue(XMLHandler.getTagValue(fnode, "key_value"));
                denormaliserTargetField[i].setTargetName(XMLHandler.getTagValue(fnode, "target_name"));
                denormaliserTargetField[i].setTargetType(XMLHandler.getTagValue(fnode, "target_type"));
                denormaliserTargetField[i].setTargetLength(Const.toInt(XMLHandler.getTagValue(fnode, "target_length"), -1));
                denormaliserTargetField[i].setTargetPrecision(Const.toInt(XMLHandler.getTagValue(fnode, "target_precision"), -1));
                denormaliserTargetField[i].setTargetDecimalSymbol(XMLHandler.getTagValue(fnode, "target_decimal_symbol"));
                denormaliserTargetField[i].setTargetGroupingSymbol(XMLHandler.getTagValue(fnode, "target_grouping_symbol"));
                denormaliserTargetField[i].setTargetCurrencySymbol(XMLHandler.getTagValue(fnode, "target_currency_symbol"));
                denormaliserTargetField[i].setTargetNullString(XMLHandler.getTagValue(fnode, "target_null_string"));
                denormaliserTargetField[i].setTargetAggregationType(XMLHandler.getTagValue(fnode, "target_aggregation_type"));
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public String getXML()
    {
        String retval = "";

        retval += "      " + XMLHandler.addTagValue("key_field", keyField);

        retval += "      <group>" + Const.CR;
        for (int i = 0; i < groupField.length; i++)
        {
            retval += "        <field>" + Const.CR;
            retval += "          " + XMLHandler.addTagValue("name", groupField[i]);
            retval += "          </field>" + Const.CR;
        }
        retval += "        </group>" + Const.CR;

        retval += "      <fields>" + Const.CR;
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            DenormaliserTargetField field = denormaliserTargetField[i];

            retval += "        <field>" + Const.CR;
            retval += "          " + XMLHandler.addTagValue("field_name", field.getFieldName());
            retval += "          " + XMLHandler.addTagValue("key_value", field.getKeyValue());
            retval += "          " + XMLHandler.addTagValue("target_name", field.getTargetName());
            retval += "          " + XMLHandler.addTagValue("target_type", field.getTargetTypeDesc());
            retval += "          " + XMLHandler.addTagValue("target_length", field.getTargetLength());
            retval += "          " + XMLHandler.addTagValue("target_precision", field.getTargetPrecision());
            retval += "          " + XMLHandler.addTagValue("target_decimal_symbol", field.getTargetDecimalSymbol());
            retval += "          " + XMLHandler.addTagValue("target_grouping_symbol", field.getTargetGroupingSymbol());
            retval += "          " + XMLHandler.addTagValue("target_currency_symbol", field.getTargetCurrencySymbol());
            retval += "          " + XMLHandler.addTagValue("target_null_string", field.getTargetNullString());
            retval += "          " + XMLHandler.addTagValue("target_aggregation_type", field.getTargetAggregationTypeDesc());
            retval += "          </field>" + Const.CR;
        }
        retval += "        </fields>" + Const.CR;

        return retval;
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            keyField = rep.getStepAttributeString(id_step, "key_field");

            int groupsize = rep.countNrStepAttributes(id_step, "group_name");
            int nrvalues = rep.countNrStepAttributes(id_step, "field_name");

            allocate(groupsize, nrvalues);

            for (int i = 0; i < groupsize; i++)
            {
                groupField[i] = rep.getStepAttributeString(id_step, i, "group_name");
            }

            for (int i = 0; i < nrvalues; i++)
            {
                denormaliserTargetField[i] = new DenormaliserTargetField();
                denormaliserTargetField[i].setFieldName(rep.getStepAttributeString(id_step, i, "field_name"));
                denormaliserTargetField[i].setKeyValue(rep.getStepAttributeString(id_step, i, "key_value"));
                denormaliserTargetField[i].setTargetName(rep.getStepAttributeString(id_step, i, "target_name"));
                denormaliserTargetField[i].setTargetType(rep.getStepAttributeString(id_step, i, "target_type"));
                denormaliserTargetField[i].setTargetLength((int) rep.getStepAttributeInteger(id_step, i, "target_length"));
                denormaliserTargetField[i].setTargetPrecision((int) rep.getStepAttributeInteger(id_step, i, "target_precision"));
                denormaliserTargetField[i].setTargetDecimalSymbol(rep.getStepAttributeString(id_step, i, "target_decimal_symbol"));
                denormaliserTargetField[i].setTargetGroupingSymbol(rep.getStepAttributeString(id_step, i, "target_grouping_symbol"));
                denormaliserTargetField[i].setTargetCurrencySymbol(rep.getStepAttributeString(id_step, i, "target_currency_symbol"));
                denormaliserTargetField[i].setTargetNullString(rep.getStepAttributeString(id_step, i, "target_null_string"));
                denormaliserTargetField[i].setTargetAggregationType(rep.getStepAttributeString(id_step, i, "target_aggregation_type"));
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
            rep.saveStepAttribute(id_transformation, id_step, "key_field", keyField);

            for (int i = 0; i < groupField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "group_name", groupField[i]);
            }

            for (int i = 0; i < denormaliserTargetField.length; i++)
            {
                DenormaliserTargetField field = denormaliserTargetField[i];

                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", field.getFieldName());
                rep.saveStepAttribute(id_transformation, id_step, i, "key_value", field.getKeyValue());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_name", field.getTargetName());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_type", field.getTargetTypeDesc());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_length", field.getTargetLength());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_precision", field.getTargetPrecision());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_decimal_symbol", field.getTargetDecimalSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_grouping_symbol", field.getTargetGroupingSymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_currency_symbol", field.getTargetCurrencySymbol());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_null_string", field.getTargetNullString());
                rep.saveStepAttribute(id_transformation, id_step, i, "target_aggregation_type", field.getTargetAggregationTypeDesc());
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;

        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
            remarks.add(cr);
        }
    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new DenormaliserDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new Denormaliser(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new DenormaliserData();
    }

}
