/*
 * ! ******************************************************************************
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.trilead.ssh2.Session;

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

    Session session = null;
    SshStepConnectionAdapter.StepSessionAdapter modernSession = null;
    try {
      if ( meta.isDynamicCommand() ) {
        // get commands
        data.commands = data.outputRowMeta.getString( row, data.indexOfCommand );
        if ( Utils.isEmpty( data.commands ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "SSH.Error.MessageEmpty" ) );
        }
      }

      // Open a session using the appropriate connection type
      SessionResultAdapter sessionresult;
      if ( data.sshConn != null ) {
        // Use modern SSH abstraction layer
        modernSession = data.sshConn.openSession();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.SessionOpened" )
            + " (modern implementation)" );
        }

        // execute commands
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SSH.Log.RunningCommand", data.commands ) );
        }
        modernSession.execCommand( data.commands );

        // Read results using adapter
        sessionresult = new SessionResultAdapter( modernSession );
      } else {
        // Use legacy Trilead implementation
        session = data.conn.openSession();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.SessionOpened" )
            + " (legacy implementation)" );
        }

        // execute commands
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SSH.Log.RunningCommand", data.commands ) );
        }
        session.execCommand( data.commands );

        // Read results using legacy SessionResult
        sessionresult = new SessionResultAdapter( session );
      }

      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "SSH.Log.CommandRunnedCommand", data.commands, sessionresult
          .getStdOut(), sessionresult.getStdErr() ) );
      }

      // Add stdout to output
      rowData[ index++ ] = sessionresult.getStd();

      if ( !Utils.isEmpty( data.stdTypeField ) ) {
        // Add stdtype to output
        rowData[ index++ ] = sessionresult.isStdTypeErr();
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
      // Close the appropriate session type
      if ( modernSession != null ) {
        modernSession.close();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.SessionClosed" ) +
            " (modern implementation)" );
        }
      } else if ( session != null ) {
        session.close();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.SessionClosed" ) +
            " (legacy implementation)" );
        }
      }
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
        boolean useModern = meta.getSshImplementation() != null || useModernSshImplementation();

        if ( useModern ) {
          logBasic( "SSH Step: Using modern SSH implementation" );

          try {
            data.sshConn = SSHData.OpenSshConnection(
              getTransMeta().getBowl(), servername, nrPort, username, password, meta.isusePrivateKey(), keyFilename,
              passphrase, timeOut, this, proxyhost, proxyport, proxyusername, proxypassword,
              meta.getSshImplementation(), log );

            logBasic( "SSH Step: Modern SSH connection created successfully" );
          } catch ( Exception e ) {
            logBasic( "SSH Step: Modern SSH implementation failed (" + e.getClass().getSimpleName() + ": " + e
              .getMessage() );
          }
        } else {
          logBasic( "SSH Step: Using legacy Trilead implementation" );

          data.conn = SSHData.OpenConnection(
            getTransMeta().getBowl(), servername, nrPort, username, password, meta.isusePrivateKey(), keyFilename,
            passphrase, timeOut, this, proxyhost, proxyport, proxyusername, proxypassword );

          logBasic( "SSH Step: Legacy Trilead connection created successfully" );
        }

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

  /**
   * Determine if we should use the modern SSH implementation.
   * This can be controlled by system properties or other configuration.
   */
  private boolean useModernSshImplementation() {
    // Check for system property to enable modern SSH by default
    String enableModern = System.getProperty( "pentaho.ssh.use.modern", "false" );
    logBasic( "SSH Step: System property pentaho.ssh.use.modern = " + enableModern );
    return "true".equalsIgnoreCase( enableModern );
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SSHMeta) smi;
    data = (SSHData) sdi;

    // Close the appropriate connection type
    try {
      if ( data.sshConn != null ) {
        data.sshConn.close();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.ConnectionClosed" ) +
            " (modern SSH implementation)" );
        }
      } else if ( data.conn != null ) {
        data.conn.close();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SSH.Log.ConnectionClosed" ) +
            " (legacy Trilead implementation)" );
        }
      }
    } catch ( Exception e ) {
      logError( "Error closing SSH connection: " + e.getMessage() );
    }

    super.dispose( smi, sdi );
  }
}
