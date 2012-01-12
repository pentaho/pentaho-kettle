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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Class providing an output step for writing data to an HBase table
 * according to meta data column/type mapping info stored in a separate
 * HBase table called "pentaho_mappings". See org.pentaho.hbase.mapping.Mapping
 * for details on the meta data format.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 *
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
  
  public static Configuration getHBaseConnection(String zookeeperHosts, 
      String zookeeperPort, URL coreConfig, URL defaultConfig) 
    throws IOException {
    Configuration con = new Configuration();
    
    if (defaultConfig != null) {
      con.addResource(defaultConfig);
    } else {
      // hopefully it's in the classpath
      con.addResource("hbase-default.xml");
    }
    
    if (coreConfig != null) {
      con.addResource(coreConfig);
    } else {
      // hopefully it's in the classpath
      con.addResource("hbase-site.xml");
    } 
    
    if (!Const.isEmpty(zookeeperHosts)) {
      // override default and site with this
      con.set("hbase.zookeeper.quorum", zookeeperHosts);
    }
    
    if (!Const.isEmpty(zookeeperPort)) {
      try {
        int port = Integer.parseInt(zookeeperPort);
        con.setInt("hbase.zookeeper.property.clientPort", port);
      } catch (NumberFormatException e) { 
        System.err.println("Unable to parse zookeeper port!");
      }
    }
    
    return con;    
  }
  
  public static URL stringToURL(String pathOrURL) throws MalformedURLException {
    URL result = null;
    
    if (!Const.isEmpty(pathOrURL)) {
      if (pathOrURL.toLowerCase().startsWith("http://") ||
          pathOrURL.toLowerCase().startsWith("file://")) {
        result = new URL(pathOrURL);
      } else {
        String c = "file://" + pathOrURL;
        result = new URL(c);
      }
    }
    
    return result;
  }
}
