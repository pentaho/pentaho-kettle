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

package org.pentaho.di.trans.steps.dbproc;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

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

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
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
            argumentType[i] = ValueMetaInterface.TYPE_NUMBER;
        }

        resultName = "result"; //$NON-NLS-1$
        resultType = ValueMetaInterface.TYPE_NUMBER;
        autoCommit = true;
    }
    
    @Override
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
    	
        if (!Const.isEmpty(resultName))
        {
        	ValueMetaInterface v = new ValueMeta(resultName, resultType);
            v.setOrigin(name);
            r.addValueMeta(v);
        }
        
        for (int i = 0; i < argument.length; i++)
        {
            if (argumentDirection[i].equalsIgnoreCase("OUT")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                ValueMetaInterface v = new ValueMeta(argument[i], argumentType[i]);
                v.setOrigin(name);
                r.addValueMeta(v);
            }
        }

        return;
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer(500);

        retval.append("    ").append(XMLHandler.addTagValue("connection", database == null ? "" : database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    ").append(XMLHandler.addTagValue("procedure", procedure)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    <lookup>").append(Const.CR); //$NON-NLS-1$

        for (int i = 0; i < argument.length; i++)
        {
            retval.append("      <arg>").append(Const.CR); //$NON-NLS-1$
            retval.append("        ").append(XMLHandler.addTagValue("name", argument[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        ").append(XMLHandler.addTagValue("direction", argumentDirection[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(argumentType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("      </arg>").append(Const.CR); //$NON-NLS-1$
        }

        retval.append("    </lookup>").append(Const.CR); //$NON-NLS-1$

        retval.append("    <result>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("name", resultName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(resultType))); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    </result>").append(Const.CR); //$NON-NLS-1$

        retval.append("    ").append(XMLHandler.addTagValue("auto_commit", autoCommit)); //$NON-NLS-1$ //$NON-NLS-2$

        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
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
                argumentType[i] = ValueMeta.getType(XMLHandler.getTagValue(anode, "type")); //$NON-NLS-1$
            }

            resultName = XMLHandler.getTagValue(stepnode, "result", "name"); // Optional, can be null //$NON-NLS-1$
                                                                                // //$NON-NLS-2$
            resultType = ValueMeta.getType(XMLHandler.getTagValue(stepnode, "result", "type")); //$NON-NLS-1$ //$NON-NLS-2$
            autoCommit = !"N".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "auto_commit"));
        }
        catch (Exception e)
        {
            throw new KettleXMLException(Messages.getString("DBProcMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String,Counter> counters) throws KettleException
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
                argumentType[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "arg_type")); //$NON-NLS-1$
            }

            resultName = rep.getStepAttributeString(id_step, "result_name"); //$NON-NLS-1$
            resultType = ValueMeta.getType(rep.getStepAttributeString(id_step, "result_type")); //$NON-NLS-1$
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
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_type", ValueMeta.getTypeDesc(argumentType[i])); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "result_name", resultName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "result_type", ValueMeta.getTypeDesc(resultType)); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "auto_commit", autoCommit); //$NON-NLS-1$

            // Also, save the step-database relationship!
            if (database != null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
        }
        catch (Exception e)
        {
            throw new KettleException(Messages.getString("DBProcMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        if (database != null)
        {
            Database db = new Database(database);
            db.shareVariablesWith(transmeta);
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
                        ValueMetaInterface v = prev.searchValueMeta(argument[i]);
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
                            if (v.getType() != argumentType[i] && !(v.isNumeric() && ValueMeta.isNumeric(argumentType[i])))
                            {
                                error_found = true;
                                error_message += "\t\t" + argument[i] + Messages.getString("DBProcMeta.CheckResult.WrongTypeArguments", v.getTypeDesc(), ValueMeta.getTypeDesc(argumentType[i])) + Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            }
                        }
                    }
                    if (error_found)
                    {
                        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
                    }
                    else
                    {
                        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DBProcMeta.CheckResult.AllArgumentsOK"), stepMeta); //$NON-NLS-1$
                    }
                    remarks.add(cr);
                }
                else
                {
                    error_message = Messages.getString("DBProcMeta.CheckResult.CouldNotReadFields") + Const.CR; //$NON-NLS-1$
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
                    remarks.add(cr);
                }
            }
            catch (KettleException e)
            {
                error_message = Messages.getString("DBProcMeta.CheckResult.ErrorOccurred") + e.getMessage(); //$NON-NLS-1$
                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
        }
        else
        {
            error_message = Messages.getString("DBProcMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("DBProcMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("DBProcMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

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
