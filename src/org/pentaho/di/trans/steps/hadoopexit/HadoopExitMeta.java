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

package org.pentaho.di.trans.steps.hadoopexit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
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

@Step(id = "HadoopExitPlugin", image = "MRO.png", name = "MapReduce Output", description = "Exit a Hadoop Mapper or Reducer transformation", categoryDescription = "Big Data")
public class HadoopExitMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = HadoopExit.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static String OUT_KEY_FIELDNAME = "outkeyfieldname";

  private static String OUT_VALUE_FIELDNAME = "outvaluefieldname";

  private String outKeyFieldname;

  private String outValueFieldname;

  public HadoopExitMeta() throws Throwable {
    super();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    readData(stepnode);
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append("    ").append(XMLHandler.addTagValue(HadoopExitMeta.OUT_KEY_FIELDNAME, getOutKeyFieldname()));
    retval.append("    ").append(XMLHandler.addTagValue(HadoopExitMeta.OUT_VALUE_FIELDNAME, getOutValueFieldname()));

    return retval.toString();
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  private void readData(Node stepnode) {
    setOutKeyFieldname(XMLHandler.getTagValue(stepnode, HadoopExitMeta.OUT_KEY_FIELDNAME)); //$NON-NLS-1$
    setOutValueFieldname(XMLHandler.getTagValue(stepnode, HadoopExitMeta.OUT_VALUE_FIELDNAME)); //$NON-NLS-1$
  }

  public void setDefault() {
    setOutKeyFieldname(null);
    setOutValueFieldname(null);
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    setOutKeyFieldname(rep.getStepAttributeString(id_step, HadoopExitMeta.OUT_KEY_FIELDNAME)); //$NON-NLS-1$
    setOutValueFieldname(rep.getStepAttributeString(id_step, HadoopExitMeta.OUT_VALUE_FIELDNAME)); //$NON-NLS-1$
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    rep.saveStepAttribute(id_transformation, id_step, HadoopExitMeta.OUT_KEY_FIELDNAME, getOutKeyFieldname()); //$NON-NLS-1$
    rep.saveStepAttribute(id_transformation, id_step, HadoopExitMeta.OUT_VALUE_FIELDNAME, getOutValueFieldname()); //$NON-NLS-1$
  }

  public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space) throws KettleStepException {
    
    // The output consists of 2 fields: outKey and outValue
    // The data types rely on the input data type so we look those up
    //
    ValueMetaInterface keyMeta = rowMeta.searchValueMeta(getOutKeyFieldname()).clone();
    ValueMetaInterface valueMeta = rowMeta.searchValueMeta(getOutValueFieldname()).clone();
    
    keyMeta.setName("outKey");
    valueMeta.setName("outValue");
    
    rowMeta.clear();

    rowMeta.addValueMeta(keyMeta);
    rowMeta.addValueMeta(valueMeta);
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev,
      String input[], String output[], RowMetaInterface info) {
    CheckResult cr;

    // Make sure we have an input stream that contains the desired field names
    if (prev == null || prev.size() == 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
          "HadoopExitMeta.CheckResult.NoDataStream"), stepinfo); //$NON-NLS-1$
      remarks.add(cr);
    } else {
      List<String> fieldnames = Arrays.asList(prev.getFieldNames());

      HadoopExitMeta stepMeta = (HadoopExitMeta) stepinfo.getStepMetaInterface();

      if ((stepMeta.getOutKeyFieldname() == null) || stepMeta.getOutValueFieldname() == null) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "HadoopExitMeta.CheckResult.NoSpecifiedFields", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
        remarks.add(cr);
      } else {

        if (fieldnames.contains(stepMeta.getOutKeyFieldname()) && fieldnames.contains(stepMeta.getOutValueFieldname())) {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
              "HadoopExitMeta.CheckResult.StepRecevingData", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
          remarks.add(cr);
        } else {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
              "HadoopExitMeta.CheckResult.NotRecevingSpecifiedFields", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
          remarks.add(cr);
        }
      }
    }
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans) {
    return new HadoopExit(stepMeta, stepDataInterface, cnr, tr, trans);
  }

  public StepDataInterface getStepData() {
    return new HadoopExitData();
  }

  public String getOutKeyFieldname() {
    return outKeyFieldname;
  }

  public void setOutKeyFieldname(String arg) {
    outKeyFieldname = arg;
  }

  public String getOutValueFieldname() {
    return outValueFieldname;
  }

  public void setOutValueFieldname(String arg) {
    outValueFieldname = arg;
  }
}