/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.trans.steps.rules;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.rules.Rules.Column;

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

public class RulesExecutorData extends BaseStepData implements StepDataInterface {
  private static Class<?> PKG = RulesExecutor.class; // for i18n purposes

  private RowMetaInterface outputRowMeta;

  private KnowledgeBuilder kbuilder;

  private KnowledgeBase kbase;

  private Column[] columnList;

  private Map<String, Column> resultMap = new HashMap<String, Column>();

  private String ruleString;

  public String getRuleString() {
    return ruleString;
  }

  public void setRuleString(String ruleString) {
    this.ruleString = ruleString;
  }

  public String getRuleFilePath() {
    return ruleFilePath;
  }

  public void setRuleFilePath(String ruleFilePath) {
    this.ruleFilePath = ruleFilePath;
  }

  private String ruleFilePath;

  public void setOutputRowMeta(RowMetaInterface outputRowMeta) {
    this.outputRowMeta = outputRowMeta;
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void initializeRules() {
    Resource ruleSet = null;
    if (ruleString != null) {
      ruleSet = ResourceFactory.newReaderResource(new StringReader(ruleString));
    } else {
      ruleSet = ResourceFactory.newFileResource(ruleFilePath);
    }
    kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    kbuilder.add(ruleSet, ResourceType.DRL);

    if (kbuilder.hasErrors()) {
      System.out.println(kbuilder.getErrors().toString());
      throw new RuntimeException(BaseMessages.getString(PKG, "RulesData.Error.CompileDRL")); //$NON-NLS-1$
    }

    Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

    kbase = KnowledgeBaseFactory.newKnowledgeBase();
    // Cache the knowledge base as its creation is intensive
    kbase.addKnowledgePackages(pkgs);
  }

  public void initializeColumns(RowMetaInterface inputRowMeta) {
    if (inputRowMeta == null) {
      BaseMessages.getString(PKG, "RulesData.InitializeColumns.InputRowMetaIsNull"); //$NON-NLS-1$
      return;
    }

    // Create objects for insertion into the rules engine
    List<ValueMetaInterface> columns = inputRowMeta.getValueMetaList();

    // This array must 1-1 match the row[] feteched by getRow()
    columnList = new Column[columns.size()];

    for (int i = 0; i < columns.size(); i++) {
      ValueMetaInterface column = columns.get(i);

      Column c = new Column(true);
      c.setName(column.getName());
      c.setType(column.getTypeDesc());
      c.setPayload(null);

      columnList[i] = c;
    }
  }

  public void loadRow(Object[] r) {
    for (int i = 0; i < columnList.length; i++) {
      columnList[i].setPayload(r[i]);
    }
  }

  public void execute() {
    StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
    for (int i = 0; i < columnList.length; i++) {
      session.insert(columnList[i]);
    }

    session.fireAllRules();

    Collection<Object> oList = session.getObjects(new ObjectFilter() {
      @Override
      public boolean accept(Object o) {
        if (o instanceof Column && !((Column) o).isExternalSource()) {
          return true;
        }
        return false;
      }
    });

    for (Object o : oList) {
      resultMap.put(((Column) o).getName(), (Column) o);
    }

    session.dispose();
  }

  /**
   * 
   * @param columnName Column.payload associated with the result, or null if not found
   * @return
   */
  public Object fetchResult(String columnName) {
    return resultMap.get(columnName);
  }

  public void shutdown() {
  }
}
