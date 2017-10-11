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

package org.pentaho.di.trans.steps.script;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;

/**
 * Dummy class used for test().
 */
public class ScriptDummy implements StepInterface {
  private RowMetaInterface inputRowMeta;
  private RowMetaInterface outputRowMeta;

  public ScriptDummy( RowMetaInterface inputRowMeta, RowMetaInterface outputRowMeta ) {
    this.inputRowMeta = inputRowMeta;
    this.outputRowMeta = outputRowMeta;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    return false;
  }

  public void addRowListener( RowListener rowListener ) {
  }

  public void dispose( StepMetaInterface sii, StepDataInterface sdi ) {
  }

  public long getErrors() {
    return 0;
  }

  public List<RowSet> getInputRowSets() {
    return null;
  }

  public long getLinesInput() {
    return 0;
  }

  public long getLinesOutput() {
    return 0;
  }

  public long getLinesRead() {
    return 0;
  }

  public long getLinesUpdated() {
    return 0;
  }

  public long getLinesWritten() {
    return 0;
  }

  public long getLinesRejected() {
    return 0;
  }

  public List<RowSet> getOutputRowSets() {
    return null;
  }

  public String getPartitionID() {
    return null;
  }

  public Object[] getRow() throws KettleException {
    return null;
  }

  public List<RowListener> getRowListeners() {
    return null;
  }

  public String getStepID() {
    return null;
  }

  public String getStepname() {
    return null;
  }

  public boolean init( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) {
    return false;
  }

  public boolean isAlive() {
    return false;
  }

  public boolean isPartitioned() {
    return false;
  }

  public boolean isStopped() {
    return false;
  }

  public void markStart() {
  }

  public void markStop() {
  }

  public void putRow( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
  }

  public void removeRowListener( RowListener rowListener ) {
  }

  public void run() {
  }

  public void setErrors( long errors ) {
  }

  public void setOutputDone() {
  }

  public void setPartitionID( String partitionID ) {
  }

  public void start() {
  }

  public void stopAll() {
  }

  public void stopRunning( StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface ) throws KettleException {
  }

  public void cleanup() {
  }

  public void pauseRunning() {
  }

  public void resumeRunning() {
  }

  public void copyVariablesFrom( VariableSpace space ) {
  }

  public String environmentSubstitute( String aString ) {
    return null;
  }

  public String[] environmentSubstitute( String[] string ) {
    return null;
  }

  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    return null;
  }

  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    return false;
  }

  public VariableSpace getParentVariableSpace() {
    return null;
  }

  public void setParentVariableSpace( VariableSpace parent ) {
  }

  public String getVariable( String variableName, String defaultValue ) {
    return defaultValue;
  }

  public String getVariable( String variableName ) {
    return null;
  }

  public void initializeVariablesFrom( VariableSpace parent ) {
  }

  public void injectVariables( Map<String, String> prop ) {
  }

  public String[] listVariables() {
    return null;
  }

  public void setVariable( String variableName, String variableValue ) {
  }

  public void shareVariablesWith( VariableSpace space ) {
  }

  public RowMetaInterface getInputRowMeta() {
    return inputRowMeta;
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  public void initBeforeStart() throws KettleStepException {
  }

  public void setLinesRejected( long linesRejected ) {
  }

  public int getCopy() {
    return 0;
  }

  public void addStepListener( StepListener stepListener ) {
  }

  public boolean isMapping() {
    return false;
  }

  public StepMeta getStepMeta() {
    return null;
  }

  public Trans getTrans() {
    return null;
  }

  public LogChannelInterface getLogChannel() {
    return null;
  }

  public boolean isUsingThreadPriorityManagment() {
    return false;
  }

  public void setUsingThreadPriorityManagment( boolean usingThreadPriorityManagment ) {
  }

  public boolean isRunning() {
    return false;
  }

  public void setRunning( boolean running ) {
    // TODO Auto-generated method stub

  }

  public void setStopped( boolean stopped ) {
    // TODO Auto-generated method stub

  }

  public int rowsetInputSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int rowsetOutputSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  public long getProcessed() {
    // TODO Auto-generated method stub
    return 0;
  }

  public Map<String, ResultFile> getResultFiles() {
    // TODO Auto-generated method stub
    return null;
  }

  public long getRuntime() {
    // TODO Auto-generated method stub
    return 0;
  }

  public StepExecutionStatus getStatus() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isPaused() {
    // TODO Auto-generated method stub
    return false;
  }

  public void identifyErrorOutput() {
    // TODO Auto-generated method stub

  }

  public void setPartitioned( boolean partitioned ) {
    // TODO Auto-generated method stub

  }

  public void setRepartitioning( int partitioningMethod ) {
    // TODO Auto-generated method stub

  }

  public boolean canProcessOneRow() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isWaitingForData() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setWaitingForData( boolean waitingForData ) {
    // TODO Auto-generated method stub
  }

  public boolean isIdle() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isPassingData() {
    // TODO Auto-generated method stub
    return false;
  }

  public void setPassingData( boolean passingData ) {
    // TODO Auto-generated method stub
  }

  public void batchComplete() throws KettleException {
    // TODO Auto-generated method stub
  }

  @Override
  public void setMetaStore( IMetaStore metaStore ) {
    // TODO Auto-generated method stub

  }

  @Override
  public IMetaStore getMetaStore() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRepository( Repository repository ) {
    // TODO Auto-generated method stub

  }

  @Override
  public Repository getRepository() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getCurrentInputRowSetNr() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setCurrentOutputRowSetNr( int index ) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getCurrentOutputRowSetNr() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setCurrentInputRowSetNr( int index ) {
    // TODO Auto-generated method stub

  }
}
