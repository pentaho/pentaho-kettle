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

package org.pentaho.di.trans.steps.hbaseinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.hbase.HBaseRowToKettleTuple;
import org.pentaho.hbase.mapping.MappingAdmin;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;

/**
 * Class providing an input step for reading data from an HBase table according
 * to meta data mapping info stored in a separate HBase table called
 * "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping for details on the
 * meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HBaseInput extends BaseStep implements StepInterface {

  protected HBaseInputMeta m_meta;
  protected HBaseInputData m_data;

  public HBaseInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {

    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  /** Connection/admin object for interacting with HBase */
  protected HBaseConnection m_hbAdmin;

  /** Byte utilities */
  protected HBaseBytesUtilShim m_bytesUtil;

  /** The mapping admin object for interacting with mapping information */
  protected MappingAdmin m_mappingAdmin;

  /** The mapping information to use in order to decode HBase column values */
  protected Mapping m_tableMapping;

  /** Information from the mapping */
  protected Map<String, HBaseValueMeta> m_columnsMappedByAlias;

  /** User-selected columns from the mapping (null indicates output all columns) */
  protected List<HBaseValueMeta> m_userOutputColumns;

  /**
   * Used when decoding columns to <key, family, column, value, time stamp>
   * tuples
   */
  protected HBaseRowToKettleTuple m_tupleHandler;

  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
      throws KettleException {

    if (first) {
      first = false;
      m_meta = (HBaseInputMeta) smi;
      m_data = (HBaseInputData) sdi;

      // Get the connection to HBase
      try {
        List<String> connectionMessages = new ArrayList<String>();
        m_hbAdmin = HBaseInputData.getHBaseConnection(
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
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.UnableToObtainConnection"), ex);
      }
      try {
        m_mappingAdmin = new MappingAdmin(m_hbAdmin);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.UnableToCreateAMappingAdminConnection"), ex);
      }

      // check on the existence and readiness of the target table
      String sourceName = environmentSubstitute(m_meta.getSourceTableName());
      if (StringUtil.isEmpty(sourceName)) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.TableName.Missing"));
      }
      try {
        if (!m_hbAdmin.tableExists(sourceName)) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.SourceTableDoesNotExist", sourceName));
        }

        if (m_hbAdmin.isTableDisabled(sourceName)
            || !m_hbAdmin.isTableAvailable(sourceName)) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.SourceTableIsNotAvailable", sourceName));
        }
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.AvailabilityReadinessProblem", sourceName), ex);
      }

      if (m_meta.getMapping() != null
          && Const.isEmpty(m_meta.getSourceMappingName())) {
        // use embedded mapping
        m_tableMapping = m_meta.getMapping();
      } else {
        // Otherwise get mapping details for the source table from HBase
        if (Const.isEmpty(m_meta.getSourceMappingName())) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.NoMappingName"));
        }
        try {
          m_tableMapping = m_mappingAdmin.getMapping(
              environmentSubstitute(m_meta.getSourceTableName()),
              environmentSubstitute(m_meta.getSourceMappingName()));
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.UnableToRetrieveMapping",
              environmentSubstitute(m_meta.getSourceMappingName()),
              environmentSubstitute(m_meta.getSourceTableName())), ex);
        }
      }
      m_columnsMappedByAlias = m_tableMapping.getMappedColumns();

      if (m_tableMapping.isTupleMapping()) {
        m_tupleHandler = new HBaseRowToKettleTuple(m_bytesUtil);
      }

      // conversion mask to use for user specified key values in range scan.
      // This can come from user-specified field information OR it can be
      // provided in the keyStart/keyStop values by suffixing the value with
      // "@converionMask"
      String dateOrNumberConversionMaskForKey = null;

      // if there are any user-chosen output fields in the meta data then
      // check them against table mapping. All selected fields must be present
      // in the mapping
      m_userOutputColumns = m_meta.getOutputFields();
      if (m_userOutputColumns != null && m_userOutputColumns.size() > 0) {
        for (HBaseValueMeta vm : m_userOutputColumns) {
          if (!vm.isKey()) {
            if (m_columnsMappedByAlias.get(vm.getAlias()) == null) {
              throw new KettleException(BaseMessages.getString(
                  HBaseInputMeta.PKG,
                  "HBaseInput.Error.UnableToFindUserSelectedColumn",
                  vm.getAlias(),
                  m_tableMapping.getTableName() + HBaseValueMeta.SEPARATOR
                      + m_tableMapping.getMappingName()));
            }
          } else {
            dateOrNumberConversionMaskForKey = vm.getConversionMask();
          }
        }
      }

      try {
        m_hbAdmin.newSourceTable(sourceName);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.UnableToSetSourceTableForScan"), ex);
      }

      HBaseInputData.initializeScan(m_hbAdmin, m_bytesUtil, m_tableMapping,
          dateOrNumberConversionMaskForKey, m_meta.getKeyStartValue(),
          m_meta.getKeyStopValue(), m_meta.getScannerCacheSize(), log, this);

      // LIMIT THE SCAN TO JUST THE COLUMNS IN THE MAPPING
      // User-selected output columns?
      if (m_userOutputColumns != null && m_userOutputColumns.size() > 0
          && !m_tableMapping.isTupleMapping()) {

        HBaseInputData.setScanColumns(m_hbAdmin, m_userOutputColumns,
            m_tableMapping);
      }

      // set any filters
      if (m_meta.getColumnFilters() != null
          && m_meta.getColumnFilters().size() > 0) {

        if (m_tableMapping.isTupleMapping()) {
          logBasic(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.FiltersNotApplicableWithTupleMapping"));
        } else {
          HBaseInputData.setScanFilters(m_hbAdmin, m_meta.getColumnFilters(),
              m_meta.getMatchAnyFilter(), m_columnsMappedByAlias, this);
        }
      }

      if (!isStopped()) {
        try {
          m_hbAdmin.executeSourceTableScan();
        } catch (Exception e) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.UnableToExecuteSourceTableScan"), e);
        }

        // set up the output fields (using the mapping)
        m_data.setOutputRowMeta(new RowMeta());
        m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null,
            this);
      }
    }

    boolean hasNext = false;
    if (!isStopped()) {
      try {
        hasNext = m_hbAdmin.resultSetNextRow();
      } catch (Exception e) {
        throw new KettleException(e.getMessage(), e);
      }
    }

    if (!hasNext) {
      try {
        m_hbAdmin.closeSourceTable();
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.ProblemClosingConnection", e.getMessage()), e);
      }
      setOutputDone();
      return false;
    }

    if (m_tableMapping.isTupleMapping()) {
      List<Object[]> tupleRows = m_data.getTupleOutputRows(m_hbAdmin,
          m_userOutputColumns, m_columnsMappedByAlias, m_tableMapping,
          m_tupleHandler, m_data.getOutputRowMeta(), m_bytesUtil);

      for (Object[] tuple : tupleRows) {
        putRow(m_data.getOutputRowMeta(), tuple);
      }
      return true;
    } else {
      Object[] outRowData = m_data.getOutputRow(m_hbAdmin, m_userOutputColumns,
          m_columnsMappedByAlias, m_tableMapping, m_data.getOutputRowMeta(),
          m_bytesUtil);
      putRow(m_data.getOutputRowMeta(), outRowData);
      return true;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.di.trans.step.BaseStep#setStopped(boolean)
   */
  @Override
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }
    super.setStopped(stopped);

    if (stopped && m_hbAdmin != null) {
      logBasic(BaseMessages.getString(HBaseInputMeta.PKG,
          "HBaseInput.ClosingConnection"));
      try {
        m_hbAdmin.closeSourceTable();
      } catch (Exception ex) {
        logError(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.ProblemClosingConnection1", ex.getMessage()));
      }
    }
  }
}
