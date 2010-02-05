/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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

package org.pentaho.di.trans.steps.randomvalue;

import java.util.Random;
import javax.crypto.KeyGenerator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.UUID4Util;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 24-jan-2005
 */
public class RandomValueData extends BaseStepData implements StepDataInterface {
	public boolean readsRows;

	public RowMetaInterface outputRowMeta;
	
	public UUID4Util u4;
	/* Generating a random Message Authentication Code (MAC MD5) */
	public KeyGenerator keyGenHmacMD5;
	/* Generating a random Message Authentication Code (MAC SHA1) */
	public KeyGenerator keyGenHmacSHA1;

    public final Random randomgen = new Random();

    /**
	 * 
	 */
	public RandomValueData() {
		super();
	}
}