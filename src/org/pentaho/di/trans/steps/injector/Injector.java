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
 
package org.pentaho.di.trans.steps.injector;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Executor class to allow a java program to inject rows of data into a transformation.
 * This step can be used as a starting point in such a "headless" transformation.
 * 
 * @since 22-jun-2006
 */
public class Injector extends BaseStep implements StepInterface
{
	private static Class<?> PKG = InjectorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public Injector(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        // Get a row from the previous step OR from an extra RowSet
        //
        Object[] row = getRow();
        if (row==null) // Nothing more to be had from any input rowset
		{
			setOutputDone();
			return false;
		}
		
		putRow(getInputRowMeta(), row);  // copy row to possible alternate rowset(s).

		if (checkFeedback(getLinesRead())) logBasic(BaseMessages.getString(PKG, "Injector.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}	
}