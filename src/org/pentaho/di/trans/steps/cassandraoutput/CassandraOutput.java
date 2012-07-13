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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.cassandraoutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Class providing an output step for writing data to a cassandra table (column
 * family). Can create the specified column family (if it doesn't already exist)
 * and can update column family meta data.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class CassandraOutput extends BaseStep implements StepInterface {

  protected CassandraOutputMeta m_meta;
  protected CassandraOutputData m_data;

  public CassandraOutput(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {

    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  /** Column meta data and schema information */
  protected CassandraColumnMetaData m_cassandraMeta;

  /** Holds batch insert CQL statement */
  protected StringBuilder m_batchInsert;

  /** Current batch of rows to insert */
  protected List<Object[]> m_batch;

  /** The number of rows seen so far for this batch */
  protected int m_rowsSeen;

  /** The batch size to use */
  protected int m_batchSize = 100;

  /** The consistency to use - null means to use the cassandra default */
  protected String m_consistency = null;

  /** The name of the column family (table) to write to */
  protected String m_columnFamilyName;

  /** The index of the key field in the incoming rows */
  protected int m_keyIndex = -1;

  protected int m_cqlBatchInsertTimeout = 0;

  /** Default batch split factor */
  protected int m_batchSplitFactor = 10;

  protected void initialize(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {

    m_meta = (CassandraOutputMeta) smi;
    m_data = (CassandraOutputData) sdi;

    first = false;
    m_rowsSeen = 0;

    // Get the connection to Cassandra
    String hostS = environmentSubstitute(m_meta.getCassandraHost());
    String portS = environmentSubstitute(m_meta.getCassandraPort());
    String userS = m_meta.getUsername();
    String passS = m_meta.getPassword();
    String batchTimeoutS = environmentSubstitute(m_meta
        .getCQLBatchInsertTimeout());
    String batchSplitFactor = environmentSubstitute(m_meta.getCQLSubBatchSize());
    String schemaHostS = environmentSubstitute(m_meta.getSchemaHost());
    String schemaPortS = environmentSubstitute(m_meta.getSchemaPort());
    if (Const.isEmpty(schemaHostS)) {
      schemaHostS = hostS;
    }
    if (Const.isEmpty(schemaPortS)) {
      schemaPortS = portS;
    }

    if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
      userS = environmentSubstitute(userS);
      passS = environmentSubstitute(passS);
    }
    String keyspaceS = environmentSubstitute(m_meta.getCassandraKeyspace());
    m_columnFamilyName = environmentSubstitute(m_meta.getColumnFamilyName());
    String keyField = environmentSubstitute(m_meta.getKeyField());

    try {

      if (!Const.isEmpty(batchTimeoutS)) {
        try {
          m_cqlBatchInsertTimeout = Integer.parseInt(batchTimeoutS);
          if (m_cqlBatchInsertTimeout < 500) {
            logBasic(BaseMessages.getString(CassandraOutputMeta.PKG,
                "CassandraOutput.Message.MinimumTimeout"));
            m_cqlBatchInsertTimeout = 500;
          }
        } catch (NumberFormatException e) {
          logError(BaseMessages.getString(CassandraOutputMeta.PKG,
              "CassandraOutput.Error.CantParseTimeout"));
          m_cqlBatchInsertTimeout = 10000;
        }
      }

      if (!Const.isEmpty(batchSplitFactor)) {
        try {
          m_batchSplitFactor = Integer.parseInt(batchSplitFactor);
        } catch (NumberFormatException e) {
          logError(BaseMessages.getString(CassandraOutputMeta.PKG,
              "CassandraOutput.Error.CantParseSubBatchSize"));
        }
      }

      if (Const.isEmpty(hostS) || Const.isEmpty(portS)
          || Const.isEmpty(keyspaceS)) {
        throw new KettleException(BaseMessages.getString(
            CassandraOutputMeta.PKG,
            "CassandraOutput.Error.MissingConnectionDetails"));
      }

      if (Const.isEmpty(m_columnFamilyName)) {
        throw new KettleException(BaseMessages.getString(
            CassandraOutputMeta.PKG,
            "CassandraOutput.Error.NoColumnFamilySpecified"));
      }

      if (Const.isEmpty(keyField)) {
        throw new KettleException(BaseMessages.getString(
            CassandraOutputMeta.PKG,
            "CassandraOutput.Error.NoIncomingKeySpecified"));
      }

      // check that the specified key field is present in the incoming data
      m_keyIndex = getInputRowMeta().indexOfValue(keyField);
      if (m_keyIndex < 0) {
        throw new KettleException(BaseMessages.getString(
            CassandraOutputMeta.PKG, "CassandraOutput.Error.CantFindKeyField",
            keyField));
      }

      logBasic(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.ConnectingForSchemaOperations", schemaHostS,
          schemaPortS, keyspaceS));
      CassandraConnection connection = null;

      try {
        connection = openConnection(true);

        if (!CassandraColumnMetaData.columnFamilyExists(connection,
            m_columnFamilyName)) {
          if (m_meta.getCreateColumnFamily()) {
            // create the column family (table)
            boolean result = CassandraOutputData.createColumnFamily(connection,
                m_columnFamilyName, getInputRowMeta(), m_keyIndex,
                m_meta.getUseCompression());
            if (!result) {
              throw new KettleException(BaseMessages.getString(
                  CassandraOutputMeta.PKG,
                  "CassandraOutput.Error.NeedAtLeastOneFieldAppartFromKey"));
            }
          } else {
            throw new KettleException(BaseMessages.getString(
                CassandraOutputMeta.PKG,
                "CassandraOutput.Error.ColumnFamilyDoesNotExist",
                m_columnFamilyName, keyspaceS));
          }
        }

        // get the column family meta data

        logBasic(BaseMessages.getString(CassandraOutputMeta.PKG,
            "CassandraOutput.Message.GettingMetaData"));
        m_cassandraMeta = new CassandraColumnMetaData(connection,
            m_columnFamilyName);

        // check that we have at least one incoming field apart from the key
        if (CassandraOutputData.numFieldsToBeWritten(m_columnFamilyName,
            getInputRowMeta(), m_keyIndex, m_cassandraMeta,
            m_meta.getInsertFieldsNotInMeta()) < 2) {
          throw new KettleException(BaseMessages.getString(
              CassandraOutputMeta.PKG,
              "CassandraOutput.Error.NeedAtLeastOneFieldAppartFromKey"));
        }

        // output (downstream) is the same as input
        m_data.setOutputRowMeta(getInputRowMeta());

        String batchSize = environmentSubstitute(m_meta.getBatchSize());
        if (!Const.isEmpty(batchSize)) {
          try {
            m_batchSize = Integer.parseInt(batchSize);
          } catch (NumberFormatException e) {
            logError(BaseMessages.getString(CassandraOutputMeta.PKG,
                "CassandraOutput.Error.CantParseBatchSize"));
            m_batchSize = 100;
          }
        } else {
          throw new KettleException(BaseMessages.getString(
              CassandraOutputMeta.PKG, "CassandraOutput.Error.NoBatchSizeSet"));
        }

        if (m_meta.getUpdateCassandraMeta()) {
          // Update cassandra meta data for unknown incoming fields?

          CassandraOutputData.updateCassandraMeta(connection,
              m_columnFamilyName, getInputRowMeta(), m_keyIndex,
              m_cassandraMeta);
        }

        // Truncate (remove all data from) column family first?
        if (m_meta.getTruncateColumnFamily()) {
          CassandraOutputData.truncateColumnFamily(connection,
              m_columnFamilyName);

        }

        // Try to execute any apriori CQL commands?
        if (!Const.isEmpty(m_meta.getAprioriCQL())) {
          String aprioriCQL = environmentSubstitute(m_meta.getAprioriCQL());
          logBasic(BaseMessages.getString(CassandraOutputMeta.PKG,
              "CassandraOutput.Message.ExecutingAprioriCQL",
              m_columnFamilyName, aprioriCQL));

          CassandraOutputData.executeAprioriCQL(connection, aprioriCQL, log,
              m_meta.getUseCompression());
        }
      } finally {
        if (connection != null) {
          closeConnection(connection);
          connection = null;
        }
      }

      m_consistency = environmentSubstitute(m_meta.getConsistency());
      m_batchInsert = CassandraOutputData.newBatch(m_batchSize, m_consistency);
      m_batch = new ArrayList<Object[]>();

    } catch (Exception ex) {
      logError(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Error.InitializationProblem"), ex);
    }
  }

  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {

    Object[] r = getRow();

    if (r == null) {
      // no more output

      // flush the last batch
      if (m_rowsSeen > 0) {
        doBatch();
      }
      m_batchInsert = null;
      m_batch = null;

      setOutputDone();
      return false;
    }

    if (first) {
      initialize(smi, sdi);
    }

    m_batch.add(r);
    m_rowsSeen++;

    if (m_rowsSeen == m_batchSize) {
      doBatch();
    }

    return true;
  }

  protected void doBatch() throws KettleException {

    try {
      doBatch(m_batch);
    } catch (Exception e) {
      logError(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Error.CommitFailed", m_batchInsert.toString(), e));
      throw new KettleException(e.fillInStackTrace());
    }

    // ready for a new batch
    m_batch.clear();
    m_rowsSeen = 0;
  }

  protected void doBatch(List<Object[]> batch) throws Exception {
    // stopped?
    if (isStopped()) {
      logDebug(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.StoppedSkippingBatch"));
      return;
    }
    // ignore empty batch
    if (batch == null || batch.isEmpty()) {
      logDebug(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.SkippingEmptyBatch"));
      return;
    }
    // construct CQL and commit
    CassandraConnection connection = null;
    int size = batch.size();
    try {
      // construct CQL
      m_batchInsert = CassandraOutputData.newBatch(m_batchSize, m_consistency);
      int rowsAdded = 0;
      for (Object[] r : batch) {
        // add the row to the batch
        if (CassandraOutputData.addRowToBatch(m_batchInsert,
            m_columnFamilyName, getInputRowMeta(), m_keyIndex, r,
            m_cassandraMeta, m_meta.getInsertFieldsNotInMeta(), log)) {
          rowsAdded++;
        }
      }
      if (rowsAdded == 0) {
        logDebug(BaseMessages.getString(CassandraOutputMeta.PKG,
            "CassandraOutput.Message.SkippingEmptyBatch"));
        return;
      }
      CassandraOutputData.completeBatch(m_batchInsert);
      // commit
      connection = openConnection(false);
      logDetailed(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.CommittingBatch", m_columnFamilyName, ""
              + size));

      CassandraOutputData.commitBatch(m_batchInsert, connection,
          m_meta.getUseCompression(), m_cqlBatchInsertTimeout);
    } catch (Exception e) {
      closeConnection(connection);
      connection = null;
      logDetailed(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Error.FailedToInsertBatch", "" + size), e);

      logDetailed(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.WillNowTrySplittingIntoSubBatches"));

      // is it possible to divide and conquer?
      if (size == 1) {
        // single error row - found it!
        if (getStepMeta().isDoingErrorHandling()) {
          putError(getInputRowMeta(), batch.get(0), 1L, e.getMessage(), null,
              "ERR_INSERT01");
        }
      } else if (size > m_batchSplitFactor) {
        // split into smaller batches and try separately
        List<Object[]> subBatch = new ArrayList<Object[]>();
        while (batch.size() > m_batchSplitFactor) {
          while (subBatch.size() < m_batchSplitFactor && batch.size() > 0) {
            // remove from the right - avoid internal shifting
            subBatch.add(batch.remove(batch.size() - 1));
          }
          doBatch(subBatch);
          subBatch.clear();
        }
        doBatch(batch);
      } else {
        // try each row individually
        List<Object[]> subBatch = new ArrayList<Object[]>();
        while (batch.size() > 0) {
          subBatch.clear();
          // remove from the right - avoid internal shifting
          subBatch.add(batch.remove(batch.size() - 1));
          doBatch(subBatch);
        }
      }
    } finally {
      closeConnection(connection);
      connection = null;
    }
  }

  @Override
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }

    super.setStopped(stopped);
  }

  protected CassandraConnection openConnection(boolean forSchemaChanges)
      throws KettleException {
    // Get the connection to Cassandra
    String hostS = environmentSubstitute(m_meta.getCassandraHost());
    String portS = environmentSubstitute(m_meta.getCassandraPort());
    String userS = m_meta.getUsername();
    String passS = m_meta.getPassword();
    String timeoutS = environmentSubstitute(m_meta.getSocketTimeout());
    String schemaHostS = environmentSubstitute(m_meta.getSchemaHost());
    String schemaPortS = environmentSubstitute(m_meta.getSchemaPort());
    if (Const.isEmpty(schemaHostS)) {
      schemaHostS = hostS;
    }
    if (Const.isEmpty(schemaPortS)) {
      schemaPortS = portS;
    }

    if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
      userS = environmentSubstitute(userS);
      passS = environmentSubstitute(passS);
    }
    String keyspaceS = environmentSubstitute(m_meta.getCassandraKeyspace());

    CassandraConnection connection = null;

    try {
      if (Const.isEmpty(timeoutS)) {
        if (forSchemaChanges) {
          connection = CassandraOutputData.getCassandraConnection(schemaHostS,
              Integer.parseInt(schemaPortS), userS, passS);
        } else {
          connection = CassandraOutputData.getCassandraConnection(hostS,
              Integer.parseInt(portS), userS, passS);
        }
      } else {
        int sockTimeout = 30000;
        try {
          sockTimeout = Integer.parseInt(timeoutS);
        } catch (NumberFormatException e) {
          logError(BaseMessages.getString(CassandraOutputMeta.PKG,
              "CassandraOutput.Error.CantParseSocketTimeout"));
        }

        if (forSchemaChanges) {
          connection = CassandraOutputData.getCassandraConnection(schemaHostS,
              Integer.parseInt(schemaPortS), userS, passS, sockTimeout);
        } else {
          connection = CassandraOutputData.getCassandraConnection(hostS,
              Integer.parseInt(portS), userS, passS, sockTimeout);
        }
      }
      connection.setKeyspace(keyspaceS);
    } catch (Exception ex) {
      closeConnection(connection);
      throw new KettleException(ex.getMessage(), ex);
    }

    return connection;
  }

  protected void closeConnection(CassandraConnection conn) {
    if (conn != null) {
      logBasic(BaseMessages.getString(CassandraOutputMeta.PKG,
          "CassandraOutput.Message.ClosingConnection"));
      conn.close();
    }
  }
}
