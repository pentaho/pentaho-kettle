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

package org.pentaho.di.trans.steps.luciddbstreamingloader;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
public class LucidDBStreamingLoader
    extends BaseStep
    implements StepInterface
{

    private static Class<?> PKG = LucidDBStreamingLoaderMeta.class;

    private LucidDBStreamingLoaderMeta meta;

    private LucidDBStreamingLoaderData data;

    public LucidDBStreamingLoader(
        StepMeta stepMeta,
        StepDataInterface stepDataInterface,
        int copyNr,
        TransMeta transMeta,
        Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    // When user want to stop it, it will execute logic here.
    public void stopRunning(StepMetaInterface smi, StepDataInterface sdi)
        throws KettleException
    {
        meta = (LucidDBStreamingLoaderMeta) smi;
        data = (LucidDBStreamingLoaderData) sdi;

        if (data.objOut != null) {

            try {
                data.objOut.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (data.client != null) {

                try {
                    data.client.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        try {
            data.sqlRunner.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
        throws KettleException
    {

        meta = (LucidDBStreamingLoaderMeta) smi;
        data = (LucidDBStreamingLoaderData) sdi;

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
                data.format = new String[data.keynrs.length];

                for (int i = 0; i < meta.getFieldStreamForKeys().length; i++) {

                    data.keynrs[i] = getInputRowMeta().indexOfValue(
                        meta.getFieldStreamForKeys()[i]);
                    
                    data.format[i] = getInputRowMeta().getValueMeta(
                        data.keynrs[i]).getTypeDesc().toUpperCase();

                    
                }
                int tmp_cnt = meta.getFieldStreamForKeys().length;
                for (int i = 0; i < meta.getFieldStreamForFields().length; i++)
                {

                    data.keynrs[tmp_cnt + i] = getInputRowMeta().indexOfValue(
                        meta.getFieldStreamForFields()[i]);
                   
                    data.format[tmp_cnt + i] = getInputRowMeta().getValueMeta(
                        data.keynrs[i]).getTypeDesc().toUpperCase();

                }
                if (isDetailed())
                    logDetailed(data.format.toString());

                // Create head format object.

                List<Object> header = new ArrayList<Object>();
                header.add("1"); // version
                List<String> format = new ArrayList<String>();
                for (int i = 0; i < data.format.length; i++) {
                    format.add(data.format[i]);
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

                        if (log.isRowLevel())
                            logRowlevel(valueMeta.getString(valueData) + ":"
                                + valueMeta.getLength() + ":"
                                + valueMeta.getTypeDesc());
                        entity.add(valueMeta.getString(valueData));

                        break;
                    case ValueMetaInterface.TYPE_INTEGER:

                        if (log.isRowLevel())
                            logRowlevel(valueMeta.getInteger(valueData) + ":"
                                + valueMeta.getLength() + ":"
                                + valueMeta.getTypeDesc());
                        entity.add(valueMeta.getInteger(valueData));
                        break;
                    case ValueMetaInterface.TYPE_DATE:

                        Date date = valueMeta.getDate(valueData);

                        if (log.isRowLevel())
                            logRowlevel(XMLHandler.date2string(date) + ":"
                                + valueMeta.getLength());
                        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                        entity.add(sqlDate);
                        break;
                    case ValueMetaInterface.TYPE_BOOLEAN:

                        if (log.isRowLevel())
                            logRowlevel(Boolean.toString(valueMeta.getBoolean(valueData))
                                + ":" + valueMeta.getLength());
                        entity.add(valueMeta.getBoolean(valueData));
                        break;
                    }
                }

            }

            data.objOut.writeObject(entity);
            data.objOut.reset();
            data.objOut.flush();

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
        meta = (LucidDBStreamingLoaderMeta) smi;
        data = (LucidDBStreamingLoaderData) sdi;

        if (super.init(smi, sdi)) {

            try {

                // 1. Initialize databases connection.
                if (log.isDebug())
                    logDebug("Connecting to LucidDB...");

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

                if (log.isDebug())
                    logDebug("Connected to LucidDB");

                if (log.isDebug())
                    logDebug("Preparing sql statements: " + Const.CR
                        + meta.getSql_statement());

                String sql = meta.getSql_statement();
                PreparedStatement ps = data.db.prepareSQL(sql);

                if (log.isDebug())
                    logDebug("Executing sql statements...");

                data.sqlRunner = new SqlRunner(data, ps);
                data.sqlRunner.start();

                if (log.isDebug())
                    logDebug("Remote rows is up now...");

                if (log.isDebug())
                    logDebug("Sleeping for 1second");
                Thread.sleep(1000);

                if (log.isDebug())
                    logDebug("Initialize local socket connection...");
                if (log.isDebug())
                    logDebug("Parameters for socket: Host: " + meta.getHost()
                        + " Port: " + meta.getPort());
                int try_cnt = 0;
                // Add a check whether remote rows is up.
                // If it is not up, it will sleep 5 second and then try to
                // connect.
                // Totally, we will try 5 times.
                while (true) {

                    try {

                        data.client = new Socket(
                            meta.getHost(),
                            Integer.valueOf(meta.getPort()));
                        data.objOut = new ObjectOutputStream(
                            data.client.getOutputStream());

                        if (log.isDebug())
                            logDebug("Local socket connection is ready");                        

                        break;

                    } catch (SocketException se) {

                        if (try_cnt < 5) {

                            logBasic("Local socket connection is not ready, so try to connect in 5 second");
                            Thread.sleep(5000);
                            data.client = null;
                            try_cnt++;
                        } else {

                            throw new KettleException(
                                "Fatal Error: Remote_rows UDX can't be connected! Please check...");
                        }

                    } catch (Exception ex) {

                        throw ex;
                    }

                }

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
        meta = (LucidDBStreamingLoaderMeta) smi;
        data = (LucidDBStreamingLoaderData) sdi;

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


    static class SqlRunner
        extends Thread
    {
        private LucidDBStreamingLoaderData data;

        private PreparedStatement ps;

        private SQLException ex;

        List<String> warnings;

        SqlRunner(LucidDBStreamingLoaderData data, PreparedStatement ps)
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
