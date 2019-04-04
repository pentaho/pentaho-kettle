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

package org.pentaho.di.trans.steps.stepsmetrics;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
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

/*
 * Created on 30-06-2008
 *
 */

public class StepsMetricsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = StepsMetrics.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredStepsDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredStepsCode = new String[] { "N", "Y" };

  public static final String YES = "Y";
  public static final String NO = "N";

  /** by which steps to display? */
  private String[] stepName;
  private String[] stepCopyNr;
  /** Array of boolean values as string, indicating if a step is required. */
  private String[] stepRequired;

  private String stepnamefield;
  private String stepidfield;
  private String steplinesinputfield;
  private String steplinesoutputfield;
  private String steplinesreadfield;
  private String steplinesupdatedfield;
  private String steplineswrittentfield;
  private String steplineserrorsfield;
  private String stepsecondsfield;

  public StepsMetricsMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    StepsMetricsMeta retval = (StepsMetricsMeta) super.clone();

    int nrfields = stepName.length;

    retval.allocate( nrfields );

    System.arraycopy( stepName, 0, retval.stepName, 0, nrfields );
    System.arraycopy( stepCopyNr, 0, retval.stepCopyNr, 0, nrfields );
    System.arraycopy( stepRequired, 0, retval.stepRequired, 0, nrfields );
    return retval;
  }

  public void allocate( int nrfields ) {
    stepName = new String[nrfields];
    stepCopyNr = new String[nrfields];
    stepRequired = new String[nrfields];
  }

  /**
   * @return Returns the stepName.
   */
  public String[] getStepName() {
    return stepName;
  }

  /**
   * @return Returns the stepCopyNr.
   */
  public String[] getStepCopyNr() {
    return stepCopyNr;
  }

  /**
   * @param stepName
   *          The stepName to set.
   */
  public void setStepName( String[] stepName ) {
    this.stepName = stepName;
  }

  /**
   * @param stepCopyNr
   *          The stepCopyNr to set.
   */
  public void setStepCopyNr( String[] stepCopyNr ) {
    this.stepCopyNr = stepCopyNr;
  }

  public String getRequiredStepsDesc( String tt ) {
    if ( tt == null ) {
      return RequiredStepsDesc[0];
    }
    if ( tt.equals( RequiredStepsCode[1] ) ) {
      return RequiredStepsDesc[1];
    } else {
      return RequiredStepsDesc[0];
    }
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    r.clear();
    String stepname = space.environmentSubstitute( stepnamefield );
    if ( !Utils.isEmpty( stepname ) ) {
      ValueMetaInterface v = new ValueMetaString( stepname );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    String stepid = space.environmentSubstitute( stepidfield );
    if ( !Utils.isEmpty( stepid ) ) {
      ValueMetaInterface v = new ValueMetaString( stepid );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplinesinput = space.environmentSubstitute( steplinesinputfield );
    if ( !Utils.isEmpty( steplinesinput ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplinesinput );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplinesoutput = space.environmentSubstitute( steplinesoutputfield );
    if ( !Utils.isEmpty( steplinesoutput ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplinesoutput );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplinesread = space.environmentSubstitute( steplinesreadfield );
    if ( !Utils.isEmpty( steplinesread ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplinesread );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplinesupdated = space.environmentSubstitute( steplinesupdatedfield );
    if ( !Utils.isEmpty( steplinesupdated ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplinesupdated );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplineswritten = space.environmentSubstitute( steplineswrittentfield );
    if ( !Utils.isEmpty( steplineswritten ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplineswritten );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String steplineserrors = space.environmentSubstitute( steplineserrorsfield );
    if ( !Utils.isEmpty( steplineserrors ) ) {
      ValueMetaInterface v = new ValueMetaInteger( steplineserrors );
      v.setOrigin( name );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      r.addValueMeta( v );
    }
    String stepseconds = space.environmentSubstitute( stepsecondsfield );
    if ( !Utils.isEmpty( stepseconds ) ) {
      ValueMetaInterface v = new ValueMetaInteger( stepseconds );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      Node steps = XMLHandler.getSubNode( stepnode, "steps" );
      int nrsteps = XMLHandler.countNodes( steps, "step" );

      allocate( nrsteps );

      for ( int i = 0; i < nrsteps; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( steps, "step", i );
        stepName[i] = XMLHandler.getTagValue( fnode, "name" );
        stepCopyNr[i] = XMLHandler.getTagValue( fnode, "copyNr" );
        stepRequired[i] = XMLHandler.getTagValue( fnode, "stepRequired" );
      }
      stepnamefield = XMLHandler.getTagValue( stepnode, "stepnamefield" );
      stepidfield = XMLHandler.getTagValue( stepnode, "stepidfield" );
      steplinesinputfield = XMLHandler.getTagValue( stepnode, "steplinesinputfield" );
      steplinesoutputfield = XMLHandler.getTagValue( stepnode, "steplinesoutputfield" );
      steplinesreadfield = XMLHandler.getTagValue( stepnode, "steplinesreadfield" );
      steplinesupdatedfield = XMLHandler.getTagValue( stepnode, "steplinesupdatedfield" );
      steplineswrittentfield = XMLHandler.getTagValue( stepnode, "steplineswrittentfield" );
      steplineserrorsfield = XMLHandler.getTagValue( stepnode, "steplineserrorsfield" );
      stepsecondsfield = XMLHandler.getTagValue( stepnode, "stepsecondsfield" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    <steps>" + Const.CR );
    for ( int i = 0; i < stepName.length; i++ ) {
      retval.append( "      <step>" + Const.CR );
      retval.append( "        " + XMLHandler.addTagValue( "name", stepName[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "copyNr", stepCopyNr[i] ) );
      retval.append( "        " + XMLHandler.addTagValue( "stepRequired", stepRequired[i] ) );
      retval.append( "        </step>" + Const.CR );
    }
    retval.append( "      </steps>" + Const.CR );

    retval.append( "        " + XMLHandler.addTagValue( "stepnamefield", stepnamefield ) );
    retval.append( "        " + XMLHandler.addTagValue( "stepidfield", stepidfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplinesinputfield", steplinesinputfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplinesoutputfield", steplinesoutputfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplinesreadfield", steplinesreadfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplinesupdatedfield", steplinesupdatedfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplineswrittentfield", steplineswrittentfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "steplineserrorsfield", steplineserrorsfield ) );
    retval.append( "        " + XMLHandler.addTagValue( "stepsecondsfield", stepsecondsfield ) );

    return retval.toString();
  }

  public void setDefault() {
    int nrsteps = 0;

    allocate( nrsteps );

    for ( int i = 0; i < nrsteps; i++ ) {
      stepName[i] = "step" + i;
      stepCopyNr[i] = "CopyNr" + i;
      stepRequired[i] = NO;
    }

    stepnamefield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Stepname" );
    stepidfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Stepid" );
    steplinesinputfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Linesinput" );
    steplinesoutputfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Linesoutput" );
    steplinesreadfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Linesread" );
    steplinesupdatedfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Linesupdated" );
    steplineswrittentfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Lineswritten" );
    steplineserrorsfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Lineserrors" );
    stepsecondsfield = BaseMessages.getString( PKG, "StepsMetricsDialog.Label.Time" );
  }

  public void setStepRequired( String[] stepRequiredin ) {
    for ( int i = 0; i < stepRequiredin.length; i++ ) {
      this.stepRequired[i] = getRequiredStepsCode( stepRequiredin[i] );
    }
  }

  public String getRequiredStepsCode( String tt ) {
    if ( tt == null ) {
      return RequiredStepsCode[0];
    }
    if ( tt.equals( RequiredStepsDesc[1] ) ) {
      return RequiredStepsCode[1];
    } else {
      return RequiredStepsCode[0];
    }
  }

  public String[] getStepRequired() {
    return stepRequired;
  }

  public String getStepNameFieldName() {
    return this.stepnamefield;
  }

  public void setStepNameFieldName( String stepnamefield ) {
    this.stepnamefield = stepnamefield;
  }

  public String getStepIdFieldName() {
    return this.stepidfield;
  }

  public void setStepIdFieldName( String stepidfield ) {
    this.stepidfield = stepidfield;
  }

  public String getStepLinesInputFieldName() {
    return this.steplinesinputfield;
  }

  public void setStepLinesInputFieldName( String steplinesinputfield ) {
    this.steplinesinputfield = steplinesinputfield;
  }

  public String getStepLinesOutputFieldName() {
    return this.steplinesoutputfield;
  }

  public void setStepLinesOutputFieldName( String steplinesoutputfield ) {
    this.steplinesoutputfield = steplinesoutputfield;
  }

  public String getStepLinesReadFieldName() {
    return this.steplinesreadfield;
  }

  public void setStepLinesReadFieldName( String steplinesreadfield ) {
    this.steplinesreadfield = steplinesreadfield;
  }

  public String getStepLinesWrittenFieldName() {
    return this.steplineswrittentfield;
  }

  public void setStepLinesWrittenFieldName( String steplineswrittentfield ) {
    this.steplineswrittentfield = steplineswrittentfield;
  }

  public String getStepLinesErrorsFieldName() {
    return this.steplineserrorsfield;
  }

  public String getStepSecondsFieldName() {
    return this.stepsecondsfield;
  }

  public void setStepSecondsFieldName( String fieldname ) {
    this.stepsecondsfield = fieldname;
  }

  public void setStepLinesErrorsFieldName( String steplineserrorsfield ) {
    this.steplineserrorsfield = steplineserrorsfield;
  }

  public String getStepLinesUpdatedFieldName() {
    return this.steplinesupdatedfield;
  }

  public void setStepLinesUpdatedFieldName( String steplinesupdatedfield ) {
    this.steplinesupdatedfield = steplinesupdatedfield;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {

      int nrsteps = rep.countNrStepAttributes( id_step, "step_name" );

      allocate( nrsteps );

      for ( int i = 0; i < nrsteps; i++ ) {
        stepName[i] = rep.getStepAttributeString( id_step, i, "step_name" );
        stepCopyNr[i] = rep.getStepAttributeString( id_step, i, "step_CopyNr" );
        stepRequired[i] = rep.getStepAttributeString( id_step, i, "step_required" );
        if ( !YES.equalsIgnoreCase( stepRequired[i] ) ) {
          stepRequired[i] = NO;
        }
      }
      stepnamefield = rep.getStepAttributeString( id_step, "stepnamefield" );
      stepidfield = rep.getStepAttributeString( id_step, "stepidfield" );
      steplinesinputfield = rep.getStepAttributeString( id_step, "steplinesinputfield" );
      steplinesoutputfield = rep.getStepAttributeString( id_step, "steplinesoutputfield" );
      steplinesreadfield = rep.getStepAttributeString( id_step, "steplinesreadfield" );
      steplineswrittentfield = rep.getStepAttributeString( id_step, "steplineswrittentfield" );
      steplinesupdatedfield = rep.getStepAttributeString( id_step, "steplinesupdatedfield" );
      steplineserrorsfield = rep.getStepAttributeString( id_step, "steplineserrorsfield" );
      stepsecondsfield = rep.getStepAttributeString( id_step, "stepsecondsfield" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      for ( int i = 0; i < stepName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "step_name", stepName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "step_CopyNr", stepCopyNr[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "step_required", stepRequired[i] );
      }
      rep.saveStepAttribute( id_transformation, id_step, "stepnamefield", stepnamefield );
      rep.saveStepAttribute( id_transformation, id_step, "stepidfield", stepidfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplinesinputfield", steplinesinputfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplinesoutputfield", steplinesoutputfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplinesreadfield", steplinesreadfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplineswrittentfield", steplineswrittentfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplinesupdatedfield", steplinesupdatedfield );
      rep.saveStepAttribute( id_transformation, id_step, "steplineserrorsfield", steplineserrorsfield );
      rep.saveStepAttribute( id_transformation, id_step, "stepsecondsfield", stepsecondsfield );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StepsMetricsMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
      if ( stepName.length > 0 ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "StepsMetricsMeta.CheckResult.AllStepsFound" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(
            PKG, "StepsMetricsMeta.CheckResult.NoStepsEntered" ), stepMeta );
      }
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StepsMetricsMeta.CheckResult.ReceivingFields" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "StepsMetricsMeta.CheckResult.StepRecevingData2" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "StepsMetricsMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
    }
    remarks.add( cr );

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new StepsMetrics( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new StepsMetricsData();
  }

  @Override
  public TransMeta.TransformationType[] getSupportedTransformationTypes() {
    return new TransMeta.TransformationType[] { TransMeta.TransformationType.Normal };
  }
}
