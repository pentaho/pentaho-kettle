/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.switchcase;

import org.pentaho.di.trans.step.StepMeta;

/**
 * Utility class that contains the case value, the target step name and the resolved target step
 * 
 * @author matt
 *
 */
public class SwitchCaseTarget {
	/** The value to switch over */
	public String caseValue;
	
	/** The case target step name (only used during serialization) */
	public String caseTargetStepname;
	
	/** The case target step */
	public StepMeta caseTargetStep;

	public SwitchCaseTarget() {
	}
}