/*
 * $Header: TeraFastJobPlugin.java
 * $Revision:
 * $Date: 22.04.2009 11:48:51
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 * Copyright (c) 2009 Aschauer EDV.  All rights reserved. 
 * This software was developed by Aschauer EDV and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Terafast 
 * PDI Plugin. The Initial Developer is Aschauer EDV.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.terafast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.AbstractStep;
import org.pentaho.di.core.util.ConfigurableStreamLogger;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 * 
 */
public class TeraFast extends AbstractStep implements StepInterface {

	private static Class<?> PKG = TeraFastMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private TeraFastMeta meta;

    private Process process;

    private OutputStream fastload;

    private OutputStream dataFile;

    private List<Integer> columnSortOrder;

    private RowMetaInterface tableRowMeta;

    /**
     * Constructor.
     * 
     * @param stepMeta
     *            the stepMeta.
     * @param stepDataInterface
     *            the stepDataInterface.
     * @param copyNr
     *            the copyNr.
     * @param transMeta
     *            the transMeta.
     * @param trans
     *            the trans.
     */
    public TeraFast(final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr,
            final TransMeta transMeta, final Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    /**
     * Create the command line for a fastload process depending on the meta information supplied.
     * 
     * @return The string to execute.
     * 
     * @throws KettleException
     *             Upon any exception
     */
    public String createCommandLine() throws KettleException {
        if (StringUtils.isBlank(this.meta.getFastloadPath().getValue())) {
            throw new KettleException("Fastload path not set");
        }
        final StringBuilder builder = new StringBuilder();
        try {
            final FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(this.meta.getFastloadPath().getValue()));
            final String fastloadExec = KettleVFS.getFilename(fileObject);
            builder.append(fastloadExec);
        } catch (Exception e) {
            throw new KettleException("Error retrieving fastload application string", e);
        }
        // Add log error log, if set.
        if (StringUtils.isNotBlank(this.meta.getLogFile().getValue())) {
            try {
                FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(this.meta.getLogFile().getValue()));
                builder.append(" -e ");
                builder.append("\"" + KettleVFS.getFilename(fileObject) + "\"");
            } catch (Exception e) {
                throw new KettleException("Error retrieving logfile string", e);
            }
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface,
     *      org.pentaho.di.trans.step.StepDataInterface)
     */
    @Override
    public boolean init(final StepMetaInterface smi, final StepDataInterface sdi) {
        this.meta = (TeraFastMeta) smi;
        // this.data = (GenericStepData) sdi;
        return super.init(smi, sdi);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.BaseStep#processRow(org.pentaho.di.trans.step.StepMetaInterface,
     *      org.pentaho.di.trans.step.StepDataInterface)
     */
    @Override
    public boolean processRow(final StepMetaInterface smi, final StepDataInterface sdi) throws KettleException {
        this.meta = (TeraFastMeta) smi;
        // this.data = (GenericStepData) sdi;

        Object[] row = getRow();
        if (row == null) {

            IOUtils.closeQuietly(this.dataFile);

            this.execute();

            setOutputDone();

            try {
                logBasic("TeraFast.Log.WatingForFastload");
                final int exitVal = this.process.waitFor();
                if (exitVal != 0) {
                    setErrors(DEFAULT_ERROR_CODE);
                }
                logBasic(BaseMessages.getString(PKG, "TeraFast.Log.ExitValueFastloadPath", "" + exitVal));
            } catch (Exception e) {
                logError(BaseMessages.getString(PKG, "TeraFast.Log.ErrorInStep"), e);
                this.setDefaultError();
                stopAll();
            }

            return false;
        }

        if (this.first) {
            this.first = false;
            try {
                final File tempDataFile = new File(resolveFileName(this.meta.getDataFile().getValue()));
                this.dataFile = FileUtils.openOutputStream(tempDataFile);
            } catch (IOException e) {
                throw new KettleException("Cannot open data file [path=" + this.dataFile + "]", e);
            }

            // determine column sort order according to field mapping
            // thus the columns in the generated datafile are always in the same order and have the same size as in the
            // targetTable
            this.tableRowMeta = this.meta.getRequiredFields(this.getTransMeta());
            RowMetaInterface streamRowMeta = this.getTransMeta().getPrevStepFields(this.getStepMeta());
            this.columnSortOrder = new ArrayList<Integer>(this.tableRowMeta.size());
            for (int i = 0; i < this.tableRowMeta.size(); i++) {
                ValueMetaInterface column = this.tableRowMeta.getValueMeta(i);
                int tableIndex = this.meta.getTableFieldList().getValue().indexOf(column.getName());
                String streamField = this.meta.getStreamFieldList().getValue().get(tableIndex);
                this.columnSortOrder.add(streamRowMeta.indexOfValue(streamField));
            }
        }

        writeToDataFile(row);

        return true;
    }

    /**
     * Write a single row to the temporary data file.
     * 
     * @param row
     *            row entries
     * @throws KettleException
     *             ...
     */
    private void writeToDataFile(final Object[] row) throws KettleException {
        try {
            int tableCol = 0;
            for (Integer index : this.columnSortOrder) {
                int length = 0;
                if (this.tableRowMeta.getValueMeta(tableCol).getType() == ValueMetaInterface.TYPE_DATE) {
                    length = FastloadControlBuilder.DEFAULT_DATE_FORMAT.length();
                } else {
                    length = this.tableRowMeta.getValueMeta(tableCol)
                    .getLength();
                }
                byte[] rowData = new byte[length];
                System.arraycopy(row[index], 0, rowData, 0, length);
                if (((byte[]) row[index]).length < rowData.length) { // fillup padding with ' ' instead of 0
                    Arrays.fill(rowData, ((byte[]) row[index]).length, rowData.length, (byte) ' ');
                }
                String nullString = new String(rowData).trim();
                if (FastloadControlBuilder.DEFAULT_NULL_VALUE.equals(nullString)) {
                    rowData = String.format("%1$#" + rowData.length + "s", FastloadControlBuilder.DEFAULT_NULL_VALUE).getBytes();
                }
                this.dataFile.write(rowData);
                this.dataFile.write(FastloadControlBuilder.DATAFILE_COLUMN_SEPERATOR.getBytes());
                tableCol++;
            }
            this.dataFile.write(SystemUtils.LINE_SEPARATOR.getBytes());
        } catch (IOException e) {
            throw new KettleException("Cannot write data file [path=" + this.dataFile + "]", e);
        }
    }

    /**
     * Execute fastload.
     * 
     * @throws KettleException
     *             ...
     */
    public void execute() throws KettleException {
        if (this.meta.getTruncateTable().getValue()) {
            Database db = new Database(this, this.meta.getDbMeta());
            db.connect();
            db.truncateTable(this.meta.getTargetTable().getValue());
            db.commit();
            db.disconnect();
        }
        startFastLoad();

        if (this.meta.getUseControlFile().getValue()) {
            this.invokeLoadingControlFile();
        } else {
            this.invokeLoadingCommand();
        }
    }

    /**
     * Start fastload command line tool and initialize streams.
     * 
     * @throws KettleException
     *             ...
     */
    private void startFastLoad() throws KettleException {
        final String command = this.createCommandLine();
        this.logBasic("About to execute: " + command);
        try {
            this.process = Runtime.getRuntime().exec(command);
            new Thread(new ConfigurableStreamLogger(getLogChannel(), this.process.getErrorStream(), LogWriter.LOG_LEVEL_ERROR, "ERROR")).start();
            new Thread(new ConfigurableStreamLogger(getLogChannel(), this.process.getInputStream(), LogWriter.LOG_LEVEL_DETAILED, "OUTPUT")).start();
            this.fastload = this.process.getOutputStream();
        } catch (Exception e) {
            throw new KettleException("Error while setup: " + command, e);
        }
    }

    /**
     * Invoke loading with control file.
     * 
     * @throws KettleException
     *             ...
     */
    private void invokeLoadingControlFile() throws KettleException {
        File controlFile = null;
        final InputStream control;
        final String controlContent;
        try {
            controlFile = new File(resolveFileName(this.meta.getControlFile().getValue()));
            control = FileUtils.openInputStream(controlFile);
            controlContent = environmentSubstitute(FileUtils.readFileToString(controlFile));
        } catch (IOException e) {
            throw new KettleException("Cannot open control file [path=" + controlFile + "]", e);
        }
        try {
            IOUtils.write(controlContent, this.fastload);
            this.fastload.flush();
        } catch (IOException e) {
            throw new KettleException("Cannot pipe content of control file to fastload [path=" + controlFile + "]", e);
        } finally {
            IOUtils.closeQuietly(control);
            IOUtils.closeQuietly(this.fastload);
        }
    }

    /**
     * Invoke loading with loading commands.
     * 
     * @throws KettleException
     *             ...
     */
    private void invokeLoadingCommand() throws KettleException {
        final FastloadControlBuilder builder = new FastloadControlBuilder();
        builder.setSessions(this.meta.getSessions().getValue());
        builder.setErrorLimit(this.meta.getErrorLimit().getValue());
        builder.logon(this.meta.getDbMeta().getHostname(), this.meta.getDbMeta().getUsername(), this.meta.getDbMeta().getPassword());
        builder.setRecordFormat(FastloadControlBuilder.RECORD_VARTEXT);
        try {
            builder.define(this.meta.getRequiredFields(this.getTransMeta()), resolveFileName(this.meta.getDataFile().getValue()));
        } catch (Exception ex) {
            throw new KettleException("Error defining data file!", ex);
        }
        builder.show();
        builder.beginLoading(this.meta.getTargetTable().getValue());

        builder.insert(this.meta.getRequiredFields(this.getTransMeta()), this.meta.getTargetTable().getValue());
        builder.endLoading();
        builder.logoff();
        final String control = builder.toString();
        try {
            IOUtils.write(control, this.fastload);
            this.fastload.flush();
        } catch (IOException e) {
            throw new KettleException("Error while execution control command [controlCommand=" + control + "]", e);
        } finally {
            IOUtils.closeQuietly(this.fastload);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.BaseStep#dispose(org.pentaho.di.trans.step.StepMetaInterface,
     *      org.pentaho.di.trans.step.StepDataInterface)
     */
    @Override
    public void dispose(final StepMetaInterface smi, final StepDataInterface sdi) {
        this.meta = (TeraFastMeta) smi;
        // this.data = (GenericStepData) sdi;

        try {
            IOUtils.write(new FastloadControlBuilder().endLoading().toString(), this.fastload);
        } catch (IOException e) {
            logError("Unexpected error encountered while issuing END LOADING", e);
        }
        IOUtils.closeQuietly(this.dataFile);
        IOUtils.closeQuietly(this.fastload);
        try {
            int exitValue = this.process.waitFor();
            logDetailed("Exit value for the fastload process was : " + exitValue);
            if (exitValue != 0) {
                setErrors(DEFAULT_ERROR_CODE);
            }
        } catch (InterruptedException e) {
            setErrors(DEFAULT_ERROR_CODE);
            logError("Unexpected error encountered while finishing the fastload process", e);
        }

        super.dispose(smi, sdi);
    }

    /**
     * @param fileName
     *            the filename to resolve. may contain Kettle Environment variables.
     * @return the data file name.
     * @throws IOException
     *             ...
     */
    private String resolveFileName(final String fileName) throws KettleException {
        final FileObject fileObject = KettleVFS.getFileObject(environmentSubstitute(fileName));
        return KettleVFS.getFilename(fileObject);
    }
}
