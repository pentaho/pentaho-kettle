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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.ObjectFilter;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.rules.Rules.Row;

public class RulesAccumulatorData extends BaseStepData implements StepDataInterface {
  private static Class<?> PKG = RulesAccumulator.class; // for i18n purposes

  private RowMetaInterface outputRowMeta;
  private RowMetaInterface inputRowMeta;

  private KnowledgeBuilder kbuilder;

  private KnowledgeBase kbase;

  private List<Object[]> results;

  private String ruleString;
  
  private List<Row> rowList = new ArrayList<Row>();
  private List<Row> resultList = new ArrayList<Row>();

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

  public void initializeInput(RowMetaInterface _inputRowMeta) {
    if (_inputRowMeta == null) {
      BaseMessages.getString(PKG, "RulesData.InitializeColumns.InputRowMetaIsNull"); //$NON-NLS-1$
      return;
    }

    this.inputRowMeta = _inputRowMeta;
  }

  public void loadRow(Object[] r) throws Exception {
    // Store rows for processing
    Map<String, Object> columns = new Hashtable<String, Object>();
    for(String field : inputRowMeta.getFieldNames()) {
      columns.put(field, r[inputRowMeta.indexOfValue(field)]);
    }
    
    rowList.add(new Row(columns, true));
  }
  
  public List<Row> getResultRows() {
    return resultList;
  }

  public void execute() throws Exception {
    if(kbase != null) {
      StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
      
      for(Row row : rowList) {
        session.insert(row);
      }
  
      session.fireAllRules();
  
      Collection<Object> oList = session.getObjects(new ObjectFilter() {
        @Override
        public boolean accept(Object o) {
          if(o instanceof Row && !((Row)o).isExternalSource()) {
            return true;
          }
          return false;
        }
      });
      
      for(Object o : oList) {
        resultList.add((Row)o);
      }
  
      session.dispose();
    }
  }

  /**
   * Get the list of rows generated by the Rules execution
   * @return List of rows generated
   */
  public List<Object[]> fetchResults() {
    return results;
  }

  public void shutdown() {
  }
}
