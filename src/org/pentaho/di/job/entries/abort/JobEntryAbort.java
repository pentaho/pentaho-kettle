/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.abort;

import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.addOkRemark;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

/**
 * Job entry type to abort a job.
 *
 * @author Samatar
 * @since 12-02-2007
 */
public class JobEntryAbort extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryAbort.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private String messageAbort;

  public JobEntryAbort(String n, String scr) {
    super(n, ""); //$NON-NLS-1$
    messageAbort = null;
  }

  public JobEntryAbort() {
    this("", ""); //$NON-NLS-1$//$NON-NLS-2$
  }

  public Object clone() {
    JobEntryAbort je = (JobEntryAbort) super.clone();
    return je;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer();

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("message", messageAbort)); //$NON-NLS-1$//$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
    try {
      super.loadXML(entrynode, databases, slaveServers);
      messageAbort = XMLHandler.getTagValue(entrynode, "message"); //$NON-NLS-1$
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryAbort.UnableToLoadFromXml.Label"), e); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
    try {
      messageAbort = rep.getJobEntryAttributeString(id_jobentry, "message"); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe) {
      throw new KettleException(BaseMessages.getString(PKG, "JobEntryAbort.UnableToLoadFromRepo.Label", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  // Save the attributes of this job entry
  //
  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
    try {
      rep.saveJobEntryAttribute(id_job, getObjectId(), "message", messageAbort); //$NON-NLS-1$

    } catch (KettleDatabaseException dbe) {
      throw new KettleException(
          BaseMessages.getString(PKG, "JobEntryAbort.UnableToSaveToRepo.Label", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public boolean evaluate(Result result) {
    String Returnmessage = null;
    String RealMessageabort = environmentSubstitute(getMessageabort());

    try {
      // Return False
      if (RealMessageabort == null) {
        Returnmessage = BaseMessages.getString(PKG, "JobEntryAbort.Meta.CheckResult.Label"); //$NON-NLS-1$
      } else {
        Returnmessage = RealMessageabort;

      }
      logError(Returnmessage);
      result.setNrErrors(1);
      return false;
    } catch (Exception e) {
      result.setNrErrors(1);
      logError(BaseMessages.getString(PKG, "JobEntryAbort.Meta.CheckResult.CouldNotExecute") + e.toString()); //$NON-NLS-1$
      return false;
    }
  }

  /**
   * Execute this job entry and return the result.
   * In this case it means, just set the result boolean in the Result class.
   * @param previousResult The result of the previous execution
   * @return The Result of the execution.
   */
  public Result execute(Result previousResult, int nr) {
    previousResult.setResult(evaluate(previousResult));
    // we fail so stop 
    // job execution
    parentJob.stopAll();
    return previousResult;
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

  public void setMessageabort(String messageabort) {
    this.messageAbort = messageabort;
  }

  public String getMessageabort() {
    return messageAbort;
  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta) {
    addOkRemark(this, "messageabort", remarks); //$NON-NLS-1$
  }  
}