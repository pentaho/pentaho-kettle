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

package be.ibridge.kettle.trans.step.constant;

import java.text.DecimalFormat;
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
 * Created on 4-apr-2003
 *
 */

public class ConstantMeta extends BaseStepMeta implements StepMetaInterface
{	
	private  String currency[];
	private  String decimal[];
	private  String group[];
	private  String value[];
	
	private  String fieldName[];
	private  String fieldType[];
	private  String fieldFormat[];

	private  int fieldLength[];
	private  int fieldPrecision[];

	public ConstantMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
	
    /**
     * @return Returns the currency.
     */
    public String[] getCurrency()
    {
        return currency;
    }
    
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String[] currency)
    {
        this.currency = currency;
    }
    
    /**
     * @return Returns the decimal.
     */
    public String[] getDecimal()
    {
        return decimal;
    }
    
    /**
     * @param decimal The decimal to set.
     */
    public void setDecimal(String[] decimal)
    {
        this.decimal = decimal;
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
    public String[] getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(String[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    /**
     * @return Returns the group.
     */
    public String[] getGroup()
    {
        return group;
    }
    
    /**
     * @param group The group to set.
     */
    public void setGroup(String[] group)
    {
        this.group = group;
    }
    
    /**
     * @return Returns the value.
     */
    public String[] getValue()
    {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String[] value)
    {
        this.value = value;
    }
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		fieldName      = new String[nrfields];
		fieldType      = new String[nrfields];
		fieldFormat    = new String[nrfields];
		fieldLength    = new int[nrfields];
		fieldPrecision = new int[nrfields];
		currency    = new String[nrfields];
		decimal     = new String[nrfields];
		group       = new String[nrfields];
		value          = new String[nrfields];
	}
	
	public Object clone()
	{
		ConstantMeta retval = (ConstantMeta)super.clone();

		int nrfields=fieldName.length;

		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]   = fieldName[i];
			retval.fieldType[i]   = fieldType[i];
			retval.fieldFormat[i] = fieldFormat[i];
			retval.currency[i] = currency[i];
			retval.decimal[i]  = decimal[i];
			retval.group[i]    = group[i];
			retval.value[i]        = value[i];
			fieldLength[i]        = fieldLength[i]; 
			fieldPrecision[i]     = fieldPrecision[i]; 
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int  nrfields=XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			String slength, sprecision;
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i]   = XMLHandler.getTagValue(fnode, "name");
				fieldType[i]   = XMLHandler.getTagValue(fnode, "type");
				fieldFormat[i] = XMLHandler.getTagValue(fnode, "format");
				currency[i] = XMLHandler.getTagValue(fnode, "currency");
				decimal[i]  = XMLHandler.getTagValue(fnode, "decimal");
				group[i]    = XMLHandler.getTagValue(fnode, "group");
				value[i]        = XMLHandler.getTagValue(fnode, "nullif");
				slength         = XMLHandler.getTagValue(fnode, "length");
				sprecision      = XMLHandler.getTagValue(fnode, "precision");
				
				fieldLength[i]    = Const.toInt(slength, -1);
				fieldPrecision[i] = Const.toInt(sprecision, -1);
			}
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		int i, nrfields=0;
	
		allocate(nrfields);

        DecimalFormat decimalFormat = new DecimalFormat();

		for (i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			fieldType[i]      = "Number";
			fieldFormat[i]    = "\u00A40,000,000.00;\u00A4-0,000,000.00";
			fieldLength[i]    = 9;
			fieldPrecision[i] = 2;
			currency[i]    = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
			decimal[i]     = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
			group[i]       = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
			value[i]       = "-";
		}

	}
	
	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		int i;
		for (i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				int type=Value.getType(fieldType[i]);
				if (type==Value.VALUE_TYPE_NONE) type=Value.VALUE_TYPE_STRING;
				Value v=new Value(fieldName[i], type);
				v.setLength(fieldLength[i], fieldPrecision[i]);
				v.setOrigin(name);
				row.addValue(v);
			}
		}
		return row;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    <fields>"+Const.CR);
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				retval.append("      <field>"+Const.CR);
				retval.append("        "+XMLHandler.addTagValue("name",      fieldName[i]));
				retval.append("        "+XMLHandler.addTagValue("type",      fieldType[i]));
				retval.append("        "+XMLHandler.addTagValue("format",    fieldFormat[i]));
				retval.append("        "+XMLHandler.addTagValue("currency",  currency[i]));
				retval.append("        "+XMLHandler.addTagValue("decimal",   decimal[i]));
				retval.append("        "+XMLHandler.addTagValue("group",     group[i]));
				retval.append("        "+XMLHandler.addTagValue("nullif",    value[i]));
				retval.append("        "+XMLHandler.addTagValue("length",    fieldLength[i]));
				retval.append("        "+XMLHandler.addTagValue("precision", fieldPrecision[i]));
				retval.append("        </field>"+Const.CR);
			}
		}
		retval.append("      </fields>"+Const.CR);

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]      =       rep.getStepAttributeString (id_step, i, "field_name");
				fieldType[i]      =       rep.getStepAttributeString (id_step, i, "field_type");
	
				fieldFormat[i]    =       rep.getStepAttributeString (id_step, i, "field_format");
				currency[i]       =       rep.getStepAttributeString (id_step, i, "field_currency");
				decimal[i]        =       rep.getStepAttributeString (id_step, i, "field_decimal");
				group[i]          =       rep.getStepAttributeString (id_step, i, "field_group");
				value[i]          =       rep.getStepAttributeString (id_step, i, "field_nullif");
				fieldLength[i]    =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				if (fieldName[i]!=null && fieldName[i].length()!=0)
				{
					rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      fieldType[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    fieldFormat[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  currency[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   decimal[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     group[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    value[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ConstantMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ConstantMeta.CheckResult.NoFields"), stepMeta);
			remarks.add(cr);
		}
        
        // Check the constants...
        ConstantData data = new ConstantData();
        ConstantMeta meta = (ConstantMeta) stepMeta.getStepMetaInterface();
        Constant.buildRow(meta, data, remarks);
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ConstantDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new Constant(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ConstantData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    }


}
