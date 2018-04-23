/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.www.SocketRepository;

import java.util.List;
import java.util.Map;

public class UserDefinedJavaClass extends BaseStep implements StepInterface {
  private TransformClassBase child;
  protected final UserDefinedJavaClassMeta meta;
  protected final UserDefinedJavaClassData data;
  public static final String KETTLE_DEFAULT_CLASS_CACHE_SIZE = "KETTLE_DEFAULT_CLASS_CACHE_SIZE";

  public UserDefinedJavaClass( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
    TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    meta = (UserDefinedJavaClassMeta) ( stepMeta.getStepMetaInterface() );
    data = (UserDefinedJavaClassData) stepDataInterface;

    if ( copyNr == 0 ) {
      meta.cookClasses();
    }

    child = meta.newChildInstance( this, meta, data );

    if ( meta.cookErrors.size() > 0 ) {
      for ( Exception e : meta.cookErrors ) {
        logErrorImpl( "Error initializing UserDefinedJavaClass:", e );
      }
      setErrorsImpl( meta.cookErrors.size() );
      stopAllImpl();
    }
  }

  public void addResultFile( ResultFile resultFile ) {
    if ( child == null ) {
      addResultFileImpl( resultFile );
    } else {
      child.addResultFile( resultFile );
    }
  }

  public void addResultFileImpl( ResultFile resultFile ) {
    super.addResultFile( resultFile );
  }

  public void addRowListener( RowListener rowListener ) {
    if ( child == null ) {
      addRowListenerImpl( rowListener );
    } else {
      child.addRowListener( rowListener );
    }
  }

  public void addRowListenerImpl( RowListener rowListener ) {
    super.addRowListener( rowListener );
  }

  public void addStepListener( StepListener stepListener ) {
    if ( child == null ) {
      addStepListenerImpl( stepListener );
    } else {
      child.addStepListener( stepListener );
    }
  }

  public void addStepListenerImpl( StepListener stepListener ) {
    super.addStepListener( stepListener );
  }

  public boolean checkFeedback( long lines ) {
    if ( child == null ) {
      return checkFeedbackImpl( lines );
    } else {
      return child.checkFeedback( lines );
    }
  }

  public boolean checkFeedbackImpl( long lines ) {
    return super.checkFeedback( lines );
  }

  public void cleanup() {
    if ( child == null ) {
      cleanupImpl();
    } else {
      child.cleanup();
    }
  }

  public void cleanupImpl() {
    super.cleanup();
  }

  public long decrementLinesRead() {
    if ( child == null ) {
      return decrementLinesReadImpl();
    } else {
      return child.decrementLinesRead();
    }
  }

  public long decrementLinesReadImpl() {
    return super.decrementLinesRead();
  }

  public long decrementLinesWritten() {
    if ( child == null ) {
      return decrementLinesWrittenImpl();
    } else {
      return child.decrementLinesWritten();
    }
  }

  public long decrementLinesWrittenImpl() {
    return super.decrementLinesWritten();
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( child == null ) {
      disposeImpl( smi, sdi );
    } else {
      child.dispose( smi, sdi );
    }
  }

  public void disposeImpl( StepMetaInterface smi, StepDataInterface sdi ) {
    super.dispose( smi, sdi );
  }

  public RowSet findInputRowSet( String sourceStep ) throws KettleStepException {
    if ( child == null ) {
      return findInputRowSetImpl( sourceStep );
    } else {
      return child.findInputRowSet( sourceStep );
    }
  }

  public RowSet findInputRowSet( String from, int fromcopy, String to, int tocopy ) {
    if ( child == null ) {
      return findInputRowSetImpl( from, fromcopy, to, tocopy );
    } else {
      return child.findInputRowSet( from, fromcopy, to, tocopy );
    }
  }

  public RowSet findInputRowSetImpl( String sourceStep ) throws KettleStepException {
    return super.findInputRowSet( sourceStep );
  }

  public RowSet findInputRowSetImpl( String from, int fromcopy, String to, int tocopy ) {
    return super.findInputRowSet( from, fromcopy, to, tocopy );
  }

  public RowSet findOutputRowSet( String targetStep ) throws KettleStepException {
    if ( child == null ) {
      return findOutputRowSetImpl( targetStep );
    } else {
      return child.findOutputRowSet( targetStep );
    }
  }

  public RowSet findOutputRowSet( String from, int fromcopy, String to, int tocopy ) {
    if ( child == null ) {
      return findOutputRowSetImpl( from, fromcopy, to, tocopy );
    } else {
      return child.findOutputRowSet( from, fromcopy, to, tocopy );
    }
  }

  public RowSet findOutputRowSetImpl( String targetStep ) throws KettleStepException {
    return super.findOutputRowSet( targetStep );
  }

  public RowSet findOutputRowSetImpl( String from, int fromcopy, String to, int tocopy ) {
    return super.findOutputRowSet( from, fromcopy, to, tocopy );
  }

  public int getClusterSize() {
    if ( child == null ) {
      return getClusterSizeImpl();
    } else {
      return child.getClusterSize();
    }
  }

  public int getClusterSizeImpl() {
    return super.getClusterSize();
  }

  public int getCopyImpl() {
    return super.getCopy();
  }

  public RowMetaInterface getErrorRowMeta() {
    if ( child == null ) {
      return getErrorRowMetaImpl();
    } else {
      return child.getErrorRowMeta();
    }
  }

  public RowMetaInterface getErrorRowMetaImpl() {
    return super.getErrorRowMeta();
  }

  public long getErrors() {
    if ( child == null ) {
      return getErrorsImpl();
    } else {
      return child.getErrors();
    }
  }

  public long getErrorsImpl() {
    return super.getErrors();
  }

  public RowMetaInterface getInputRowMeta() {
    if ( child == null ) {
      return getInputRowMetaImpl();
    } else {
      return child.getInputRowMeta();
    }
  }

  public RowMetaInterface getInputRowMetaImpl() {
    return super.getInputRowMeta();
  }

  public List<RowSet> getInputRowSets() {
    return child.getInputRowSets();
  }

  public List<RowSet> getInputRowSetsImpl() {
    return super.getInputRowSets();
  }

  public long getLinesInput() {
    if ( child == null ) {
      return getLinesInputImpl();
    } else {
      return child.getLinesInput();
    }
  }

  public long getLinesInputImpl() {
    return super.getLinesInput();
  }

  public long getLinesOutput() {
    if ( child == null ) {
      return getLinesOutputImpl();
    } else {
      return child.getLinesOutput();
    }
  }

  public long getLinesOutputImpl() {
    return super.getLinesOutput();
  }

  public long getLinesRead() {
    if ( child == null ) {
      return getLinesReadImpl();
    } else {
      return child.getLinesRead();
    }
  }

  public long getLinesReadImpl() {
    return super.getLinesRead();
  }

  public long getLinesRejected() {
    if ( child == null ) {
      return getLinesRejectedImpl();
    } else {
      return child.getLinesRejected();
    }
  }

  public long getLinesRejectedImpl() {
    return super.getLinesRejected();
  }

  public long getLinesSkipped() {
    if ( child == null ) {
      return getLinesSkippedImpl();
    } else {
      return child.getLinesSkipped();
    }
  }

  public long getLinesSkippedImpl() {
    return super.getLinesSkipped();
  }

  public long getLinesUpdated() {
    if ( child == null ) {
      return getLinesUpdatedImpl();
    } else {
      return child.getLinesUpdated();
    }
  }

  public long getLinesUpdatedImpl() {
    return super.getLinesUpdated();
  }

  public long getLinesWritten() {
    if ( child == null ) {
      return getLinesWrittenImpl();
    } else {
      return child.getLinesWritten();
    }
  }

  public long getLinesWrittenImpl() {
    return super.getLinesWritten();
  }

  public List<RowSet> getOutputRowSets() {
    if ( child == null ) {
      return getOutputRowSetsImpl();
    } else {
      return child.getOutputRowSets();
    }
  }

  public List<RowSet> getOutputRowSetsImpl() {
    return super.getOutputRowSets();
  }

  public String getPartitionID() {
    if ( child == null ) {
      return getPartitionIDImpl();
    } else {
      return child.getPartitionID();
    }
  }

  public String getPartitionIDImpl() {
    return super.getPartitionID();
  }

  public Map<String, BlockingRowSet> getPartitionTargets() {
    if ( child == null ) {
      return getPartitionTargetsImpl();
    } else {
      return child.getPartitionTargets();
    }
  }

  public Map<String, BlockingRowSet> getPartitionTargetsImpl() {
    return super.getPartitionTargets();
  }

  public long getProcessed() {
    if ( child == null ) {
      return getProcessedImpl();
    } else {
      return child.getProcessed();
    }
  }

  public long getProcessedImpl() {
    return super.getProcessed();
  }

  public int getRepartitioning() {
    if ( child == null ) {
      return getRepartitioningImpl();
    } else {
      return child.getRepartitioning();
    }
  }

  public int getRepartitioningImpl() {
    return super.getRepartitioning();
  }

  public Map<String, ResultFile> getResultFiles() {
    if ( child == null ) {
      return getResultFilesImpl();
    } else {
      return child.getResultFiles();
    }
  }

  public Map<String, ResultFile> getResultFilesImpl() {
    return super.getResultFiles();
  }

  public Object[] getRow() throws KettleException {
    if ( child == null ) {
      return getRowImpl();
    } else {
      return child.getRow();
    }
  }

  public Object[] getRowFrom( RowSet rowSet ) throws KettleStepException {
    if ( child == null ) {
      return getRowFromImpl( rowSet );
    } else {
      return child.getRowFrom( rowSet );
    }
  }

  public Object[] getRowFromImpl( RowSet rowSet ) throws KettleStepException {
    return super.getRowFrom( rowSet );
  }

  public Object[] getRowImpl() throws KettleException {
    return super.getRow();
  }

  public List<RowListener> getRowListeners() {
    if ( child == null ) {
      return getRowListenersImpl();
    } else {
      return child.getRowListeners();
    }
  }

  public List<RowListener> getRowListenersImpl() {
    return super.getRowListeners();
  }

  public long getRuntime() {
    if ( child == null ) {
      return getRuntimeImpl();
    } else {
      return child.getRuntime();
    }
  }

  public long getRuntimeImpl() {
    return super.getRuntime();
  }

  public int getSlaveNr() {
    if ( child == null ) {
      return getSlaveNrImpl();
    } else {
      return child.getSlaveNr();
    }
  }

  public int getSlaveNrImpl() {
    if ( child == null ) {
      return getSlaveNrImpl();
    } else {
      return super.getSlaveNr();
    }
  }

  public SocketRepository getSocketRepository() {
    if ( child == null ) {
      return getSocketRepositoryImpl();
    } else {
      return child.getSocketRepository();
    }
  }

  public SocketRepository getSocketRepositoryImpl() {
    return super.getSocketRepository();
  }

  public StepExecutionStatus getStatus() {
    if ( child == null ) {
      return getStatusImpl();
    } else {
      return child.getStatus();
    }
  }

  public String getStatusDescription() {
    if ( child == null ) {
      return getStatusDescriptionImpl();
    } else {
      return child.getStatusDescription();
    }
  }

  public String getStatusDescriptionImpl() {
    return super.getStatusDescription();
  }

  public StepExecutionStatus getStatusImpl() {
    return super.getStatus();
  }

  public StepDataInterface getStepDataInterface() {
    if ( child == null ) {
      return getStepDataInterfaceImpl();
    } else {
      return child.getStepDataInterface();
    }
  }

  public StepDataInterface getStepDataInterfaceImpl() {
    return super.getStepDataInterface();
  }

  public String getStepID() {
    if ( child == null ) {
      return getStepIDImpl();
    } else {
      return child.getStepID();
    }
  }

  public String getStepIDImpl() {
    return super.getStepID();
  }

  public List<StepListener> getStepListeners() {
    if ( child == null ) {
      return getStepListenersImpl();
    } else {
      return child.getStepListeners();
    }
  }

  public List<StepListener> getStepListenersImpl() {
    return super.getStepListeners();
  }

  public StepMeta getStepMeta() {
    if ( child == null ) {
      return getStepMetaImpl();
    } else {
      return child.getStepMeta();
    }
  }

  public StepMeta getStepMetaImpl() {
    return super.getStepMeta();
  }

  public String getStepname() {
    if ( child == null ) {
      return getStepnameImpl();
    } else {
      return child.getStepname();
    }
  }

  public String getStepnameImpl() {
    return super.getStepname();
  }

  public Trans getTransImpl() {
    return super.getTrans();
  }

  public TransMeta getTransMeta() {
    if ( child == null ) {
      return getTransMetaImpl();
    } else {
      return child.getTransMeta();
    }
  }

  public TransMeta getTransMetaImpl() {
    return super.getTransMeta();
  }

  public String getTypeId() {
    if ( child == null ) {
      return getTypeIdImpl();
    } else {
      return child.getTypeId();
    }
  }

  public String getTypeIdImpl() {
    return super.getTypeId();
  }

  public int getUniqueStepCountAcrossSlaves() {
    if ( child == null ) {
      return getUniqueStepCountAcrossSlavesImpl();
    } else {
      return child.getUniqueStepCountAcrossSlaves();
    }
  }

  public int getUniqueStepCountAcrossSlavesImpl() {
    return super.getUniqueStepCountAcrossSlaves();
  }

  public int getUniqueStepNrAcrossSlaves() {
    if ( child == null ) {
      return getUniqueStepNrAcrossSlavesImpl();
    } else {
      return child.getUniqueStepNrAcrossSlaves();
    }
  }

  public int getUniqueStepNrAcrossSlavesImpl() {
    return super.getUniqueStepNrAcrossSlaves();
  }

  public String getVariable( String variableName ) {
    if ( child == null ) {
      return getVariableImpl( variableName );
    } else {
      return child.getVariable( variableName );
    }
  }

  public String getVariable( String variableName, String defaultValue ) {
    if ( child == null ) {
      return getVariableImpl( variableName, defaultValue );
    } else {
      return child.getVariable( variableName, defaultValue );
    }
  }

  public String getVariableImpl( String variableName ) {
    return super.getVariable( variableName );
  }

  public String getVariableImpl( String variableName, String defaultValue ) {
    return super.getVariable( variableName, defaultValue );
  }

  public long incrementLinesInput() {
    if ( child == null ) {
      return incrementLinesInputImpl();
    } else {
      return child.incrementLinesInput();
    }
  }

  public long incrementLinesInputImpl() {
    return super.incrementLinesInput();
  }

  public long incrementLinesOutput() {
    if ( child == null ) {
      return incrementLinesOutputImpl();
    } else {
      return child.incrementLinesOutput();
    }
  }

  public long incrementLinesOutputImpl() {
    return super.incrementLinesOutput();
  }

  public long incrementLinesRead() {
    if ( child == null ) {
      return incrementLinesReadImpl();
    } else {
      return child.incrementLinesRead();
    }
  }

  public long incrementLinesReadImpl() {
    return super.incrementLinesRead();
  }

  public long incrementLinesRejected() {
    if ( child == null ) {
      return incrementLinesRejectedImpl();
    } else {
      return child.incrementLinesRejected();
    }
  }

  public long incrementLinesRejectedImpl() {
    return super.incrementLinesRejected();
  }

  public long incrementLinesSkipped() {
    if ( child == null ) {
      return incrementLinesSkippedImpl();
    } else {
      return child.incrementLinesSkipped();
    }
  }

  public long incrementLinesSkippedImpl() {
    return super.incrementLinesSkipped();
  }

  public long incrementLinesUpdated() {
    if ( child == null ) {
      return incrementLinesUpdatedImpl();
    } else {
      return child.incrementLinesUpdated();
    }
  }

  public long incrementLinesUpdatedImpl() {
    return super.incrementLinesUpdated();
  }

  public long incrementLinesWritten() {
    if ( child == null ) {
      return incrementLinesWrittenImpl();
    } else {
      return child.incrementLinesWritten();
    }
  }

  public long incrementLinesWrittenImpl() {
    return super.incrementLinesWritten();
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    if ( meta.cookErrors.size() > 0 ) {
      return false;
    }

    if ( meta.cookedTransformClass == null ) {
      logError( "No UDFC marked as Transformation class" );
      return false;
    }

    if ( child == null ) {
      return initImpl( stepMetaInterface, stepDataInterface );
    } else {
      return child.init( stepMetaInterface, stepDataInterface );
    }
  }

  public void initBeforeStart() throws KettleStepException {
    if ( child == null ) {
      initBeforeStartImpl();
    } else {
      child.initBeforeStart();
    }
  }

  public void initBeforeStartImpl() throws KettleStepException {
    super.initBeforeStart();
  }

  public boolean initImpl( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    return super.init( stepMetaInterface, stepDataInterface );
  }

  public boolean isDistributed() {
    if ( child == null ) {
      return isDistributedImpl();
    } else {
      return child.isDistributed();
    }
  }

  public boolean isDistributedImpl() {
    return super.isDistributed();
  }

  public boolean isInitialising() {
    if ( child == null ) {
      return isInitialisingImpl();
    } else {
      return child.isInitialising();
    }
  }

  public boolean isInitialisingImpl() {
    return super.isInitialising();
  }

  public boolean isPartitioned() {
    if ( child == null ) {
      return isPartitionedImpl();
    } else {
      return child.isPartitioned();
    }
  }

  public boolean isPartitionedImpl() {
    return super.isPartitioned();
  }

  public boolean isSafeModeEnabled() {
    if ( child == null ) {
      return isSafeModeEnabledImpl();
    } else {
      return child.isSafeModeEnabled();
    }
  }

  public boolean isSafeModeEnabledImpl() {
    return getTrans().isSafeModeEnabled();
  }

  public boolean isStopped() {
    if ( child == null ) {
      return isStoppedImpl();
    } else {
      return child.isStopped();
    }
  }

  public boolean isStoppedImpl() {
    return super.isStopped();
  }

  public boolean isUsingThreadPriorityManagment() {
    if ( child == null ) {
      return isUsingThreadPriorityManagmentImpl();
    } else {
      return child.isUsingThreadPriorityManagment();
    }
  }

  public boolean isUsingThreadPriorityManagmentImpl() {
    return super.isUsingThreadPriorityManagment();
  }

  public void logBasic( String s ) {
    if ( child == null ) {
      logBasicImpl( s );
    } else {
      child.logBasic( s );
    }
  }

  public void logBasicImpl( String s ) {
    super.logBasic( s );
  }

  public void logDebug( String s ) {
    if ( child == null ) {
      logDebugImpl( s );
    } else {
      child.logDebug( s );
    }
  }

  public void logDebugImpl( String s ) {
    super.logDebug( s );
  }

  public void logDetailed( String s ) {
    if ( child == null ) {
      logDetailedImpl( s );
    } else {
      child.logDetailed( s );
    }
  }

  public void logDetailedImpl( String s ) {
    super.logDetailed( s );
  }

  public void logError( String s ) {
    if ( child == null ) {
      logErrorImpl( s );
    } else {
      child.logError( s );
    }
  }

  public void logError( String s, Throwable e ) {
    if ( child == null ) {
      logErrorImpl( s, e );
    } else {
      child.logError( s, e );
    }
  }

  public void logErrorImpl( String s ) {
    super.logError( s );
  }

  public void logErrorImpl( String s, Throwable e ) {
    super.logError( s, e );
  }

  public void logMinimal( String s ) {
    if ( child == null ) {
      logMinimalImpl( s );
    } else {
      child.logMinimal( s );
    }
  }

  public void logMinimalImpl( String s ) {
    super.logMinimal( s );
  }

  public void logRowlevel( String s ) {
    if ( child == null ) {
      logRowlevelImpl( s );
    } else {
      child.logRowlevel( s );
    }
  }

  public void logRowlevelImpl( String s ) {
    super.logRowlevel( s );
  }

  public void logSummary() {
    if ( child == null ) {
      logSummaryImpl();
    } else {
      child.logSummary();
    }
  }

  public void logSummaryImpl() {
    super.logSummary();
  }

  public void markStart() {
    if ( child == null ) {
      markStartImpl();
    } else {
      child.markStart();
    }
  }

  public void markStartImpl() {
    super.markStart();
  }

  public void markStop() {
    if ( child == null ) {
      markStopImpl();
    } else {
      child.markStop();
    }
  }

  public void markStopImpl() {
    super.markStop();
  }

  public void openRemoteInputStepSocketsOnce() throws KettleStepException {
    if ( child == null ) {
      openRemoteInputStepSocketsOnceImpl();
    } else {
      child.openRemoteInputStepSocketsOnce();
    }
  }

  public void openRemoteInputStepSocketsOnceImpl() throws KettleStepException {
    super.openRemoteInputStepSocketsOnce();
  }

  public void openRemoteOutputStepSocketsOnce() throws KettleStepException {
    if ( child == null ) {
      openRemoteOutputStepSocketsOnceImpl();
    } else {
      child.openRemoteOutputStepSocketsOnce();
    }
  }

  public void openRemoteOutputStepSocketsOnceImpl() throws KettleStepException {
    super.openRemoteOutputStepSocketsOnce();
  }

  public boolean outputIsDone() {
    if ( child == null ) {
      return outputIsDoneImpl();
    } else {
      return child.outputIsDone();
    }
  }

  public boolean outputIsDoneImpl() {
    return super.outputIsDone();
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( child == null ) {
      return false;
    } else {
      return child.processRow( smi, sdi );
    }
  }

  public void putError( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
    String fieldNames, String errorCodes ) throws KettleStepException {
    if ( child == null ) {
      putErrorImpl( rowMeta, row, nrErrors, errorDescriptions, fieldNames, errorCodes );
    } else {
      child.putError( rowMeta, row, nrErrors, errorDescriptions, fieldNames, errorCodes );
    }
  }

  public void putErrorImpl( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
    String fieldNames, String errorCodes ) throws KettleStepException {
    super.putError( rowMeta, row, nrErrors, errorDescriptions, fieldNames, errorCodes );
  }

  public void putRow( RowMetaInterface row, Object[] data ) throws KettleStepException {
    if ( child == null ) {
      putRowImpl( row, data );
    } else {
      child.putRow( row, data );
    }
  }

  public void putRowImpl( RowMetaInterface row, Object[] data ) throws KettleStepException {
    super.putRow( row, data );
  }

  public void putRowTo( RowMetaInterface rowMeta, Object[] row, RowSet rowSet ) throws KettleStepException {
    if ( child == null ) {
      putRowToImpl( rowMeta, row, rowSet );
    } else {
      child.putRowTo( rowMeta, row, rowSet );
    }
  }

  public void putRowToImpl( RowMetaInterface rowMeta, Object[] row, RowSet rowSet ) throws KettleStepException {
    super.putRowTo( rowMeta, row, rowSet );
  }

  public void removeRowListener( RowListener rowListener ) {
    if ( child == null ) {
      removeRowListenerImpl( rowListener );
    } else {
      child.removeRowListener( rowListener );
    }
  }

  public void removeRowListenerImpl( RowListener rowListener ) {
    super.removeRowListener( rowListener );
  }

  public int rowsetInputSize() {
    if ( child == null ) {
      return rowsetInputSizeImpl();
    } else {
      return child.rowsetInputSize();
    }
  }

  public int rowsetInputSizeImpl() {
    return super.rowsetInputSize();
  }

  public int rowsetOutputSize() {
    if ( child == null ) {
      return rowsetOutputSizeImpl();
    } else {
      return child.rowsetOutputSize();
    }
  }

  public int rowsetOutputSizeImpl() {
    return super.rowsetOutputSize();
  }

  public void safeModeChecking( RowMetaInterface row ) throws KettleRowException {
    if ( child == null ) {
      safeModeCheckingImpl( row );
    } else {
      child.safeModeChecking( row );
    }
  }

  public void safeModeCheckingImpl( RowMetaInterface row ) throws KettleRowException {
    super.safeModeChecking( row );
  }

  public void setErrors( long errors ) {
    if ( child == null ) {
      setErrorsImpl( errors );
    } else {
      child.setErrors( errors );
    }
  }

  public void setErrorsImpl( long errors ) {
    super.setErrors( errors );
  }

  public void setInputRowMeta( RowMetaInterface rowMeta ) {
    if ( child == null ) {
      setInputRowMetaImpl( rowMeta );
    } else {
      child.setInputRowMeta( rowMeta );
    }
  }

  public void setInputRowMetaImpl( RowMetaInterface rowMeta ) {
    super.setInputRowMeta( rowMeta );
  }

  public void setInputRowSets( List<RowSet> inputRowSets ) {
    if ( child == null ) {
      setInputRowSetsImpl( inputRowSets );
    } else {
      child.setInputRowSets( inputRowSets );
    }
  }

  public void setInputRowSetsImpl( List<RowSet> inputRowSets ) {
    super.setInputRowSets( inputRowSets );
  }

  public void setLinesInput( long newLinesInputValue ) {
    if ( child == null ) {
      setLinesInputImpl( newLinesInputValue );
    } else {
      child.setLinesInput( newLinesInputValue );
    }
  }

  public void setLinesInputImpl( long newLinesInputValue ) {
    super.setLinesInput( newLinesInputValue );
  }

  public void setLinesOutput( long newLinesOutputValue ) {
    if ( child == null ) {
      setLinesOutputImpl( newLinesOutputValue );
    } else {
      child.setLinesOutput( newLinesOutputValue );
    }
  }

  public void setLinesOutputImpl( long newLinesOutputValue ) {
    super.setLinesOutput( newLinesOutputValue );
  }

  public void setLinesRead( long newLinesReadValue ) {
    if ( child == null ) {
      setLinesReadImpl( newLinesReadValue );
    } else {
      child.setLinesRead( newLinesReadValue );
    }
  }

  public void setLinesReadImpl( long newLinesReadValue ) {
    super.setLinesRead( newLinesReadValue );
  }

  public void setLinesRejected( long linesRejected ) {
    if ( child == null ) {
      setLinesRejectedImpl( linesRejected );
    } else {
      child.setLinesRejected( linesRejected );
    }
  }

  public void setLinesRejectedImpl( long linesRejected ) {
    super.setLinesRejected( linesRejected );
  }

  public void setLinesSkipped( long newLinesSkippedValue ) {
    if ( child == null ) {
      setLinesSkippedImpl( newLinesSkippedValue );
    } else {
      child.setLinesSkipped( newLinesSkippedValue );
    }
  }

  public void setLinesSkippedImpl( long newLinesSkippedValue ) {
    super.setLinesSkipped( newLinesSkippedValue );
  }

  public void setLinesUpdated( long newLinesUpdatedValue ) {
    if ( child == null ) {
      setLinesUpdatedImpl( newLinesUpdatedValue );
    } else {
      child.setLinesUpdated( newLinesUpdatedValue );
    }
  }

  public void setLinesUpdatedImpl( long newLinesUpdatedValue ) {
    super.setLinesUpdated( newLinesUpdatedValue );
  }

  public void setLinesWritten( long newLinesWrittenValue ) {
    if ( child == null ) {
      setLinesWrittenImpl( newLinesWrittenValue );
    } else {
      child.setLinesWritten( newLinesWrittenValue );
    }
  }

  public void setLinesWrittenImpl( long newLinesWrittenValue ) {
    super.setLinesWritten( newLinesWrittenValue );
  }

  public void setOutputDone() {
    if ( child == null ) {
      setOutputDoneImpl();
    } else {
      child.setOutputDone();
    }
  }

  public void setOutputDoneImpl() {
    super.setOutputDone();
  }

  public void setOutputRowSets( List<RowSet> outputRowSets ) {
    if ( child == null ) {
      setOutputRowSetsImpl( outputRowSets );
    } else {
      child.setOutputRowSets( outputRowSets );
    }
  }

  public void setOutputRowSetsImpl( List<RowSet> outputRowSets ) {
    super.setOutputRowSets( outputRowSets );
  }

  public void setStepListeners( List<StepListener> stepListeners ) {
    if ( child == null ) {
      setStepListenersImpl( stepListeners );
    } else {
      child.setStepListeners( stepListeners );
    }
  }

  public void setStepListenersImpl( List<StepListener> stepListeners ) {
    super.setStepListeners( stepListeners );
  }

  public void setVariable( String variableName, String variableValue ) {
    if ( child == null ) {
      setVariableImpl( variableName, variableValue );
    } else {
      child.setVariable( variableName, variableValue );
    }
  }

  public void setVariableImpl( String variableName, String variableValue ) {
    super.setVariable( variableName, variableValue );
  }

  public void stopAll() {
    if ( child == null ) {
      stopAllImpl();
    } else {
      child.stopAll();
    }
  }

  public void stopAllImpl() {
    super.stopAll();
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    if ( child == null ) {
      stopRunningImpl( stepMetaInterface, stepDataInterface );
    } else {
      child.stopRunning( stepMetaInterface, stepDataInterface );
    }
  }

  public void stopRunningImpl( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    super.stopRunning( stepMetaInterface, stepDataInterface );
  }

  public String toString() {
    if ( child == null ) {
      return toStringImpl();
    } else {
      return child.toString();
    }
  }

  public String toStringImpl() {
    return super.toString();
  }

}
