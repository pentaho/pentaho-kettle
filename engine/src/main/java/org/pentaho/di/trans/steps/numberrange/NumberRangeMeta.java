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

package org.pentaho.di.trans.steps.numberrange;

import java.util.LinkedList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
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
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Configuration for the NumberRangePlugin
 *
 * @author ronny.roeller@fredhopper.com
 *
 */

public class NumberRangeMeta extends BaseStepMeta implements StepMetaInterface {

  private String inputField;

  private String outputField;

  private String fallBackValue;

  private List<NumberRangeRule> rules;

  public NumberRangeMeta() {
    super();
  }

  public void emptyRules() {
    rules = new LinkedList<NumberRangeRule>();
  }

  public NumberRangeMeta( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    loadXML( stepnode, databases, metaStore );
  }

  public NumberRangeMeta( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    readRep( rep, metaStore, id_step, databases );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append( XMLHandler.addTagValue( "inputField", inputField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "outputField", outputField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fallBackValue", getFallBackValue() ) );

    retval.append( "    <rules>" ).append( Const.CR );
    for ( NumberRangeRule rule : rules ) {
      retval.append( "      <rule>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "lower_bound", rule.getLowerBound() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "upper_bound", rule.getUpperBound() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "value", rule.getValue() ) );
      retval.append( "      </rule>" ).append( Const.CR );
    }
    retval.append( "    </rules>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    ValueMetaInterface mcValue = new ValueMetaString( outputField );
    mcValue.setOrigin( name );
    mcValue.setLength( 255 );
    row.addValueMeta( mcValue );
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      inputField = XMLHandler.getTagValue( stepnode, "inputField" );
      outputField = XMLHandler.getTagValue( stepnode, "outputField" );

      emptyRules();
      String fallBackValue = XMLHandler.getTagValue( stepnode, "fallBackValue" );
      setFallBackValue( fallBackValue );

      Node fields = XMLHandler.getSubNode( stepnode, "rules" );
      int count = XMLHandler.countNodes( fields, "rule" );
      for ( int i = 0; i < count; i++ ) {

        Node fnode = XMLHandler.getSubNodeByNr( fields, "rule", i );

        String lowerBoundStr = XMLHandler.getTagValue( fnode, "lower_bound" );
        String upperBoundStr = XMLHandler.getTagValue( fnode, "upper_bound" );
        String value = XMLHandler.getTagValue( fnode, "value" );

        double lowerBound = Double.parseDouble( lowerBoundStr );
        double upperBound = Double.parseDouble( upperBoundStr );
        addRule( lowerBound, upperBound, value );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to read step info from XML node", e );
    }
  }

  @Override
  public void setDefault() {
    emptyRules();
    setFallBackValue( "unknown" );
    addRule( -Double.MAX_VALUE, 5, "Less than 5" );
    addRule( 5, 10, "5-10" );
    addRule( 10, Double.MAX_VALUE, "More than 10" );
    inputField = "";
    outputField = "range";
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      inputField = rep.getStepAttributeString( id_step, "inputField" );
      outputField = rep.getStepAttributeString( id_step, "outputField" );

      emptyRules();
      String fallBackValue = rep.getStepAttributeString( id_step, "fallBackValue" );
      setFallBackValue( fallBackValue );
      int nrfields = rep.countNrStepAttributes( id_step, "lower_bound" );
      for ( int i = 0; i < nrfields; i++ ) {
        String lowerBoundStr = rep.getStepAttributeString( id_step, i, "lower_bound" );
        String upperBoundStr = rep.getStepAttributeString( id_step, i, "upper_bound" );
        String value = rep.getStepAttributeString( id_step, i, "value" );

        double lowerBound = Double.parseDouble( lowerBoundStr );
        double upperBound = Double.parseDouble( upperBoundStr );

        addRule( lowerBound, upperBound, value );

      }
    } catch ( Exception dbe ) {
      throw new KettleException( "error reading step with id_step=" + id_step + " from the repository", dbe );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "inputField", inputField );
      rep.saveStepAttribute( id_transformation, id_step, "outputField", outputField );
      rep.saveStepAttribute( id_transformation, id_step, "fallBackValue", getFallBackValue() );

      int i = 0;
      for ( NumberRangeRule rule : rules ) {
        rep
          .saveStepAttribute( id_transformation, id_step, i, "lower_bound", String
            .valueOf( rule.getLowerBound() ) );
        rep
          .saveStepAttribute( id_transformation, id_step, i, "upper_bound", String
            .valueOf( rule.getUpperBound() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "value", String.valueOf( rule.getValue() ) );
        i++;
      }
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save step information to the repository, id_step=" + id_step, dbe );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult(
          CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "
          + prev.size() + " fields", stepinfo );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans disp ) {
    return new NumberRange( stepMeta, stepDataInterface, cnr, transMeta, disp );
  }

  @Override
  public StepDataInterface getStepData() {
    return new NumberRangeData();
  }

  public String getInputField() {
    return inputField;
  }

  public String getOutputField() {
    return outputField;
  }

  public void setOutputField( String outputField ) {
    this.outputField = outputField;
  }

  public List<NumberRangeRule> getRules() {
    return rules;
  }

  public String getFallBackValue() {
    return fallBackValue;
  }

  public void setInputField( String inputField ) {
    this.inputField = inputField;
  }

  public void setFallBackValue( String fallBackValue ) {
    this.fallBackValue = fallBackValue;
  }

  public void addRule( double lowerBound, double upperBound, String value ) {
    NumberRangeRule rule = new NumberRangeRule( lowerBound, upperBound, value );
    rules.add( rule );
  }

  public void setRules( List<NumberRangeRule> rules ) {
    this.rules = rules;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
