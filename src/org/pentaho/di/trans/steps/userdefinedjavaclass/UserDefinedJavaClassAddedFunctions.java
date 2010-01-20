 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 /**********************************************************************
 **                                                                   **
 ** This Script has been modified for higher performance              **
 ** and more functionality in December-2006,                          **
 ** by proconis GmbH / Germany                                        **
 **                                                                   ** 
 ** http://www.proconis.de                                            **
 ** info@proconis.de                                                  **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans.steps.userdefinedjavaclass;




public class UserDefinedJavaClassAddedFunctions {

	public static final long serialVersionUID = 1L;

	public static final int STRING_FUNCTION = 0;
	public static final int NUMERIC_FUNCTION = 1;
	public static final int DATE_FUNCTION = 2;
	public static final int LOGIC_FUNCTION = 3;
	public static final int SPECIAL_FUNCTION = 4;	
	public static final int FILE_FUNCTION = 5;
		
	// built from TransformClassBase.java with the following Vim commands:
	// :v/ *public/d
	// :%s/.\+\(\<[^(]\+\)(.*/\1/g
	// :%s/.*/"&",/
	public static  String[] javaETLFunctionList = {
		"addResultFile", "addRowListener", "addStepListener", 
		"checkFeedback", "cleanup", 
		"decrementLinesRead", "decrementLinesWritten", "dispose", 
		"findInputRowSet", "findInputRowSet", "findOutputRowSet", "findOutputRowSet", 
		"getClusterSize", "getCopy", "getErrorRowMeta", "getErrors", "getFields", 
		"getInfoSteps", "getInputRowMeta", "getInputRowSets", "getLinesInput", 
		"getLinesOutput", "getLinesRead", "getLinesRejected", "getLinesSkipped", 
		"getLinesUpdated", "getLinesWritten", "getOutputRowSets", "getPartitionID", 
		"getPartitionTargets", "getProcessed", "getRepartitioning", "getResultFiles", 
		"getRow", "getRowFrom", "getRowListeners", "getRuntime", "getSlaveNr", 
		"getSocketRepository", "getStatus", "getStatusDescription", "getStepDataInterface", 
		"getStepID", "getStepListeners", "getStepMeta", "getStepMetaInterface", 
		"getStepname", "getTrans", "getTransMeta", "getTypeId", "getUniqueStepCountAcrossSlaves", 
		"getUniqueStepNrAcrossSlaves", "getVariable", "getVariable", 
		"incrementLinesInput", "incrementLinesOutput", "incrementLinesRead", 
		"incrementLinesRejected", "incrementLinesSkipped", "incrementLinesUpdated", 
		"incrementLinesWritten", "init", "initBeforeStart", "isDistributed", "isInitialising", 
		"isPartitioned", "isSafeModeEnabled", "isStopped", "isUsingThreadPriorityManagment", 
		"logBasic", "logDebug", "logDetailed", "logError", "logError", "logMinimal", "logRowlevel", "logSummary", 
		"markStart", "markStop", 
		"openRemoteInputStepSocketsOnce", "openRemoteOutputStepSocketsOnce", "outputIsDone", 
		"processRow", "putError", "putRow", "putRowTo", 
		"removeRowListener", "rowsetInputSize", "rowsetOutputSize", 
		"safeModeChecking", "setErrors", "setInputRowMeta", "setInputRowSets", 
		"setLinesInput", "setLinesOutput", "setLinesRead", "setLinesRejected", 
		"setLinesSkipped", "setLinesUpdated", "setLinesWritten", "setOutputDone", 
		"setOutputRowSets", "setStepListeners", "setVariable", "stopAll", "stopRunning", 
		"toString", 
        };
}
