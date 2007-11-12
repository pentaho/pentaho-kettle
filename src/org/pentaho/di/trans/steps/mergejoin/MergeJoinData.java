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
 

package org.pentaho.di.trans.steps.mergejoin;

import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Biswapesh
 * @since 24-nov-2005
 *
 */

public class MergeJoinData extends BaseStepData implements StepDataInterface
{
	public Object[] one, two;
	public RowMetaInterface oneMeta, twoMeta;
	public RowMetaInterface outputRowMeta; //just for speed: oneMeta+twoMeta
	public Object[] one_dummy, two_dummy;
    public List<Object[]> ones, twos;
    public Object[] one_next, two_next;
    public boolean one_optional, two_optional;
    public int[] keyNrs1;
    public int[] keyNrs2;
	
    public RowSet oneRowSet;
	public RowSet twoRowSet;
    
	/**
	 * Default initializer
	 */
	public MergeJoinData()
	{
		super();
		ones = null;
		twos = null;
		one_next = null;
		two_next = null;
		one_dummy = null;
		two_dummy = null;
		one_optional = false;
		two_optional = false;
		keyNrs1 = null;
		keyNrs2 = null;
	}

}
