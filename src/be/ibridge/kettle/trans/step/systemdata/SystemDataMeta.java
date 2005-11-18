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

package be.ibridge.kettle.trans.step.systemdata;

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
 * Created on 05-aug-2003
 *
 */

public class SystemDataMeta extends BaseStepMeta implements StepMetaInterface
{
	public final static int TYPE_SYSTEM_INFO_NONE             =  0;
	public final static int TYPE_SYSTEM_INFO_SYSTEM_DATE      =  1;
	public final static int TYPE_SYSTEM_INFO_SYSTEM_START     =  2;
	public final static int TYPE_SYSTEM_INFO_DATE_FROM        =  3;
	public final static int TYPE_SYSTEM_INFO_DATE_TO          =  4;
	public final static int TYPE_SYSTEM_INFO_PREV_DAY_START   =  5;
	public final static int TYPE_SYSTEM_INFO_PREV_DAY_END     =  6;
	public final static int TYPE_SYSTEM_INFO_THIS_DAY_START   =  7;
	public final static int TYPE_SYSTEM_INFO_THIS_DAY_END     =  8;
	public final static int TYPE_SYSTEM_INFO_NEXT_DAY_START   =  9;
	public final static int TYPE_SYSTEM_INFO_NEXT_DAY_END     = 10;
	public final static int TYPE_SYSTEM_INFO_PREV_MONTH_START = 11;
	public final static int TYPE_SYSTEM_INFO_PREV_MONTH_END   = 12;
	public final static int TYPE_SYSTEM_INFO_THIS_MONTH_START = 13;
	public final static int TYPE_SYSTEM_INFO_THIS_MONTH_END   = 14;
	public final static int TYPE_SYSTEM_INFO_NEXT_MONTH_START = 15;
	public final static int TYPE_SYSTEM_INFO_NEXT_MONTH_END   = 16;
	public final static int TYPE_SYSTEM_INFO_COPYNR           = 17;
	public final static int TYPE_SYSTEM_INFO_TRANS_NAME       = 18;
	public final static int TYPE_SYSTEM_INFO_FILENAME         = 19;
	public final static int TYPE_SYSTEM_INFO_MODIFIED_USER    = 20;
	public final static int TYPE_SYSTEM_INFO_MODIFIED_DATE    = 21;
	public final static int TYPE_SYSTEM_INFO_BATCH_ID         = 22;
	public final static int TYPE_SYSTEM_INFO_HOSTNAME         = 23;
	public final static int TYPE_SYSTEM_INFO_IP_ADDRESS       = 24;
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_01      = 25;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_02      = 26;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_03      = 27;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_04      = 28;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_05      = 29;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_06      = 30;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_07      = 31;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_08      = 32;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_09      = 33;	
	public final static int TYPE_SYSTEM_INFO_ARGUMENT_10      = 34;	

	public final static String type_desc[] = 
		{
			"-",
			"system date (variable)",
			"system date (fixed)",
			"start date range",
			"end date range",
			"yesterday start",
			"yesterday end",
			"today start",
			"today end",
			"tomorrow start",
			"tomorrow end",
			"last month start",
			"last month end",
			"this month start",
			"this month end",
			"next month start",
			"next month end",
			"copy of step",
			"transformation name",
			"transformation file name",
			"User modified",
			"Date modified",
			"batch ID",
			"Hostname",
			"IP address",
			"command line argument 1",
			"command line argument 2",
			"command line argument 3",
			"command line argument 4",
			"command line argument 5",
			"command line argument 6",
			"command line argument 7",
			"command line argument 8",
			"command line argument 9",
			"command line argument 10"
		};

	public final static String typeLongDesc[] = 
		{
			"-",
			"system date (variable)",
			"system date (fixed)",
			"start date range",
			"end date range",
			"Yesterday 00:00:00",
			"Yesterday 23:59:59",
			"Today 00:00:00",
			"Today 23:59:59",
			"Tomorrow 00:00:00",
			"Tomorrow 23:59:59",
			"First day of last month 00:00:00",
			"Last day of last month 23:59:59",
			"First day of this month 00:00:00",
			"Last day of this month 23:59:59",
			"First day of next month 00:00:00",
			"Last day of next month 23:59:59",
			"copy of step",
			"transformation name",
			"transformation file name",
			"User that modified the transformation last",
			"Date when the transformation was modified last",
			"transformation batch ID",
			"Hostname",
			"IP address",
			"command line argument 1",
			"command line argument 2",
			"command line argument 3",
			"command line argument 4",
			"command line argument 5",
			"command line argument 6",
			"command line argument 7",
			"command line argument 8",
			"command line argument 9",
			"command line argument 10"
		};
	
	private String fieldName[];
	private int    fieldType[];
	
	public SystemDataMeta()
	{
		super(); // allocate BaseStepMeta
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
    
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int count)
	{
		fieldName = new String[count];
		fieldType = new int   [count];
	}

	public Object clone()
	{
		SystemDataMeta retval = (SystemDataMeta)super.clone();

		int count=fieldName.length;
		
		retval.allocate(count);
				
		for (int i=0;i<count;i++)
		{
			retval.fieldName[i] = fieldName[i];
			retval.fieldType[i] = fieldType[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int count= XMLHandler.countNodes(fields, "field");
			String type;
			
			allocate(count);
					
			for (int i=0;i<count;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i] = XMLHandler.getTagValue(fnode, "name");
				type         = XMLHandler.getTagValue(fnode, "type");
				fieldType[i] = getType(type);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}
	
	public static final int getType(String type)
	{
		for (int i=0;i<type_desc.length;i++)
		{
			if (type_desc[i].equalsIgnoreCase(type)) return i;
			if (typeLongDesc[i].equalsIgnoreCase(type)) return i;
		}
		return 0;
	}
	
	public static final String getTypeDesc(int t)
	{
		if (t<0 || t>=type_desc.length) return null;
		return type_desc[t];
	}

	public void setDefault()
	{
		int count=0;
		
		allocate(count);

		for (int i=0;i<count;i++)
		{
			fieldName[i] = "field"+i;
			fieldType[i] = TYPE_SYSTEM_INFO_SYSTEM_DATE;
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		for (int i=0;i<fieldName.length;i++)
		{
			Value v;

			switch(fieldType[i])
			{
			case TYPE_SYSTEM_INFO_SYSTEM_START:      // All date values...
			case TYPE_SYSTEM_INFO_SYSTEM_DATE:  
			case TYPE_SYSTEM_INFO_DATE_FROM: 
			case TYPE_SYSTEM_INFO_DATE_TO: 
			case TYPE_SYSTEM_INFO_PREV_DAY_START: 
			case TYPE_SYSTEM_INFO_PREV_DAY_END: 
			case TYPE_SYSTEM_INFO_THIS_DAY_START: 
			case TYPE_SYSTEM_INFO_THIS_DAY_END: 
			case TYPE_SYSTEM_INFO_NEXT_DAY_START: 
			case TYPE_SYSTEM_INFO_NEXT_DAY_END: 
			case TYPE_SYSTEM_INFO_PREV_MONTH_START: 
			case TYPE_SYSTEM_INFO_PREV_MONTH_END: 
			case TYPE_SYSTEM_INFO_THIS_MONTH_START: 
			case TYPE_SYSTEM_INFO_THIS_MONTH_END: 
			case TYPE_SYSTEM_INFO_NEXT_MONTH_START: 
			case TYPE_SYSTEM_INFO_NEXT_MONTH_END: 
			case TYPE_SYSTEM_INFO_MODIFIED_DATE:
				v = new Value(fieldName[i], Value.VALUE_TYPE_DATE); 
				break;
				
			case TYPE_SYSTEM_INFO_COPYNR:
			case TYPE_SYSTEM_INFO_BATCH_ID:
				v = new Value(fieldName[i], Value.VALUE_TYPE_INTEGER);
				break;
				
			case TYPE_SYSTEM_INFO_TRANS_NAME :
			case TYPE_SYSTEM_INFO_FILENAME   : 
			case TYPE_SYSTEM_INFO_ARGUMENT_01: 
			case TYPE_SYSTEM_INFO_ARGUMENT_02: 
			case TYPE_SYSTEM_INFO_ARGUMENT_03: 
			case TYPE_SYSTEM_INFO_ARGUMENT_04: 
			case TYPE_SYSTEM_INFO_ARGUMENT_05: 
			case TYPE_SYSTEM_INFO_ARGUMENT_06: 
			case TYPE_SYSTEM_INFO_ARGUMENT_07: 
			case TYPE_SYSTEM_INFO_ARGUMENT_08: 
			case TYPE_SYSTEM_INFO_ARGUMENT_09: 
			case TYPE_SYSTEM_INFO_ARGUMENT_10: 
			case TYPE_SYSTEM_INFO_MODIFIED_USER:
			case TYPE_SYSTEM_INFO_HOSTNAME:
			case TYPE_SYSTEM_INFO_IP_ADDRESS:
				v=new Value(fieldName[i], Value.VALUE_TYPE_STRING);
				break;
			default: 
				v = new Value(fieldName[i], Value.VALUE_TYPE_NONE);
				break;
			}
			v.setOrigin(name);
			row.addValue(v);
		}
		return row;
	}

	public String getXML()
	{
		String retval="";

		retval+="    <fields>"+Const.CR;
		
		for (int i=0;i<fieldName.length;i++)
		{
			retval+="      <field>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name", fieldName[i]);
			retval+="        "+XMLHandler.addTagValue("type", type_desc[fieldType[i]]);
			retval+="        </field>"+Const.CR;
		}
		retval+="      </fields>"+Const.CR;

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
				fieldName[i] =          rep.getStepAttributeString(id_step, i, "field_name");
				fieldType[i] = getType( rep.getStepAttributeString(id_step, i, "field_type"));
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
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      getTypeDesc(fieldType[i]));
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}

	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Step is expecting nor reading info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "No input received from other steps.", stepMeta);
			remarks.add(cr);
		}
	}
    
    /**
     * Default a step doesn't use any arguments.
     * Implement this to notify the GUI that a window has to be displayed BEFORE launching a transformation.
     * You can also use this to specify certain Environment variable values.
     * 
     * @return A row of argument values. (name and optionally a default value)
     *         Put up to 10 values in the row for the possible 10 arguments.
     *         The name of the value is "1" through "10" for the 10 possible arguments.
     */
    public Row getUsedArguments()
    {
        Row row = new Row();
        
        // Put 10 values in the row for the possible 10 arguments.
        // Set the type to Value.VALUE_TYPE_NONE if it's not used!
        //
        for (int argNr=0;argNr<10;argNr++)
        {
            boolean found = false;
            for (int i=0;i<fieldName.length;i++)
            {
                if (fieldType[i]==TYPE_SYSTEM_INFO_ARGUMENT_01+argNr) found=true;
            }
            if (found)
            {
                Value value = new Value(""+(argNr+1), Value.VALUE_TYPE_STRING);
                row.addValue(value);
            }
        }
        
        return row;
    }

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new SystemDataDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SystemData(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SystemDataData();
	}

}
