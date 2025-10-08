/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.ssh.ExecResult;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Write commands to SSH *
 *
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class SSH extends BaseStep {
  private static final Class<?> PKG = SSHMeta.class; // for i18n purposes, needed by Translator2!!

  private SSHMeta meta;
  private SSHData data;

  public SSH( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SSHMeta) smi;
    data = (SSHData) sdi;

    Object[] row;
    if ( meta.isDynamicCommand() ) {
      row = getRow();
      if ( row == null ) {
        setOutputDone();
        return false;
      }
      if ( first ) {
        first = false;
        data.outputRowMeta = getInputRowMeta().clone();
        data.nrInputFields = data.outputRowMeta.size();
        meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
          metaStore );
        data.nrOutputFields = data.outputRowMeta.size();

        // Check if commands field is provided
        if ( meta.isDynamicCommand() ) {
          if ( Utils.isEmpty( meta.getcommandfieldname() ) ) {
            throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.CommandFieldMissing" ) );
          }
          // cache the position of the source filename field
          data.indexOfCommand = data.outputRowMeta.indexOfValue( meta.getcommandfieldname() );
          if ( data.indexOfCommand < 0 ) {
            // The field is unreachable !
            throw new KettleException( BaseMessages.getString( PKG, "SSH.Exception.CouldnotFindField", meta
              .getcommandfieldname() ) );
          }
        }
      }
    } else {
      if ( !data.wroteOneRow ) {
        row = new Object[] {}; // empty row
        incrementLinesRead();
        data.wroteOneRow = true;
        if ( first ) {
          first = false;
          data.outputRowMeta = new RowMeta();
          data.nrInputFields = 0;
          meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
            metaStore );
          data.nrOutputFields = data.outputRowMeta.size();
          data.commands = environmentSubstitute( meta.getCommand() );
        }
      } else {
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }

    RowMetaInterface imeta = getInputRowMeta();
    if ( imeta == null ) {
      imeta = new RowMeta();
      this.setInputRowMeta( imeta );
    }
    // Reserve room
    Object[] rowData = new Object[ data.nrOutputFields ];
    for ( int i = 0; i < data.nrInputFields; i++ ) {
      rowData[ i ] = row[ i ]; // no data is changed, clone is not needed here.
    }
    int index = data.nrInputFields;

    try {
      if ( meta.isDynamicCommand() ) {
        // get commands
        data.commands = data.outputRowMeta.getString( row, data.indexOfCommand );
        if ( Utils.isEmpty( data.commands ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.MessageEmpty" ) );
        }
      }

      // Connect if not already connected
      if ( !data.isConnected() ) {
        data.getSshConnection().connect();
        data.setConnected( true );
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.SessionOpened" ) );
        }
      }

      // execute commands
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "SSH.Log.RunningCommand", data.commands ) );
      }
      ExecResult execResult = data.getSshConnection().exec( data.commands, 30000L ); // 30 second timeout

      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "SSH.Log.CommandRunnedCommand", data.commands, execResult
          .getStdout(), execResult.getStderr() ) );
      }

      // Add stdout to output
      rowData[ index++ ] = execResult.getCombined();

      if ( !Utils.isEmpty( data.stdTypeField ) ) {
        // Add stdtype to output
        rowData[ index ] = execResult.hasErrorOutput();
      }

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "SSH.Log.OutputLine", data.outputRowMeta.getString( rowData ) ) );
      }

      putRow( data.outputRowMeta, rowData );

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SSH.LineNumber", "" + getLinesRead() ) );
        }
      }
    } catch ( Exception e ) {

      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "SSH.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), row, 1, errorMessage, null, "SSH001" );
      }
    } finally {
      // No session cleanup needed - connection lifecycle is managed separately
    }

    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SSHMeta) smi;
    data = (SSHData) sdi;

    if ( super.init( smi, sdi ) ) {
      String servername = environmentSubstitute( meta.getServerName() );
      int nrPort = Const.toInt( environmentSubstitute( meta.getPort() ), 22 );
      String username = environmentSubstitute( meta.getuserName() );
      String password = Utils.resolvePassword( variables, meta.getpassword() );
      String keyFilename = environmentSubstitute( meta.getKeyFileName() );
      String passphrase = environmentSubstitute( meta.getPassphrase() );
      int timeOut = Const.toInt( environmentSubstitute( meta.getTimeOut() ), 0 );
      String proxyhost = environmentSubstitute( meta.getProxyHost() );
      int proxyport = Const.toInt( environmentSubstitute( meta.getProxyPort() ), 0 );
      String proxyusername = environmentSubstitute( meta.getProxyUsername() );
      String proxypassword = environmentSubstitute( meta.getProxyPassword() );

      // Check target server
      if ( Utils.isEmpty( servername ) ) {
        logError( BaseMessages.getString( PKG, "SSH.MissingServerName" ) );
      }

      // Check if username field is provided
      if ( Utils.isEmpty( meta.getuserName() ) ) {
        logError( BaseMessages.getString( PKG, "SSH.Error.UserNamedMissing" ) );
        return false;
      }

      // Get output fields
      data.stdOutField = environmentSubstitute( meta.getStdOutFieldName() );
      if ( Utils.isEmpty( data.stdOutField ) ) {
        logError( BaseMessages.getString( PKG, "SSH.Error.StdOutFieldNameMissing" ) );
        return false;
      }
      data.stdTypeField = environmentSubstitute( meta.getStdErrFieldName() );

      try {
        logBasic( "Creating SSH connection" );

        SshConnectionParameters params = SshConnectionParameters.builder()
            .bowl( getTransMeta().getBowl() )
            .server( servername )
            .port( nrPort )
            .username( username )
            .password( password )
            .useKey( meta.isusePrivateKey() )
            .keyFilename( keyFilename )
            .passPhrase( passphrase )
            .timeOut( timeOut )
            .space( this )
            .proxyhost( proxyhost )
            .proxyport( proxyport )
            .proxyusername( proxyusername )
            .proxypassword( proxypassword )
            .build();

        data.setSshConnection( SSHData.openSshConnection( params, log ) );

        logBasic( "SSH connection created successfully" );

      } catch ( Exception e ) {
        logError( "SSH connection initialization failed:" );
        logError( "  Server: " + servername + ":" + nrPort );
        logError( "  Username: " + username );
        logError( "  Use Private Key: " + meta.isusePrivateKey() );
        if ( meta.isusePrivateKey() ) {
          logError( "  Key File: " + keyFilename );
        }
        logError( "  Timeout: " + timeOut + "ms" );
        logError( "  Error: " + e.getClass().getSimpleName() + ": " + e.getMessage() );

        if ( log.isDebug() ) {
          logError( "Full stack trace:", e );
        }

        logError( BaseMessages.getString( PKG, "SSH.Error.OpeningConnection", e.getMessage() ) );
        return false;
      }

      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SSHMeta) smi;
    data = (SSHData) sdi;

    // Close the SSH connection
    try {
      if ( data.getSshConnection() != null ) {
        data.getSshConnection().close();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.ConnectionClosed" ) );
        }
      }
    } catch ( Exception e ) {
      logError( "Error closing SSH connection: " + e.getMessage() );
    }

    super.dispose( smi, sdi );
  }
}
