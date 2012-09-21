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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.hadoop.HadoopConfigurationBootstrap;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.hadoop.shim.HadoopConfiguration;
import org.pentaho.hbase.shim.api.HBaseValueMeta;
import org.pentaho.hbase.shim.api.Mapping;
import org.pentaho.hbase.shim.spi.HBaseBytesUtilShim;
import org.pentaho.hbase.shim.spi.HBaseConnection;
import org.pentaho.hbase.shim.spi.HBaseShim;

/**
 * Class providing an output step for writing data to an HBase table according
 * to meta data column/type mapping info stored in a separate HBase table called
 * "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping for details on the
 * meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class HBaseOutputData extends BaseStepData implements StepDataInterface {

  /** The output data format */
  protected RowMetaInterface m_outputRowMeta;

  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }

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
   * Sets up a new target table put operation using the connection shim
   * 
   * @param inRowMeta the incoming kettle row meta data
   * @param keyIndex the index of the key in the incoming row structure
   * @param kettleRow the current incoming kettle row
   * @param tableMapping the HBase table mapping to use
   * @param bu the byte util shim to use for conversion to and from byte arrays
   * @param hbAdmin the connection shim
   * @param writeToWAL true if the write ahead log should be written to
   * @return false if the key is null (missing) for the current incoming kettle
   *         row
   * @throws Exception if a problem occurs when initializing the new put
   *           operation
   */
  public static boolean initializeNewPut(RowMetaInterface inRowMeta,
      int keyIndex, Object[] kettleRow, Mapping tableMapping,
      HBaseBytesUtilShim bu, HBaseConnection hbAdmin, boolean writeToWAL)
      throws Exception {

    ValueMetaInterface keyvm = inRowMeta.getValueMeta(keyIndex);

    if (keyvm.isNull(kettleRow[keyIndex])) {
      return false;
    }

    byte[] encodedKey = HBaseValueMeta.encodeKeyValue(kettleRow[keyIndex],
        keyvm, tableMapping.getKeyType(), bu);

    hbAdmin.newTargetTablePut(encodedKey, writeToWAL);

    return true;
  }

  /**
   * Adds those incoming kettle field values that are defined in the table
   * mapping for the current row to the target table put operation
   * 
   * @param inRowMeta the incoming kettle row meta data
   * @param kettleRow the current incoming kettle row
   * @param keyIndex the index of the key in the incoming row structure
   * @param columnsMappedByAlias the columns in the table mapping
   * @param hbAdmin the connection shim
   * @param bu the byte util shim to use for conversion to and from byte arrays
   * @throws KettleException if a problem occurs when adding a column to the put
   *           operation
   */
  public static void addColumnsToPut(RowMetaInterface inRowMeta,
      Object[] kettleRow, int keyIndex,
      Map<String, HBaseValueMeta> columnsMappedByAlias,
      HBaseConnection hbAdmin, HBaseBytesUtilShim bu) throws KettleException {

    for (int i = 0; i < inRowMeta.size(); i++) {
      ValueMetaInterface current = inRowMeta.getValueMeta(i);
      if (i != keyIndex && !current.isNull(kettleRow[i])) {
        HBaseValueMeta hbaseColMeta = columnsMappedByAlias.get(current
            .getName());
        String columnFamily = hbaseColMeta.getColumnFamily();
        String columnName = hbaseColMeta.getColumnName();

        boolean binaryColName = false;
        if (columnName.startsWith("@@@binary@@@")) {
          // assume hex encoded column name
          columnName = columnName.replace("@@@binary@@@", "");
          binaryColName = true;
        }
        byte[] encoded = HBaseValueMeta.encodeColumnValue(kettleRow[i],
            current, hbaseColMeta, bu);

        try {
          hbAdmin.addColumnToTargetPut(columnFamily, columnName, binaryColName,
              encoded);
        } catch (Exception ex) {
          throw new KettleException(BaseMessages.getString(HBaseOutputMeta.PKG,
              "HBaseOutput.Error.UnableToAddColumnToTargetTablePut"), ex);
        }
      }
    }
  }

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
}
