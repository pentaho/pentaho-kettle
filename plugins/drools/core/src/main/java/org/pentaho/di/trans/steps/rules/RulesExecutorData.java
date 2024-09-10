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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.rules.Rules.Column;

/**
 * This Transformation Step allows a user to execute a rule set against an individual rule or a collection of rules.
 * <p>
 * Additional columns can be added to the output from the rules and these (of course) can be used for routing if
 * desired.
 *
 * @author cboyden
 *
 */

public class RulesExecutorData extends BaseStepData implements StepDataInterface {
  private static final Class<?> PKG = RulesExecutor.class; // for i18n purposes

  private RowMetaInterface outputRowMeta;

  private KieBase kieBase;

  private Column[] columnList;

  private final Map<String, Column> resultMap = new HashMap<>();

  private String ruleString;

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

  public void initializeColumns( RowMetaInterface inputRowMeta ) {
    if ( inputRowMeta == null ) {
      BaseMessages.getString( PKG, "RulesData.InitializeColumns.InputRowMetaIsNull" );
      return;
    }

    // Create objects for insertion into the rules engine
    List<ValueMetaInterface> columns = inputRowMeta.getValueMetaList();

    // This array must 1-1 match the row[] feteched by getRow()
    columnList = new Column[columns.size()];

    for ( int i = 0; i < columns.size(); i++ ) {
      ValueMetaInterface column = columns.get( i );

      Column c = new Column( true );
      c.setName( column.getName() );
      c.setType( column.getTypeDesc() );
      c.setPayload( null );

      columnList[i] = c;
    }
  }

  public void loadRow( Object[] r ) {
    for ( int i = 0; i < columnList.length; i++ ) {
      columnList[i].setPayload( r[i] );
    }
    resultMap.clear();
  }

  public void execute() {
    KieSession kieSession = initNewKnowledgeSession();

    Collection<Object> oList = fetchColumns( kieSession );
    for ( Object o : oList ) {
      resultMap.put( ( (Column) o ).getName(), (Column) o );
    }

    kieSession.dispose();
  }

  protected KieSession initNewKnowledgeSession() {
    KieSession kieSession = kieBase.newKieSession();
    for ( Column column : columnList ) {
      kieSession.insert( column );
    }

    kieSession.fireAllRules();
    return kieSession;
  }

  protected Collection<Object> fetchColumns( KieSession kieSession ) {
    Collection<?> oList = kieSession.getObjects( new ObjectFilter() {

      @Override
      public boolean accept( Object o ) {
        return o instanceof Rules.Row && !( (Rules.Row) o ).isExternalSource();
      }
    } );

    return (Collection<Object>) oList;
  }

  /**
   *
   * @param columnName
   *          Column.payload associated with the result, or null if not found
   */
  public Object fetchResult( String columnName ) {
    return resultMap.get( columnName );
  }

  public void shutdown() {
  }

}
