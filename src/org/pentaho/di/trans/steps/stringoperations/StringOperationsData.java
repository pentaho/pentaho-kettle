/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.stringoperations;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * Apply certain operations too string.
 * 
 * @author Samatar Hassan
 * @since 02 April 2009
 */
public class StringOperationsData extends BaseStepData implements StepDataInterface {
	
	public int inStreamNrs[]; // string infields

	public String outStreamNrs[];
	
	/** Runtime trim operators */
	public int trimOperators[];
	
	/** Runtime trim operators */
	public int lowerUpperOperators[];

	public int padType[];
	
	public String padChar[];
	
	public int padLen[];
	
	public int initCap[];
	
	public int maskHTML[];
	
	public int digits[];
	
	public int removeSpecialCharacters[];
	
	public RowMetaInterface outputRowMeta;
	
	public int inputFieldsNr;
	
	public int nrFieldsInStream;
	
	/**
	 * Default constructor.
	 */
	public StringOperationsData() {
		super();
		this.inputFieldsNr=0;
		this.nrFieldsInStream=0;
	}
}