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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
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
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.spi.HBaseShim;

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

  /** Admin object for interacting with HBase */
  protected HBaseShim m_hbAdmin;

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
        m_tupleHandler = new HBaseRowToKettleTuple();
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
      byte[] keyLowerBound = null;
      byte[] keyUpperBound = null;

      // Set up the scan
      if (!Const.isEmpty(m_meta.getKeyStartValue())) {
        String keyStartS = environmentSubstitute(m_meta.getKeyStartValue());
        String convM = dateOrNumberConversionMaskForKey;

        if (m_tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
          // assume we have a hex encoded string
          keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
              m_tableMapping.getKeyType());
        } else if (m_tableMapping.getKeyType() != Mapping.KeyType.STRING) {
          // allow a conversion mask in the start key field to override any
          // specified for
          // the key in the user specified fields
          String[] parts = keyStartS.split("@");
          if (parts.length == 2) {
            keyStartS = parts[0];
            convM = parts[1];
          }

          if (!Const.isEmpty(convM) && convM.length() > 0) {

            if (m_tableMapping.getKeyType() == Mapping.KeyType.DATE
                || m_tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
              SimpleDateFormat sdf = new SimpleDateFormat();
              sdf.applyPattern(convM);
              try {
                Date d = sdf.parse(keyStartS);
                keyLowerBound = HBaseValueMeta.encodeKeyValue(d,
                    m_tableMapping.getKeyType());
              } catch (ParseException e) {
                throw new KettleException(BaseMessages.getString(
                    HBaseInputMeta.PKG,
                    "HBaseInput.Error.UnableToParseLowerBoundKeyValue",
                    keyStartS), e);
              }
            } else {
              // Number type
              // Double/Float or Long/Integer
              DecimalFormat df = new DecimalFormat();
              df.applyPattern(convM);
              Number num = null;
              try {
                num = df.parse(keyStartS);
                keyLowerBound = HBaseValueMeta.encodeKeyValue(num,
                    m_tableMapping.getKeyType());
              } catch (ParseException e) {
                throw new KettleException(BaseMessages.getString(
                    HBaseInputMeta.PKG,
                    "HBaseInput.Error.UnableToParseLowerBoundKeyValue",
                    keyStartS), e);
              }
            }
          } else {
            // just try it as a string
            keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
                m_tableMapping.getKeyType());
          }
        } else {
          // it is a string
          keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
              m_tableMapping.getKeyType());
        }

        if (!Const.isEmpty(m_meta.getKeyStopValue())) {
          String keyStopS = environmentSubstitute(m_meta.getKeyStopValue());
          convM = dateOrNumberConversionMaskForKey;

          if (m_tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
            // assume we have a hex encoded string
            keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS,
                m_tableMapping.getKeyType());
          } else if (m_tableMapping.getKeyType() != Mapping.KeyType.STRING) {

            // allow a conversion mask in the stop key field to override any
            // specified for
            // the key in the user specified fields
            String[] parts = keyStopS.split("@");
            if (parts.length == 2) {
              keyStopS = parts[0];
              convM = parts[1];
            }

            if (!Const.isEmpty(convM) && convM.length() > 0) {
              if (m_tableMapping.getKeyType() == Mapping.KeyType.DATE
                  || m_tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern(convM);
                try {
                  Date d = sdf.parse(keyStopS);
                  keyUpperBound = HBaseValueMeta.encodeKeyValue(d,
                      m_tableMapping.getKeyType());
                } catch (ParseException e) {
                  throw new KettleException(BaseMessages.getString(
                      HBaseInputMeta.PKG,
                      "HBaseInput.Error.UnableToParseUpperBoundKeyValue",
                      keyStopS), e);
                }
              } else {
                // Number type
                // Double/Float or Long/Integer
                DecimalFormat df = new DecimalFormat();
                df.applyPattern(convM);
                Number num = null;
                try {
                  num = df.parse(keyStopS);
                  keyUpperBound = HBaseValueMeta.encodeKeyValue(num,
                      m_tableMapping.getKeyType());
                } catch (ParseException e) {
                  throw new KettleException(BaseMessages.getString(
                      HBaseInputMeta.PKG,
                      "HBaseInput.Error.UnableToParseUpperBoundKeyValue",
                      keyStopS), e);
                }
              }
            } else {
              // just try it as a string
              keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS,
                  m_tableMapping.getKeyType());
            }
          } else {
            // it is a string
            keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS,
                m_tableMapping.getKeyType());
          }
        }
      }

      int cacheSize = 0;

      // set any user-specified scanner caching
      if (!Const.isEmpty(m_meta.getScannerCacheSize())) {
        String temp = environmentSubstitute(m_meta.getScannerCacheSize());
        cacheSize = Integer.parseInt(temp);

        logBasic(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Message.SettingScannerCaching", cacheSize));
      }
      try {
        m_hbAdmin.newSourceTableScan(keyLowerBound, keyUpperBound, cacheSize);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.UnableToConfigureSourceTableScan"), ex);
      }

      // LIMIT THE SCAN TO JUST THE COLUMNS IN THE MAPPING
      // User-selected output columns?
      if (m_userOutputColumns != null && m_userOutputColumns.size() > 0
          && !m_tableMapping.isTupleMapping()) {
        for (HBaseValueMeta currentCol : m_userOutputColumns) {
          if (!currentCol.isKey()) {
            String colFamilyName = currentCol.getColumnFamily();
            String qualifier = currentCol.getColumnName();

            boolean binaryColName = false;
            if (qualifier.startsWith("@@@binary@@@")) {
              qualifier = qualifier.replace("@@@binary@@@", "");
              binaryColName = true;
            }

            try {
              m_hbAdmin
                  .addColumnToScan(colFamilyName, qualifier, binaryColName);
            } catch (Exception ex) {
              throw new KettleException(BaseMessages.getString(
                  HBaseInputMeta.PKG,
                  "HBaseInput.Error.UnableToAddColumnToScan"), ex);
            }
          }
        }
      }

      // set any filters
      if (m_meta.getColumnFilters() != null
          && m_meta.getColumnFilters().size() > 0) {

        for (ColumnFilter cf : m_meta.getColumnFilters()) {
          String fieldAliasS = environmentSubstitute(cf.getFieldAlias());
          HBaseValueMeta mappedCol = m_columnsMappedByAlias.get(fieldAliasS);
          if (mappedCol == null) {
            throw new KettleException(BaseMessages.getString(
                HBaseInputMeta.PKG,
                "HBaseInput.Error.ColumnFilterIsNotInTheMapping", fieldAliasS));
          }

          // check the type (if set in the ColumnFilter) against the type
          // of this field in the mapping
          String fieldTypeS = environmentSubstitute(cf.getFieldType());
          if (!Const.isEmpty(fieldTypeS)) {
            if (!mappedCol.getHBaseTypeDesc().equalsIgnoreCase(fieldTypeS)) {
              throw new KettleException(BaseMessages.getString(
                  HBaseInputMeta.PKG, "HBaseInput.Error.FieldTypeMismatch",
                  fieldTypeS, fieldAliasS, mappedCol.getHBaseTypeDesc()));
            }
          }

          try {
            m_hbAdmin.addColumnFilterToScan(cf, mappedCol, this,
                m_meta.getMatchAnyFilter());
          } catch (Exception ex) {
            throw new KettleException(BaseMessages.getString(
                HBaseInputMeta.PKG,
                "HBaseInput.Error.UnableToAddColumnFilterToScan"), ex);
          }
        }
      }

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

    boolean hasNext = false;
    try {
      hasNext = m_hbAdmin.resultSetNextRow();
    } catch (Exception e) {
      throw new KettleException(e.getMessage(), e);
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

    int size = (m_userOutputColumns != null && m_userOutputColumns.size() > 0) ? m_userOutputColumns
        .size() : m_tableMapping.getMappedColumns().keySet().size() + 1; // + 1
                                                                         // for
                                                                         // the
                                                                         // key
    Object[] outputRowData = RowDataUtil.allocateRowData(size);

    // User-selected output columns?
    if (m_userOutputColumns != null && m_userOutputColumns.size() > 0) {
      if (m_tableMapping.isTupleMapping()) {
        List<Object[]> hrowToKettleRow = m_tupleHandler
            .hbaseRowToKettleTupleMode(null, m_hbAdmin, m_tableMapping,
                m_userOutputColumns, m_data.getOutputRowMeta());
        for (Object[] tuple : hrowToKettleRow) {
          putRow(m_data.getOutputRowMeta(), tuple);
        }
        return true;
      } else {
        for (HBaseValueMeta currentCol : m_userOutputColumns) {
          if (currentCol.isKey()) {
            byte[] rawKey = null;
            try {
              rawKey = m_hbAdmin.getResultSetCurrentRowKey();
            } catch (Exception e) {
              throw new KettleException(e);
            }
            Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey,
                m_tableMapping);
            int keyIndex = m_data.getOutputRowMeta().indexOfValue(
                currentCol.getAlias());
            outputRowData[keyIndex] = decodedKey;
          } else {
            String colFamilyName = currentCol.getColumnFamily();
            String qualifier = currentCol.getColumnName();

            boolean binaryColName = false;
            if (qualifier.startsWith("@@@binary@@@")) {
              qualifier = qualifier.replace("@@@binary@@@", "");
              // assume hex encoded
              binaryColName = true;
            }

            byte[] kv = null;
            try {
              kv = m_hbAdmin.getResultSetCurrentRowColumnLatest(colFamilyName,
                  qualifier, binaryColName);
            } catch (Exception e) {
              throw new KettleException(e);
            }

            int outputIndex = m_data.getOutputRowMeta().indexOfValue(
                currentCol.getAlias());
            if (outputIndex < 0) {
              throw new KettleException(BaseMessages.getString(
                  HBaseInputMeta.PKG,
                  "HBaseInput.Error.ColumnNotDefinedInOutput",
                  currentCol.getAlias()));
            }

            Object decodedVal = HBaseValueMeta.decodeColumnValue(
                (kv == null) ? null : kv, currentCol);

            outputRowData[outputIndex] = decodedVal;
          }
        }
      }
    } else {

      // all the columns in the mapping
      if (m_tableMapping.isTupleMapping()) {
        List<Object[]> hrowToKettleRow = m_tupleHandler
            .hbaseRowToKettleTupleMode(null, m_hbAdmin, m_tableMapping,
                m_columnsMappedByAlias, m_data.getOutputRowMeta());

        for (Object[] tuple : hrowToKettleRow) {
          putRow(m_data.getOutputRowMeta(), tuple);
        }
        return true;
      } else {

        // do the key first
        byte[] rawKey = null;
        try {
          rawKey = m_hbAdmin.getResultSetCurrentRowKey();
        } catch (Exception e) {
          throw new KettleException(e);
        }

        Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey,
            m_tableMapping);
        int keyIndex = m_data.getOutputRowMeta().indexOfValue(
            m_tableMapping.getKeyName());
        outputRowData[keyIndex] = decodedKey;

        Set<String> aliasSet = m_columnsMappedByAlias.keySet();

        for (String name : aliasSet) {
          HBaseValueMeta currentCol = m_columnsMappedByAlias.get(name);
          String colFamilyName = currentCol.getColumnFamily();
          String qualifier = currentCol.getColumnName();

          boolean binaryColName = false;
          if (qualifier.startsWith("@@@binary@@@")) {
            qualifier = qualifier.replace("@@@binary@@@", "");
            // assume hex encoded
            binaryColName = true;
          }

          byte[] kv = null;
          try {
            kv = m_hbAdmin.getResultSetCurrentRowColumnLatest(colFamilyName,
                qualifier, binaryColName);
          } catch (Exception e) {
            throw new KettleException(e);
          }

          int outputIndex = m_data.getOutputRowMeta().indexOfValue(name);
          if (outputIndex < 0) {
            throw new KettleException(BaseMessages.getString(
                HBaseInputMeta.PKG,
                "HBaseInput.Error.ColumnNotDefinedInOutput", name));
          }

          Object decodedVal = HBaseValueMeta.decodeColumnValue(
              (kv == null) ? null : kv, currentCol);

          outputRowData[outputIndex] = decodedVal;
        }
      }
    }

    // output the row
    putRow(m_data.getOutputRowMeta(), outputRowData);
    return true;
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
