/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.execprocess;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.shared.SharedObjectInterface;
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
 * Created on 03-11-2008
 *
 */

public class ExecProcessMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ExecProcessMeta.class; // for i18n purposes, needed by Translator2!!

  /** dynamic process field name */
  private String processfield;

  /** function result: new value name */
  private String resultfieldname;

  /** function result: error fieldname */
  private String errorfieldname;

  /** function result: exit value fieldname */
  private String exitvaluefieldname;

  /** fail if the exit status is different from 0 **/
  private boolean failwhennotsuccess;

  /** Output Line Delimiter - defaults to empty string for backward compatibility **/
  public String outputLineDelimiter = "";

  public ExecProcessMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the processfield.
   */
  public String getProcessField() {
    return processfield;
  }

  /**
   * @param processfield
   *          The processfield to set.
   */
  public void setProcessField( String processfield ) {
    this.processfield = processfield;
  }

  /**
   * @return Returns the resultName.
   */
  public String getResultFieldName() {
    return resultfieldname;
  }

  /**
   * @param resultfieldname
   *          The resultfieldname to set.
   */
  public void setResultFieldName( String resultfieldname ) {
    this.resultfieldname = resultfieldname;
  }

  /**
   * @return Returns the errorfieldname.
   */
  public String getErrorFieldName() {
    return errorfieldname;
  }

  /**
   * @param errorfieldname
   *          The errorfieldname to set.
   */
  public void setErrorFieldName( String errorfieldname ) {
    this.errorfieldname = errorfieldname;
  }

  /**
   * @return Returns the exitvaluefieldname.
   */
  public String getExitValueFieldName() {
    return exitvaluefieldname;
  }

  /**
   * @param exitvaluefieldname
   *          The exitvaluefieldname to set.
   */
  public void setExitValueFieldName( String exitvaluefieldname ) {
    this.exitvaluefieldname = exitvaluefieldname;
  }

  /**
   * @return Returns the failwhennotsuccess.
   */
  public boolean isFailWhenNotSuccess() {
    return failwhennotsuccess;
  }

  /**
   * @param failwhennotsuccess
   *          The failwhennotsuccess to set.
   */
  public void setFailWhentNoSuccess( boolean failwhennotsuccess ) {
    this.failwhennotsuccess = failwhennotsuccess;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, databases );
  }

  public Object clone() {
    ExecProcessMeta retval = (ExecProcessMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    resultfieldname = "Result output";
    errorfieldname = "Error output";
    exitvaluefieldname = "Exit value";
    failwhennotsuccess = false;
  }

  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Output fields (String)
    String realOutputFieldname = space.environmentSubstitute( resultfieldname );
    if ( !Const.isEmpty( realOutputFieldname ) ) {
      ValueMetaInterface v = new ValueMetaString( realOutputFieldname );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
    String realerrofieldname = space.environmentSubstitute( errorfieldname );
    if ( !Const.isEmpty( realerrofieldname ) ) {
      ValueMetaInterface v = new ValueMetaString( realerrofieldname );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
    String realexitvaluefieldname = space.environmentSubstitute( exitvaluefieldname );
    if ( !Const.isEmpty( realexitvaluefieldname ) ) {
      ValueMetaInterface v = new ValueMetaInteger( realexitvaluefieldname );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      inputRowMeta.addValueMeta( v );
    }
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append( "    " + XMLHandler.addTagValue( "processfield", processfield ) );
    retval.append( "    " + XMLHandler.addTagValue( "resultfieldname", resultfieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "errorfieldname", errorfieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "exitvaluefieldname", exitvaluefieldname ) );
    retval.append( "    " + XMLHandler.addTagValue( "failwhennotsuccess", failwhennotsuccess ) );
    retval.append( "    " + XMLHandler.addTagValue( "outputlinedelimiter", outputLineDelimiter ) );
    return retval.toString();
  }

  private void readData( Node stepnode, List<? extends SharedObjectInterface> databases ) throws KettleXMLException {
    try {
      processfield = XMLHandler.getTagValue( stepnode, "processfield" );
      resultfieldname = XMLHandler.getTagValue( stepnode, "resultfieldname" );
      errorfieldname = XMLHandler.getTagValue( stepnode, "errorfieldname" );
      exitvaluefieldname = XMLHandler.getTagValue( stepnode, "exitvaluefieldname" );
      failwhennotsuccess = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "failwhennotsuccess" ) );
      outputLineDelimiter = XMLHandler.getTagValue( stepnode, "outputlinedelimiter" );
      if ( outputLineDelimiter == null ) {
        outputLineDelimiter = ""; // default to empty string for backward compatibility
      }
    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "ExecProcessMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      processfield = rep.getStepAttributeString( id_step, "processfield" );
      resultfieldname = rep.getStepAttributeString( id_step, "resultfieldname" );
      errorfieldname = rep.getStepAttributeString( id_step, "errorfieldname" );
      exitvaluefieldname = rep.getStepAttributeString( id_step, "exitvaluefieldname" );
      failwhennotsuccess = rep.getStepAttributeBoolean( id_step, "failwhennotsuccess" );
      outputLineDelimiter = rep.getStepAttributeString( id_step, "outputlinedelimiter" );
      if ( outputLineDelimiter == null ) {
        outputLineDelimiter = ""; // default to empty string for backward compatibility
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ExecProcessMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "processfield", processfield );
      rep.saveStepAttribute( id_transformation, id_step, "resultfieldname", resultfieldname );
      rep.saveStepAttribute( id_transformation, id_step, "errorfieldname", errorfieldname );
      rep.saveStepAttribute( id_transformation, id_step, "exitvaluefieldname", exitvaluefieldname );
      rep.saveStepAttribute( id_transformation, id_step, "failwhennotsuccess", failwhennotsuccess );
      rep.saveStepAttribute( id_transformation, id_step, "outputlinedelimiter", outputLineDelimiter );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "ExecProcessMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    if ( Const.isEmpty( resultfieldname ) ) {
      error_message = BaseMessages.getString( PKG, "ExecProcessMeta.CheckResult.ResultFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
    } else {
      error_message = BaseMessages.getString( PKG, "ExecProcessMeta.CheckResult.ResultFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
    }
    remarks.add( cr );

    if ( Const.isEmpty( processfield ) ) {
      error_message = BaseMessages.getString( PKG, "ExecProcessMeta.CheckResult.ProcessFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
    } else {
      error_message = BaseMessages.getString( PKG, "ExecProcessMeta.CheckResult.ProcessFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
    }
    remarks.add( cr );

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExecProcessMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExecProcessMeta.CheckResult.NoInpuReceived" ), stepMeta );
    }
    remarks.add( cr );

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ExecProcess( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ExecProcessData();
  }

  public boolean supportsErrorHandling() {
    return failwhennotsuccess;
  }

  public void setOutputLineDelimiter( String value ) {
    this.outputLineDelimiter = value;
  }

  public String getOutputLineDelimiter() {
    return outputLineDelimiter;
  }

}
