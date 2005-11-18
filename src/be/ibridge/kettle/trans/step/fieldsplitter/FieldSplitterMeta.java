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

package be.ibridge.kettle.trans.step.fieldsplitter;

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
    
    
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
			splitField = XMLHandler.getTagValue(stepnode, "splitfield");
			delimiter  = XMLHandler.getTagValue(stepnode, "delimiter");
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields   = XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
					
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				field     [i]  = XMLHandler.getTagValue(fnode, "name"); 
				fieldID       [i]  = XMLHandler.getTagValue(fnode, "id");
				String sidrem  = XMLHandler.getTagValue(fnode, "idrem");
				String stype   = XMLHandler.getTagValue(fnode, "type");
				fieldFormat    [i]  = XMLHandler.getTagValue(fnode, "format");
				fieldGroup     [i]  = XMLHandler.getTagValue(fnode, "group");
				fieldDecimal   [i]  = XMLHandler.getTagValue(fnode, "decimal");
				fieldCurrency  [i]  = XMLHandler.getTagValue(fnode, "currency");
				String slen    = XMLHandler.getTagValue(fnode, "length");
				String sprc    = XMLHandler.getTagValue(fnode, "precision");
				fieldDefault    [i]  = XMLHandler.getTagValue(fnode, "nullif");
				
				removeID[i] = "Y".equalsIgnoreCase(sidrem);
				fieldType[i]  = Value.getType(stype);
				fieldLength   [i]=Const.toInt(slen, -1); 
				fieldPrecision[i]=Const.toInt(sprc, -1); 
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		splitField = "";
		delimiter  = ",";
		
		int nrfields   = 0;

		allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			field          [i]  = "field"+i; 
			fieldID        [i]  = "id"+i;
			removeID       [i]  = true;
			fieldType      [i]  = Value.VALUE_TYPE_NUMBER;
			fieldFormat    [i]  = "";
			fieldGroup     [i]  = "";
			fieldDecimal   [i]  = "";
			fieldCurrency  [i]  = "";
			fieldLength    [i]  = -1; 
			fieldPrecision [i]  = -1; 
			fieldDefault   [i]  = "";
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;

		if (r==null)  // give back rename values 
		{
			row = new Row();
		}
		else  // Remove fields to normalise, Add the typefield and norm fields, leave the rest!         
		{
			row=r;

			// Remove the field to split
			int idx = row.searchValueIndex(splitField);
			if (idx>=0) row.removeValue(idx);
			
			// Add the new fields at the place of the index --> replace!
			for (int i=0;i<field.length;i++)
			{
				Value v = new Value(field[i], fieldType[i]);
				v.setLength(fieldLength[i], fieldPrecision[i]);
				v.setOrigin(name);
				if (idx>=row.size()) row.addValue(v);
				else                 row.addValue(idx+i, v);
			}
		}
		
		return row;
	}

	public String getXML()
	{
		String retval="";
		int i;

		retval+="   "+XMLHandler.addTagValue("splitfield", splitField);
		retval+="   "+XMLHandler.addTagValue("delimiter", delimiter);
		
		retval+="    <fields>";
		for (i=0;i<field.length;i++)
		{
			retval+="      <field>";
			retval+="        "+XMLHandler.addTagValue("name",      field[i]);
			retval+="        "+XMLHandler.addTagValue("id",        fieldID[i]);
			retval+="        "+XMLHandler.addTagValue("idrem",     removeID[i]);
			retval+="        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(fieldType[i]));
			retval+="        "+XMLHandler.addTagValue("format",    fieldFormat[i]);
			retval+="        "+XMLHandler.addTagValue("group",     fieldGroup[i]);
			retval+="        "+XMLHandler.addTagValue("decimal",   fieldDecimal[i]);
			retval+="        "+XMLHandler.addTagValue("length",    fieldLength[i]);
			retval+="        "+XMLHandler.addTagValue("precision", fieldPrecision[i]);
			retval+="        "+XMLHandler.addTagValue("nullif",    fieldDefault[i]);
			retval+="        </field>";
		}
		retval+="      </fields>";

		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			splitField  = rep.getStepAttributeString(id_step, "splitfield");
			delimiter   = rep.getStepAttributeString(id_step, "delimiter");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				field[i]       =       rep.getStepAttributeString (id_step, i, "field_name");
				fieldID[i]         =       rep.getStepAttributeString (id_step, i, "field_id");
				removeID[i]       =       rep.getStepAttributeBoolean(id_step, i, "field_idrem");
				fieldType[i]        =  Value.getType( rep.getStepAttributeString (id_step, i, "field_type") );
				fieldFormat[i]      =       rep.getStepAttributeString (id_step, i, "field_format");
				fieldGroup[i]       =       rep.getStepAttributeString (id_step, i, "field_group");
				fieldDecimal[i]     =       rep.getStepAttributeString (id_step, i, "field_decimal");
				fieldLength[i]      =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i]   =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
				fieldDefault[i]      =       rep.getStepAttributeString (id_step, i, "field_nullif");
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
			rep.saveStepAttribute(id_transformation, id_step, "splitfield", splitField);
			rep.saveStepAttribute(id_transformation, id_step, "delimiter",  delimiter);
	
			for (int i=0;i<field.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_id",        fieldID[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_idrem",     removeID[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      Value.getTypeDesc(fieldType[i]));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    fieldFormat[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     fieldGroup[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   fieldDecimal[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    fieldDefault[i]);
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		String error_message="";
		CheckResult cr;
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
			remarks.add(cr);
			
			error_message = "";
			
			Value v = prev.searchValue(splitField);
			if (v==null)
			{
				error_message="Field to split ["+splitField+"] is not present in the input stream!";
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Splitfield ["+splitField+"]found in the input stream.", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			error_message="Couldn't read fields from the previous step."+Const.CR;
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
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
		return new FieldSplitterDialog(shell, info, transMeta, name);
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
