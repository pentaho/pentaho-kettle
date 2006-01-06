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

package be.ibridge.kettle.trans.step.rowgenerator;

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

public class RowGeneratorMeta extends BaseStepMeta implements StepMetaInterface
{
	private  long   rowLimit;
	
	private  String currency[];
	private  String decimal[];
	private  String group[];
	private  String value[];
	
	private  String fieldName[];
	private  String fieldType[];
	private  String fieldFormat[];

	private  int fieldLength[];
	private  int fieldPrecision[];

	public RowGeneratorMeta()
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
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
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
		RowGeneratorMeta retval = (RowGeneratorMeta)super.clone();

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
			int nrfields;
			String lim;
					
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			nrfields=XMLHandler.countNodes(fields, "field");
	
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
			
			// Is there a limit on the number of rows we process?
			lim=XMLHandler.getTagValue(stepnode, "limit");
			rowLimit = Const.toLong(lim, 0);
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
			
		for (i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			fieldType[i]      = "Number";
			fieldFormat[i]    = "¤0,000,000.00;¤-0,000,000.00";
			fieldLength[i]    = 9;
			fieldPrecision[i] = 2;
			currency[i]    = "€";
			decimal[i]     = ",";
			group[i]       = ".";
			value[i]       = "-";
		}
			
		rowLimit=10;
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
		String retval="";
		int i;
		
		retval+="    <fields>"+Const.CR;
		for (i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				retval+="      <field>"+Const.CR;
				retval+="        "+XMLHandler.addTagValue("name",      fieldName[i]);
				retval+="        "+XMLHandler.addTagValue("type",      fieldType[i]);
				retval+="        "+XMLHandler.addTagValue("format",    fieldFormat[i]);
				retval+="        "+XMLHandler.addTagValue("currency",  currency[i]);
				retval+="        "+XMLHandler.addTagValue("decimal",   decimal[i]);
				retval+="        "+XMLHandler.addTagValue("group",     group[i]);
				retval+="        "+XMLHandler.addTagValue("nullif",    value[i]);
				retval+="        "+XMLHandler.addTagValue("length",    fieldLength[i]);
				retval+="        "+XMLHandler.addTagValue("precision", fieldPrecision[i]);
				retval+="        </field>"+Const.CR;
			}
		}
		retval+="      </fields>"+Const.CR;
		retval+="    "+XMLHandler.addTagValue("limit", rowLimit);

		return retval;
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
				currency[i]    =       rep.getStepAttributeString (id_step, i, "field_currency");
				decimal[i]     =       rep.getStepAttributeString (id_step, i, "field_decimal");
				group[i]       =       rep.getStepAttributeString (id_step, i, "field_group");
				value[i]           =       rep.getStepAttributeString (id_step, i, "field_nullif");
				fieldLength[i]    =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
			}
			
			rowLimit = (int)rep.getStepAttributeInteger(id_step, "limit");
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
			
			rep.saveStepAttribute(id_transformation, id_step, "limit",      rowLimit);
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step type can't read from the input stream(s).", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step doesn't read from the input stream(s).", stepMeta);
			remarks.add(cr);
			
			if (rowLimit==0.0)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Step will not return any rows.", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step will return "+(long)rowLimit+" rows", stepMeta);
				remarks.add(cr);
			}
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This is not expecting nor reading data from input steps", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "No input received from other steps.", stepMeta);
			remarks.add(cr);
		}
        
        // Check the constants...
        RowGeneratorData data = new RowGeneratorData();
        RowGeneratorMeta meta = (RowGeneratorMeta) stepMeta.getStepMetaInterface();
        RowGenerator.buildRow(meta, data, remarks);

	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new RowGeneratorDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new RowGenerator(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new RowGeneratorData();
	}

}
