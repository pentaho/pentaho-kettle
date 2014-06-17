/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.filterrows;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
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
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2003
 *
 */

public class FilterRowsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = FilterRowsMeta.class; // for i18n purposes, needed by Translator2!!

  /**
   * This is the main condition for the complete filter.
   *
   * @since version 2.1
   */
  private Condition condition;

  public FilterRowsMeta() {
    super(); // allocate BaseStepMeta
    condition = new Condition();
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  /**
   * @return Returns the condition.
   */
  public Condition getCondition() {
    return condition;
  }

  /**
   * @param condition
   *          The condition to set.
   */
  public void setCondition( Condition condition ) {
    this.condition = condition;
  }

  public void allocate() {
    condition = new Condition();
  }

  public Object clone() {
    FilterRowsMeta retval = (FilterRowsMeta) super.clone();

    if ( condition != null ) {
      retval.condition = (Condition) condition.clone();
    } else {
      retval.condition = null;
    }

    return retval;
  }

  public String getXML() throws KettleException {
    StringBuffer retval = new StringBuffer( 200 );

    List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();
    retval.append( XMLHandler.addTagValue( "send_true_to", targetStreams.get( 0 ).getStepname() ) );
    retval.append( XMLHandler.addTagValue( "send_false_to", targetStreams.get( 1 ).getStepname() ) );
    retval.append( "    <compare>" ).append( Const.CR );

    if ( condition != null ) {
      retval.append( condition.getXML() );
    }

    retval.append( "    </compare>" ).append( Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

      targetStreams.get( 0 ).setSubject( XMLHandler.getTagValue( stepnode, "send_true_to" ) );
      targetStreams.get( 1 ).setSubject( XMLHandler.getTagValue( stepnode, "send_false_to" ) );

      Node compare = XMLHandler.getSubNode( stepnode, "compare" );
      Node condnode = XMLHandler.getSubNode( compare, "condition" );

      // The new situation...
      if ( condnode != null ) {
        condition = new Condition( condnode );
      } else {
        // Old style condition: Line1 OR Line2 OR Line3: @deprecated!
        condition = new Condition();

        int nrkeys = XMLHandler.countNodes( compare, "key" );
        if ( nrkeys == 1 ) {
          Node knode = XMLHandler.getSubNodeByNr( compare, "key", 0 );

          String key = XMLHandler.getTagValue( knode, "name" );
          String value = XMLHandler.getTagValue( knode, "value" );
          String field = XMLHandler.getTagValue( knode, "field" );
          String comparator = XMLHandler.getTagValue( knode, "condition" );

          condition.setOperator( Condition.OPERATOR_NONE );
          condition.setLeftValuename( key );
          condition.setFunction( Condition.getFunction( comparator ) );
          condition.setRightValuename( field );
          condition.setRightExact( new ValueMetaAndData( "value", value ) );
        } else {
          for ( int i = 0; i < nrkeys; i++ ) {
            Node knode = XMLHandler.getSubNodeByNr( compare, "key", i );

            String key = XMLHandler.getTagValue( knode, "name" );
            String value = XMLHandler.getTagValue( knode, "value" );
            String field = XMLHandler.getTagValue( knode, "field" );
            String comparator = XMLHandler.getTagValue( knode, "condition" );

            Condition subc = new Condition();
            if ( i > 0 ) {
              subc.setOperator( Condition.OPERATOR_OR );
            } else {
              subc.setOperator( Condition.OPERATOR_NONE );
            }
            subc.setLeftValuename( key );
            subc.setFunction( Condition.getFunction( comparator ) );
            subc.setRightValuename( field );
            subc.setRightExact( new ValueMetaAndData( "value", value ) );

            condition.addCondition( subc );
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "FilterRowsMeta.Exception..UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    allocate();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      allocate();

      List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

      targetStreams.get( 0 ).setSubject( rep.getStepAttributeString( id_step, "send_true_to" ) );
      targetStreams.get( 1 ).setSubject( rep.getStepAttributeString( id_step, "send_false_to" ) );

      condition = rep.loadConditionFromStepAttribute( id_step, "id_condition" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FilterRowsMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository" ), e );
    }
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    for ( StreamInterface stream : getStepIOMeta().getTargetStreams() ) {
      stream.setStepMeta( StepMeta.findStep( steps, (String) stream.getSubject() ) );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      if ( condition != null ) {
        List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

        rep.saveConditionStepAttribute( id_transformation, id_step, "id_condition", condition );
        rep.saveStepAttribute( id_transformation, id_step, "send_true_to", targetStreams.get( 0 ).getStepname() );
        rep.saveStepAttribute( id_transformation, id_step, "send_false_to", targetStreams.get( 1 ).getStepname() );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FilterRowsMeta.Exception.UnableToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Clear the sortedDescending flag on fields used within the condition - otherwise the comparisons will be
    // inverted!!
    String[] conditionField = condition.getUsedFields();
    for ( int i = 0; i < conditionField.length; i++ ) {
      int idx = rowMeta.indexOfValue( conditionField[i] );
      if ( idx >= 0 ) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( idx );
        valueMeta.setSortedDescending( false );
      }
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();

    if ( targetStreams.get( 0 ).getStepname() != null ) {
      int trueTargetIdx = Const.indexOfString( targetStreams.get( 0 ).getStepname(), output );
      if ( trueTargetIdx < 0 ) {
        cr =
          new CheckResult(
            CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "FilterRowsMeta.CheckResult.TargetStepInvalid", "true", targetStreams
                .get( 0 ).getStepname() ), stepMeta );
        remarks.add( cr );
      }
    }

    if ( targetStreams.get( 1 ).getStepname() != null ) {
      int falseTargetIdx = Const.indexOfString( targetStreams.get( 1 ).getStepname(), output );
      if ( falseTargetIdx < 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
            .getString( PKG, "FilterRowsMeta.CheckResult.TargetStepInvalid", "false", targetStreams
              .get( 1 ).getStepname() ), stepMeta );
        remarks.add( cr );
      }
    }

    if ( condition.isEmpty() ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FilterRowsMeta.CheckResult.NoConditionSpecified" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FilterRowsMeta.CheckResult.ConditionSpecified" ), stepMeta );
    }
    remarks.add( cr );

    // Look up fields in the input stream <prev>
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FilterRowsMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      List<String> orphanFields = getOrphanFields( condition, prev );
      if ( orphanFields.size() > 0 ) {
        error_message = BaseMessages.getString( PKG, "FilterRowsMeta.CheckResult.FieldsNotFoundFromPreviousStep" )
          + Const.CR;
        for ( String field : orphanFields ) {
          error_message += "\t\t" + field + Const.CR;
        }
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
            "FilterRowsMeta.CheckResult.AllFieldsFoundInInputStream" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      error_message =
        BaseMessages.getString( PKG, "FilterRowsMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
          + Const.CR;
      cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FilterRowsMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FilterRowsMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new FilterRows( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new FilterRowsData();
  }

  /**
   * Returns the Input/Output metadata for this step.
   */
  public StepIOMetaInterface getStepIOMeta() {
    if ( ioMeta == null ) {

      ioMeta = new StepIOMeta( true, true, false, false, false, false );

      ioMeta.addStream( new Stream( StreamType.TARGET, null, BaseMessages.getString(
        PKG, "FilterRowsMeta.InfoStream.True.Description" ), StreamIcon.TRUE, null ) );
      ioMeta.addStream( new Stream( StreamType.TARGET, null, BaseMessages.getString(
        PKG, "FilterRowsMeta.InfoStream.False.Description" ), StreamIcon.FALSE, null ) );
    }

    return ioMeta;
  }

  @Override
  public void resetStepIoMeta() {
  }

  /**
   * When an optional stream is selected, this method is called to handled the ETL metadata implications of that.
   *
   * @param stream
   *          The optional stream to handle.
   */
  public void handleStreamSelection( StreamInterface stream ) {
    // This step targets another step.
    // Make sure that we don't specify the same step for true and false...
    // If the user requests false, we blank out true and vice versa
    //
    List<StreamInterface> targets = getStepIOMeta().getTargetStreams();
    int index = targets.indexOf( stream );
    if ( index == 0 ) {
      // True
      //
      StepMeta falseStep = targets.get( 1 ).getStepMeta();
      if ( falseStep != null && falseStep.equals( stream.getStepMeta() ) ) {
        targets.get( 1 ).setStepMeta( null );
      }
    }
    if ( index == 1 ) {
      // False
      //
      StepMeta trueStep = targets.get( 0 ).getStepMeta();
      if ( trueStep != null && trueStep.equals( stream.getStepMeta() ) ) {
        targets.get( 0 ).setStepMeta( null );
      }
    }
  }

  @Override
  public boolean excludeFromCopyDistributeVerification() {
    return true;
  }

  /**
   * Get non-existing referenced input fields
   * @param condition
   * @param prev
   * @return
   */
  public List<String> getOrphanFields( Condition condition, RowMetaInterface prev ) {
    List<String> orphans = new ArrayList<String>(  );
    if ( condition == null || prev == null ) {
      return orphans;
    }
    String[] key = condition.getUsedFields();
    for ( int i = 0; i < key.length; i++ ) {
      if ( Const.isEmpty( key[i] ) ) {
        continue;
      }
      ValueMetaInterface v = prev.searchValueMeta( key[i] );
      if ( v == null ) {
        orphans.add( key[i] );
      }
    }
    return orphans;
  }

}
