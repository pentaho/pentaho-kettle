/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.script;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Interface to make the real ScriptValueMod and ScriptValueModDummy similar.
 * 
 * @author Sven Boden
 */
public interface ScriptInterface extends StepInterface
{
	boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException;

	void addRowListener(RowListener rowListener);

	void dispose(StepMetaInterface sii, StepDataInterface sdi);

	long getErrors();

	List<RowSet> getInputRowSets();

	long getLinesInput();

	long getLinesOutput();

	long getLinesRead();

	long getLinesUpdated();

	long getLinesWritten();
	
	long getLinesRejected();

	List<RowSet> getOutputRowSets();

	String getPartitionID();

	Object[] getRow() throws KettleException;

	List<RowListener> getRowListeners();

	String getStepID();

	String getStepname();

	boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface);

	boolean isAlive();

	boolean isPartitioned();

	boolean isStopped();

	void markStart();

	void markStop();

	void putRow(RowMetaInterface rowMeta, Object[] row) throws KettleException;

	void removeRowListener(RowListener rowListener);

	void run();

	void setErrors(long errors);

	void setOutputDone();

	void setPartitionID(String partitionID);

	void start();

	void stopAll();

	void stopRunning(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) throws KettleException;

	void cleanup();

	void pauseRunning();

	void resumeRunning();
};