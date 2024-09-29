/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mailvalidator;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
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
import org.pentaho.di.core.annotations.Step;

/*
 * Created on 03-Juin-2008
 *
 */

@Step(id = "MailValidator", name = "BaseStep.TypeLongDesc.MailValidator",
        i18nPackageName = "org.pentaho.di.trans.step.mailvalidator",
        description = "BaseStep.TypeTooltipDesc.MailValidator",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Validation",
        image = "ui/images/MAV.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+Validator")
public class MailValidatorMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MailValidatorMeta.class; // for i18n purposes, needed by Translator2!!

  /** dynamic email address */
  private String emailfield;

  private boolean ResultAsString;

  private boolean smtpCheck;

  private String emailValideMsg;

  private String emailNotValideMsg;

  private String errorsFieldName;

  private String timeout;

  private String defaultSMTP;

  private String emailSender;

  private String defaultSMTPField;

  private boolean isdynamicDefaultSMTP;

  private String resultfieldname;

  public MailValidatorMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the emailfield.
   */
  public String getEmailField() {
    return emailfield;
  }

  /**
   * @deprecated use {@link #setEmailField(String)} instead
   * @param emailfield
   *          The emailfield to set.
   */
  @Deprecated
  public void setEmailfield( String emailfield ) {
    setEmailField( emailfield );
  }

  public void setEmailField( String emailfield ) {
    this.emailfield = emailfield;
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
   * @param emailValideMsg
   *          The emailValideMsg to set.
   */
  public void setEmailValideMsg( String emailValideMsg ) {
    this.emailValideMsg = emailValideMsg;
  }

  /**
   * @deprecated use {@link #getEmailValideMsg()} instead
   * @return Returns the emailValideMsg.
   */
  @Deprecated
  public String getEMailValideMsg() {
    return getEmailValideMsg();
  }

  public String getEmailValideMsg() {
    return emailValideMsg;
  }

  /**
   * @deprecated use {@link #getEmailNotValideMsg()} instead
   * @return Returns the emailNotValideMsg.
   */
  @Deprecated
  public String getEMailNotValideMsg() {
    return getEmailNotValideMsg();
  }

  public String getEmailNotValideMsg() {
    return emailNotValideMsg;
  }

  /**
   * @return Returns the errorsFieldName.
   */
  public String getErrorsField() {
    return errorsFieldName;
  }

  /**
   * @param errorsFieldName
   *          The errorsFieldName to set.
   */
  public void setErrorsField( String errorsFieldName ) {
    this.errorsFieldName = errorsFieldName;
  }

  /**
   * @return Returns the timeout.
   */
  public String getTimeOut() {
    return timeout;
  }

  /**
   * @param timeout
   *          The timeout to set.
   */
  public void setTimeOut( String timeout ) {
    this.timeout = timeout;
  }

  /**
   * @return Returns the defaultSMTP.
   */
  public String getDefaultSMTP() {
    return defaultSMTP;
  }

  /**
   * @param defaultSMTP
   *          The defaultSMTP to set.
   */
  public void setDefaultSMTP( String defaultSMTP ) {
    this.defaultSMTP = defaultSMTP;
  }

  /**
   * @deprecated use {@link #getEmailSender()} instead
   * @return Returns the emailSender.
   */
  @Deprecated
  public String geteMailSender() {
    return getEmailSender();
  }

  public String getEmailSender() {
    return emailSender;
  }

  /**
   * @deprecated use {@link #setEmailSender(String)} instead
   * @param emailSender
   *          The emailSender to set.
   */
  @Deprecated
  public void seteMailSender( String emailSender ) {
    setEmailSender( emailSender );
  }

  public void setEmailSender( String emailSender ) {
    this.emailSender = emailSender;
  }

  /**
   * @return Returns the defaultSMTPField.
   */
  public String getDefaultSMTPField() {
    return defaultSMTPField;
  }

  /**
   * @param defaultSMTPField
   *          The defaultSMTPField to set.
   */
  public void setDefaultSMTPField( String defaultSMTPField ) {
    this.defaultSMTPField = defaultSMTPField;
  }

  /**
   * @deprecated use {@link #isDynamicDefaultSMTP()} instead
   * @return Returns the isdynamicDefaultSMTP.
   */
  @Deprecated
  public boolean isdynamicDefaultSMTP() {
    return isDynamicDefaultSMTP();
  }

  public boolean isDynamicDefaultSMTP() {
    return isdynamicDefaultSMTP;
  }

  /**
   * @deprecated use {@link #setDynamicDefaultSMTP(boolean)} instead
   * @param isdynamicDefaultSMTP
   *          The isdynamicDefaultSMTP to set.
   */
  @Deprecated
  public void setdynamicDefaultSMTP( boolean isdynamicDefaultSMTP ) {
    setDynamicDefaultSMTP( isdynamicDefaultSMTP );
  }

  public void setDynamicDefaultSMTP( boolean isdynamicDefaultSMTP ) {
    this.isdynamicDefaultSMTP = isdynamicDefaultSMTP;
  }

  /**
   * @param emailNotValideMsg
   *          The emailNotValideMsg to set.
   */
  public void setEmailNotValideMsg( String emailNotValideMsg ) {
    this.emailNotValideMsg = emailNotValideMsg;
  }

  public boolean isResultAsString() {
    return ResultAsString;
  }

  public void setResultAsString( boolean ResultAsString ) {
    this.ResultAsString = ResultAsString;
  }

  public void setSMTPCheck( boolean smtpcheck ) {
    this.smtpCheck = smtpcheck;
  }

  public boolean isSMTPCheck() {
    return smtpCheck;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    MailValidatorMeta retval = (MailValidatorMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    resultfieldname = "result";
    emailValideMsg = "email address is valid";
    emailNotValideMsg = "email address is not valid";
    ResultAsString = false;
    errorsFieldName = "Error message";
    timeout = "0";
    defaultSMTP = null;
    emailSender = "noreply@domain.com";
    smtpCheck = false;
    isdynamicDefaultSMTP = false;
    defaultSMTPField = null;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    String realResultFieldName = space.environmentSubstitute( resultfieldname );
    if ( ResultAsString ) {
      ValueMetaInterface v = new ValueMetaString( realResultFieldName );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );

    } else {
      ValueMetaInterface v = new ValueMetaBoolean( realResultFieldName );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    String realErrorsFieldName = space.environmentSubstitute( errorsFieldName );
    if ( !Utils.isEmpty( realErrorsFieldName ) ) {
      ValueMetaInterface v = new ValueMetaString( realErrorsFieldName );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "emailfield", emailfield ) );
    retval.append( "    " + XMLHandler.addTagValue( "resultfieldname", resultfieldname ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "ResultAsString", ResultAsString ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "smtpCheck", smtpCheck ) );

    retval.append( "    " + XMLHandler.addTagValue( "emailValideMsg", emailValideMsg ) );
    retval.append( "    " + XMLHandler.addTagValue( "emailNotValideMsg", emailNotValideMsg ) );
    retval.append( "    " + XMLHandler.addTagValue( "errorsFieldName", errorsFieldName ) );
    retval.append( "    " + XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "    " + XMLHandler.addTagValue( "defaultSMTP", defaultSMTP ) );
    retval.append( "    " + XMLHandler.addTagValue( "emailSender", emailSender ) );
    retval.append( "    " + XMLHandler.addTagValue( "defaultSMTPField", defaultSMTPField ) );

    retval.append( "    " + XMLHandler.addTagValue( "isdynamicDefaultSMTP", isdynamicDefaultSMTP ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      emailfield = XMLHandler.getTagValue( stepnode, "emailfield" );
      resultfieldname = XMLHandler.getTagValue( stepnode, "resultfieldname" );
      ResultAsString = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "ResultAsString" ) );
      smtpCheck = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "smtpCheck" ) );

      emailValideMsg = XMLHandler.getTagValue( stepnode, "emailValideMsg" );
      emailNotValideMsg = XMLHandler.getTagValue( stepnode, "emailNotValideMsg" );
      errorsFieldName = XMLHandler.getTagValue( stepnode, "errorsFieldName" );
      timeout = XMLHandler.getTagValue( stepnode, "timeout" );
      defaultSMTP = XMLHandler.getTagValue( stepnode, "defaultSMTP" );
      emailSender = XMLHandler.getTagValue( stepnode, "emailSender" );
      defaultSMTPField = XMLHandler.getTagValue( stepnode, "defaultSMTPField" );

      isdynamicDefaultSMTP = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "isdynamicDefaultSMTP" ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "MailValidatorMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      emailfield = rep.getStepAttributeString( id_step, "emailfield" );
      resultfieldname = rep.getStepAttributeString( id_step, "resultfieldname" );
      ResultAsString = rep.getStepAttributeBoolean( id_step, "ResultAsString" );
      smtpCheck = rep.getStepAttributeBoolean( id_step, "smtpCheck" );

      emailValideMsg = rep.getStepAttributeString( id_step, "emailValideMsg" );
      emailNotValideMsg = rep.getStepAttributeString( id_step, "emailNotValideMsg" );
      errorsFieldName = rep.getStepAttributeString( id_step, "errorsFieldName" );
      timeout = rep.getStepAttributeString( id_step, "timeout" );
      defaultSMTP = rep.getStepAttributeString( id_step, "defaultSMTP" );
      emailSender = rep.getStepAttributeString( id_step, "emailSender" );
      defaultSMTPField = rep.getStepAttributeString( id_step, "defaultSMTPField" );

      isdynamicDefaultSMTP = rep.getStepAttributeBoolean( id_step, "isdynamicDefaultSMTP" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "MailValidatorMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "emailfield", emailfield );
      rep.saveStepAttribute( id_transformation, id_step, "resultfieldname", resultfieldname );
      rep.saveStepAttribute( id_transformation, id_step, "ResultAsString", ResultAsString );
      rep.saveStepAttribute( id_transformation, id_step, "smtpCheck", smtpCheck );

      rep.saveStepAttribute( id_transformation, id_step, "emailValideMsg", emailValideMsg );
      rep.saveStepAttribute( id_transformation, id_step, "emailNotValideMsg", emailNotValideMsg );
      rep.saveStepAttribute( id_transformation, id_step, "errorsFieldName", errorsFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "timeout", timeout );
      rep.saveStepAttribute( id_transformation, id_step, "defaultSMTP", defaultSMTP );
      rep.saveStepAttribute( id_transformation, id_step, "emailSender", emailSender );
      rep.saveStepAttribute( id_transformation, id_step, "defaultSMTPField", defaultSMTPField );

      rep.saveStepAttribute( id_transformation, id_step, "isdynamicDefaultSMTP", isdynamicDefaultSMTP );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailValidatorMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    if ( Utils.isEmpty( resultfieldname ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.ResultFieldMissing" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.ResultFieldOk" ), stepMeta );
    }
    remarks.add( cr );

    if ( this.ResultAsString ) {
      if ( Utils.isEmpty( emailValideMsg ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.EmailValidMsgMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.EmailValidMsgOk" ), stepMeta );
      }
      remarks.add( cr );

      if ( Utils.isEmpty( emailNotValideMsg ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.EmailNotValidMsgMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.EmailNotValidMsgOk" ), stepMeta );
      }
      remarks.add( cr );
    }

    if ( Utils.isEmpty( emailfield ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.eMailFieldMissing" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.eMailFieldOK" ), stepMeta );
    }
    remarks.add( cr );

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "MailValidatorMeta.CheckResult.NoInpuReceived" ), stepMeta );
    }
    remarks.add( cr );
    if ( ResultAsString ) {
      if ( Utils.isEmpty( emailValideMsg ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailValidMsgMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailValidMsgOk" ), stepMeta );
      }
      remarks.add( cr );

      if ( Utils.isEmpty( emailNotValideMsg ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailNotValidMsgMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailNotValidMsgOk" ), stepMeta );
      }
      remarks.add( cr );
    }
    // SMTP check
    if ( smtpCheck ) {
      // sender
      if ( Utils.isEmpty( emailSender ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailSenderMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "MailValidatorMeta.CheckResult.eMailSenderOk" ), stepMeta );
      }
      remarks.add( cr );

      // dynamic default SMTP
      if ( isdynamicDefaultSMTP ) {
        if ( Utils.isEmpty( defaultSMTPField ) ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "MailValidatorMeta.CheckResult.dynamicDefaultSMTPFieldMissing" ), stepMeta );
        } else {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
              PKG, "MailValidatorMeta.CheckResult.dynamicDefaultSMTPFieldOk" ), stepMeta );
        }
        remarks.add( cr );
      }
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new MailValidator( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new MailValidatorData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

}
