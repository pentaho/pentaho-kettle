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


package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.i18n.BaseMessages;

public class RunThread implements Runnable {

  /** for i18n purposes, needed byTranslator2!! */
  private static Class<?> PKG = BaseStep.class;

  private StepInterface step;
  private StepMetaInterface meta;
  private StepDataInterface data;
  private LogChannelInterface log;

  public RunThread( StepMetaDataCombi combi ) {
    this.step = combi.step;
    this.meta = combi.meta;
    this.data = combi.data;
    this.log = step.getLogChannel();
  }

  public void run() {
    try {
      step.setRunning( true );
      step.getLogChannel().snap( Metrics.METRIC_STEP_EXECUTION_START );

      step.beforeStartProcessing( meta, data );
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( "System.Log.StartingToRun" ) );
      }

      // Wait
      while ( step.processRow( meta, data ) ) {
        if ( step.isStopped() ) {
          break;
        }
      }
    } catch ( Throwable t ) {
      try {
        // check for OOME
        if ( t instanceof OutOfMemoryError ) {
          // Handle this different with as less overhead as possible to get an error message in the log.
          // Otherwise it crashes likely with another OOME in Me$$ages.getString() and does not log
          // nor call the setErrors() and stopAll() below.
          log.logError( "UnexpectedError: ", t );
        } else {
          log.logError( BaseMessages.getString( "System.Log.UnexpectedError" ), t );
        }

        String logChannelId = log.getLogChannelId();
        LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject( logChannelId );
        String parentLogChannelId = loggingObject.getParent().getLogChannelId();
        List<String> logChannelChildren = LoggingRegistry.getInstance().getLogChannelChildren( parentLogChannelId );
        int childIndex = Const.indexOfString( log.getLogChannelId(), logChannelChildren );
        if ( log.isDebug() ) {
          log.logDebug( "child index = " + childIndex + ", logging object : " + loggingObject.toString() + " parent=" + parentLogChannelId );
        }
        KettleLogStore.getAppender().getBuffer( "2bcc6b3f-c660-4a8b-8b17-89e8cbd5b29b", false );
        // baseStep.logError(Const.getStackTracker(t));
      } catch ( OutOfMemoryError e ) {
        e.printStackTrace();
      } finally {
        step.setErrors( 1 );
        step.stopAll();
      }
    } finally {
      step.afterFinishProcessing( meta, data );
      step.dispose( meta, data );
      step.getLogChannel().snap( Metrics.METRIC_STEP_EXECUTION_STOP );
      try {
        long li = step.getLinesInput();
        long lo = step.getLinesOutput();
        long lr = step.getLinesRead();
        long lw = step.getLinesWritten();
        long lu = step.getLinesUpdated();
        long lj = step.getLinesRejected();
        long e = step.getErrors();
        if ( li > 0 || lo > 0 || lr > 0 || lw > 0 || lu > 0 || lj > 0 || e > 0 ) {
          log.logBasic( BaseMessages.getString( PKG, "BaseStep.Log.SummaryInfo", String.valueOf( li ),
            String.valueOf( lo ), String.valueOf( lr ), String.valueOf( lw ),
            String.valueOf( lu ), String.valueOf( e + lj ) ) );
        } else {
          log.logDetailed( BaseMessages.getString( PKG, "BaseStep.Log.SummaryInfo", String.valueOf( li ),
            String.valueOf( lo ), String.valueOf( lr ), String.valueOf( lw ),
            String.valueOf( lu ), String.valueOf( e + lj ) ) );
        }
      } catch ( Throwable t ) {
        //
        // it's likely an OOME, so we don't want to introduce overhead by using BaseMessages.getString(), see above
        //
        log.logError( "UnexpectedError: " + Const.getStackTracker( t ) );
      } finally {
        step.markStop();
      }
    }
  }
}
