/***** BEGIN LICENSE BLOCK *****
Version: MPL 1.1/GPL 2.0/LGPL 2.1

The contents of this project are subject to the Mozilla Public License Version
1.1 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Mozilla Corporation Metrics ETL for AMO

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2008
the Initial Developer. All Rights Reserved.

Contributor(s):

Alternatively, the contents of this file may be used under the terms of
either the GNU General Public License Version 2 or later (the "GPL"), or
the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
in which case the provisions of the GPL or the LGPL are applicable instead
of those above. If you wish to allow use of your version of this file only
under the terms of either the GPL or the LGPL, and not to allow others to
use your version of this file under the terms of the MPL, indicate your
decision by deleting the provisions above and replace them with the notice
and other provisions required by the LGPL or the GPL. If you do not delete
the provisions above, a recipient may use your version of this file under
the terms of any one of the MPL, the GPL or the LGPL.

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.www.SocketRepository;

import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta.FieldInfo;

public abstract class TransformClassBase
{
    protected boolean first = true;
    protected UserDefinedJavaClass parent;
    protected UserDefinedJavaClassMeta meta;
    protected UserDefinedJavaClassData data;

    public TransformClassBase(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data) throws KettleStepException
    {
        this.parent = parent;
        this.meta = meta;
        this.data = data;
        
        try
        {
            data.inputRowMeta = getTransMeta().getPrevStepFields(getStepMeta()).clone();
            data.outputRowMeta = getTransMeta().getThisStepFields(getStepMeta(), null, data.inputRowMeta.clone());
        }
        catch (KettleStepException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    public void addResultFile(ResultFile resultFile)
    {
        parent.addResultFileImpl(resultFile);
    }

    public void addRowListener(RowListener rowListener)
    {
        parent.addRowListenerImpl(rowListener);
    }

    public void addStepListener(StepListener stepListener)
    {
        parent.addStepListenerImpl(stepListener);
    }

    public boolean checkFeedback(long lines)
    {
        return parent.checkFeedbackImpl(lines);
    }

    public void cleanup()
    {
        parent.cleanupImpl();
    }

    public long decrementLinesRead()
    {
        return parent.decrementLinesReadImpl();
    }

    public long decrementLinesWritten()
    {
        return parent.decrementLinesWrittenImpl();
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        parent.disposeImpl(smi, sdi);
    }

    public RowSet findInputRowSet(String sourceStep) throws KettleStepException
    {
        return parent.findInputRowSetImpl(sourceStep);
    }

    public RowSet findInputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        return parent.findInputRowSetImpl(from, fromcopy, to, tocopy);
    }

    public RowSet findOutputRowSet(String targetStep) throws KettleStepException
    {
        return parent.findOutputRowSetImpl(targetStep);
    }

    public RowSet findOutputRowSet(String from, int fromcopy, String to, int tocopy)
    {
        return parent.findOutputRowSetImpl(from, fromcopy, to, tocopy);
    }

    public int getClusterSize()
    {
        return parent.getClusterSizeImpl();
    }

    public int getCopy()
    {
        return parent.getCopyImpl();
    }

    public RowMetaInterface getErrorRowMeta()
    {
        return parent.getErrorRowMetaImpl();
    }

    public long getErrors()
    {
        return parent.getErrorsImpl();
    }

    public RowMetaInterface getInputRowMeta()
    {
        return parent.getInputRowMetaImpl();
    }

    public List<RowSet> getInputRowSets()
    {
        return parent.getInputRowSetsImpl();
    }

    public long getLinesInput()
    {
        return parent.getLinesInputImpl();
    }

    public long getLinesOutput()
    {
        return parent.getLinesOutputImpl();
    }

    public long getLinesRead()
    {
        return parent.getLinesReadImpl();
    }

    public long getLinesRejected()
    {
        return parent.getLinesRejectedImpl();
    }

    public long getLinesSkipped()
    {
        return parent.getLinesSkippedImpl();
    }

    public long getLinesUpdated()
    {
        return parent.getLinesUpdatedImpl();
    }

    public long getLinesWritten()
    {
        return parent.getLinesWrittenImpl();
    }

    public List<RowSet> getOutputRowSets()
    {
        return parent.getOutputRowSetsImpl();
    }

    public String getPartitionID()
    {
        return parent.getPartitionIDImpl();
    }

    public Map<String, RowSet> getPartitionTargets()
    {
        return parent.getPartitionTargetsImpl();
    }

    public long getProcessed()
    {
        return parent.getProcessedImpl();
    }

    public int getRepartitioning()
    {
        return parent.getRepartitioningImpl();
    }

    public Map<String, ResultFile> getResultFiles()
    {
        return parent.getResultFilesImpl();
    }

    public Object[] getRow() throws KettleException
    {
        return parent.getRowImpl();
    }

    public Object[] getRowFrom(RowSet rowSet) throws KettleStepException
    {
        return parent.getRowFromImpl(rowSet);
    }

    public List<RowListener> getRowListeners()
    {
        return parent.getRowListenersImpl();
    }

    public long getRuntime()
    {
        return parent.getRuntimeImpl();
    }

    public int getSlaveNr()
    {
        return parent.getSlaveNrImpl();
    }

    public SocketRepository getSocketRepository()
    {
        return parent.getSocketRepositoryImpl();
    }

    public int getStatus()
    {
        return parent.getStatusImpl();
    }

    public String getStatusDescription()
    {
        return parent.getStatusDescriptionImpl();
    }

    public StepDataInterface getStepDataInterface()
    {
        return parent.getStepDataInterfaceImpl();
    }

    public String getStepID()
    {
        return parent.getStepIDImpl();
    }

    public List<StepListener> getStepListeners()
    {
        return parent.getStepListenersImpl();
    }

    public StepMeta getStepMeta()
    {
        return parent.getStepMetaImpl();
    }

    public StepMetaInterface getStepMetaInterface()
    {
        return parent.getStepMetaInterfaceImpl();
    }

    public String getStepname()
    {
        return parent.getStepnameImpl();
    }

    public Trans getTrans()
    {
        return parent.getTransImpl();
    }

    public TransMeta getTransMeta()
    {
        return parent.getTransMetaImpl();
    }

    public String getTypeId()
    {
        return parent.getTypeIdImpl();
    }

    public int getUniqueStepCountAcrossSlaves()
    {
        return parent.getUniqueStepCountAcrossSlavesImpl();
    }

    public int getUniqueStepNrAcrossSlaves()
    {
        return parent.getUniqueStepNrAcrossSlavesImpl();
    }

    public String getVariable(String variableName)
    {
        return parent.getVariableImpl(variableName);
    }

    public String getVariable(String variableName, String defaultValue)
    {
        return parent.getVariableImpl(variableName, defaultValue);
    }

    public long incrementLinesInput()
    {
        return parent.incrementLinesInputImpl();
    }

    public long incrementLinesOutput()
    {
        return parent.incrementLinesOutputImpl();
    }

    public long incrementLinesRead()
    {
        return parent.incrementLinesReadImpl();
    }

    public long incrementLinesRejected()
    {
        return parent.incrementLinesRejectedImpl();
    }

    public long incrementLinesSkipped()
    {
        return parent.incrementLinesSkippedImpl();
    }

    public long incrementLinesUpdated()
    {
        return parent.incrementLinesUpdatedImpl();
    }

    public long incrementLinesWritten()
    {
        return parent.incrementLinesWrittenImpl();
    }

    public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface)
    {
        return parent.initImpl(stepMetaInterface, stepDataInterface);
    }

    public void initBeforeStart() throws KettleStepException
    {
        parent.initBeforeStartImpl();
    }

    public boolean isDistributed()
    {
        return parent.isDistributedImpl();
    }

    public boolean isInitialising()
    {
        return parent.isInitialisingImpl();
    }

    public boolean isPartitioned()
    {
        return parent.isPartitionedImpl();
    }

    public boolean isSafeModeEnabled()
    {
        return parent.isSafeModeEnabledImpl();
    }

    public boolean isStopped()
    {
        return parent.isStoppedImpl();
    }

    public boolean isUsingThreadPriorityManagment()
    {
        return parent.isUsingThreadPriorityManagmentImpl();
    }

    public void logBasic(String s)
    {
        parent.logBasicImpl(s);
    }

    public void logDebug(String s)
    {
        parent.logDebugImpl(s);
    }

    public void logDetailed(String s)
    {
        parent.logDetailedImpl(s);
    }

    public void logError(String s)
    {
        parent.logErrorImpl(s);
    }

    public void logError(String s, Throwable e)
    {
        parent.logErrorImpl(s, e);
    }

    public void logMinimal(String s)
    {
        parent.logMinimalImpl(s);
    }

    public void logRowlevel(String s)
    {
        parent.logRowlevelImpl(s);
    }

    public void logSummary()
    {
        parent.logSummaryImpl();
    }

    public void markStart()
    {
        parent.markStartImpl();
    }

    public void markStop()
    {
        parent.markStopImpl();
    }

    public void openRemoteInputStepSocketsOnce() throws KettleStepException
    {
        parent.openRemoteInputStepSocketsOnceImpl();
    }

    public void openRemoteOutputStepSocketsOnce() throws KettleStepException
    {
        parent.openRemoteOutputStepSocketsOnceImpl();
    }

    public boolean outputIsDone()
    {
        return parent.outputIsDoneImpl();
    }

    public abstract boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException;

    public void putError(RowMetaInterface rowMeta, Object[] row, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes)
            throws KettleStepException
    {
        parent.putErrorImpl(rowMeta, row, nrErrors, errorDescriptions, fieldNames, errorCodes);
    }

    public void putRow(RowMetaInterface row, Object[] data) throws KettleStepException
    {
        parent.putRowImpl(row, data);
    }

    public void putRowTo(RowMetaInterface rowMeta, Object[] row, RowSet rowSet) throws KettleStepException
    {
        parent.putRowToImpl(rowMeta, row, rowSet);
    }

    public void removeRowListener(RowListener rowListener)
    {
        parent.removeRowListenerImpl(rowListener);
    }

    public int rowsetInputSize()
    {
        return parent.rowsetInputSizeImpl();
    }

    public int rowsetOutputSize()
    {
        return parent.rowsetOutputSizeImpl();
    }

    public void safeModeChecking(RowMetaInterface row) throws KettleRowException
    {
        parent.safeModeCheckingImpl(row);
    }

    public void setErrors(long errors)
    {
        parent.setErrorsImpl(errors);
    }

    public void setInputRowMeta(RowMetaInterface rowMeta)
    {
        parent.setInputRowMetaImpl(rowMeta);
    }

    public void setInputRowSets(ArrayList<RowSet> inputRowSets)
    {
        parent.setInputRowSetsImpl(inputRowSets);
    }

    public void setLinesInput(long newLinesInputValue)
    {
        parent.setLinesInputImpl(newLinesInputValue);
    }

    public void setLinesOutput(long newLinesOutputValue)
    {
        parent.setLinesOutputImpl(newLinesOutputValue);
    }

    public void setLinesRead(long newLinesReadValue)
    {
        parent.setLinesReadImpl(newLinesReadValue);
    }

    public void setLinesRejected(long linesRejected)
    {
        parent.setLinesRejectedImpl(linesRejected);
    }

    public void setLinesSkipped(long newLinesSkippedValue)
    {
        parent.setLinesSkippedImpl(newLinesSkippedValue);
    }

    public void setLinesUpdated(long newLinesUpdatedValue)
    {
        parent.setLinesUpdatedImpl(newLinesUpdatedValue);
    }

    public void setLinesWritten(long newLinesWrittenValue)
    {
        parent.setLinesWrittenImpl(newLinesWrittenValue);
    }

    public void setOutputDone()
    {
        parent.setOutputDoneImpl();
    }

    public void setOutputRowSets(ArrayList<RowSet> outputRowSets)
    {
        parent.setOutputRowSetsImpl(outputRowSets);
    }

    public void setStepListeners(List<StepListener> stepListeners)
    {
        parent.setStepListenersImpl(stepListeners);
    }

    public void setVariable(String variableName, String variableValue)
    {
        parent.setVariableImpl(variableName, variableValue);
    }

    public void stopAll()
    {
        parent.stopAllImpl();
    }

    public void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException
    {
        parent.stopRunningImpl(stepMetaInterface, stepDataInterface);
    }

    public String toString()
    {
        return parent.toStringImpl();
    }

    public static String[] getInfoSteps()
    {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static void getFields(RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, List fields)
    throws KettleStepException
    {
        for (FieldInfo fi : (List<FieldInfo>)fields)
        {
            ValueMetaInterface v;
            v = new ValueMeta(fi.name, fi.type);
            v.setLength(fi.length);
            v.setPrecision(fi.precision);
            v.setOrigin(originStepname);
            row.addValueMeta(v);
        }
    }
}