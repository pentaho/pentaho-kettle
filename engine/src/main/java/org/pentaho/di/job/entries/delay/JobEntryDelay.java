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

package org.pentaho.di.job.entries.delay;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
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
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Job entry type to sleep for a time. It uses a piece of javascript to do this.
 *
 * @author Samatar
 * @since 21-02-2007
 */
public class JobEntryDelay extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryDelay.class; // for i18n purposes, needed by Translator2!!

  private static String DEFAULT_MAXIMUM_TIMEOUT = "0";

  private String maximumTimeout; // maximum timeout in seconds

  public int scaleTime;

  public JobEntryDelay( String n ) {
    super( n, "" );
  }

  public JobEntryDelay() {
    this( "" );
  }

  @Override
  public Object clone() {
    JobEntryDelay je = (JobEntryDelay) super.clone();
    return je;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maximumTimeout", maximumTimeout ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "scaletime", scaleTime ) );

    return retval.toString();
  }

  @Override
  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      maximumTimeout = XMLHandler.getTagValue( entrynode, "maximumTimeout" );
      scaleTime = Integer.parseInt( XMLHandler.getTagValue( entrynode, "scaletime" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobEntryDelay.UnableToLoadFromXml.Label" ), e );
    }
  }

  @Override
  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      maximumTimeout = rep.getJobEntryAttributeString( id_jobentry, "maximumTimeout" );
      scaleTime = (int) rep.getJobEntryAttributeInteger( id_jobentry, "scaletime" );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobEntryDelay.UnableToLoadFromRepo.Label" )
        + id_jobentry, dbe );
    }
  }

  //
  // Save the attributes of this job entry
  //
  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "maximumTimeout", maximumTimeout );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "scaletime", scaleTime );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JobEntryDelay.UnableToSaveToRepo.Label" ) + id_job, dbe );
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
  @Override
  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );
    int Multiple;
    String Waitscale;

    // Scale time
    switch ( scaleTime ) {
      case 0:
        // Second
        Multiple = 1000;
        Waitscale = BaseMessages.getString( PKG, "JobEntryDelay.SScaleTime.Label" );
        break;
      case 1:
        // Minute
        Multiple = 60000;
        Waitscale = BaseMessages.getString( PKG, "JobEntryDelay.MnScaleTime.Label" );
        break;
      default:
        // Hour
        Multiple = 3600000;
        Waitscale = BaseMessages.getString( PKG, "JobEntryDelay.HrScaleTime.Label" );
        break;
    }

    try {
      // starttime (in seconds ,Minutes or Hours)
      long timeStart = System.currentTimeMillis() / Multiple;

      long iMaximumTimeout = Const.toInt( getRealMaximumTimeout(), Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 ) );

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryDelay.LetsWaitFor.Label", iMaximumTimeout, Waitscale ) );
      }

      boolean continueLoop = true;
      //
      // Sanity check on some values, and complain on insanity
      //
      if ( iMaximumTimeout < 0 ) {
        iMaximumTimeout = Const.toInt( DEFAULT_MAXIMUM_TIMEOUT, 0 );
        logBasic( BaseMessages.getString( PKG, "JobEntryDelay.MaximumTimeReset.Label", String
          .valueOf( iMaximumTimeout ), String.valueOf( Waitscale ) ) );
      }

      // Loop until the delay time has expired.
      //
      while ( continueLoop && !parentJob.isStopped() ) {
        // Update Time value
        long now = System.currentTimeMillis() / Multiple;

        // Let's check the limit time
        if ( ( iMaximumTimeout > 0 ) && ( now >= ( timeStart + iMaximumTimeout ) ) ) {
          // We have reached the time limit
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryDelay.WaitTimeIsElapsed.Label" ) );
          }
          continueLoop = false;
          result.setResult( true );
        } else {
          Thread.sleep( 100 );
        }
      }
    } catch ( Exception e ) {
      // We get an exception
      result.setResult( false );
      logError( "Error  : " + e.getMessage() );
    }

    return result;
  }

  @Override
  public boolean resetErrorsBeforeExecution() {
    // we should be able to evaluate the errors in
    // the previous jobentry.
    return false;
  }

  @Override
  public boolean evaluates() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return false;
  }

  public String getMaximumTimeout() {
    return maximumTimeout;
  }

  public String getRealMaximumTimeout() {
    return Const.trim( environmentSubstitute( getMaximumTimeout() ) );
  }

  @Deprecated
  public String getrealMaximumTimeout() {
    return getRealMaximumTimeout();
  }

  public void setMaximumTimeout( String s ) {
    maximumTimeout = s;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "maximumTimeout", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.longValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "scaleTime", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

  public int getScaleTime() {
    return scaleTime;
  }

  public void setScaleTime( int scaleTime ) {
    this.scaleTime = scaleTime;
  }
}
