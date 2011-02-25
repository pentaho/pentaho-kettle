/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.denormaliser;

import java.util.List;
import java.util.Map;

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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * The Denormaliser transformation step meta-data
 * 
 * @since 17-jan-2006
 * @author Matt
 */

public class DenormaliserMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = DenormaliserMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
    
    public String[] getDenormaliserTargetFields()
    {
        String fields[] = new String[denormaliserTargetField.length];
        for (int i=0;i<fields.length;i++)
        {
            fields[i] = denormaliserTargetField[i].getTargetName();
        }
        
        return fields;
    }
    
    public DenormaliserTargetField searchTargetField(String targetName)
    {
        for (int i=0;i<denormaliserTargetField.length;i++)
        {
            DenormaliserTargetField field = denormaliserTargetField[i];
            if (field.getTargetName().equalsIgnoreCase(targetName)) return field;            
        }
        return null;
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

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters) throws KettleXMLException
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
    
    @Override
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

        // Remove the key value (there will be different entries for each output row)
        //
        if (keyField != null && keyField.length() > 0)
        {
            int idx = row.indexOfValue(keyField);
            if (idx < 0) { throw new KettleStepException(BaseMessages.getString(PKG, "DenormaliserMeta.Exception.UnableToLocateKeyField",keyField )); } //$NON-NLS-1$ //$NON-NLS-2$
            row.removeValueMeta(idx);
        }
        else
        {
            throw new KettleStepException(BaseMessages.getString(PKG, "DenormaliserMeta.Exception.RequiredKeyField")); //$NON-NLS-1$
        }

        // Remove all field value(s) (there will be different entries for each output row)
        //
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            String fieldname = denormaliserTargetField[i].getFieldName();
            if (fieldname != null && fieldname.length() > 0)
            {
                int idx = row.indexOfValue(fieldname);
                if (idx >= 0)
                {
                    row.removeValueMeta(idx);
                }
            }
            else
            {
                throw new KettleStepException(BaseMessages.getString(PKG, "DenormaliserMeta.Exception.RequiredTargetFieldName", (i + 1) + "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        // Re-add the target fields
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            DenormaliserTargetField field = denormaliserTargetField[i];
            ValueMetaInterface target = new ValueMeta(field.getTargetName(), field.getTargetType());
            target.setLength(field.getTargetLength(), field.getTargetPrecision());
            target.setOrigin(name);
            row.addValueMeta(target);
        }
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            keyField = XMLHandler.getTagValue(stepnode, "key_field"); //$NON-NLS-1$

            Node groupn = XMLHandler.getSubNode(stepnode, "group"); //$NON-NLS-1$
            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$

            int sizegroup = XMLHandler.countNodes(groupn, "field"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(sizegroup, nrfields);

            for (int i = 0; i < sizegroup; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(groupn, "field", i); //$NON-NLS-1$
                groupField[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
            }

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
                denormaliserTargetField[i] = new DenormaliserTargetField();
                denormaliserTargetField[i].setFieldName(XMLHandler.getTagValue(fnode, "field_name")); //$NON-NLS-1$
                denormaliserTargetField[i].setKeyValue(XMLHandler.getTagValue(fnode, "key_value")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetName(XMLHandler.getTagValue(fnode, "target_name")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetType(XMLHandler.getTagValue(fnode, "target_type")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetFormat(XMLHandler.getTagValue(fnode, "target_format")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetLength(Const.toInt(XMLHandler.getTagValue(fnode, "target_length"), -1)); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetPrecision(Const.toInt(XMLHandler.getTagValue(fnode, "target_precision"), -1)); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetDecimalSymbol(XMLHandler.getTagValue(fnode, "target_decimal_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetGroupingSymbol(XMLHandler.getTagValue(fnode, "target_grouping_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetCurrencySymbol(XMLHandler.getTagValue(fnode, "target_currency_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetNullString(XMLHandler.getTagValue(fnode, "target_null_string")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetAggregationType(XMLHandler.getTagValue(fnode, "target_aggregation_type")); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "DenormaliserMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("      " + XMLHandler.addTagValue("key_field", keyField)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("      <group>" + Const.CR); //$NON-NLS-1$
        for (int i = 0; i < groupField.length; i++)
        {
            retval.append("        <field>" + Const.CR); //$NON-NLS-1$
            retval.append("          " + XMLHandler.addTagValue("name", groupField[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          </field>" + Const.CR); //$NON-NLS-1$
        }
        retval.append("        </group>" + Const.CR); //$NON-NLS-1$

        retval.append("      <fields>" + Const.CR); //$NON-NLS-1$
        for (int i = 0; i < denormaliserTargetField.length; i++)
        {
            DenormaliserTargetField field = denormaliserTargetField[i];

            retval.append("        <field>" + Const.CR); //$NON-NLS-1$
            retval.append("          " + XMLHandler.addTagValue("field_name", field.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("key_value", field.getKeyValue())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_name", field.getTargetName())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_type", field.getTargetTypeDesc())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_format", field.getTargetFormat())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_length", field.getTargetLength())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_precision", field.getTargetPrecision())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_decimal_symbol", field.getTargetDecimalSymbol())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_grouping_symbol", field.getTargetGroupingSymbol())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_currency_symbol", field.getTargetCurrencySymbol())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_null_string", field.getTargetNullString())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          " + XMLHandler.addTagValue("target_aggregation_type", field.getTargetAggregationTypeDesc())); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("          </field>" + Const.CR); //$NON-NLS-1$
        }
        retval.append("        </fields>" + Const.CR); //$NON-NLS-1$

        return retval.toString();
    }
    
    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            keyField = rep.getStepAttributeString(id_step, "key_field"); //$NON-NLS-1$

            int groupsize = rep.countNrStepAttributes(id_step, "group_name"); //$NON-NLS-1$
            int nrvalues = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$

            allocate(groupsize, nrvalues);

            for (int i = 0; i < groupsize; i++)
            {
                groupField[i] = rep.getStepAttributeString(id_step, i, "group_name"); //$NON-NLS-1$
            }

            for (int i = 0; i < nrvalues; i++)
            {
                denormaliserTargetField[i] = new DenormaliserTargetField();
                denormaliserTargetField[i].setFieldName(rep.getStepAttributeString(id_step, i, "field_name")); //$NON-NLS-1$
                denormaliserTargetField[i].setKeyValue(rep.getStepAttributeString(id_step, i, "key_value")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetName(rep.getStepAttributeString(id_step, i, "target_name")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetType(rep.getStepAttributeString(id_step, i, "target_type")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetFormat(rep.getStepAttributeString(id_step, i, "target_format")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetLength((int) rep.getStepAttributeInteger(id_step, i, "target_length")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetPrecision((int) rep.getStepAttributeInteger(id_step, i, "target_precision")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetDecimalSymbol(rep.getStepAttributeString(id_step, i, "target_decimal_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetGroupingSymbol(rep.getStepAttributeString(id_step, i, "target_grouping_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetCurrencySymbol(rep.getStepAttributeString(id_step, i, "target_currency_symbol")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetNullString(rep.getStepAttributeString(id_step, i, "target_null_string")); //$NON-NLS-1$
                denormaliserTargetField[i].setTargetAggregationType(rep.getStepAttributeString(id_step, i, "target_aggregation_type")); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "key_field", keyField); //$NON-NLS-1$

            for (int i = 0; i < groupField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "group_name", groupField[i]); //$NON-NLS-1$
            }

            for (int i = 0; i < denormaliserTargetField.length; i++)
            {
                DenormaliserTargetField field = denormaliserTargetField[i];

                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", field.getFieldName()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "key_value", field.getKeyValue()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_name", field.getTargetName()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_type", field.getTargetTypeDesc()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_format", field.getTargetFormat()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_length", field.getTargetLength()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_precision", field.getTargetPrecision()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_decimal_symbol", field.getTargetDecimalSymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_grouping_symbol", field.getTargetGroupingSymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_currency_symbol", field.getTargetCurrencySymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_null_string", field.getTargetNullString()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "target_aggregation_type", field.getTargetAggregationTypeDesc()); //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "DenormaliserMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }
    
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
        CheckResult cr;

        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "DenormaliserMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "DenormaliserMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new Denormaliser(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new DenormaliserData();
    }

    @Override
    public StepMetaInjectionInterface getStepMetaInjectionInterface() {
      return new DenormaliserMetaInjection(this);
    }
}
