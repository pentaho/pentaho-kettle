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
import java.util.List;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.hadoop.HadoopConfigurationBootstrap;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.hadoop.shim.HadoopConfiguration;
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
  public static HBaseShim getHBaseConnection(String zookeeperHosts,
      String zookeeperPort, String siteConfig, String defaultConfig,
      List<String> logging) throws Exception {

    Properties connProps = new Properties();
    if (!Const.isEmpty(zookeeperHosts)) {
      connProps.setProperty(HBaseShim.ZOOKEEPER_QUORUM_KEY, zookeeperHosts);
    }
    if (!Const.isEmpty(zookeeperPort)) {
      connProps.setProperty(HBaseShim.ZOOKEEPER_PORT_KEY, zookeeperPort);
    }
    if (!Const.isEmpty(siteConfig)) {
      connProps.setProperty(HBaseShim.SITE_KEY, siteConfig);
    }
    if (!Const.isEmpty(defaultConfig)) {
      connProps.setProperty(HBaseShim.DEFAULTS_KEY, defaultConfig);
    }

    HadoopConfiguration active = HadoopConfigurationBootstrap.getHadoopConfigurationProvider()
        .getActiveConfiguration();
    HBaseShim hbaseShim = active.getHBaseShim();
    hbaseShim.configureConnection(connProps, logging);

    return hbaseShim;
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
}
