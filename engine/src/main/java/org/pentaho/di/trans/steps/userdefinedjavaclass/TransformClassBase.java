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

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta.FieldInfo;
import org.pentaho.di.www.SocketRepository;

public abstract class TransformClassBase {
  private static Class<?> PKG = UserDefinedJavaClassMeta.class; // for i18n purposes, needed by Translator2!!

  protected boolean first = true;
  protected boolean updateRowMeta = true;
  protected UserDefinedJavaClass parent;
  protected UserDefinedJavaClassMeta meta;
  protected UserDefinedJavaClassData data;

  public TransformClassBase( UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta,
    UserDefinedJavaClassData data ) throws KettleStepException {
    this.parent = parent;
    this.meta = meta;
    this.data = data;

    try {
      data.inputRowMeta = getTransMeta().getPrevStepFields( getStepMeta() ).clone();
      data.outputRowMeta = getTransMeta().getThisStepFields( getStepMeta(), null, data.inputRowMeta.clone() );

      data.parameterMap = new HashMap<String, String>();
      for ( UsageParameter par : meta.getUsageParameters() ) {
        if ( par.tag != null && par.value != null ) {
          data.parameterMap.put( par.tag, par.value );
        }
      }

      data.infoMap = new HashMap<String, String>();
      for ( StepDefinition stepDefinition : meta.getInfoStepDefinitions() ) {
        if ( stepDefinition.tag != null
          && stepDefinition.stepMeta != null && stepDefinition.stepMeta.getName() != null ) {
          data.infoMap.put( stepDefinition.tag, stepDefinition.stepMeta.getName() );
        }
      }

      data.targetMap = new HashMap<String, String>();
      for ( StepDefinition stepDefinition : meta.getTargetStepDefinitions() ) {
        if ( stepDefinition.tag != null
          && stepDefinition.stepMeta != null && stepDefinition.stepMeta.getName() != null ) {
          data.targetMap.put( stepDefinition.tag, stepDefinition.stepMeta.getName() );
        }
      }
    } catch ( KettleStepException e ) {
      e.printStackTrace();
      throw e;
    }
  }

  public void addResultFile( ResultFile resultFile ) {
    parent.addResultFileImpl( resultFile );
  }

  public void addRowListener( RowListener rowListener ) {
    parent.addRowListenerImpl( rowListener );
  }

  public void addStepListener( StepListener stepListener ) {
    parent.addStepListenerImpl( stepListener );
  }

  public boolean checkFeedback( long lines ) {
    return parent.checkFeedbackImpl( lines );
  }

  public void cleanup() {
    parent.cleanupImpl();
  }

  public long decrementLinesRead() {
    return parent.decrementLinesReadImpl();
  }

  public long decrementLinesWritten() {
    return parent.decrementLinesWrittenImpl();
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    parent.disposeImpl( smi, sdi );
  }

  public RowSet findInputRowSet( String sourceStep ) throws KettleStepException {
    return parent.findInputRowSetImpl( sourceStep );
  }

  public RowSet findInputRowSet( String from, int fromcopy, String to, int tocopy ) {
    return parent.findInputRowSetImpl( from, fromcopy, to, tocopy );
  }

  public RowSet findOutputRowSet( String targetStep ) throws KettleStepException {
    return parent.findOutputRowSetImpl( targetStep );
  }

  public RowSet findOutputRowSet( String from, int fromcopy, String to, int tocopy ) {
    return parent.findOutputRowSetImpl( from, fromcopy, to, tocopy );
  }

  public int getClusterSize() {
    return parent.getClusterSizeImpl();
  }

  public int getCopy() {
    return parent.getCopyImpl();
  }

  public RowMetaInterface getErrorRowMeta() {
    return parent.getErrorRowMetaImpl();
  }

  public long getErrors() {
    return parent.getErrorsImpl();
  }

  public RowMetaInterface getInputRowMeta() {
    return parent.getInputRowMetaImpl();
  }

  public List<RowSet> getInputRowSets() {
    return parent.getInputRowSetsImpl();
  }

  public long getLinesInput() {
    return parent.getLinesInputImpl();
  }

  public long getLinesOutput() {
    return parent.getLinesOutputImpl();
  }

  public long getLinesRead() {
    return parent.getLinesReadImpl();
  }

  public long getLinesRejected() {
    return parent.getLinesRejectedImpl();
  }

  public long getLinesSkipped() {
    return parent.getLinesSkippedImpl();
  }

  public long getLinesUpdated() {
    return parent.getLinesUpdatedImpl();
  }

  public long getLinesWritten() {
    return parent.getLinesWrittenImpl();
  }

  public List<RowSet> getOutputRowSets() {
    return parent.getOutputRowSetsImpl();
  }

  public String getPartitionID() {
    return parent.getPartitionIDImpl();
  }

  public Map<String, BlockingRowSet> getPartitionTargets() {
    return parent.getPartitionTargetsImpl();
  }

  public long getProcessed() {
    return parent.getProcessedImpl();
  }

  public int getRepartitioning() {
    return parent.getRepartitioningImpl();
  }

  public Map<String, ResultFile> getResultFiles() {
    return parent.getResultFilesImpl();
  }

  public Object[] getRow() throws KettleException {
    Object[] row = parent.getRowImpl();

    if ( updateRowMeta ) {
      // Update data.inputRowMeta and data.outputRowMeta
      RowMetaInterface inputRowMeta = parent.getInputRowMeta();
      data.inputRowMeta = inputRowMeta;
      data.outputRowMeta =
        inputRowMeta == null ? null : getTransMeta().getThisStepFields(
          getStepMeta(), null, inputRowMeta.clone() );
      updateRowMeta = false;
    }

    return row;
  }

  public Object[] getRowFrom( RowSet rowSet ) throws KettleStepException {
    return parent.getRowFromImpl( rowSet );
  }

  public List<RowListener> getRowListeners() {
    return parent.getRowListenersImpl();
  }

  public long getRuntime() {
    return parent.getRuntimeImpl();
  }

  public int getSlaveNr() {
    return parent.getSlaveNrImpl();
  }

  public SocketRepository getSocketRepository() {
    return parent.getSocketRepositoryImpl();
  }

  public StepExecutionStatus getStatus() {
    return parent.getStatusImpl();
  }

  public String getStatusDescription() {
    return parent.getStatusDescriptionImpl();
  }

  public StepDataInterface getStepDataInterface() {
    return parent.getStepDataInterfaceImpl();
  }

  public String getStepID() {
    return parent.getStepIDImpl();
  }

  public List<StepListener> getStepListeners() {
    return parent.getStepListenersImpl();
  }

  public StepMeta getStepMeta() {
    return parent.getStepMetaImpl();
  }

  public String getStepname() {
    return parent.getStepnameImpl();
  }

  public Trans getTrans() {
    return parent.getTransImpl();
  }

  public TransMeta getTransMeta() {
    return parent.getTransMetaImpl();
  }

  public String getTypeId() {
    return parent.getTypeIdImpl();
  }

  public int getUniqueStepCountAcrossSlaves() {
    return parent.getUniqueStepCountAcrossSlavesImpl();
  }

  public int getUniqueStepNrAcrossSlaves() {
    return parent.getUniqueStepNrAcrossSlavesImpl();
  }

  public String getVariable( String variableName ) {
    return parent.getVariableImpl( variableName );
  }

  public String getVariable( String variableName, String defaultValue ) {
    return parent.getVariableImpl( variableName, defaultValue );
  }

  public long incrementLinesInput() {
    return parent.incrementLinesInputImpl();
  }

  public long incrementLinesOutput() {
    return parent.incrementLinesOutputImpl();
  }

  public long incrementLinesRead() {
    return parent.incrementLinesReadImpl();
  }

  public long incrementLinesRejected() {
    return parent.incrementLinesRejectedImpl();
  }

  public long incrementLinesSkipped() {
    return parent.incrementLinesSkippedImpl();
  }

  public long incrementLinesUpdated() {
    return parent.incrementLinesUpdatedImpl();
  }

  public long incrementLinesWritten() {
    return parent.incrementLinesWrittenImpl();
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    return parent.initImpl( stepMetaInterface, stepDataInterface );
  }

  public void initBeforeStart() throws KettleStepException {
    parent.initBeforeStartImpl();
  }

  public boolean isDistributed() {
    return parent.isDistributedImpl();
  }

  public boolean isInitialising() {
    return parent.isInitialisingImpl();
  }

  public boolean isPartitioned() {
    return parent.isPartitionedImpl();
  }

  public boolean isSafeModeEnabled() {
    return parent.isSafeModeEnabledImpl();
  }

  public boolean isStopped() {
    return parent.isStoppedImpl();
  }

  public boolean isUsingThreadPriorityManagment() {
    return parent.isUsingThreadPriorityManagmentImpl();
  }

  public void logBasic( String s ) {
    parent.logBasicImpl( s );
  }

  public void logDebug( String s ) {
    parent.logDebugImpl( s );
  }

  public void logDetailed( String s ) {
    parent.logDetailedImpl( s );
  }

  public void logError( String s ) {
    parent.logErrorImpl( s );
  }

  public void logError( String s, Throwable e ) {
    parent.logErrorImpl( s, e );
  }

  public void logMinimal( String s ) {
    parent.logMinimalImpl( s );
  }

  public void logRowlevel( String s ) {
    parent.logRowlevelImpl( s );
  }

  public void logSummary() {
    parent.logSummaryImpl();
  }

  public void markStart() {
    parent.markStartImpl();
  }

  public void markStop() {
    parent.markStopImpl();
  }

  public void openRemoteInputStepSocketsOnce() throws KettleStepException {
    parent.openRemoteInputStepSocketsOnceImpl();
  }

  public void openRemoteOutputStepSocketsOnce() throws KettleStepException {
    parent.openRemoteOutputStepSocketsOnceImpl();
  }

  public boolean outputIsDone() {
    return parent.outputIsDoneImpl();
  }

  public abstract boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException;

  public void putError( RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions,
    String fieldNames, String errorCodes ) throws KettleStepException {
    parent.putErrorImpl( rowMeta, row, nrErrors, errorDescriptions, fieldNames, errorCodes );
  }

  public void putRow( RowMetaInterface row, Object[] data ) throws KettleStepException {
    parent.putRowImpl( row, data );
  }

  public void putRowTo( RowMetaInterface rowMeta, Object[] row, RowSet rowSet ) throws KettleStepException {
    parent.putRowToImpl( rowMeta, row, rowSet );
  }

  public void removeRowListener( RowListener rowListener ) {
    parent.removeRowListenerImpl( rowListener );
  }

  public int rowsetInputSize() {
    return parent.rowsetInputSizeImpl();
  }

  public int rowsetOutputSize() {
    return parent.rowsetOutputSizeImpl();
  }

  public void safeModeChecking( RowMetaInterface row ) throws KettleRowException {
    parent.safeModeCheckingImpl( row );
  }

  public void setErrors( long errors ) {
    parent.setErrorsImpl( errors );
  }

  public void setInputRowMeta( RowMetaInterface rowMeta ) {
    parent.setInputRowMetaImpl( rowMeta );
  }

  public void setInputRowSets( List<RowSet> inputRowSets ) {
    parent.setInputRowSetsImpl( inputRowSets );
  }

  public void setLinesInput( long newLinesInputValue ) {
    parent.setLinesInputImpl( newLinesInputValue );
  }

  public void setLinesOutput( long newLinesOutputValue ) {
    parent.setLinesOutputImpl( newLinesOutputValue );
  }

  public void setLinesRead( long newLinesReadValue ) {
    parent.setLinesReadImpl( newLinesReadValue );
  }

  public void setLinesRejected( long linesRejected ) {
    parent.setLinesRejectedImpl( linesRejected );
  }

  public void setLinesSkipped( long newLinesSkippedValue ) {
    parent.setLinesSkippedImpl( newLinesSkippedValue );
  }

  public void setLinesUpdated( long newLinesUpdatedValue ) {
    parent.setLinesUpdatedImpl( newLinesUpdatedValue );
  }

  public void setLinesWritten( long newLinesWrittenValue ) {
    parent.setLinesWrittenImpl( newLinesWrittenValue );
  }

  public void setOutputDone() {
    parent.setOutputDoneImpl();
  }

  public void setOutputRowSets( List<RowSet> outputRowSets ) {
    parent.setOutputRowSetsImpl( outputRowSets );
  }

  public void setStepListeners( List<StepListener> stepListeners ) {
    parent.setStepListenersImpl( stepListeners );
  }

  public void setVariable( String variableName, String variableValue ) {
    parent.setVariableImpl( variableName, variableValue );
  }

  public void stopAll() {
    parent.stopAllImpl();
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
    parent.stopRunningImpl( stepMetaInterface, stepDataInterface );
  }

  public String toString() {
    return parent.toStringImpl();
  }

  public static String[] getInfoSteps() {
    return null;
  }

  @SuppressWarnings( "unchecked" )
  public static void getFields( boolean clearResultFields, RowMetaInterface row, String originStepname,
    RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, List<?> fields ) throws KettleStepException {
    if ( clearResultFields ) {
      row.clear();
    }
    for ( FieldInfo fi : (List<FieldInfo>) fields ) {
      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( fi.name, fi.type );
        v.setLength( fi.length );
        v.setPrecision( fi.precision );
        v.setOrigin( originStepname );
        row.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
  }

  public static StepIOMetaInterface getStepIOMeta( UserDefinedJavaClassMeta meta ) {
    StepIOMetaInterface ioMeta = new StepIOMeta( true, true, true, false, true, true );

    for ( StepDefinition stepDefinition : meta.getInfoStepDefinitions() ) {
      ioMeta.addStream( new Stream(
        StreamType.INFO, stepDefinition.stepMeta, stepDefinition.description, StreamIcon.INFO, null ) );
    }
    for ( StepDefinition stepDefinition : meta.getTargetStepDefinitions() ) {
      ioMeta.addStream( new Stream(
        StreamType.TARGET, stepDefinition.stepMeta, stepDefinition.description, StreamIcon.TARGET, null ) );
    }

    return ioMeta;
  }

  public String getParameter( String tag ) {
    if ( tag == null ) {
      return null;
    }
    return parent.environmentSubstitute( data.parameterMap.get( tag ) );
  }

  public RowSet findInfoRowSet( String tag ) throws KettleException {
    if ( tag == null ) {
      return null;
    }
    String stepname = data.infoMap.get( tag );
    if ( Utils.isEmpty( stepname ) ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransformClassBase.Exception.UnableToFindInfoStepNameForTag", tag ) );
    }
    RowSet rowSet = findInputRowSet( stepname );
    if ( rowSet == null ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransformClassBase.Exception.UnableToFindInfoRowSetForStep", stepname ) );
    }
    return rowSet;
  }

  public RowSet findTargetRowSet( String tag ) throws KettleException {
    if ( tag == null ) {
      return null;
    }
    String stepname = data.targetMap.get( tag );
    if ( Utils.isEmpty( stepname ) ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransformClassBase.Exception.UnableToFindTargetStepNameForTag", tag ) );
    }
    RowSet rowSet = findOutputRowSet( stepname );
    if ( rowSet == null ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransformClassBase.Exception.UnableToFindTargetRowSetForStep", stepname ) );
    }
    return rowSet;
  }

  private final Map<String, FieldHelper> inFieldHelpers = new HashMap<String, FieldHelper>();
  private final Map<String, FieldHelper> infoFieldHelpers = new HashMap<String, FieldHelper>();
  private final Map<String, FieldHelper> outFieldHelpers = new HashMap<String, FieldHelper>();

  public enum Fields {
    In, Out, Info;
  }

  public FieldHelper get( Fields type, String name ) throws KettleStepException {
    FieldHelper fh;
    switch ( type ) {
      case In:
        fh = inFieldHelpers.get( name );
        if ( fh == null ) {
          try {
            fh = new FieldHelper( data.inputRowMeta, name );
          } catch ( IllegalArgumentException e ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name ) );
          }
          inFieldHelpers.put( name, fh );
        }
        break;
      case Out:
        fh = outFieldHelpers.get( name );
        if ( fh == null ) {
          try {
            fh = new FieldHelper( data.outputRowMeta, name );
          } catch ( IllegalArgumentException e ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name ) );
          }
          outFieldHelpers.put( name, fh );
        }
        break;
      case Info:
        fh = infoFieldHelpers.get( name );
        if ( fh == null ) {
          RowMetaInterface rmi = getTransMeta().getPrevInfoFields( getStepname() );
          try {
            fh = new FieldHelper( rmi, name );
          } catch ( IllegalArgumentException e ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name ) );
          }
          infoFieldHelpers.put( name, fh );
        }
        break;
      default:
        throw new KettleStepException( BaseMessages.getString(
          PKG, "TransformClassBase.Exception.InvalidFieldsType", type.name(), name ) );
    }
    return fh;
  }

  public Object[] createOutputRow( Object[] inputRow, int outputRowSize ) {
    if ( meta.isClearingResultFields() ) {
      return RowDataUtil.allocateRowData( outputRowSize );
    } else {
      return RowDataUtil.createResizedCopy( inputRow, outputRowSize );
    }
  }
}
