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

package be.ibridge.kettle.trans.step.http;

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
 * Created on 10-dec-2006
 * 
 */

public class HTTPMeta extends BaseStepMeta implements StepMetaInterface
{
    /** URL / service to be called */
    private String  url;

    /** function arguments : fieldname*/
    private String  argumentField[];

    /** IN / OUT / INOUT */
    private String  argumentParameter[];

    /** function result: new value name */
    private String  fieldName;

    public HTTPMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the argument.
     */
    public String[] getArgumentField()
    {
        return argumentField;
    }

    /**
     * @param argument The argument to set.
     */
    public void setArgumentField(String[] argument)
    {
        this.argumentField = argument;
    }

    /**
     * @return Returns the argumentDirection.
     */
    public String[] getArgumentParameter()
    {
        return argumentParameter;
    }

    /**
     * @param argumentDirection The argumentDirection to set.
     */
    public void setArgumentParameter(String[] argumentDirection)
    {
        this.argumentParameter = argumentDirection;
    }

    /**
     * @return Returns the procedure.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param procedure The procedure to set.
     */
    public void setUrl(String procedure)
    {
        this.url = procedure;
    }

    /**
     * @return Returns the resultName.
     */
    public String getFieldName()
    {
        return fieldName;
    }

    /**
     * @param resultName The resultName to set.
     */
    public void setFieldName(String resultName)
    {
        this.fieldName = resultName;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode, databases);
    }

    public void allocate(int nrargs)
    {
        argumentField = new String[nrargs];
        argumentParameter = new String[nrargs];
    }

    public Object clone()
    {
        HTTPMeta retval = (HTTPMeta) super.clone();
        int nrargs = argumentField.length;

        retval.allocate(nrargs);

        for (int i = 0; i < nrargs; i++)
        {
            retval.argumentField[i] = argumentField[i];
            retval.argumentParameter[i] = argumentParameter[i];
        }

        return retval;
    }

    public void setDefault()
    {
        int i;
        int nrargs;

        nrargs = 0;

        allocate(nrargs);

        for (i = 0; i < nrargs; i++)
        {
            argumentField[i] = "arg" + i; //$NON-NLS-1$
            argumentParameter[i] = "arg"; //$NON-NLS-1$
        }

        fieldName = "result"; //$NON-NLS-1$
    }

    public Row getFields(Row r, String name, Row info)
    {
        Row row;
        if (r == null)
            row = new Row(); // give back values
        else
            row = r; // add to the existing row of values...

        if (!Const.isEmpty(fieldName))
        {
            Value v = new Value(fieldName, Value.VALUE_TYPE_STRING);
            v.setOrigin(name);
            row.addValue(v);
        }

        return row;
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("url", url)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    <lookup>" + Const.CR); //$NON-NLS-1$

        for (int i = 0; i < argumentField.length; i++)
        {
            retval.append("      <arg>" + Const.CR); //$NON-NLS-1$
            retval.append("        " + XMLHandler.addTagValue("name", argumentField[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        " + XMLHandler.addTagValue("parameter", argumentParameter[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        </arg>" + Const.CR); //$NON-NLS-1$
        }

        retval.append("      </lookup>" + Const.CR); //$NON-NLS-1$

        retval.append("    <result>" + Const.CR); //$NON-NLS-1$
        retval.append("      " + XMLHandler.addTagValue("name", fieldName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      </result>" + Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    private void readData(Node stepnode, ArrayList databases) throws KettleXMLException
    {
        try
        {
            int nrargs;

            url = XMLHandler.getTagValue(stepnode, "url"); //$NON-NLS-1$

            Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
            nrargs = XMLHandler.countNodes(lookup, "arg"); //$NON-NLS-1$

            allocate(nrargs);

            for (int i = 0; i < nrargs; i++)
            {
                Node anode = XMLHandler.getSubNodeByNr(lookup, "arg", i); //$NON-NLS-1$

                argumentField[i] = XMLHandler.getTagValue(anode, "name"); //$NON-NLS-1$
                argumentParameter[i] = XMLHandler.getTagValue(anode, "parameter"); //$NON-NLS-1$
            }

            fieldName = XMLHandler.getTagValue(stepnode, "result", "name"); // Optional, can be null //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("HTTPMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            url = rep.getStepAttributeString(id_step, "url"); //$NON-NLS-1$

            int nrargs = rep.countNrStepAttributes(id_step, "arg_name"); //$NON-NLS-1$
            allocate(nrargs);

            for (int i = 0; i < nrargs; i++)
            {
                argumentField[i] = rep.getStepAttributeString(id_step, i, "arg_name"); //$NON-NLS-1$
                argumentParameter[i] = rep.getStepAttributeString(id_step, i, "arg_parameter"); //$NON-NLS-1$
            }

            fieldName = rep.getStepAttributeString(id_step, "result_name"); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("HTTPMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "url", url); //$NON-NLS-1$

            for (int i = 0; i < argumentField.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", argumentField[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_parameter", argumentParameter[i]); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "result_name", fieldName); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("HTTPMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("HTTPMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("HTTPMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new HTTPDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new HTTP(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new HTTPData();
    }
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
