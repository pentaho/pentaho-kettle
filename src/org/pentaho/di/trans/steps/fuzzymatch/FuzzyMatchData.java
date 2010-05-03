/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.HashSet;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;


/**
 * @author Samatar
 * @since 24-jan-2010
 */
public class FuzzyMatchData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface previousRowMeta;
	public RowMetaInterface outputRowMeta;
	
    /** used to store values in used to look up things */
	public HashSet<Object[]> look;

	public boolean readLookupValues;
	
	/**	index of main stream field		**/
	public int indexOfMainField;
	
	public int minimalDistance;
	
	public int maximalDistance;
	
	public double minimalSimilarity;
	
	public double maximalSimilarity;
	
	public String valueSeparator;
	
    public RowMetaInterface infoMeta;
    
	public StreamInterface	infoStream;
	
	public boolean addValueFieldName;
	public boolean addAdditionalFields;
	
	/**	index of return fields from lookup stream		**/
	public int[] indexOfCachedFields;
	public int nrCachedFields;
	public RowMetaInterface infoCache;
	
	public FuzzyMatchData()
	{
        super();
        this.look = new HashSet<Object[]>();
        this.indexOfMainField=-1;
        this.addValueFieldName=false;
        this.valueSeparator="";
        this.nrCachedFields=1;
        this.addAdditionalFields=false;
	}

}
