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

/* Modifications to original RegexEval step made by Daniel Einspanjer */

package org.pentaho.di.trans.steps.regexeval;

import java.util.regex.Pattern;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Runtime data for the RegexEval step.
 * 
 * @author Samatar Hassan
 * @author Daniel Einspanjer
 * @since 27-03-2008
 */
public class RegexEvalData extends BaseStepData implements StepDataInterface
{	
	
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface conversionRowMeta;
	public int              indexOfFieldToEvaluate;

	public Pattern          pattern;
	
	public int nrExtraFields;
	public int nrIncomingFields;
	public boolean addResultField;
	
	public RegexEvalData()
	{
		super();

		indexOfFieldToEvaluate = -1;
		addResultField=true;
	}
}
