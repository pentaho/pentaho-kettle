/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.trans.steps.hbaseinput;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class HBaseInputData extends BaseStepData implements StepDataInterface {
  
  /** The output data format */
  protected RowMetaInterface m_outputRowMeta;
    
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }
  
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }
  
  public static Configuration getHBaseConnection(String zookeeperHosts, 
      URL coreConfig, URL defaultConfig) 
    throws IOException {
    Configuration con = new Configuration();
    
    if (defaultConfig != null) {
      // TODO - might have to check URL for spaces (need to for URIs)
      con.addResource(defaultConfig);
    } else {
      // hopefully it's in the classpath
      con.addResource("hbase-default.xml");
    }
    
    if (coreConfig != null) {
      // TODO - might have to check URL for spaces (need to for URIs)
      con.addResource(coreConfig);
    } else {
      // hopefully it's in the classpath
      con.addResource("hbase-site.xml");
    } 
    
    if (!Const.isEmpty(zookeeperHosts)) {
      // override default and site with this
      con.set("hbase.zookeeper.quorum", zookeeperHosts);
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