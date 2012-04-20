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

package org.pentaho.di.trans.steps.hbaserowdecoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
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
import org.pentaho.hbase.mapping.HBaseValueMeta;
import org.pentaho.hbase.mapping.Mapping;
import org.pentaho.di.core.Const;
import org.w3c.dom.Node;

/**
 * Meta class for the HBase row decoder.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 *
 */
@Step(id = "HBaseRowDecoder", image = "HBRD.png", name = "HBase Row Decoder", description="Decodes an incoming key and HBase result object according to a mapping", categoryDescription="Big Data")
public class HBaseRowDecoderMeta extends BaseStepMeta implements
    StepMetaInterface {
  
  /** The incoming field that contains the HBase row key */
  protected String m_incomingKeyField = "";
  
  /** The incoming field that contains the HBase row Result object */
  protected String m_incomingResultField = "";
  
  /** The mapping to use */
  protected Mapping m_mapping;
  
  /**
   * Set the incoming field that holds the HBase row key
   * 
   * @param inKey the name of the field that holds the key
   */
  public void setIncomingKeyField(String inKey) {
    m_incomingKeyField = inKey;
  }
  
  /**
   * Get the incoming field that holds the HBase row key
   * 
   * @return the name of the field that holds the key
   */
  public String getIncomingKeyField() {
    return m_incomingKeyField;
  }
  
  /**
   * Set the incoming field that holds the HBase row Result object
   * 
   * @param inResult the name of the field that holds the HBase row Result object
   */
  public void setIncomingResultField(String inResult) {
    m_incomingResultField = inResult;
  }
  
  /**
   * Get the incoming field that holds the HBase row Result object
   * 
   * @return the name of the field that holds the HBase row Result object
   */
  public String getIncomingResultField() {
    return m_incomingResultField;
  }
  
  /**
   * Set the mapping to use for decoding the row
   * 
   * @param m the mapping to use
   */
  public void setMapping(Mapping m) {
    m_mapping = m;
  }
  
  /**
   * Get the mapping to use for decoding the row
   * 
   * @return the mapping to use
   */
  public Mapping getMapping() {
    return m_mapping;
  }
  
  public void setDefault() {
    m_incomingKeyField = "";
    m_incomingResultField = "";
  }
  
  public void getFields(RowMetaInterface rowMeta, String origin, 
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) 
    throws KettleStepException {
    
    rowMeta.clear(); // start afresh - eats the input
    
    if (m_mapping != null) {
      if (!Const.isEmpty(m_mapping.getMappingName())) {
        int kettleType;
        
        if (m_mapping.getKeyType() == Mapping.KeyType.DATE || 
            m_mapping.getKeyType() == Mapping.KeyType.UNSIGNED_DATE) {
          kettleType = ValueMetaInterface.TYPE_DATE;
        } else if (m_mapping.getKeyType() == Mapping.KeyType.STRING) {
          kettleType = ValueMetaInterface.TYPE_STRING;
        } else {
          kettleType = ValueMetaInterface.TYPE_INTEGER;
        }

        ValueMetaInterface keyMeta = new ValueMeta(m_mapping.getKeyName(), kettleType);

        keyMeta.setOrigin(origin);
        rowMeta.addValueMeta(keyMeta);
        
        // Add the rest of the fields in the mapping
        Map<String, HBaseValueMeta> mappedColumnsByAlias = 
          m_mapping.getMappedColumns();
        Set<String> aliasSet = mappedColumnsByAlias.keySet();
        for (String alias : aliasSet) {
          HBaseValueMeta columnMeta = mappedColumnsByAlias.get(alias);
          columnMeta.setOrigin(origin);
          rowMeta.addValueMeta(columnMeta);
        }
      }
    }
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

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    
    return new HBaseRowDecoder(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new HBaseRowDecoderData();
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_incomingKeyField)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("incoming_key_field", 
          m_incomingKeyField));
    }
    if (!Const.isEmpty(m_incomingResultField)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("incoming_result_field", 
          m_incomingResultField));
    }
    
    if (m_mapping != null) {
      retval.append(m_mapping.getXML());
    }
    
    return retval.toString();
  }
 
  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
        Map<String, Counter> counters) throws KettleXMLException {
    
    m_incomingKeyField = XMLHandler.getTagValue(stepnode, "incoming_key_field");
    m_incomingResultField = XMLHandler.getTagValue(stepnode, "incoming_result_field");
    
    m_mapping = new Mapping();
    m_mapping.loadXML(stepnode);
    
  }
  
  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
        throws KettleException {

    m_incomingKeyField = rep.getStepAttributeString(id_step, 0, "incoming_key_field");
    m_incomingResultField = rep.getStepAttributeString(id_step, 0, "incoming_result_field");
    
    m_mapping = new Mapping();
    m_mapping.readRep(rep, id_step);
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {
    
    if (!Const.isEmpty(m_incomingKeyField)) {
      rep.saveStepAttribute(id_transformation, id_step, 0, "incoming_key_field", 
          m_incomingKeyField);
    }
    if (!Const.isEmpty(m_incomingResultField)) {
      rep.saveStepAttribute(id_transformation, id_step, 0, "incoming_result_field", 
          m_incomingResultField);
    }
    
    if (m_mapping != null) {
      m_mapping.saveRep(rep, id_transformation, id_step);
    }
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

    return new HBaseRowDecoderDialog(shell, meta, transMeta, name);
  }

}
