 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.randomccnumber;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Generate random credit card number.
 * 
 * @author Samatar
 * @since 01-4-2010
 */
public class RandomCCNumberGenerator extends BaseStep implements StepInterface {
	private static Class<?> PKG = RandomCCNumberGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private RandomCCNumberGeneratorMeta meta;

	private RandomCCNumberGeneratorData data;

	public RandomCCNumberGenerator(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		
		if (first) {
			first = false;
			data.outputRowMeta = new RowMeta();
			meta.getFields(data.outputRowMeta, getStepname(), null, null,this);
		}
		for(int i=0; i< data.cardTypes.length && !isStopped(); i++) {
			
			// Return card numbers
			String[] cardNumber = RandomCreditCardNumberGenerator.GenerateCreditCardNumbers(data.cardTypes[i], data.cardLen[i],  data.cardSize[i]);
			
			for(int j=0; j<cardNumber.length && !isStopped(); j++) {
				// Create a new row
				Object[] row = buildEmptyRow();
				incrementLinesRead();

				int index=0;
				// add card number
				row[index++]=cardNumber[j];
			
				if(data.addCardTypeOutput) {
					// add card type
					row[index++]=meta.getFieldCCType()[i];
				}

				if(data.addCardLengthOutput) {
					// add card len
					row[index++]= new Long(data.cardLen[i]);
				}
				if (isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.ValueReturned",data.outputRowMeta.getString(row)));
				
				putRow(data.outputRowMeta, row); 
			}
		}

		setOutputDone();
		return false;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (RandomCCNumberGeneratorMeta) smi;
		data = (RandomCCNumberGeneratorData) sdi;

		if (super.init(smi, sdi)) {
			// Add init code here.
			
			if(meta.getFieldCCType()==null) {
				logError(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.NoFieldSpecified"));
				return false;
			}
			if(meta.getFieldCCType().length==0) {
				logError(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.NoFieldSpecified"));
				return false;
			}	
			
			if(Const.isEmpty(meta.getCardNumberFieldName())) {
				logError(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.CardNumberFieldMissing"));
				return false;
			}
			
			data.cardTypes = new int[meta.getFieldCCType().length];
			data.cardLen = new int[meta.getFieldCCType().length];
			data.cardSize = new int[meta.getFieldCCType().length];
			
			for(int i=0; i<meta.getFieldCCType().length; i++) {
				data.cardTypes[i] = RandomCreditCardNumberGenerator.getCardType(meta.getFieldCCType()[i]);
				String len= environmentSubstitute(meta.getFieldCCLength()[i]);
				data.cardLen[i] = Const.toInt(len, -1);
				if(data.cardLen[i]<0) {
					logError(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.WrongLength", len, String.valueOf(i)));
					return false;
				}
				String size= environmentSubstitute(meta.getFieldCCSize()[i]);
				data.cardSize[i] = Const.toInt(size, -1);
				if(data.cardSize[i]<0) {
					logError(BaseMessages.getString(PKG, "RandomCCNumberGenerator.Log.WrongSize", size, String.valueOf(i)));
					return false;
				}
			}
		
			data.addCardTypeOutput= !Const.isEmpty(meta.getCardTypeFieldName());
			data.addCardLengthOutput= !Const.isEmpty(meta.getCardLengthFieldName());
			
			return true;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
	}

}