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

package org.pentaho.di.trans.steps.hbaseoutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

/**
 * Class providing an output step for writing data to an HBase table according
 * to meta data column/type mapping info stored in a separate HBase table called
 * "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping for details on the
 * meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HBaseOutput extends BaseStep implements StepInterface {

  protected HBaseOutputMeta m_meta;
  protected HBaseOutputData m_data;

  public HBaseOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {

    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  /** Configuration object for connecting to HBase */
  protected HBaseConnection m_hbAdmin;

  /** Byte utilities */
  protected HBaseBytesUtilShim m_bytesUtil;

  /** The mapping admin object for interacting with mapping information */
  protected MappingAdmin m_mappingAdmin;

  /** The mapping information to use in order to decode HBase column values */
  protected Mapping m_tableMapping;

  /** Information from the mapping */
  protected Map<String, HBaseValueMeta> m_columnsMappedByAlias;

  /** True if the target table has been connected to successfully */
  protected boolean m_targetTableActive = false;

  /** Index of the key in the incoming fields */
  protected int m_incomingKeyIndex;

  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {

    Object[] r = getRow();

    if (r == null) {
      // no more input

      // clean up/close connections etc.
      // target table will be null if we haven't seen any input
      if (m_hbAdmin != null && m_targetTableActive) {
        try {
          if (!m_hbAdmin.targetTableIsAutoFlush()) {
            logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
                "HBaseOutput.FlushingWriteBuffer"));
            m_hbAdmin.flushCommitsTargetTable();
          }
        } catch (Exception ex) {
          throw new KettleException(
              BaseMessages.getString(HBaseOutputMeta.PKG,
                  "HBaseOutput.Error.ProblemFlushingBufferedData",
                  ex.getMessage()), ex);
        }

        try {
          logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.ClosingConnectionToTable"));
          m_hbAdmin.closeTargetTable();
          m_targetTableActive = false;
        } catch (Exception ex) {
          throw new KettleException(
              BaseMessages.getString(HBaseOutputMeta.PKG,
                  "HBaseOutput.Error.ProblemWhenClosingConnection",
                  ex.getMessage()), ex);
        }
      }

      setOutputDone();
      return false;
    }

    if (first) {
      first = false;
      m_meta = (HBaseOutputMeta) smi;
      m_data = (HBaseOutputData) sdi;

      // Get the connection to HBase
      try {
        logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.ConnectingToHBase"));

        List<String> connectionMessages = new ArrayList<String>();
        m_hbAdmin = HBaseOutputData.getHBaseConnection(
            environmentSubstitute(m_meta.getZookeeperHosts()),
            environmentSubstitute(m_meta.getZookeeperPort()),
            environmentSubstitute(m_meta.getCoreConfigURL()),
            environmentSubstitute(m_meta.getDefaultConfigURL()),
            connectionMessages);
        m_bytesUtil = m_hbAdmin.getBytesUtil();

        if (connectionMessages.size() > 0) {
          for (String m : connectionMessages) {
            logBasic(m);
          }
        }
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.UnableToObtainConnection", ex.getMessage()), ex);
      }
      try {
        m_mappingAdmin = new MappingAdmin(m_hbAdmin);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.UnableToObtainConnection", ex.getMessage()), ex);
      }

      // check on the existence and readiness of the target table
      String targetName = environmentSubstitute(m_meta.getTargetTableName());
      if (Const.isEmpty(targetName)) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.NoTargetTableSpecified"));
      }
      try {
        if (!m_hbAdmin.tableExists(targetName)) {
          throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.Error.TargetTableDoesNotExist", targetName));
        }

        if (m_hbAdmin.isTableDisabled(targetName)
            || !m_hbAdmin.isTableAvailable(targetName)) {
          throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.Error.TargetTableIsNotAvailable", targetName));
        }
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.ProblemWhenCheckingAvailReadiness", targetName,
            ex.getMessage()), ex);
      }

      // Get mapping details for the target table

      if (m_meta.getMapping() != null
          && Const.isEmpty(m_meta.getTargetMappingName())) {
        m_tableMapping = m_meta.getMapping();
      } else {
        try {
          logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.RetrievingMappingDetails"));

          m_tableMapping = m_mappingAdmin.getMapping(
              environmentSubstitute(m_meta.getTargetTableName()),
              environmentSubstitute(m_meta.getTargetMappingName()));
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.Error.ProblemGettingMappingInfo", ex.getMessage()),
              ex);
        }
      }
      m_columnsMappedByAlias = m_tableMapping.getMappedColumns();

      if (m_tableMapping.isTupleMapping()) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.CantWriteUsingATupleMapping"));
      }

      // check that all incoming fields are in the mapping.
      // fewer fields than the mapping defines is OK as long as we have
      // the key as an incoming field. Can either use strict type checking
      // or use an error stream for rows where type-conversion to the mapping
      // types fail. Probably should use an error stream - e.g. could get rows
      // with negative numeric key value where mapping specifies an unsigned key
      boolean incomingKey = false;
      RowMetaInterface inMeta = getInputRowMeta();
      for (int i = 0; i < inMeta.size(); i++) {
        ValueMetaInterface vm = inMeta.getValueMeta(i);
        String inName = vm.getName();

        if (m_tableMapping.getKeyName().equals(inName)) {
          incomingKey = true;
          m_incomingKeyIndex = i;
          // should we check the type?
        } else {
          HBaseValueMeta hvm = m_columnsMappedByAlias.get(inName.trim());
          if (hvm == null) {
            throw new KettleException(BaseMessages.getString(
                HBaseOutputMeta.PKG, "HBaseOutput.Error.CantFindIncomingField",
                inName, m_tableMapping.getMappingName()));
          }
        }
      }

      if (!incomingKey) {
        throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.Error.TableKeyNotPresentInIncomingFields",
            m_tableMapping.getKeyName(), m_tableMapping.getMappingName()));
      }

      try {
        logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
            "HBaseOutput.ConnectingToTargetTable"));

        Properties tableProps = new Properties();
        // set a write buffer size (and disable auto flush)
        if (!Const.isEmpty(m_meta.getWriteBufferSize())) {
          long writeBuffer = Long.parseLong(environmentSubstitute(m_meta
              .getWriteBufferSize()));

          logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.SettingWriteBuffer", writeBuffer));
          tableProps.setProperty(HBaseConnection.HTABLE_WRITE_BUFFER_SIZE_KEY,
              "" + writeBuffer);

          if (m_meta.getDisableWriteToWAL()) {
            logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
                "HBaseOutput.DisablingWriteToWAL"));
          }
        }
        m_hbAdmin.newTargetTable(targetName, tableProps);
        m_targetTableActive = true;
      } catch (Exception e) {
        throw new KettleException(
            BaseMessages.getString(HBaseOutputMeta.PKG,
                "HBaseOutput.Error.ProblemConnectingToTargetTable",
                e.getMessage()), e);
      }

      // output (downstream) is the same as input
      m_data.setOutputRowMeta(getInputRowMeta());
    }

    // Put the data

    // first deal with the key
    // key must not be missing!
    ValueMetaInterface keyvm = getInputRowMeta().getValueMeta(
        m_incomingKeyIndex);
    if (keyvm.isNull(r[m_incomingKeyIndex])) {
      String errorDescriptions = BaseMessages.getString(HBaseOutputMeta.PKG,
          "HBaseOutput.Error.IncomingRowHasNullKeyValue");
      if (getStepMeta().isDoingErrorHandling()) {
        String errorFields = m_tableMapping.getKeyName();
        putError(getInputRowMeta(), r, 1, errorDescriptions, errorFields,
            "HBaaseOutput001");
      } else {
        throw new KettleException(errorDescriptions);
      }
    }

    byte[] encodedKey = HBaseValueMeta.encodeKeyValue(r[m_incomingKeyIndex],
        keyvm, m_tableMapping.getKeyType(), m_bytesUtil);

    try {
      m_hbAdmin.newTargetTablePut(encodedKey, !m_meta.getDisableWriteToWAL());
    } catch (Exception ex) {
      throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
          "HBaseOutput.Error.UnableToSetTargetTable"), ex);
    }

    // now encode the rest of the fields. Nulls do not get inserted of course
    for (int i = 0; i < getInputRowMeta().size(); i++) {
      ValueMetaInterface current = getInputRowMeta().getValueMeta(i);
      if (i != m_incomingKeyIndex && !current.isNull(r[i])) {
        HBaseValueMeta hbaseColMeta = m_columnsMappedByAlias.get(current
            .getName());
        String columnFamily = hbaseColMeta.getColumnFamily();
        String columnName = hbaseColMeta.getColumnName();

        boolean binaryColName = false;
        if (columnName.startsWith("@@@binary@@@")) {
          // assume hex encoded column name
          columnName = columnName.replace("@@@binary@@@", "");
          binaryColName = true;
        }
        byte[] encoded = HBaseValueMeta.encodeColumnValue(r[i], current,
            hbaseColMeta, m_bytesUtil);

        try {
          m_hbAdmin.addColumnToTargetPut(columnFamily, columnName,
              binaryColName, encoded);
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.Error.UnableToAddColumnToTargetTablePut"), ex);
        }
      }
    }

    try {
      m_hbAdmin.executeTargetTablePut();
    } catch (Exception e) {
      String errorDescriptions = BaseMessages.getString(HBaseOutputMeta.PKG,
          "HBaseOutput.Error.ProblemInsertingRowIntoHBase", e.getMessage());
      if (getStepMeta().isDoingErrorHandling()) {
        String errorFields = "Unknown";
        putError(getInputRowMeta(), r, 1, errorDescriptions, errorFields,
            "HBaseOutput002");
      } else {
        throw new KettleException(errorDescriptions, e);
      }
    }

    // pass on the data to any downstream steps
    putRow(m_data.getOutputRowMeta(), r);

    if (log.isRowLevel()) {
      log.logRowlevel(toString(), "Read row #" + getLinesRead() + " : " + r);
    }

    if (checkFeedback(getLinesRead())) {
      logBasic("Linenr " + getLinesRead());
    }

    return true;
  }

  @Override
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }
    super.setStopped(stopped);

    if (stopped) {
      if (m_hbAdmin != null && m_targetTableActive) {
        try {
          if (!m_hbAdmin.targetTableIsAutoFlush()) {
            logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
                "HBaseOutput.FlushingWriteBuffer"));
            m_hbAdmin.flushCommitsTargetTable();
          }
        } catch (Exception ex) {
          logError(
              BaseMessages.getString(HBaseOutputMeta.PKG,
                  "HBaseOutput.Error.ProblemFlushingBufferedData",
                  ex.getMessage()), ex);
        }

        try {
          logBasic(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.ClosingConnectionToTable"));
          m_hbAdmin.closeTargetTable();
          m_targetTableActive = false;
        } catch (Exception ex) {
          logError(
              BaseMessages.getString(HBaseOutputMeta.PKG,
                  "HBaseOutput.Error.ProblemWhenClosingConnection",
                  ex.getMessage()), ex);
        }
      }
    }
  }
}
