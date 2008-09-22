/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 
package org.pentaho.di.trans.steps.creditcardvalidator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class CreditCardValidatorData extends BaseStepData implements StepDataInterface
{
	public int indexOfField;
	public String realResultFieldname;
	public String realCardTypeFieldname;
	public String realNotValidMsgFieldname;
	public RowMetaInterface outputRowMeta;
	public int NrPrevFields;
	public RowMetaInterface previousRowMeta;
    
	/**
	 * 
	 */
	public CreditCardValidatorData()
	{
		super();
		indexOfField=-1;
		realResultFieldname=null;
		realCardTypeFieldname=null;
		realNotValidMsgFieldname=null;
	}

}
