/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.rules.Rules.Row;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class RulesAccumulatorData extends BaseStepData implements StepDataInterface {
  private static final Class<?> PKG = RulesAccumulator.class; // for i18n purposes

  private RowMetaInterface outputRowMeta;
  private RowMetaInterface inputRowMeta;

  private KieBase kieBase;

  private List<Object[]> results;

  private String ruleString;

  private final List<Row> rowList = new ArrayList<>();
  private final List<Row> resultList = new ArrayList<>();

  public String getRuleString() {
    return ruleString;
  }

  public void setRuleString( String ruleString ) {
    this.ruleString = ruleString;
  }

  public String getRuleFilePath() {
    return ruleFilePath;
  }

  public void setRuleFilePath( String ruleFilePath ) {
    this.ruleFilePath = ruleFilePath;
  }

  private String ruleFilePath;

  public void setOutputRowMeta( RowMetaInterface outputRowMeta ) {
    this.outputRowMeta = outputRowMeta;
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void initializeRules() {

    // To ensure the plugin classloader use for dependency resolution
    ClassLoader orig = Thread.currentThread().getContextClassLoader();
    ClassLoader loader = getClass().getClassLoader();
    Thread.currentThread().setContextClassLoader( loader );
    KieServices kieServices = KieServices.Factory.get();
    KieFileSystem kfs = kieServices.newKieFileSystem();
    String internalFilePath = "src/main/resources/kettle.drl";

    if ( ruleString != null ) {
      kfs.write( internalFilePath, ruleString );
    } else {
      try {
        FileInputStream fis = new FileInputStream( ruleFilePath );
        kfs.write( internalFilePath, kieServices.getResources().newInputStreamResource( fis ) );
      } catch ( FileNotFoundException e ) {
        throw new RuntimeException( BaseMessages.getString( PKG, "RulesData.Error.CompileDRL" ) );
      }
    }
    KieBuilder kieBuilder = kieServices.newKieBuilder( kfs ).buildAll();
    Results results = kieBuilder.getResults();

    if ( results.hasMessages( Message.Level.ERROR ) ) {
      System.out.println( results.getMessages() );
      throw new RuntimeException( BaseMessages.getString( PKG, "RulesData.Error.CompileDRL" ) );
    }

    KieContainer kieContainer = kieServices.newKieContainer( kieServices.getRepository().getDefaultReleaseId() );
    kieBase = kieContainer.getKieBase();

    // reset classloader back to original
    Thread.currentThread().setContextClassLoader( orig );
  }

  public void initializeInput( RowMetaInterface _inputRowMeta ) {
    if ( _inputRowMeta == null ) {
      BaseMessages.getString( PKG, "RulesData.InitializeColumns.InputRowMetaIsNull" );
      return;
    }

    this.inputRowMeta = _inputRowMeta;
  }

  public void loadRow( Object[] r ) {
    // Store rows for processing
    Map<String, Object> columns = new Hashtable<>();
    for ( String field : inputRowMeta.getFieldNames() ) {
      columns.put( field, r[inputRowMeta.indexOfValue( field )] );
    }

    rowList.add( new Row( columns, true ) );
  }

  public List<Row> getResultRows() {
    return resultList;
  }

  public void execute() throws Exception {
    if ( kieBase != null ) {
      KieSession kieSession = kieBase.newKieSession();

      for ( Row row : rowList ) {
        kieSession.insert( row );
      }

      kieSession.fireAllRules();

      Collection<?> oList = kieSession.getObjects( new ObjectFilter() {

        @Override
        public boolean accept( Object o ) {
          return o instanceof Row && !( (Row) o ).isExternalSource();
        }
      } );

      for ( Object o : oList ) {
        resultList.add( (Row) o );
      }

      kieSession.dispose();
    }
  }

  /**
   * Get the list of rows generated by the Rules execution
   *
   * @return List of rows generated
   */
  public List<Object[]> fetchResults() {
    return results;
  }

  public void shutdown() {
  }
}
