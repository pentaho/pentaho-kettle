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


package be.ibridge.kettle.trans.step.rowsfromresult;

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

public class RowsFromResultMeta extends BaseStepMeta implements StepMetaInterface
{
    private String name[];
    private int    type[];
    private int    length[];
    private int    precision[];
    
	/**
     * @return Returns the length.
     */
    public int[] getLength()
    {
        return length;
    }

    /**
     * @param length The length to set.
     */
    public void setLength(int[] length)
    {
        this.length = length;
    }

    /**
     * @return Returns the name.
     */
    public String[] getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String[] name)
    {
        this.name = name;
    }

    /**
     * @return Returns the precision.
     */
    public int[] getPrecision()
    {
        return precision;
    }

    /**
     * @param precision The precision to set.
     */
    public void setPrecision(int[] precision)
    {
        this.precision = precision;
    }

    /**
     * @return Returns the type.
     */
    public int[] getType()
    {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(int[] type)
    {
        this.type = type;
    }

    public RowsFromResultMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
    public void allocate(int nrFields)
    {
        name = new String[nrFields];
        type = new int[nrFields];
        length = new int[nrFields];
        precision = new int[nrFields];
    }
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer();
        retval.append("    <fields>");
        for (int i=0;i<name.length;i++)
        {
            retval.append("      <field>");
            retval.append("        "+XMLHandler.addTagValue("name",      name[i]));
            retval.append("        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(type[i])));
            retval.append("        "+XMLHandler.addTagValue("length",    length[i]));
            retval.append("        "+XMLHandler.addTagValue("precision", precision[i]));
            retval.append("        </field>");
        }
        retval.append("      </fields>");

        return retval.toString();
    }
    
	private void readData(Node stepnode)
	{
        Node fields = XMLHandler.getSubNode(stepnode, "fields");
        int nrfields   = XMLHandler.countNodes(fields, "field");

        allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            Node line = XMLHandler.getSubNodeByNr(fields, "field", i);
            name     [i] = XMLHandler.getTagValue(line, "name");
            type     [i] = Value.getType(XMLHandler.getTagValue(line, "type"));
            length   [i] = Const.toInt(XMLHandler.getTagValue(line, "length"), -2);
            precision[i] = Const.toInt(XMLHandler.getTagValue(line, "precision"), -2);
        }

	}

	public void setDefault()
	{
        allocate(0);
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
                name[i]      =      rep.getStepAttributeString (id_step, i, "field_name");
                type[i]      = Value.getType( rep.getStepAttributeString (id_step, i, "field_type"));
                length[i]    = (int)rep.getStepAttributeInteger(id_step, i, "field_length");
                precision[i] = (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
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
            for (int i=0;i<name.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      name[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      Value.getTypeDesc(type[i]));
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    length[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", precision[i]);
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
        }
	}
    
    public Row getFields(Row r, String origin, Row info) throws KettleStepException
    {
        for (int i=0;i<this.name.length;i++)
        {
            Value v = new Value(name[i], type[i], length[i], precision[i]);
            v.setOrigin(origin);
            r.addValue(v);
        }
        return r;
    }

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step is expecting nor reading info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "No input received from other steps.", stepMeta);
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new RowsFromResultDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new RowsFromResult(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new RowsFromResultData();
	}

}
