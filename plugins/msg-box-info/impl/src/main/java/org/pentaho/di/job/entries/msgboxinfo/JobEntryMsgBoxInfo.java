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

package org.pentaho.di.job.entries.msgboxinfo;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIFactory;
import org.pentaho.di.core.gui.ThreadDialogs;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job entry type to display a message box.
 *
 * @author Samatar
 * @since 12-02-2007
 */


@JobEntry(id = "MSGBOX_INFO",name = "JobEntry.MsgBoxInfo.TypeDesc",
        description = "JobEntry.MsgBoxInfo.Tooltip",categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Utility",
        i18nPackageName = "i18n:org.pentaho.di.job.entry:JobEntry.MsgBoxInfo",documentationUrl = "http://wiki.pentaho.com/display/EAI/Display+Msgbox+info")
public class JobEntryMsgBoxInfo extends JobEntryBase implements Cloneable, JobEntryInterface {
  private String bodymessage;
  private String titremessage;

  public JobEntryMsgBoxInfo( String n, String scr ) {
    super( n, "" );
    bodymessage = null;
    titremessage = null;
  }

  public JobEntryMsgBoxInfo() {
    this( "", "" );
  }

  public Object clone() {
    JobEntryMsgBoxInfo je = (JobEntryMsgBoxInfo) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 50 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "bodymessage", bodymessage ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "titremessage", titremessage ) );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      bodymessage = XMLHandler.getTagValue( entrynode, "bodymessage" );
      titremessage = XMLHandler.getTagValue( entrynode, "titremessage" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load job entry of type 'Msgbox Info' from XML node", e );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      bodymessage = rep.getJobEntryAttributeString( id_jobentry, "bodymessage" );
      titremessage = rep.getJobEntryAttributeString( id_jobentry, "titremessage" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'Msgbox Info' from the repository with id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "bodymessage", bodymessage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "titremessage", titremessage );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'Msgbox Info' to the repository for id_job="
        + id_job, dbe );
    }
  }

  /**
   * Display the Message Box.
   */
  public boolean evaluate( Result result ) {
    try {
      // default to ok

      // Try to display MSGBOX
      boolean response = true;

      ThreadDialogs dialogs = GUIFactory.getThreadDialogs();
      if ( dialogs != null ) {
        response =
          dialogs.threadMessageBox( getRealBodyMessage() + Const.CR, getRealTitleMessage(), true, Const.INFO );
      }

      return response;

    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( "Couldn't display message box: " + e.toString() );
      return false;
    }

  }

  /**
   * Execute this job entry and return the result. In this case it means, just set the result boolean in the Result
   * class.
   *
   * @param prev_result
   *          The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute( Result prev_result, int nr ) {
    prev_result.setResult( evaluate( prev_result ) );
    return prev_result;
  }

  public boolean resetErrorsBeforeExecution() {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return false;
  }

  public String getRealTitleMessage() {
    return environmentSubstitute( getTitleMessage() );
  }

  public String getRealBodyMessage() {
    return environmentSubstitute( getBodyMessage() );
  }

  public String getTitleMessage() {
    if ( titremessage == null ) {
      titremessage = "";
    }
    return titremessage;
  }

  public String getBodyMessage() {
    if ( bodymessage == null ) {
      bodymessage = "";
    }
    return bodymessage;

  }

  public void setBodyMessage( String s ) {

    bodymessage = s;

  }

  public void setTitleMessage( String s ) {

    titremessage = s;

  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.addOkRemark( this, "bodyMessage", remarks );
    JobEntryValidatorUtils.addOkRemark( this, "titleMessage", remarks );
  }

}
