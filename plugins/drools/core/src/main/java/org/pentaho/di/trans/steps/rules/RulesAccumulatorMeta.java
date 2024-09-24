/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step( id = "RuleAccumulator",
        image = "rules_acc.svg",
        i18nPackageName = "org.pentaho.di.trans.steps.rules",
        name = "RulesAccumulator.StepConfigurationDialog.Title",
        description = "RulesAccumulator.StepConfigurationDialog.TooltipDesc",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Scripting" )

public class RulesAccumulatorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = Rules.class; // for i18n purposes

  // Contain storage keys in single location to cut down on save/load bugs
  private static enum StorageKeys {
    NODE_FIELDS( "fields" ), SUBNODE_FIELD( "field" ), COLUMN_NAME( "column-name" ), COLUMN_TYPE( "column-type" ),
      RULE_FILE(
        "rule-file" ), RULE_DEFINITION( "rule-definition" );

    private final String storageKey;

    StorageKeys( String name ) {
      this.storageKey = name;
    }

    @Override
    public String toString() {
      return storageKey;
    }
  }

  private List<ValueMetaInterface> ruleResultColumns = new ArrayList<ValueMetaInterface>();

  private String ruleFile;

  private String ruleDefinition;

  private boolean keepInputFields = true;

  public List<ValueMetaInterface> getRuleResultColumns() {
    return ruleResultColumns;
  }

  public void setRuleResultColumns( List<ValueMetaInterface> ruleResultColumns ) {
    this.ruleResultColumns = ruleResultColumns;
  }

  public void setRuleFile( String ruleFile ) {
    this.ruleFile = ruleFile;
  }

  public String getRuleFile() {
    return ruleFile;
  }

  public void setRuleDefinition( String ruleDefinition ) {
    this.ruleDefinition = ruleDefinition;
  }

  public String getRuleDefinition() {
    return ruleDefinition;
  }

  public boolean isKeepInputFields() {
    return keepInputFields;
  }

  public void setKeepInputFields( boolean keepInputFields ) {
    this.keepInputFields = keepInputFields;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    return new RulesAccumulator( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new RulesAccumulatorData();
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> _databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      Node fields = XMLHandler.getSubNode( stepnode, StorageKeys.NODE_FIELDS.toString() );
      int nrfields = XMLHandler.countNodes( fields, StorageKeys.SUBNODE_FIELD.toString() );

      ValueMetaInterface vm = null;
      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, StorageKeys.SUBNODE_FIELD.toString(), i );

        String name = XMLHandler.getTagValue( fnode, StorageKeys.COLUMN_NAME.toString() );
        int type = ValueMeta.getType( XMLHandler.getTagValue( fnode, StorageKeys.COLUMN_TYPE.toString() ) );
        vm = ValueMetaFactory.createValueMeta( name, type );

        getRuleResultColumns().add( vm );
      }

      setRuleFile( XMLHandler.getTagValue( stepnode, StorageKeys.RULE_FILE.toString() ) );
      setRuleDefinition( XMLHandler.getTagValue( stepnode, StorageKeys.RULE_DEFINITION.toString() ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "RulesMeta.Error.LoadFromXML" ), e );
    }
  }

  @Override
  public String getXML() {
    StringBuffer retval = new StringBuffer( 300 );

    retval.append( "    <" + StorageKeys.NODE_FIELDS + ">" ).append( Const.CR );
    for ( int i = 0; i < ruleResultColumns.size(); i++ ) {
      retval.append( "      <" + StorageKeys.SUBNODE_FIELD + ">" ).append( Const.CR );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( StorageKeys.COLUMN_NAME.toString(), ruleResultColumns.get( i ).getName() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( StorageKeys.COLUMN_TYPE.toString(), ruleResultColumns.get( i ).getTypeDesc() ) );
      retval.append( "      </" + StorageKeys.SUBNODE_FIELD + ">" ).append( Const.CR );
    }
    retval.append( "    </" + StorageKeys.NODE_FIELDS + ">" ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( StorageKeys.RULE_FILE.toString(), getRuleFile() ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( StorageKeys.RULE_DEFINITION.toString(), getRuleDefinition() ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> _databases ) throws KettleException {

    int nrfields = rep.countNrStepAttributes( idStep, StorageKeys.COLUMN_NAME.toString() );

    ValueMetaInterface vm = null;
    for ( int i = 0; i < nrfields; i++ ) {

      String name = rep.getStepAttributeString( idStep, i, StorageKeys.COLUMN_NAME.toString() );
      int type = ValueMeta.getType( rep.getStepAttributeString( idStep, i, StorageKeys.COLUMN_TYPE.toString() ) );

      vm = ValueMetaFactory.createValueMeta( name, type );
      getRuleResultColumns().add( vm );
    }

    setRuleFile( rep.getStepAttributeString( idStep, StorageKeys.RULE_FILE.toString() ) );
    setRuleDefinition( rep.getStepAttributeString( idStep, StorageKeys.RULE_DEFINITION.toString() ) );
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {

    for ( int i = 0; i < ruleResultColumns.size(); i++ ) {
      rep.saveStepAttribute( idTransformation, idStep, i, StorageKeys.COLUMN_NAME.toString(), ruleResultColumns
        .get( i ).getName() );
      rep.saveStepAttribute( idTransformation, idStep, i, StorageKeys.COLUMN_TYPE.toString(), ruleResultColumns
        .get( i ).getTypeDesc() );
    }

    rep.saveStepAttribute( idTransformation, idStep, StorageKeys.RULE_FILE.toString(), getRuleFile() );
    rep.saveStepAttribute( idTransformation, idStep, StorageKeys.RULE_DEFINITION.toString(), getRuleDefinition() );
  }

  @Override
  public void setDefault() {
  }

  @Override
  public void getFields( RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !keepInputFields ) {
      row.clear();
    }

    if ( ruleResultColumns != null ) {
      for ( int i = 0; i < ruleResultColumns.size(); i++ ) {
        ruleResultColumns.get( i ).setOrigin( origin );
        row.addValueMeta( ruleResultColumns.get( i ) );
      }
    }
  }

  public String[] getExpectedResultList() {
    String[] result = new String[ruleResultColumns.size()];

    for ( int i = 0; i < ruleResultColumns.size(); i++ ) {
      result[i] = ruleResultColumns.get( i ).getName();
    }

    return result;
  }

}
