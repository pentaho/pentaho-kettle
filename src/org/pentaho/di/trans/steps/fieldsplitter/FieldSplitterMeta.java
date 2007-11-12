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

package org.pentaho.di.trans.steps.fieldsplitter;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
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
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/*
 * Created on 31-okt-2003
 *
 */

/**
<CODE>
  Example1:<p>
  -------------<p>
  DATUM;VALUES<p>
  20031031;500,300,200,100<p>
<p>
        ||<t>        delimiter     = ,<p>
       \||/<t>       field[]       = SALES1, SALES2, SALES3, SALES4<p>
        \/<t>        id[]          = <empty><p>
          <t>        idrem[]       = no, no, no, no<p>
           <t>       type[]        = Number, Number, Number, Number<p>
            <t>      format[]      = ###.##, ###.##, ###.##, ###.##<p> 
            <t>      group[]       = <empty><p>
            <t>      decimal[]     = .<p>
            <t>      currency[]    = <empty><p>
            <t>      length[]      = 3, 3, 3, 3<p>
            <t>      precision[]   = 0, 0, 0, 0<p>
  <p>
  DATUM;SALES1;SALES2;SALES3;SALES4<p>
  20031031;500;300;200;100<p>
<p>
  Example2:<p>
  -----------<p>
<p>
  20031031;Sales2=310.50, Sales4=150.23<p>
<p>
        ||        delimiter     = ,<p>
       \||/       field[]       = SALES1, SALES2, SALES3, SALES4<p>
        \/        id[]          = Sales1, Sales2, Sales3, Sales4<p>
                  idrem[]       = yes, yes, yes, yes (remove ID's from split field)<p>
                  type[]        = Number, Number, Number, Number<p>
                  format[]      = ###.##, ###.##, ###.##, ###.##<p> 
                  group[]       = <empty><p>
                  decimal[]     = .<p>
                  currency[]    = <empty><p>
                  length[]      = 3, 3, 3, 3<p>
                  precision[]   = 0, 0, 0, 0<p>
<p>                  
  DATUM;SALES1;SALES2;SALES3;SALES4<p>
  20031031;;310,50;;150,23<p>
<p>      

</CODE>
**/
@Step(name="FieldSplitter",image="ui/images/SPL.png",tooltip="BaseStep.TypeTooltipDesc.SplitFields",description="BaseStep.TypeLongDesc.SplitFields",
		category=StepCategory.CATEGORY_TRANSFORM)
public class FieldSplitterMeta extends BaseStepMeta implements StepMetaInterface
{
    /** Field to split */
	private String  splitField;
	
	/** Split fields based upon this delimiter.*/
	private String  delimiter;
	
	/** new field names */
	private String  field[];
	
	/** Field ID's to scan for */
	private String  fieldID[];
	
	/** flag: remove ID */
	private boolean removeID[];
	
	/** type of new field */
	private int     fieldType[];
	
	/** formatting mask to convert value */
	private String  fieldFormat[]; 

	/** Grouping symbol */
	private String  fieldGroup[];
	
	/** Decimal point . or , */
	private String  fieldDecimal[];
	
	/** Currency symbol */
	private String  fieldCurrency[];
	
	/** Length of field */
	private int     fieldLength[];
	
	/** Precision of field */
	private int     fieldPrecision[];
	
	/** Default value in case no value was found (ID option) */
	private String  fieldDefault[];
	
	public FieldSplitterMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	
    /**
     * @return Returns the delimiter.
     */
    public String getDelimiter()
    {
        return delimiter;
    }
    
    /**
     * @param delimiter The delimiter to set.
     */
    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }
    
    /**
     * @return Returns the field.
     */
    public String[] getField()
    {
        return field;
    }
    
    /**
     * @param field The field to set.
     */
    public void setField(String[] field)
    {
        this.field = field;
    }
    
    /**
     * @return Returns the fieldCurrency.
     */
    public String[] getFieldCurrency()
    {
        return fieldCurrency;
    }
    
    /**
     * @param fieldCurrency The fieldCurrency to set.
     */
    public void setFieldCurrency(String[] fieldCurrency)
    {
        this.fieldCurrency = fieldCurrency;
    }
    
    /**
     * @return Returns the fieldDecimal.
     */
    public String[] getFieldDecimal()
    {
        return fieldDecimal;
    }
    
    /**
     * @param fieldDecimal The fieldDecimal to set.
     */
    public void setFieldDecimal(String[] fieldDecimal)
    {
        this.fieldDecimal = fieldDecimal;
    }
    
    /**
     * @return Returns the fieldDefault.
     */
    public String[] getFieldDefault()
    {
        return fieldDefault;
    }
    
    /**
     * @param fieldDefault The fieldDefault to set.
     */
    public void setFieldDefault(String[] fieldDefault)
    {
        this.fieldDefault = fieldDefault;
    }
    
    /**
     * @return Returns the fieldFormat.
     */
    public String[] getFieldFormat()
    {
        return fieldFormat;
    }
    
    /**
     * @param fieldFormat The fieldFormat to set.
     */
    public void setFieldFormat(String[] fieldFormat)
    {
        this.fieldFormat = fieldFormat;
    }
    
    /**
     * @return Returns the fieldGroup.
     */
    public String[] getFieldGroup()
    {
        return fieldGroup;
    }
    
    /**
     * @param fieldGroup The fieldGroup to set.
     */
    public void setFieldGroup(String[] fieldGroup)
    {
        this.fieldGroup = fieldGroup;
    }
    
    /**
     * @return Returns the fieldID.
     */
    public String[] getFieldID()
    {
        return fieldID;
    }
    
    /**
     * @param fieldID The fieldID to set.
     */
    public void setFieldID(String[] fieldID)
    {
        this.fieldID = fieldID;
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
    
    /**
     * @return Returns the removeID.
     */
    public boolean[] removeID()
    {
        return removeID;
    }
    
    /**
     * @param removeID The removeID to set.
     */
    public void setRemoveID(boolean[] removeID)
    {
        this.removeID = removeID;
    }
    
    /**
     * @return Returns the splitField.
     */
    public String getSplitField()
    {
        return splitField;
    }
    
    /**
     * @param splitField The splitField to set.
     */
    public void setSplitField(String splitField)
    {
        this.splitField = splitField;
    }
    
    
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		field      = new String[nrfields];
		fieldID        = new String[nrfields];
		removeID      = new boolean[nrfields];
		fieldType       = new int[nrfields];
		fieldFormat     = new String[nrfields];
		fieldGroup      = new String[nrfields];
		fieldDecimal    = new String[nrfields];
		fieldCurrency   = new String[nrfields];
		fieldLength     = new int[nrfields];
		fieldPrecision  = new int[nrfields];
		fieldDefault     = new String[nrfields];
	}

	public Object clone()
	{
		FieldSplitterMeta retval = (FieldSplitterMeta)super.clone();
		
		int nrfields   = field.length;
		
		retval.allocate(nrfields);

		for (int i=0;i<nrfields;i++)
		{
			retval.field     [i] = field[i];
			retval.fieldID       [i] = fieldID[i]; 
			retval.fieldFormat    [i] = fieldFormat[i];
			retval.fieldGroup     [i] = fieldGroup[i];
			retval.fieldDecimal   [i] = fieldDecimal[i];
			retval.fieldCurrency  [i] = fieldCurrency[i];
			retval.fieldDefault    [i] = fieldDefault[i]; 
			retval.removeID     [i] = removeID[i];
			retval.fieldType      [i] = fieldType[i];
			retval.fieldLength    [i] = fieldLength[i];
			retval.fieldPrecision [i] = fieldPrecision[i]; 
		}

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			splitField = XMLHandler.getTagValue(stepnode, "splitfield"); //$NON-NLS-1$
			delimiter  = XMLHandler.getTagValue(stepnode, "delimiter"); //$NON-NLS-1$
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
	
			allocate(nrfields);
					
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				field     [i]  = XMLHandler.getTagValue(fnode, "name");  //$NON-NLS-1$
				fieldID       [i]  = XMLHandler.getTagValue(fnode, "id"); //$NON-NLS-1$
				String sidrem  = XMLHandler.getTagValue(fnode, "idrem"); //$NON-NLS-1$
				String stype   = XMLHandler.getTagValue(fnode, "type"); //$NON-NLS-1$
				fieldFormat    [i]  = XMLHandler.getTagValue(fnode, "format"); //$NON-NLS-1$
				fieldGroup     [i]  = XMLHandler.getTagValue(fnode, "group"); //$NON-NLS-1$
				fieldDecimal   [i]  = XMLHandler.getTagValue(fnode, "decimal"); //$NON-NLS-1$
				fieldCurrency  [i]  = XMLHandler.getTagValue(fnode, "currency"); //$NON-NLS-1$
				String slen    = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
				String sprc    = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$
				fieldDefault    [i]  = XMLHandler.getTagValue(fnode, "nullif"); //$NON-NLS-1$
				
				removeID[i] = "Y".equalsIgnoreCase(sidrem); //$NON-NLS-1$
				fieldType[i]  = ValueMeta.getType(stype);
				fieldLength   [i]=Const.toInt(slen, -1); 
				fieldPrecision[i]=Const.toInt(sprc, -1); 
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("FieldSplitterMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		splitField = ""; //$NON-NLS-1$
		delimiter  = ","; //$NON-NLS-1$
		
		int nrfields   = 0;

		allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			field          [i]  = "field"+i;  //$NON-NLS-1$
			fieldID        [i]  = "id"+i; //$NON-NLS-1$
			removeID       [i]  = true;
			fieldType      [i]  = ValueMetaInterface.TYPE_NUMBER;
			fieldFormat    [i]  = ""; //$NON-NLS-1$
			fieldGroup     [i]  = ""; //$NON-NLS-1$
			fieldDecimal   [i]  = ""; //$NON-NLS-1$
			fieldCurrency  [i]  = ""; //$NON-NLS-1$
			fieldLength    [i]  = -1; 
			fieldPrecision [i]  = -1; 
			fieldDefault   [i]  = ""; //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
	{
		// Remove the field to split
		int idx = r.indexOfValue(splitField);
		if (idx<0) //not found
		{
			throw new RuntimeException(Messages.getString("FieldSplitter.Log.CouldNotFindFieldToSplit",splitField)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// Add the new fields at the place of the index --> replace!
		for (int i=0;i<field.length;i++)
		{
			ValueMetaInterface v = new ValueMeta(field[i], fieldType[i]);
			v.setLength(fieldLength[i], fieldPrecision[i]);
			v.setOrigin(name);
            v.setConversionMask(fieldFormat[i]);
            v.setDecimalSymbol(fieldDecimal[i]);
            v.setGroupingSymbol(fieldGroup[i]);
            v.setCurrencySymbol(fieldCurrency[i]);
            //TODO when implemented in UI v.setDateFormatLenient(dateFormatLenient);
            //TODO when implemented in UI v.setDateFormatLocale(dateFormatLocale);			
			if(i==0 && idx>=0)
			{
				//the first valueMeta (splitField) will be replaced
				r.setValueMeta(idx, v);
			}
			else
			{
				//other valueMeta will be added
				if (idx>=r.size()) r.addValueMeta(v);
				r.addValueMeta(idx+i, v);
			}
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(500);

		retval.append("   ").append(XMLHandler.addTagValue("splitfield", splitField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("   ").append(XMLHandler.addTagValue("delimiter", delimiter)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<field.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name",      field[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("id",        fieldID[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("idrem",     removeID[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("type",      ValueMeta.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("format",    fieldFormat[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("group",     fieldGroup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("decimal",   fieldDecimal[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("length",    fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("nullif",    fieldDefault[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </field>"); //$NON-NLS-1$
		}
		retval.append("    </fields>"); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleException
	{
		try
		{
			splitField  = rep.getStepAttributeString(id_step, "splitfield"); //$NON-NLS-1$
			delimiter   = rep.getStepAttributeString(id_step, "delimiter"); //$NON-NLS-1$
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				field[i]       =       rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
				fieldID[i]         =       rep.getStepAttributeString (id_step, i, "field_id"); //$NON-NLS-1$
				removeID[i]       =       rep.getStepAttributeBoolean(id_step, i, "field_idrem"); //$NON-NLS-1$
				fieldType[i]        =  ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ); //$NON-NLS-1$
				fieldFormat[i]      =       rep.getStepAttributeString (id_step, i, "field_format"); //$NON-NLS-1$
				fieldGroup[i]       =       rep.getStepAttributeString (id_step, i, "field_group"); //$NON-NLS-1$
				fieldDecimal[i]     =       rep.getStepAttributeString (id_step, i, "field_decimal"); //$NON-NLS-1$
				fieldLength[i]      =  (int)rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
				fieldPrecision[i]   =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
				fieldDefault[i]      =       rep.getStepAttributeString (id_step, i, "field_nullif"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("FieldSplitterMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "splitfield", splitField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "delimiter",  delimiter); //$NON-NLS-1$
	
			for (int i=0;i<field.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_id",        fieldID[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_idrem",     removeID[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      ValueMeta.getTypeDesc(fieldType[i])); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    fieldFormat[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     fieldGroup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   fieldDecimal[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    fieldDefault[i]); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("FieldSplitterMeta.Exception.UnalbeToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		String error_message=""; //$NON-NLS-1$
		CheckResult cr;
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FieldSplitterMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			error_message = ""; //$NON-NLS-1$
			
			int i = prev.indexOfValue(splitField);
			if (i<0)
			{
				error_message=Messages.getString("FieldSplitterMeta.CheckResult.SplitedFieldNotPresentInInputStream",splitField); //$NON-NLS-1$ //$NON-NLS-2$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FieldSplitterMeta.CheckResult.SplitedFieldFoundInInputStream",splitField), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				remarks.add(cr);
			}
		}
		else
		{
			error_message=Messages.getString("FieldSplitterMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FieldSplitterMeta.CheckResult.StepReceivingInfoFromOtherStep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FieldSplitterMeta.CheckResult.NoInputReceivedFromOtherStep"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new FieldSplitter(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new FieldSplitterData();
	}

}
