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

package org.pentaho.di.job.entries.mailvalidator;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.steps.mailvalidator.MailValidation;
import org.pentaho.di.trans.steps.mailvalidator.MailValidationResult;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job entry mail validator.
 *
 * @author Samatar
 * @since 23-06-2008
 */
@JobEntry( id = "MAIL_VALIDATOR,JobCategory.Category.Mail_VALIDATOR", name = "JobEntry.MailValidator.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.mailvalidator",
        description = "JobEntry.MailValidator.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Mail",
        image = "ui/images/MAV.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail+Validator" )
public class JobEntryMailValidator extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMailValidator.class; // for i18n purposes, needed by Translator2!!

  private boolean smtpCheck;
  private String timeout;
  private String defaultSMTP;
  private String emailSender;
  private String emailAddress;

  public JobEntryMailValidator( String n, String scr ) {
    super( n, "" );
    emailAddress = null;
    smtpCheck = false;
    timeout = "0";
    defaultSMTP = null;
    emailSender = "noreply@domain.com";
  }

  public JobEntryMailValidator() {
    this( "", "" );
  }

  public void setSMTPCheck( boolean smtpcheck ) {
    this.smtpCheck = smtpcheck;
  }

  public boolean isSMTPCheck() {
    return smtpCheck;
  }

  public String getEmailAddress() {
    return this.emailAddress;
  }

  public void setEmailAddress( String emailAddress ) {
    this.emailAddress = emailAddress;
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
   * @return Returns the emailSender.
   */
  public String geteMailSender() {
    return emailSender;
  }

  /**
   * @param emailSender
   *          The emailSender to set.
   */
  public void seteMailSender( String emailSender ) {
    this.emailSender = emailSender;
  }

  public Object clone() {
    JobEntryMailValidator je = (JobEntryMailValidator) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 100 );
    retval.append( "      " ).append( XMLHandler.addTagValue( "smtpCheck", smtpCheck ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", timeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "defaultSMTP", defaultSMTP ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "emailSender", emailSender ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "emailAddress", emailAddress ) );

    retval.append( super.getXML() );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      smtpCheck = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "smtpCheck" ) );
      timeout = XMLHandler.getTagValue( entrynode, "timeout" );
      defaultSMTP = XMLHandler.getTagValue( entrynode, "defaultSMTP" );
      emailSender = XMLHandler.getTagValue( entrynode, "emailSender" );
      emailAddress = XMLHandler.getTagValue( entrynode, "emailAddress" );

    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobEntryMailValidator.Meta.UnableToLoadFromXML" ), e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      smtpCheck = rep.getJobEntryAttributeBoolean( id_jobentry, "smtpCheck" );
      timeout = rep.getJobEntryAttributeString( id_jobentry, "timeout" );
      defaultSMTP = rep.getJobEntryAttributeString( id_jobentry, "defaultSMTP" );
      emailSender = rep.getJobEntryAttributeString( id_jobentry, "emailSender" );
      emailAddress = rep.getJobEntryAttributeString( id_jobentry, "emailAddress" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryMailValidator.Meta.UnableToLoadFromRep" )
        + id_jobentry, dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "smtpCheck", smtpCheck );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", timeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "defaultSMTP", defaultSMTP );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "emailSender", emailSender );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "emailAddress", emailAddress );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryMailValidator.Meta.UnableToSaveToRep" )
        + id_job, dbe );
    }
  }

  /**
   * Execute this job entry and return the result. In this case it means, just set the result boolean in the Result
   * class.
   *
   * @param previousResult
   *          The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );

    String realEmailAddress = environmentSubstitute( emailAddress );
    if ( Utils.isEmpty( realEmailAddress ) ) {
      logError( BaseMessages.getString( PKG, "JobEntryMailValidator.Error.EmailEmpty" ) );
      return result;
    }
    String realSender = environmentSubstitute( emailSender );
    if ( smtpCheck ) {
      // check sender
      if ( Utils.isEmpty( realSender ) ) {
        logError( BaseMessages.getString( PKG, "JobEntryMailValidator.Error.EmailSenderEmpty" ) );
        return result;
      }
    }

    String realDefaultSMTP = environmentSubstitute( defaultSMTP );
    int timeOut = Const.toInt( environmentSubstitute( timeout ), 0 );

    // Split the mail-address: separated by space
    String[] mailsCheck = realEmailAddress.split( " " );
    boolean exitloop = false;
    boolean mailIsValid = false;
    String MailError = null;
    for ( int i = 0; i < mailsCheck.length && !exitloop; i++ ) {
      String email = mailsCheck[i];
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryMailValidator.CheckingMail", email ) );
      }

      // Check if address is valid
      MailValidationResult resultValidator =
        MailValidation.isAddressValid( log, email, realSender, realDefaultSMTP, timeOut, smtpCheck );

      mailIsValid = resultValidator.isValide();
      MailError = resultValidator.getErrorMessage();

      if ( log.isDetailed() ) {
        if ( mailIsValid ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryMailValidator.MailValid", email ) );
        } else {
          logDetailed( BaseMessages.getString( PKG, "JobEntryMailValidator.MailNotValid", email ) );
          logDetailed( MailError );
        }

      }
      // invalid mail? exit loop
      if ( !resultValidator.isValide() ) {
        exitloop = true;
      }
    }

    result.setResult( mailIsValid );
    if ( mailIsValid ) {
      result.setNrErrors( 0 );
    }

    // return result

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    JobEntryValidatorUtils.andValidator().validate( this, "emailAddress", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "emailSender", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.emailValidator() ) );

    if ( isSMTPCheck() ) {
      JobEntryValidatorUtils.andValidator().validate( this, "defaultSMTP", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    }
  }
}
