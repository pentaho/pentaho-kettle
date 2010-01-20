/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * Copyright (c) 2010 DynamoBI Corporation.  All rights reserved.
 * This software was developed by DynamoBI Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Farrago 
 * Streaming Loader.  The Initial Developer is DynamoBI Corporation.
 * 
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.farragostreamingloader;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Perform main transformation.<br>
 * The logic is below:<br>
 * 1. Execute remote_rows udx thru jdbc (initialize database and generate
 * sql_statment on remote_rows).<br>
 * 2. When incoming row is the 1st row, cache the index of every field and
 * create header format object.<br>
 * 3. Send out data one row/per time thru socket based on TCP/IP.<br>
 * 4. Once all rows are sent out, close socket connection and return.<br>
 * 
 * @author Ray Zhang
 * @since Jan-05-2010
 */
public class FarragoStreamingLoader
    extends BaseStep
    implements StepInterface
{

    private static Class<?> PKG = FarragoStreamingLoaderMeta.class; 

    private FarragoStreamingLoaderMeta meta;

    private FarragoStreamingLoaderData data;
   
    public FarragoStreamingLoader(
        StepMeta stepMeta,
        StepDataInterface stepDataInterface,
        int copyNr,
        TransMeta transMeta,
        Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
        throws KettleException
    {

        meta = (FarragoStreamingLoaderMeta) smi;
        data = (FarragoStreamingLoaderData) sdi;

        try {

            Object[] r = getRow(); // Get row from input rowset & set row
            // busy!

            if (r == null) // no more input to be expected...
            {
                if (data.objOut != null) {

                    data.objOut.close();
                    if (data.client != null) {

                        data.client.close();
                    }
                }

                return false;
            }

            if (first) {

                first = false;

                data.keynrs = new int[meta.getFieldStreamForKeys().length
                    + meta.getFieldStreamForFields().length];
                data.format = new int[data.keynrs.length];

                for (int i = 0; i < meta.getFieldStreamForKeys().length; i++) {

                    data.keynrs[i] = getInputRowMeta().indexOfValue(
                        meta.getFieldStreamForKeys()[i]);
                    data.format[i] = getInputRowMeta().getValueMeta(
                        data.keynrs[i]).getLength();
                }
                int tmp_cnt = meta.getFieldStreamForKeys().length;
                for (int i = 0; i < meta.getFieldStreamForFields().length; i++)
                {

                    data.keynrs[tmp_cnt + i] = getInputRowMeta().indexOfValue(
                        meta.getFieldStreamForFields()[i]);
                    data.format[tmp_cnt + i] = getInputRowMeta().getValueMeta(
                        data.keynrs[i]).getLength();
                }
                logDebug(data.format.toString());

                // Create head format object.
                // TODO: I think we need to re-sign data structure for header. 
                List<Object> header = new ArrayList<Object>();
                header.add("1"); // version
                List<Integer> format = new ArrayList<Integer>();
                for (int i = 0; i < data.format.length; i++) {

                    format.add( data.format[i] );
                }
                header.add(format);

                data.objOut.writeObject(header);
            }

            List<Object> entity = new ArrayList<Object>();

            for (int i = 0; i < data.keynrs.length; i++) {

                int index = data.keynrs[i];
                ValueMetaInterface valueMeta = getInputRowMeta().getValueMeta(
                    index);
                Object valueData = r[index];

                if (valueData != null) {
                    switch (valueMeta.getType()) {
                    case ValueMetaInterface.TYPE_STRING:

                    	logRowlevel(valueMeta.getString(valueData) + ":"
                            + valueMeta.getLength() + ":"
                            + valueMeta.getTypeDesc());
                        entity.add(valueMeta.getString(valueData));

                        break;
                    case ValueMetaInterface.TYPE_INTEGER:

                        logRowlevel(valueMeta.getInteger(valueData)
                            + ":" + valueMeta.getLength() + ":"
                            + valueMeta.getTypeDesc());
                        entity.add(valueMeta.getInteger(valueData));
                        break;
                    case ValueMetaInterface.TYPE_DATE:

                        Date date = valueMeta.getDate(valueData);

                        if (log.isRowLevel()) logRowlevel(XMLHandler.date2string(date) + ":" + valueMeta.getLength());
                        entity.add(date);
                        break;
                    case ValueMetaInterface.TYPE_BOOLEAN:

                    	logRowlevel(Boolean.toString(valueMeta.getBoolean(valueData))
                            + ":" + valueMeta.getLength());
                        entity.add(valueMeta.getBoolean(valueData));
                        break;
                    }
                }

            }
            
            data.objOut.writeObject(entity);
            
            return true;
        } catch (Exception e) {
            logError(BaseMessages.getString(
                PKG,
                "FarragoStreamingLoader.Log.ErrorInStep"), e); //$NON-NLS-1$
            setErrors(1);
            stopAll();
            setOutputDone(); // signal end to receiver(s)
            return false;
        }
    }

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (FarragoStreamingLoaderMeta) smi;
        data = (FarragoStreamingLoaderData) sdi;

        if (super.init(smi, sdi)) {

            try {

                // 1. Initialize databases connection.
                logBasic("Connecting to LucidDB...");

                data.db = new Database(this, meta.getDatabaseMeta());
                data.db.shareVariablesWith(this);
                // Connect to the database
                if (getTransMeta().isUsingUniqueConnections()) {
                    synchronized (getTrans()) {
                        data.db.connect(
                            getTrans().getThreadName(),
                            getPartitionID());
                    }
                } else {
                    data.db.connect(getPartitionID());
                }

                logBasic("Connected to LucidDB");

                logBasic("Preparing sql statements: " + Const.CR
                    + meta.getSql_statement());

                String sql = meta.getSql_statement();
                PreparedStatement ps = data.db.prepareSQL(sql);

                logBasic("Executing sql statements...");

                data.sqlRunner = new SqlRunner(data, ps);
                data.sqlRunner.start();

                logBasic("Romote rows is up now...");
                
                logDebug("Sleeping for 1second");
                Thread.sleep(1000);

                logBasic("Initialize local socket connection...");
                logDebug("Parameters for socket: Host: " + meta.getHost()
                    + " Port: " + meta.getPort());
                data.client = new Socket(
                    meta.getHost(),
                    Integer.valueOf(meta.getPort()));

                data.objOut = new ObjectOutputStream(
                    data.client.getOutputStream());

                logBasic("Local socket connection is ready");

            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logError(e.getMessage());
                return false;
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logError(e.getMessage());
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logError(e.getMessage());
                return false;
            } catch (KettleDatabaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logError(e.getMessage());
                return false;
            } catch (Exception e) {
                
                e.printStackTrace();
                logError(e.getMessage());
                return false;
                
            }

            return true;
        }
        return false;
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (FarragoStreamingLoaderMeta) smi;
        data = (FarragoStreamingLoaderData) sdi;

        // Close the output streams if still needed.
        //
        try {
            // Stop the SQL execution thread
            if (data.sqlRunner != null) {
                data.sqlRunner.join();
                data.sqlRunner = null;
            }
            // And finally, release the database connection
            if (data.db != null) {
                data.db.disconnect();
                data.db = null;
            }
        } catch (Exception e) {
            setErrors(1L);
            logError(
                "Unexpected error encountered while closing the client connection",
                e);
        }

        super.dispose(smi, sdi);
    }

    //
    // Run is were the action happens!
    //
    public void run()
    {
        BaseStep.runStepThread(this, meta, data);
    }

    static class SqlRunner
        extends Thread
    {
        private FarragoStreamingLoaderData data;

        private PreparedStatement ps;

        private SQLException ex;

        List<String> warnings;

        SqlRunner(FarragoStreamingLoaderData data, PreparedStatement ps)
        {
            this.data = data;
            this.ps = ps;
            warnings = new ArrayList<String>();
        }

        public void run()
        {
            try {
                // TODO cross-check result against actual
                // number
                // of rows sent.
                ps.executeUpdate();

                // Pump out any warnings and save them.
                SQLWarning warning = ps.getWarnings();
                while (warning != null) {
                    warnings.add(warning.getMessage());
                    warning = warning.getNextWarning();
                }
            } catch (SQLException ex) {
                this.ex = ex;
            } finally {
                try {
                    data.db.closePreparedStatement(ps);
                } catch (KettleException ke) {
                    // not much we can do with this
                } finally {
                    ps = null;
                }
            }
        }

        void checkExcn()
            throws SQLException
        {
            // This is called from the main thread context to rethrow any
            // saved
            // excn.
            if (ex != null) {
                throw ex;
            }
        }
    }
}
