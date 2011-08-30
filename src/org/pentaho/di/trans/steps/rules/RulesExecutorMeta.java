/* Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
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

/**
 * This Transformation Step allows a user to execute a rule set against
 * an individual rule or a collection of rules.
 * 
 * Additional columns can be added to the output from the rules and these
 * (of course) can be used for routing if desired.
 * 
 * @author cboyden
 *
 */

public class RulesExecutorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = Rules.class; // for i18n purposes

  // Contain storage keys in single location to cut down on save/load bugs
  private static enum StorageKeys {
    NODE_FIELDS("fields"), //$NON-NLS-1$
    SUBNODE_FIELD("field"), //$NON-NLS-1$
    COLUMN_NAME("column-name"), //$NON-NLS-1$
    COLUMN_TYPE("column-type"), //$NON-NLS-1$
    RULE_FILE("rule-file"), //$NON-NLS-1$
    RULE_DEFINITION("rule-definition"); //$NON-NLS-1$

    private final String storageKey;

    StorageKeys(String name) {
      this.storageKey = name;
    }

    @Override
    public String toString() {
      return storageKey;
    }
  };

  private List<ValueMetaInterface> ruleResultColumns = new ArrayList<ValueMetaInterface>();

  private String ruleFile;

  private String ruleDefinition;

  private boolean keepInputFields = true;

  public List<ValueMetaInterface> getRuleResultColumns() {
    return ruleResultColumns;
  }

  public void setRuleResultColumns(List<ValueMetaInterface> ruleResultColumns) {
    this.ruleResultColumns = ruleResultColumns;
  }

  public void setRuleFile(String ruleFile) {
    this.ruleFile = ruleFile;
  }

  public String getRuleFile() {
    return ruleFile;
  }

  public void setRuleDefinition(String ruleDefinition) {
    this.ruleDefinition = ruleDefinition;
  }

  public String getRuleDefinition() {
    return ruleDefinition;
  }

  public boolean isKeepInputFields() {
    return keepInputFields;
  }

  public void setKeepInputFields(boolean keepInputFields) {
    this.keepInputFields = keepInputFields;
  }

  @Override
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    return new RulesExecutor(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  @Override
  public StepDataInterface getStepData() {
    return new RulesExecutorData();
  }

  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info) {
  }

  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> _databases, Map<String, Counter> counters)
      throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode(stepnode, StorageKeys.NODE_FIELDS.toString());
      int nrfields = XMLHandler.countNodes(fields, StorageKeys.SUBNODE_FIELD.toString());

      ValueMetaInterface vm = null;
      for (int i = 0; i < nrfields; i++) {
        vm = new ValueMeta();
        Node fnode = XMLHandler.getSubNodeByNr(fields, StorageKeys.SUBNODE_FIELD.toString(), i);

        vm.setName(XMLHandler.getTagValue(fnode, StorageKeys.COLUMN_NAME.toString()));
        vm.setType(ValueMeta.getType(XMLHandler.getTagValue(fnode, StorageKeys.COLUMN_TYPE.toString())));

        getRuleResultColumns().add(vm);
      }

      setRuleFile(XMLHandler.getTagValue(stepnode, StorageKeys.RULE_FILE.toString()));
      setRuleDefinition(XMLHandler.getTagValue(stepnode, StorageKeys.RULE_DEFINITION.toString()));
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "RulesMeta.Error.LoadFromXML"), e); //$NON-NLS-1$
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer(300);

    retval.append("    <" + StorageKeys.NODE_FIELDS + ">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < ruleResultColumns.size(); i++) {
      retval.append("      <" + StorageKeys.SUBNODE_FIELD + ">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
      retval
          .append("        ").append(XMLHandler.addTagValue(StorageKeys.COLUMN_NAME.toString(), ruleResultColumns.get(i).getName())); //$NON-NLS-1$
      retval
          .append("        ").append(XMLHandler.addTagValue(StorageKeys.COLUMN_TYPE.toString(), ruleResultColumns.get(i).getTypeDesc())); //$NON-NLS-1$
      retval.append("      </" + StorageKeys.SUBNODE_FIELD + ">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    }
    retval.append("    </" + StorageKeys.NODE_FIELDS + ">").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue(StorageKeys.RULE_FILE.toString(), getRuleFile())); //$NON-NLS-1$
    retval.append("    ").append(XMLHandler.addTagValue(StorageKeys.RULE_DEFINITION.toString(), getRuleDefinition())); //$NON-NLS-1$

    return retval.toString();
  }

  @Override
  public void readRep(Repository rep, ObjectId idStep, List<DatabaseMeta> _databases, Map<String, Counter> counters)
      throws KettleException {

    int nrfields = rep.countNrStepAttributes(idStep, StorageKeys.COLUMN_NAME.toString());

    ValueMetaInterface vm = null;
    for (int i = 0; i < nrfields; i++) {
      vm = new ValueMeta();

      vm.setName(rep.getStepAttributeString(idStep, i, StorageKeys.COLUMN_NAME.toString()));
      vm.setType(ValueMeta.getType(rep.getStepAttributeString(idStep, i, StorageKeys.COLUMN_TYPE.toString())));

      getRuleResultColumns().add(vm);
    }

    setRuleFile(rep.getStepAttributeString(idStep, StorageKeys.RULE_FILE.toString()));
    setRuleDefinition(rep.getStepAttributeString(idStep, StorageKeys.RULE_DEFINITION.toString()));
  }

  @Override
  public void saveRep(Repository rep, ObjectId idTransformation, ObjectId idStep) throws KettleException {

    for (int i = 0; i < ruleResultColumns.size(); i++) {
      rep.saveStepAttribute(idTransformation, idStep, i, StorageKeys.COLUMN_NAME.toString(), ruleResultColumns.get(i)
          .getName());
      rep.saveStepAttribute(idTransformation, idStep, i, StorageKeys.COLUMN_TYPE.toString(), ruleResultColumns.get(i)
          .getTypeDesc());
    }

    rep.saveStepAttribute(idTransformation, idStep, StorageKeys.RULE_FILE.toString(), getRuleFile());
    rep.saveStepAttribute(idTransformation, idStep, StorageKeys.RULE_DEFINITION.toString(), getRuleDefinition());
  }

  @Override
  public void setDefault() {
  }

  @Override
  public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space) throws KettleStepException {
    if (!keepInputFields) {
      row.clear();
    }

    if (ruleResultColumns != null) {
      for (int i = 0; i < ruleResultColumns.size(); i++) {
        row.addValueMeta(ruleResultColumns.get(i));
      }
    }
  }

  public String[] getExpectedResultList() {
    String[] result = new String[ruleResultColumns.size()];

    for (int i = 0; i < ruleResultColumns.size(); i++) {
      result[i] = ruleResultColumns.get(i).getName();
    }

    return result;
  }

}
