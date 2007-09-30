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

package be.ibridge.kettle.trans.step.dbproc;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
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
 * Created on 26-apr-2003
 * 
 */

public class DBProcMeta extends BaseStepMeta implements StepMetaInterface
{
    /** database connection */
    private DatabaseMeta database;

    /** proc.-name to be called */
    private String       procedure;

    /** function arguments */
    private String       argument[];

    /** IN / OUT / INOUT */
    private String       argumentDirection[];

    /** value type for OUT */
    private int          argumentType[];

    /** function result: new value name */
    private String       resultName;

    /** function result: new value type */
    private int          resultType;

    /** The flag to set auto commit on or off on the connection */
    private boolean      autoCommit;

    public DBProcMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the argument.
     */
    public String[] getArgument()
    {
        return argument;
    }

    /**
     * @param argument The argument to set.
     */
    public void setArgument(String[] argument)
    {
        this.argument = argument;
    }

    /**
     * @return Returns the argumentDirection.
     */
    public String[] getArgumentDirection()
    {
        return argumentDirection;
    }

    /**
     * @param argumentDirection The argumentDirection to set.
     */
    public void setArgumentDirection(String[] argumentDirection)
    {
        this.argumentDirection = argumentDirection;
    }

    /**
     * @return Returns the argumentType.
     */
    public int[] getArgumentType()
    {
        return argumentType;
    }

    /**
     * @param argumentType The argumentType to set.
     */
    public void setArgumentType(int[] argumentType)
    {
        this.argumentType = argumentType;
    }

    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabase()
    {
        return database;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabase(DatabaseMeta database)
    {
        this.database = database;
    }

    /**
     * @return Returns the procedure.
     */
    public String getProcedure()
    {
        return procedure;
    }

    /**
     * @param procedure The procedure to set.
     */
    public void setProcedure(String procedure)
    {
        this.procedure = procedure;
    }

    /**
     * @return Returns the resultName.
     */
    public String getResultName()
    {
        return resultName;
    }

    /**
     * @param resultName The resultName to set.
     */
    public void setResultName(String resultName)
    {
        this.resultName = resultName;
    }

    /**
     * @return Returns the resultType.
     */
    public int getResultType()
    {
        return resultType;
    }

    /**
     * @param resultType The resultType to set.
     */
    public void setResultType(int resultType)
    {
        this.resultType = resultType;
    }

    /**
     * @return Returns the autoCommit.
     */
    public boolean isAutoCommit()
    {
        return autoCommit;
    }

    /**
     * @param autoCommit The autoCommit to set.
     */
    public void setAutoCommit(boolean autoCommit)
    {
        this.autoCommit = autoCommit;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        readData(stepnode, databases);
    }

    public void allocate(int nrargs)
    {
        argument = new String[nrargs];
        argumentDirection = new String[nrargs];
        argumentType = new int[nrargs];
    }

    public Object clone()
    {
        DBProcMeta retval = (DBProcMeta) super.clone();
        int nrargs = argument.length;

        retval.allocate(nrargs);

        for (int i = 0; i < nrargs; i++)
        {
            retval.argument[i] = argument[i];
            retval.argumentDirection[i] = argumentDirection[i];
            retval.argumentType[i] = argumentType[i];
        }

        return retval;
    }

    public void setDefault()
    {
        int i;
        int nrargs;

        database = null;

        nrargs = 0;

        allocate(nrargs);

        for (i = 0; i < nrargs; i++)
        {
            argument[i] = "arg" + i; //$NON-NLS-1$
            argumentDirection[i] = "IN"; //$NON-NLS-1$
            argumentType[i] = Value.VALUE_TYPE_NUMBER;
        }

        resultName = "result"; //$NON-NLS-1$
        resultType = Value.VALUE_TYPE_NUMBER;
        autoCommit = true;
    }

    public Row getFields(Row r, String name, Row info)
    {
        Row row;
        if (r == null)
            row = new Row(); // give back values
        else
            row = r; // add to the existing row of values...

        int i;
        for (i = 0; i < argument.length; i++)
        {
            if (argumentDirection[i].equalsIgnoreCase("OUT") || argumentDirection[i].equalsIgnoreCase("INOUT")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                Value v = new Value(argument[i], argumentType[i]);
                v.setOrigin(name);
                row.addValue(v);
            }
        }
        if (!Const.isEmpty(resultName))
        {
            Value v = new Value(resultName, resultType);
            v.setOrigin(name);
            row.addValue(v);
        }

        return row;
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("connection", database == null ? "" : database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    " + XMLHandler.addTagValue("procedure", procedure)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    <lookup>" + Const.CR); //$NON-NLS-1$

        for (int i = 0; i < argument.length; i++)
        {
            retval.append("      <arg>" + Const.CR); //$NON-NLS-1$
            retval.append("        " + XMLHandler.addTagValue("name", argument[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        " + XMLHandler.addTagValue("direction", argumentDirection[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        " + XMLHandler.addTagValue("type", Value.getTypeDesc(argumentType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        </arg>" + Const.CR); //$NON-NLS-1$
        }

        retval.append("      </lookup>" + Const.CR); //$NON-NLS-1$

        retval.append("    <result>" + Const.CR); //$NON-NLS-1$
        retval.append("      " + XMLHandler.addTagValue("name", resultName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      " + XMLHandler.addTagValue("type", Value.getTypeDesc(resultType))); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      </result>" + Const.CR); //$NON-NLS-1$

        retval.append("    " + XMLHandler.addTagValue("auto_commit", autoCommit)); //$NON-NLS-1$ //$NON-NLS-2$

        return retval.toString();
    }

    private void readData(Node stepnode, ArrayList databases) throws KettleXMLException
    {
        try
        {
            int i;
            int nrargs;

            String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
            database = DatabaseMeta.findDatabase(databases, con);
            procedure = XMLHandler.getTagValue(stepnode, "procedure"); //$NON-NLS-1$

            Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
            nrargs = XMLHandler.countNodes(lookup, "arg"); //$NON-NLS-1$

            allocate(nrargs);

            for (i = 0; i < nrargs; i++)
            {
                Node anode = XMLHandler.getSubNodeByNr(lookup, "arg", i); //$NON-NLS-1$

                argument[i] = XMLHandler.getTagValue(anode, "name"); //$NON-NLS-1$
                argumentDirection[i] = XMLHandler.getTagValue(anode, "direction"); //$NON-NLS-1$
                argumentType[i] = Value.getType(XMLHandler.getTagValue(anode, "type")); //$NON-NLS-1$
            }

            resultName = XMLHandler.getTagValue(stepnode, "result", "name"); // Optional, can be null //$NON-NLS-1$
                                                                                // //$NON-NLS-2$
            resultType = Value.getType(XMLHandler.getTagValue(stepnode, "result", "type")); //$NON-NLS-1$ //$NON-NLS-2$
            autoCommit = !"N".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "auto_commit"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("DBProcMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters) throws KettleException
    {
        try
        {
            long id_connection = rep.getStepAttributeInteger(id_step, "id_connection"); //$NON-NLS-1$
            database = DatabaseMeta.findDatabase(databases, id_connection);
            procedure = rep.getStepAttributeString(id_step, "procedure"); //$NON-NLS-1$

            int nrargs = rep.countNrStepAttributes(id_step, "arg_name"); //$NON-NLS-1$
            allocate(nrargs);

            for (int i = 0; i < nrargs; i++)
            {
                argument[i] = rep.getStepAttributeString(id_step, i, "arg_name"); //$NON-NLS-1$
                argumentDirection[i] = rep.getStepAttributeString(id_step, i, "arg_direction"); //$NON-NLS-1$
                argumentType[i] = Value.getType(rep.getStepAttributeString(id_step, i, "arg_type")); //$NON-NLS-1$
            }

            resultName = rep.getStepAttributeString(id_step, "result_name"); //$NON-NLS-1$
            resultType = Value.getType(rep.getStepAttributeString(id_step, "result_type")); //$NON-NLS-1$
            autoCommit = rep.getStepAttributeBoolean(id_step, 0, "auto_commit", true);
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("DBProcMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "id_connection", database == null ? -1 : database.getID()); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "procedure", procedure); //$NON-NLS-1$

            for (int i = 0; i < argument.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", argument[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_direction", argumentDirection[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_type", Value.getTypeDesc(argumentType[i])); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "result_name", resultName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "result_type", Value.getTypeDesc(resultType)); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "auto_commit", autoCommit); //$NON-NLS-1$

            // Also, save the step-database relationship!
            if (database != null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("DBProcMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
    {
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        if (database != null)
        {
            Database db = new Database(database);
            try
            {
                db.connect();

                // Look up fields in the input stream <prev>
                if (prev != null && prev.size() > 0)
                {
                    boolean first = true;
                    error_message = ""; //$NON-NLS-1$
                    boolean error_found = false;

                    for (int i = 0; i < argument.length; i++)
                    {
                        Value v = prev.searchValue(argument[i]);
                        if (v == null)
                        {
                            if (first)
                            {
                                first = false;
                                error_message += Messages.getString("DBProcMeta.CheckResult.MissingArguments") + Const.CR; //$NON-NLS-1$
                            }
                            error_found = true;
                            error_message += "\t\t" + argument[i] + Const.CR; //$NON-NLS-1$
                        }
                        else
                        // Argument exists in input stream: same type?
                        {
                            if (v.getType() != argumentType[i] && !(v.isNumeric() && Value.isNumeric(argumentType[i])))
                            {
                                error_found = true;
                                error_message += "\t\t" + argument[i] + Messages.getString("DBProcMeta.CheckResult.WrongTypeArguments", v.getTypeDesc(), Value.getTypeDesc(argumentType[i])) + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            }
                        }
                    }
                    if (error_found)
                    {
                        cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                    }
                    else
                    {
                        cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DBProcMeta.CheckResult.AllArgumentsOK"), stepMeta); //$NON-NLS-1$
                    }
                    remarks.add(cr);
                }
                else
                {
                    error_message = Messages.getString("DBProcMeta.CheckResult.CouldNotReadFields") + Const.CR; //$NON-NLS-1$
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                    remarks.add(cr);
                }
            }
            catch (KettleException e)
            {
                error_message = Messages.getString("DBProcMeta.CheckResult.ErrorOccurred") + e.getMessage(); //$NON-NLS-1$
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
        }
        else
        {
            error_message = Messages.getString("DBProcMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DBProcMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("DBProcMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }

    public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
    {
        return new DBProcDialog(shell, info, transMeta, name);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new DBProc(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new DBProcData();
    }

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (database != null)
        {
            return new DatabaseMeta[] { database };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
