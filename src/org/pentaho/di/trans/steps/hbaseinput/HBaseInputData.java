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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.hadoop.HadoopConfigurationBootstrap;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hbase.HBaseRowToKettleTuple;
import org.pentaho.hbase.shim.api.ColumnFilter;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseShim;

/**
 * Class providing an input step for reading data from an HBase table according
 * to meta data mapping info stored in a separate HBase table called
 * "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping for details on the
 * meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 * 
 */
public class HBaseInputData extends BaseStepData implements StepDataInterface {

  /** The output data format */
  protected RowMetaInterface m_outputRowMeta;

  /**
   * Get the output row format
   * 
   * @return the output row format
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }

  /**
   * Set the output row format
   * 
   * @param rmi the output row format
   */
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }

  /**
   * Get an administrative connection to HBase.
   * 
   * @param zookeeperHosts the list of zookeeper host(s)
   * @param zookeeperPort the zookeeper port (null for default)
   * @param siteConfig optional path to site config
   * @param defaultConfig optional path to default config
   * @param logging a list for holding log messages generated when obtaining the
   *          connection
   * @return an administrative connection to HBase
   * @throws Exception if a problem occurs
   */
  public static HBaseConnection getHBaseConnection(String zookeeperHosts,
      String zookeeperPort, String siteConfig, String defaultConfig,
      List<String> logging) throws Exception {

    Properties connProps = new Properties();
    if (!Const.isEmpty(zookeeperHosts)) {
      connProps.setProperty(HBaseConnection.ZOOKEEPER_QUORUM_KEY,
          zookeeperHosts);
    }
    if (!Const.isEmpty(zookeeperPort)) {
      connProps.setProperty(HBaseConnection.ZOOKEEPER_PORT_KEY, zookeeperPort);
    }
    if (!Const.isEmpty(siteConfig)) {
      connProps.setProperty(HBaseConnection.SITE_KEY, siteConfig);
    }
    if (!Const.isEmpty(defaultConfig)) {
      connProps.setProperty(HBaseConnection.DEFAULTS_KEY, defaultConfig);
    }

    HadoopConfiguration active = HadoopConfigurationBootstrap
        .getHadoopConfigurationProvider().getActiveConfiguration();
    HBaseShim hbaseShim = active.getHBaseShim();
    HBaseConnection conn = hbaseShim.getHBaseConnection();
    conn.configureConnection(connProps, logging);

    return conn;
  }

  /**
   * Utility method to covert a string to a URL object.
   * 
   * @param pathOrURL file or http URL as a string
   * @return a URL
   * @throws MalformedURLException if there is a problem with the URL.
   */
  public static URL stringToURL(String pathOrURL) throws MalformedURLException {
    URL result = null;

    if (!Const.isEmpty(pathOrURL)) {
      if (pathOrURL.toLowerCase().startsWith("http://")
          || pathOrURL.toLowerCase().startsWith("file://")) {
        result = new URL(pathOrURL);
      } else {
        String c = "file://" + pathOrURL;
        result = new URL(c);
      }
    }

    return result;
  }

  /**
   * Initialize the table scan with start and stop key values (if supplied)
   * 
   * @param hbAdmin the connection to use
   * @param bytesUtil the byte conversion utils to use
   * @param tableMapping the table mapping info to use
   * @param dateOrNumberConversionMaskForKey conversion pattern for data/numbers
   * @param keyStartS the key start value
   * @param keyStopS the key stop value
   * @param scannerCacheSize the size of the scanner cache
   * @param log the log
   * @param vars variables
   * @throws KettleException if a problem occurs
   */
  public static void initializeScan(HBaseConnection hbAdmin,
      HBaseBytesUtilShim bytesUtil, Mapping tableMapping,
      String dateOrNumberConversionMaskForKey, String keyStartS,
      String keyStopS, String scannerCacheSize, LogChannelInterface log,
      VariableSpace vars) throws KettleException {

    byte[] keyLowerBound = null;
    byte[] keyUpperBound = null;

    // Set up the scan
    if (!Const.isEmpty(keyStartS)) {
      keyStartS = vars.environmentSubstitute(keyStartS);
      String convM = dateOrNumberConversionMaskForKey;

      if (tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
        // assume we have a hex encoded string
        keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
            tableMapping.getKeyType(), bytesUtil);
      } else if (tableMapping.getKeyType() != Mapping.KeyType.STRING) {
        // allow a conversion mask in the start key field to override any
        // specified for
        // the key in the user specified fields
        String[] parts = keyStartS.split("@");
        if (parts.length == 2) {
          keyStartS = parts[0];
          convM = parts[1];
        }

        if (!Const.isEmpty(convM) && convM.length() > 0) {

          if (tableMapping.getKeyType() == Mapping.KeyType.DATE
              || tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(convM);
            try {
              Date d = sdf.parse(keyStartS);
              keyLowerBound = HBaseValueMeta.encodeKeyValue(d,
                  tableMapping.getKeyType(), bytesUtil);
            } catch (ParseException e) {
              throw new KettleException(
                  BaseMessages.getString(HBaseInputMeta.PKG,
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
                  tableMapping.getKeyType(), bytesUtil);
            } catch (ParseException e) {
              throw new KettleException(
                  BaseMessages.getString(HBaseInputMeta.PKG,
                      "HBaseInput.Error.UnableToParseLowerBoundKeyValue",
                      keyStartS), e);
            }
          }
        } else {
          // just try it as a string
          keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
              tableMapping.getKeyType(), bytesUtil);
        }
      } else {
        // it is a string
        keyLowerBound = HBaseValueMeta.encodeKeyValue(keyStartS,
            tableMapping.getKeyType(), bytesUtil);
      }

      if (!Const.isEmpty(keyStopS)) {
        keyStopS = vars.environmentSubstitute(keyStopS);
        convM = dateOrNumberConversionMaskForKey;

        if (tableMapping.getKeyType() == Mapping.KeyType.BINARY) {
          // assume we have a hex encoded string
          keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS,
              tableMapping.getKeyType(), bytesUtil);
        } else if (tableMapping.getKeyType() != Mapping.KeyType.STRING) {

          // allow a conversion mask in the stop key field to override any
          // specified for
          // the key in the user specified fields
          String[] parts = keyStopS.split("@");
          if (parts.length == 2) {
            keyStopS = parts[0];
            convM = parts[1];
          }

          if (!Const.isEmpty(convM) && convM.length() > 0) {
            if (tableMapping.getKeyType() == Mapping.KeyType.DATE
                || tableMapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
              SimpleDateFormat sdf = new SimpleDateFormat();
              sdf.applyPattern(convM);
              try {
                Date d = sdf.parse(keyStopS);
                keyUpperBound = HBaseValueMeta.encodeKeyValue(d,
                    tableMapping.getKeyType(), bytesUtil);
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
                    tableMapping.getKeyType(), bytesUtil);
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
                tableMapping.getKeyType(), bytesUtil);
          }
        } else {
          // it is a string
          keyUpperBound = HBaseValueMeta.encodeKeyValue(keyStopS,
              tableMapping.getKeyType(), bytesUtil);
        }
      }
    }

    int cacheSize = 0;

    // set any user-specified scanner caching
    if (!Const.isEmpty(scannerCacheSize)) {
      String temp = vars.environmentSubstitute(scannerCacheSize);
      cacheSize = Integer.parseInt(temp);

      if (log != null) {
        log.logBasic(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Message.SettingScannerCaching", cacheSize));
      }
    }
    try {
      hbAdmin.newSourceTableScan(keyLowerBound, keyUpperBound, cacheSize);
    } catch (Exception ex) {
      throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
          "HBaseInput.Error.UnableToConfigureSourceTableScan"), ex);
    }
  }

  /**
   * Set the specific columns to be returned by the scan.
   * 
   * @param hbAdmin the connection to use
   * @param limitCols the columns to limit the scan to
   * @param tableMapping the mapping information
   * @throws KettleException if a problem occurs
   */
  public static void setScanColumns(HBaseConnection hbAdmin,
      List<HBaseValueMeta> limitCols, Mapping tableMapping)
      throws KettleException {
    for (HBaseValueMeta currentCol : limitCols) {
      if (!currentCol.isKey()) {
        String colFamilyName = currentCol.getColumnFamily();
        String qualifier = currentCol.getColumnName();

        boolean binaryColName = false;
        if (qualifier.startsWith("@@@binary@@@")) {
          qualifier = qualifier.replace("@@@binary@@@", "");
          binaryColName = true;
        }

        try {
          hbAdmin.addColumnToScan(colFamilyName, qualifier, binaryColName);
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.UnableToAddColumnToScan"), ex);
        }
      }
    }
  }

  /**
   * Set column filters to apply server-side to the scan results.
   * 
   * @param hbAdmin the connection to use
   * @param columnFilters the column filters to apply
   * @param matchAnyFilter if true then a row will be returned if any of the
   *          filters match (otherwise all have to match)
   * @param columnsMappedByAlias the columns defined in the mapping
   * @param vars variables to use
   * @throws KettleException if a problem occurs
   */
  public static void setScanFilters(HBaseConnection hbAdmin,
      Collection<ColumnFilter> columnFilters, boolean matchAnyFilter,
      Map<String, HBaseValueMeta> columnsMappedByAlias, VariableSpace vars)
      throws KettleException {

    for (ColumnFilter cf : columnFilters) {
      String fieldAliasS = vars.environmentSubstitute(cf.getFieldAlias());
      HBaseValueMeta mappedCol = columnsMappedByAlias.get(fieldAliasS);
      if (mappedCol == null) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.ColumnFilterIsNotInTheMapping", fieldAliasS));
      }

      // check the type (if set in the ColumnFilter) against the type
      // of this field in the mapping
      String fieldTypeS = vars.environmentSubstitute(cf.getFieldType());
      if (!Const.isEmpty(fieldTypeS)) {
        if (!mappedCol.getHBaseTypeDesc().equalsIgnoreCase(fieldTypeS)) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.FieldTypeMismatch", fieldTypeS, fieldAliasS,
              mappedCol.getHBaseTypeDesc()));
        }
      }

      try {
        hbAdmin.addColumnFilterToScan(cf, mappedCol, vars, matchAnyFilter);
      } catch (Exception ex) {
        throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
            "HBaseInput.Error.UnableToAddColumnFilterToScan"), ex);
      }
    }
  }

  /**
   * Convert/decode the current hbase row into a list of "tuple" kettle rows
   * 
   * @param hbAdmin the connection to use
   * @param userOutputColumns user-specified subset of columns (if any) from the
   *          mapping
   * @param columnsMappedByAlias columns in the mapping keyed by alias
   * @param tableMapping the mapping to use
   * @param tupleHandler the HBaseRowToKettleTuple to delegate to
   * @param outputRowMeta the outgoing row meta
   * @param bytesUtil the byte conversion utils to use
   * @return a list of kettle rows
   * @throws KettleException if a problem occurs
   */
  public static List<Object[]> getTupleOutputRows(HBaseConnection hbAdmin,
      List<HBaseValueMeta> userOutputColumns,
      Map<String, HBaseValueMeta> columnsMappedByAlias, Mapping tableMapping,
      HBaseRowToKettleTuple tupleHandler, RowMetaInterface outputRowMeta,
      HBaseBytesUtilShim bytesUtil) throws KettleException {

    if (userOutputColumns != null && userOutputColumns.size() > 0) {
      return tupleHandler.hbaseRowToKettleTupleMode(null, hbAdmin,
          tableMapping, userOutputColumns, outputRowMeta);
    } else {
      return tupleHandler.hbaseRowToKettleTupleMode(null, hbAdmin,
          tableMapping, columnsMappedByAlias, outputRowMeta);
    }
  }

  /**
   * Convert/decode the current hbase row into a kettle row
   * 
   * @param hbAdmin the connection to use
   * @param userOutputColumns user-specified subset of columns (if any) from the
   *          mapping
   * @param columnsMappedByAlias columns in the mapping keyed by alias
   * @param tableMapping the mapping to use
   * @param outputRowMeta the outgoing row meta
   * @param bytesUtil the byte conversion utils to use
   * @return a kettle row
   * @throws KettleException if a problem occurs
   */
  public static Object[] getOutputRow(HBaseConnection hbAdmin,
      List<HBaseValueMeta> userOutputColumns,
      Map<String, HBaseValueMeta> columnsMappedByAlias, Mapping tableMapping,
      RowMetaInterface outputRowMeta, HBaseBytesUtilShim bytesUtil)
      throws KettleException {

    int size = (userOutputColumns != null && userOutputColumns.size() > 0) ? userOutputColumns
        .size() : tableMapping.getMappedColumns().keySet().size() + 1; // + 1
                                                                       // for
                                                                       // the
                                                                       // key

    Object[] outputRowData = RowDataUtil.allocateRowData(size);

    // User-selected output columns?
    if (userOutputColumns != null && userOutputColumns.size() > 0) {
      for (HBaseValueMeta currentCol : userOutputColumns) {
        if (currentCol.isKey()) {
          byte[] rawKey = null;
          try {
            rawKey = hbAdmin.getResultSetCurrentRowKey();
          } catch (Exception e) {
            throw new KettleException(e);
          }
          Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey,
              tableMapping, bytesUtil);
          int keyIndex = outputRowMeta.indexOfValue(currentCol.getAlias());
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
            kv = hbAdmin.getResultSetCurrentRowColumnLatest(colFamilyName,
                qualifier, binaryColName);
          } catch (Exception e) {
            throw new KettleException(e);
          }

          int outputIndex = outputRowMeta.indexOfValue(currentCol.getAlias());
          if (outputIndex < 0) {
            throw new KettleException(BaseMessages.getString(
                HBaseInputMeta.PKG,
                "HBaseInput.Error.ColumnNotDefinedInOutput",
                currentCol.getAlias()));
          }

          Object decodedVal = HBaseValueMeta.decodeColumnValue(
              (kv == null) ? null : kv, currentCol, bytesUtil);

          outputRowData[outputIndex] = decodedVal;
        }
      }
    } else {
      // do the key first
      byte[] rawKey = null;
      try {
        rawKey = hbAdmin.getResultSetCurrentRowKey();
      } catch (Exception e) {
        throw new KettleException(e);
      }

      Object decodedKey = HBaseValueMeta.decodeKeyValue(rawKey, tableMapping,
          bytesUtil);
      int keyIndex = outputRowMeta.indexOfValue(tableMapping.getKeyName());
      outputRowData[keyIndex] = decodedKey;

      Set<String> aliasSet = columnsMappedByAlias.keySet();

      for (String name : aliasSet) {
        HBaseValueMeta currentCol = columnsMappedByAlias.get(name);
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
          kv = hbAdmin.getResultSetCurrentRowColumnLatest(colFamilyName,
              qualifier, binaryColName);
        } catch (Exception e) {
          throw new KettleException(e);
        }

        int outputIndex = outputRowMeta.indexOfValue(name);
        if (outputIndex < 0) {
          throw new KettleException(BaseMessages.getString(HBaseInputMeta.PKG,
              "HBaseInput.Error.ColumnNotDefinedInOutput", name));
        }

        Object decodedVal = HBaseValueMeta.decodeColumnValue(
            (kv == null) ? null : kv, currentCol, bytesUtil);

        outputRowData[outputIndex] = decodedVal;
      }
    }

    return outputRowData;
  }
}
