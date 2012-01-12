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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

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
@Step(id = "HBaseOutput", image = "HBO.png", name = "HBase Output", description="Writes data to an HBase table according to a mapping", categoryDescription="Hadoop")
public class HBaseOutputMeta extends BaseStepMeta implements StepMetaInterface {
  
  /** comma separated list of hosts that the zookeeper quorum is running on */
  protected String m_zookeeperHosts;
  
  /** the port that zookeeper is listening on - if blank, then the default is used */
  protected String m_zookeeperPort;
  
  /** path/url to hbase-site.xml */
  protected String m_coreConfigURL;
  
  /** path/url to hbase-default.xml */
  protected String m_defaultConfigURL;
  
  /** the name of the HBase table to write to */
  protected String m_targetTableName;
  
  /** the name of the mapping for columns/types for the target table */
  protected String m_targetMappingName;
  
  /** if true then the WAL will not be written to */
  protected boolean m_disableWriteToWAL;
  
  /** The size of the write buffer in bytes (empty - default from hbase-default.xml is used) */
  protected String m_writeBufferSize;
  
  public void setZookeeperHosts(String z) {
    m_zookeeperHosts = z;
  }
  
  public String getZookeeperHosts() {
    return m_zookeeperHosts;
  }
  
  /**
   * Set the port that zookeeper is listening on
   * 
   * @param port the port
   */
  public void setZookeeperPort(String port) {
    m_zookeeperPort = port;
  }
  
  /**
   * Get the port that zookeeper is listening on
   * 
   * @return the port
   */
  public String getZookeeperPort() {
    return m_zookeeperPort;
  }
  
  public void setCoreConfigURL(String coreConfig) {
    m_coreConfigURL = coreConfig;
  }
  
  public String getCoreConfigURL() {
    return m_coreConfigURL;
  }
  
  public void setDefaulConfigURL(String defaultConfig) {
    m_defaultConfigURL = defaultConfig;
  }
  
  public String getDefaultConfigURL() {
    return m_defaultConfigURL;
  }
  
  public void setTargetTableName(String targetTable) {
    m_targetTableName = targetTable;
  }
  
  public String getTargetTableName() {
    return m_targetTableName;
  }
  
  public void setTargetMappingName(String targetMapping) {
    m_targetMappingName = targetMapping;
  }
  
  public String getTargetMappingName() {
    return m_targetMappingName;
  }
  
  public void setDisableWriteToWAL(boolean d) {
    m_disableWriteToWAL = d;
  }
  
  public boolean getDisableWriteToWAL() {
    return m_disableWriteToWAL;
  }
  
  public void setWriteBufferSize(String size) {
    m_writeBufferSize = size;
  }
  
  public String getWriteBufferSize() {
    return m_writeBufferSize;
  }
  

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
      RowMetaInterface info) {

    CheckResult cr;

    if ((prev == null) || (prev.size() == 0)) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
          "Not receiving any fields from previous steps!", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is connected to previous one, receiving " + prev.size() +
          " fields", stepMeta);
      remarks.add(cr);
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is receiving info from other steps.", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
          "No input received from other steps!", stepMeta);
      remarks.add(cr);
    }
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_zookeeperHosts)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("zookeeper_hosts", 
          m_zookeeperHosts));
    }
    if (!Const.isEmpty(m_zookeeperPort)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("zookeeper_port", 
          m_zookeeperPort));
    }
    if (!Const.isEmpty(m_coreConfigURL)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("core_config_url", 
          m_coreConfigURL));
    }
    if (!Const.isEmpty(m_defaultConfigURL)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("default_config_url", 
          m_defaultConfigURL));
    }
    if (!Const.isEmpty(m_targetTableName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("target_table_name", 
          m_targetTableName));
    }
    if (!Const.isEmpty(m_targetMappingName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("target_mapping_name", 
          m_targetMappingName));
    }
    if (!Const.isEmpty(m_writeBufferSize)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("write_buffer_size", 
          m_writeBufferSize));
    }
    retval.append("\n    ").append(XMLHandler.addTagValue("disable_wal", m_disableWriteToWAL));
    
    return retval.toString();
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, 
      int copyNr, TransMeta transMeta, Trans trans) {
    return new HBaseOutput(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new HBaseOutputData();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    
    m_zookeeperHosts = XMLHandler.getTagValue(stepnode, "zookeeper_hosts");
    m_zookeeperPort = XMLHandler.getTagValue(stepnode, "zookeeper_port");
    m_coreConfigURL = XMLHandler.getTagValue(stepnode, "core_config_url");
    m_defaultConfigURL = XMLHandler.getTagValue(stepnode, "default_config_url"); 
    m_targetTableName = XMLHandler.getTagValue(stepnode, "target_table_name");
    m_targetMappingName = XMLHandler.getTagValue(stepnode, "target_mapping_name");
    m_writeBufferSize = XMLHandler.getTagValue(stepnode, "write_buffer_size");
    String disableWAL = XMLHandler.getTagValue(stepnode, "disable_wal");
    m_disableWriteToWAL = disableWAL.equalsIgnoreCase("Y");
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleException {
    
    m_zookeeperHosts = rep.getStepAttributeString(id_step, 0, "zookeeper_hosts");
    m_zookeeperPort = rep.getStepAttributeString(id_step, 0, "zookeeper_port");
    m_coreConfigURL = rep.getStepAttributeString(id_step, 0, "core_config_url");
    m_defaultConfigURL = rep.getStepAttributeString(id_step, 0, "default_config_url");
    m_targetTableName = rep.getStepAttributeString(id_step, 0, "target_table_name");
    m_targetMappingName = rep.getStepAttributeString(id_step, 0, "target_mapping_name");
    m_writeBufferSize = rep.getStepAttributeString(id_step, 0, "write_buffer_size");
    m_disableWriteToWAL = rep.getStepAttributeBoolean(id_step, 0, "disable_wal");
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
      throws KettleException {
    if (!Const.isEmpty(m_zookeeperHosts)) {
      rep.saveStepAttribute(id_transformation, id_step, "zookeeper_hosts", 
          m_zookeeperHosts);
    }
    if (!Const.isEmpty(m_zookeeperPort)) {
      rep.saveStepAttribute(id_transformation, id_step, "zookeeper_port", 
          m_zookeeperPort);
    }
    if (!Const.isEmpty(m_coreConfigURL)) {
      rep.saveStepAttribute(id_transformation, id_step, "core_config_url", 
          m_coreConfigURL);
    }
    if (!Const.isEmpty(m_defaultConfigURL)) {
      rep.saveStepAttribute(id_transformation, id_step, "default_config_url", 
          m_defaultConfigURL);
    }
    if (!Const.isEmpty(m_targetTableName)) {
      rep.saveStepAttribute(id_transformation, id_step,"target_table_name", 
          m_targetTableName);
    }
    if (!Const.isEmpty(m_targetMappingName)) {
      rep.saveStepAttribute(id_transformation, id_step,"target_mapping_name", 
          m_targetMappingName);
    }
    if (!Const.isEmpty(m_writeBufferSize)) {
      rep.saveStepAttribute(id_transformation, id_step,"write_buffer_size", 
          m_writeBufferSize);
    }
    rep.saveStepAttribute(id_transformation, id_step,"disable_wal", 
        m_disableWriteToWAL);
  }

  public void setDefault() {
    m_coreConfigURL = null;
    m_defaultConfigURL = null;
    m_targetTableName = null;
    m_targetMappingName = null;
    m_disableWriteToWAL = false;
    m_writeBufferSize = null;
  }
  
  /**
   * Get the UI for this step.
   *
   * @param shell a <code>Shell</code> value
   * @param meta a <code>StepMetaInterface</code> value
   * @param transMeta a <code>TransMeta</code> value
   * @param name a <code>String</code> value
   * @return a <code>StepDialogInterface</code> value
   */
  public StepDialogInterface getDialog(Shell shell, 
                                       StepMetaInterface meta,
                                       TransMeta transMeta, 
                                       String name) {

    return new HBaseOutputDialog(shell, meta, transMeta, name);

  }
}
