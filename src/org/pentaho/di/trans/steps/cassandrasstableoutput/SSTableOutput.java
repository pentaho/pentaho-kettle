/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.cassandrasstableoutput;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Output step for writing Cassandra SSTables (sorted-string tables).
 * 
 * @author Rob Turner (robert{[at]}robertturner{[dot]}com{[dot]}au)
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class SSTableOutput extends BaseStep implements StepInterface {

  protected SSTableOutputMeta m_meta;
  protected SSTableOutputData m_data;

  public SSTableOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {

    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  /** The number of rows seen so far for this batch */
  protected int rowsSeen;

  /** The directory to output to */
  protected String directory;

  /** The keyspace to use */
  protected String keyspace;

  /** The name of the column family (table) to write to */
  protected String columnFamily;

  /** The key field used to determine unique keys (IDs) for rows */
  protected String keyField;

  /** Size (MB) of write buffer */
  protected String bufferSize;

  /** Writes the SSTable output */
  protected SSTableWriter writer;

  /** Used to determine input fields */
  protected RowMetaInterface inputMetadata;

  /** List of field names (optimization) */
  private String[] fieldNames;

  /** List of field indices (optimization) */
  private int[] fieldValueIndices;

  private void initialize(StepMetaInterface smi, StepDataInterface sdi)
      throws Exception {
    first = false;
    rowsSeen = 0;
    m_meta = (SSTableOutputMeta) smi;
    m_data = (SSTableOutputData) sdi;
    inputMetadata = getInputRowMeta();

    String yamlPath = environmentSubstitute(m_meta.getYamlPath());
    if (Const.isEmpty(yamlPath)) {
      throw new Exception(BaseMessages.getString(SSTableOutputMeta.PKG,
          "SSTableOutput.Error.NoPathToYAML"));
    }
    logBasic(BaseMessages.getString(SSTableOutputMeta.PKG,
        "SSTableOutput.Message.YAMLPath", yamlPath));

    System.setProperty("cassandra.config", "file:" + yamlPath);

    directory = environmentSubstitute(m_meta.getDirectory());
    keyspace = environmentSubstitute(m_meta.getCassandraKeyspace());
    columnFamily = environmentSubstitute(m_meta.getColumnFamilyName());
    keyField = environmentSubstitute(m_meta.getKeyField());
    bufferSize = environmentSubstitute(m_meta.getBufferSize());
    if (Const.isEmpty(columnFamily)) {
      throw new KettleException(BaseMessages.getString(SSTableOutputMeta.PKG,
          "SSTableOutput.Error.NoColumnFamilySpecified"));
    }
    if (Const.isEmpty(keyField)) {
      throw new KettleException(BaseMessages.getString(SSTableOutputMeta.PKG,
          "SSTableOutput.Error.NoKeySpecified"));
    }
    // what are the fields? where are they?
    fieldNames = inputMetadata.getFieldNames();
    fieldValueIndices = new int[fieldNames.length];
    for (int i = 0; i < fieldNames.length; i++) {
      fieldValueIndices[i] = inputMetadata.indexOfValue(fieldNames[i]);
    }
    // create/init writer
    if (writer != null) {
      writer.close();
    }
    writer = new SSTableWriter();
    writer.setDirectory(directory);
    writer.setKeyspace(keyspace);
    writer.setColumnFamily(columnFamily);
    writer.setKeyField(keyField);
    writer.setBufferSize(Integer.parseInt(bufferSize));
    writer.init();
  }

  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {
    // still processing?
    if (isStopped()) {
      return false;
    }
    Object[] r = getRow();
    try {
      if (r == null) {
        // no more output - clean up/close connections
        setOutputDone();
        closeWriter();
        return false;
      }
      if (first) {
        initialize(smi, sdi);
      }
      // create record
      Map<String, Object> record = new HashMap<String, Object>();
      for (int i = 0; i < fieldNames.length; i++) {
        Object value = r[fieldValueIndices[i]];
        if (SSTableWriter.isNull(value)) {
          continue;
        }
        record.put(fieldNames[i], value);
      }
      // write it
      writer.processRow(record);
    } catch (Exception e) {
      logError(BaseMessages.getString(SSTableOutputMeta.PKG,
          "SSTableOutput.Error.FailedToProcessRow"), e);
      // single error row - found it!
      putError(getInputRowMeta(), r, 1L, e.getMessage(), null,
          "ERR_SSTABLE_OUTPUT_01");
    }

    // error will occur after adding it
    return true;
  }

  @Override
  public void setStopped(boolean stopped) {
    super.setStopped(stopped);
    if (stopped) {
      closeWriter();
    }
  }

  public void closeWriter() {
    if (writer != null) {
      try {
        writer.close();
        writer = null;
      } catch (Exception e) {
        // YUM!!
        logError(BaseMessages.getString(SSTableOutputMeta.PKG,
            "SSTableOutput.Error.FailedToCloseWriter"), e);
      }
    }
  }
}
