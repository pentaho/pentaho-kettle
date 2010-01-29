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

import java.util.List;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Get random value.
 * 
 * @author Matt, Samatar
 * @since 8-8-2008
 */
public class RandomValue extends BaseStep implements StepInterface {
	private RandomValueMeta meta;

	private RandomValueData data;

	public RandomValue(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		setName(stepMeta.getName());
	}

	private Object[] getRandomValue(RowMetaInterface inputRowMeta,
			Object[] inputRowData) {
		Object[] row = new Object[data.outputRowMeta.size()];
		for (int i = 0; i < inputRowMeta.size(); i++) {
			row[i] = inputRowData[i]; // no data is changed, clone is not
									  // needed here.
		}

		for (int i = 0, index = inputRowMeta.size(); i < meta.getFieldName().length; i++, index++) {
			switch (meta.getFieldType()[i]) {
			case RandomValueMeta.TYPE_RANDOM_NUMBER:
				row[index] = data.randomgen.nextDouble();
				break;
			case RandomValueMeta.TYPE_RANDOM_INTEGER:
				row[index] = new Long(data.randomgen.nextInt()); // nextInt() already returns all 2^32 numbers.
				break;
			case RandomValueMeta.TYPE_RANDOM_STRING:
				row[index] = Long.toString(Math.abs(data.randomgen.nextLong()), 32);
				break;
			case RandomValueMeta.TYPE_RANDOM_UUID:
				row[index] = UUIDUtil.getUUIDAsString();
				break;
			case RandomValueMeta.TYPE_RANDOM_UUID4:
				row[index] = data.u4.getUUID4AsString();
				break;
			default:
				break;
			}
		}

		return row;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		Object[] row;
		if (data.readsRows) {
			row = getRow();
			if (row == null) {
				setOutputDone();
				return false;
			}

			if (first) {
				first = false;
				data.outputRowMeta = getInputRowMeta().clone();
				meta.getFields(data.outputRowMeta, getStepname(), null, null,this);
			}

		} else {
			row = new Object[] {}; // empty row
			incrementLinesRead();

			if (first) {
				first = false;
				data.outputRowMeta = new RowMeta();
				meta.getFields(data.outputRowMeta, getStepname(), null, null,this);
			}
		}

		RowMetaInterface imeta = getInputRowMeta();
		if (imeta == null) {
			imeta = new RowMeta();
			this.setInputRowMeta(imeta);
		}

		row = getRandomValue(imeta, row);

		if (log.isRowLevel())
			logRowlevel(Messages.getString("RandomValue.Log.ValueReturned",data.outputRowMeta.getString(row)));

		putRow(data.outputRowMeta, row);

		if (!data.readsRows) // Just one row and then stop!
		{
			setOutputDone();
			return false;
		}

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RandomValueMeta) smi;
		data = (RandomValueData) sdi;

		if (super.init(smi, sdi)) {
			// Add init code here.
			data.readsRows = false;
	        List<StepMeta> previous = getTransMeta().findPreviousSteps(getStepMeta());
			if (previous != null && previous.size() > 0) {
				data.readsRows = true;
			}

			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}