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

package org.pentaho.di.trans.steps.syslog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.syslog.SyslogDefs;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.productivity.java.syslog4j.SyslogConstants;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

/**
 * Write message to SyslogMessage *
 *
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class SyslogMessage extends BaseStep implements StepInterface {
  private static Class<?> PKG = SyslogMessageMeta.class; // for i18n purposes, needed by Translator2!!

  private SyslogMessageMeta meta;
  private SyslogMessageData data;

  public SyslogMessage( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SyslogMessageMeta) smi;
    data = (SyslogMessageData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }
    if ( first ) {
      first = false;
      // Check if message field is provided
      if ( Utils.isEmpty( meta.getMessageFieldName() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "SyslogMessage.Error.MessageFieldMissing" ) );
      }

      // cache the position of the source filename field
      data.indexOfMessageFieldName = getInputRowMeta().indexOfValue( meta.getMessageFieldName() );
      if ( data.indexOfMessageFieldName < 0 ) {
        // The field is unreachable !
        throw new KettleException( BaseMessages.getString( PKG, "SyslogMessage.Exception.CouldnotFindField", meta
            .getMessageFieldName() ) );
      }

    }

    try {
      // get message
      String message = getInputRowMeta().getString( r, data.indexOfMessageFieldName );

      if ( Utils.isEmpty( message ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "SyslogMessage.Error.MessageEmpty" ) );
      }

      // Send message
      SyslogDefs.sendMessage( data.syslog, SyslogDefs.getPriority( meta.getPriority() ), message,
          meta.isAddTimestamp(), data.datePattern, meta.isAddHostName() );

      putRow( getInputRowMeta(), r ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SyslogMessage.LineNumber", getLinesRead() ) );
        }
      }
    } catch ( Exception e ) {

      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "SyslogMessage.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "SyslogMessage001" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SyslogMessageMeta) smi;
    data = (SyslogMessageData) sdi;

    if ( super.init( smi, sdi ) ) {
      String servername = environmentSubstitute( meta.getServerName() );

      // Check target server
      if ( Utils.isEmpty( servername ) ) {
        logError( BaseMessages.getString( PKG, "SyslogMessage.MissingServerName" ) );
      }

      // Check if message field is provided
      if ( Utils.isEmpty( meta.getMessageFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "SyslogMessage.Error.MessageFieldMissing" ) );
        return false;
      }

      int nrPort = Const.toInt( environmentSubstitute( meta.getPort() ), SyslogDefs.DEFAULT_PORT );

      if ( meta.isAddTimestamp() ) {
        // add timestamp to message
        data.datePattern = environmentSubstitute( meta.getDatePattern() );
        if ( Utils.isEmpty( data.datePattern ) ) {
          logError( BaseMessages.getString( PKG, "SyslogMessage.DatePatternEmpty" ) );
          return false;
        }
      }

      try {
        // Connect to syslog ...
        data.syslog = getSyslog();
        data.syslog.initialize( SyslogConstants.UDP, new UDPNetSyslogConfig() );
        data.syslog.getConfig().setHost( servername );
        data.syslog.getConfig().setPort( nrPort );
        data.syslog.getConfig().setFacility( meta.getFacility() );
        data.syslog.getConfig().setSendLocalName( false );
        data.syslog.getConfig().setSendLocalTimestamp( false );
      } catch ( Exception ex ) {
        logError( BaseMessages.getString( PKG, "SyslogMessage.UnknownHost", servername, ex.getMessage() ) );
        logError( Const.getStackTracker( ex ) );
        return false;
      }
      return true;
    }
    return false;
  }

  protected SyslogIF getSyslog() {
    return new UDPNetSyslog();
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SyslogMessageMeta) smi;
    data = (SyslogMessageData) sdi;

    if ( data.syslog != null ) {
      // release resource on syslog
      data.syslog.shutdown();
    }
    super.dispose( smi, sdi );
  }
}
