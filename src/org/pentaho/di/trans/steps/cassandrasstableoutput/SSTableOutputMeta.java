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

package org.pentaho.di.trans.steps.cassandrasstableoutput;

import java.util.List;
import java.util.Map;

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
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Provides metadata for the Cassandra SSTable output step.
 * 
 * @author Rob Turner (robert{[at]}robertturner{[dot]}com{[dot]}au)
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
@Step(id = "SSTableOutput", image = "Cassandra.png", name = "SSTable Output", description = "Writes to a filesystem directory as a Cassandra SSTable", categoryDescription = "Big Data")
public class SSTableOutputMeta extends BaseStepMeta implements
    StepMetaInterface {

  protected static final Class<?> PKG = SSTableOutputMeta.class;

  /** The path to the yaml file */
  protected String m_yamlPath;

  /** The directory to output to */
  protected String directory;

  /** The keyspace (database) to use */
  protected String cassandraKeyspace;

  /** The column family (table) to write to */
  protected String columnFamily = "";

  /** The field in the incoming data to use as the key for inserts */
  protected String keyField = "";

  /** Size (MB) of write buffer */
  protected String bufferSize = "16";

  /**
   * Get the path the the yaml file
   * 
   * @return the path to the yaml file
   */
  public String getYamlPath() {
    return m_yamlPath;
  }

  /**
   * Set the path the the yaml file
   * 
   * @param path the path to the yaml file
   */
  public void setYamlPath(String path) {
    m_yamlPath = path;
  }

  /**
   * Where the SSTables are written to
   * 
   * @return String directory
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Where the SSTables are written to
   * 
   * @param directory String
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Set the keyspace (db) to use
   * 
   * @param keyspace the keyspace to use
   */
  public void setCassandraKeyspace(String keyspace) {
    cassandraKeyspace = keyspace;
  }

  /**
   * Get the keyspace (db) to use
   * 
   * @return the keyspace (db) to use
   */
  public String getCassandraKeyspace() {
    return cassandraKeyspace;
  }

  /**
   * Set the column family (table) to write to
   * 
   * @param colFam the name of the column family to write to
   */
  public void setColumnFamilyName(String colFam) {
    columnFamily = colFam;
  }

  /**
   * Get the name of the column family to write to
   * 
   * @return the name of the columm family to write to
   */
  public String getColumnFamilyName() {
    return columnFamily;
  }

  /**
   * Set the incoming field to use as the key for inserts
   * 
   * @param keyField the name of the incoming field to use as the key
   */
  public void setKeyField(String keyField) {
    this.keyField = keyField;
  }

  /**
   * Get the name of the incoming field to use as the key for inserts
   * 
   * @return the name of the incoming field to use as the key for inserts
   */
  public String getKeyField() {
    return keyField;
  }

  /**
   * Size (MB) of write buffer
   * 
   * @return String
   */
  public String getBufferSize() {
    return bufferSize;
  }

  /**
   * Size (MB) of write buffer
   * 
   * @param bufferSize String
   */
  public void setBufferSize(String bufferSize) {
    this.bufferSize = bufferSize;
  }

  @Override
  public boolean supportsErrorHandling() {
    // enable define error handling option
    return true;
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer();

    if (!Const.isEmpty(m_yamlPath)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("yaml_path", m_yamlPath));
    }

    if (!Const.isEmpty(directory)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("output_directory", directory));
    }

    if (!Const.isEmpty(cassandraKeyspace)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("cassandra_keyspace", cassandraKeyspace));
    }

    if (!Const.isEmpty(cassandraKeyspace)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("cassandra_keyspace", cassandraKeyspace));
    }

    if (!Const.isEmpty(columnFamily)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("column_family", columnFamily));
    }

    if (!Const.isEmpty(keyField)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("key_field", keyField));
    }

    if (!Const.isEmpty(bufferSize)) {
      retval.append("\n    ").append(
          XMLHandler.addTagValue("buffer_size_mb", bufferSize));
    }

    return retval.toString();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_yamlPath = XMLHandler.getTagValue(stepnode, "yaml_path");
    directory = XMLHandler.getTagValue(stepnode, "output_directory");
    cassandraKeyspace = XMLHandler.getTagValue(stepnode, "cassandra_keyspace");
    columnFamily = XMLHandler.getTagValue(stepnode, "column_family");
    keyField = XMLHandler.getTagValue(stepnode, "key_field");
    bufferSize = XMLHandler.getTagValue(stepnode, "buffer_size_mb");
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    m_yamlPath = rep.getStepAttributeString(id_step, 0, "yaml_path");
    directory = rep.getStepAttributeString(id_step, 0, "output_directory");
    cassandraKeyspace = rep.getStepAttributeString(id_step, 0,
        "cassandra_keyspace");
    columnFamily = rep.getStepAttributeString(id_step, 0, "column_family");
    keyField = rep.getStepAttributeString(id_step, 0, "key_field");
    bufferSize = rep.getStepAttributeString(id_step, 0, "buffer_size_mb");
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {

    if (!Const.isEmpty(m_yamlPath)) {
      rep.saveStepAttribute(id_transformation, id_step, "yaml_path", m_yamlPath);
    }

    if (!Const.isEmpty(directory)) {
      rep.saveStepAttribute(id_transformation, id_step, "output_directory",
          directory);
    }

    if (!Const.isEmpty(cassandraKeyspace)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_keyspace",
          cassandraKeyspace);
    }

    if (!Const.isEmpty(columnFamily)) {
      rep.saveStepAttribute(id_transformation, id_step, "column_family",
          columnFamily);
    }

    if (!Const.isEmpty(keyField)) {
      rep.saveStepAttribute(id_transformation, id_step, "key_field", keyField);
    }

    if (!Const.isEmpty(bufferSize)) {
      rep.saveStepAttribute(id_transformation, id_step, "buffer_size_mb",
          bufferSize);
    }

  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input,
      String[] output, RowMetaInterface info) {

    CheckResult cr;

    if ((prev == null) || (prev.size() == 0)) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
          "Not receiving any fields from previous steps!", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is connected to previous one, receiving " + prev.size()
              + " fields", stepMeta);
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

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {

    return new SSTableOutput(stepMeta, stepDataInterface, copyNr, transMeta,
        trans);
  }

  public StepDataInterface getStepData() {
    return new SSTableOutputData();
  }

  public void setDefault() {
    directory = System.getProperty("java.io.tmpdir");
    bufferSize = "16";
    columnFamily = "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.di.trans.step.BaseStepMeta#getDialogClassName()
   */
  @Override
  public String getDialogClassName() {
    return "org.pentaho.di.trans.steps.cassandrasstableoutput.SSTableOutputDialog";
  }
}
