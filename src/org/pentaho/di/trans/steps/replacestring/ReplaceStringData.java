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
package org.pentaho.di.trans.steps.replacestring;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Samatar Hassan
 * @since 28 September 2008
 */
public class ReplaceStringData extends BaseStepData implements StepDataInterface {
	
	public int inStreamNrs[];

	public String outStreamNrs[];
	
	public int useRegEx[];
	
	public String replaceString[];
	
	public String replaceByString[];
	
	public int replaceFieldIndex[];
	
	public int wholeWord[];
	
	public int caseSensitive[];
	
	public String realChangeField;
	
	public String valueChange[];
	
	public String finalvalueChange;
	
	public RowMetaInterface outputRowMeta;
	
	public int inputFieldsNr;

    public Pattern[] patterns;
    
    public int numFields;
	
	
	/**
	 * Default constructor.
	 */
	public ReplaceStringData() {
		super();
		realChangeField=null;
		valueChange=null;
		finalvalueChange=null;
		inputFieldsNr=0;
	}
}