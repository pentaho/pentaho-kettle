/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan 
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.checksum;

import java.security.MessageDigest;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Caculate a checksum for each row.
 * 
 * @author Samatar Hassan
 * @since 30-06-2008
 */
public class CheckSum extends BaseStep implements StepInterface {

	private static Class<?> PKG = CheckSumMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CheckSumMeta meta;

	private CheckSumData data;

	public CheckSum(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (CheckSumMeta) smi;
		data = (CheckSumData) sdi;

		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

			if (meta.getFieldName() == null || meta.getFieldName().length > 0) {
				data.fieldnrs = new int[meta.getFieldName().length];

				for (int i = 0; i < meta.getFieldName().length; i++) {
					data.fieldnrs[i] = getInputRowMeta().indexOfValue(
							meta.getFieldName()[i]);
					if (data.fieldnrs[i] < 0) {
						logError(BaseMessages.getString(PKG, "CheckSum.Log.CanNotFindField", meta.getFieldName()[i]));
						throw new KettleException(BaseMessages.getString(PKG, "CheckSum.Log.CanNotFindField", meta.getFieldName()[i]));
					}
				}
			} else {
				data.fieldnrs = new int[r.length];
				for (int i = 0; i < r.length; i++) {
					data.fieldnrs[i] = i;
				}
			}
			data.fieldnr = data.fieldnrs.length;

		} // end if first

		boolean sendToErrorRow = false;
		String errorMessage = null;
		Object[] outputRowData = null;

		try {
			if (meta.getCheckSumType().equals(CheckSumMeta.TYPE_ADLER32)
					|| meta.getCheckSumType().equals(CheckSumMeta.TYPE_CRC32)) {
				// get checksum
				Long checksum=calculCheckSum(r);
				outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), checksum);
			} else {
				// get checksum
				
				byte[] o= createCheckSum(r);
				switch(meta.getResultType())
				{
					case CheckSumMeta.result_TYPE_BINARY  : 
						outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), o);
						break;
					case CheckSumMeta.result_TYPE_HEXADECIMAL : 
						outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), byteToHexEncode(o));
						break;
					default: 
						outputRowData = RowDataUtil.addValueData(r, getInputRowMeta().size(), getStringFromBytes(o));
					break;
				}
			}

			if (checkFeedback(getLinesRead())) {
				if (log.isDetailed())
					logDetailed(BaseMessages.getString(PKG, "CheckSum.Log.LineNumber", "" + getLinesRead())); //$NON-NLS-1$
			}

			// add new values to the row.
			putRow(data.outputRowMeta, outputRowData); // copy row to output
														// rowset(s);
		} catch (Exception e) {
			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(BaseMessages.getString(PKG, "CheckSum.ErrorInStepRunning") + e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow) {
				// Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, meta
						.getResultFieldName(), "CheckSum001");
			}
		}
		return true;
	}

	private byte[] createCheckSum(Object[] r) throws Exception {
		StringBuffer Buff = new StringBuffer();

		// Loop through fields
		for (int i = 0; i < data.fieldnr; i++) {
			String fieldvalue = getInputRowMeta()
					.getString(r, data.fieldnrs[i]);
			Buff.append(fieldvalue);
		}
		MessageDigest digest;
		if(meta.getCheckSumType().equals(CheckSumMeta.TYPE_MD5))
			digest = MessageDigest.getInstance(CheckSumMeta.TYPE_MD5);
		else
			digest = MessageDigest.getInstance(CheckSumMeta.TYPE_SHA1);
		
		digest.update(Buff.toString().getBytes());
		byte[] hash = digest.digest();

		return hash;
	}

	private static String getStringFromBytes(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			sb.append((int) (0x00FF & b));
			if (i + 1 < bytes.length) {
				sb.append("-");
			}
		}
		return sb.toString();
	}
	 public String byteToHexEncode(byte[] in)
	 {
		 	if(in==null) return null;
	        final char hexDigits[] ={ '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };
			
			String hex = new String(in);
			
			char[] s = hex.toCharArray();
			StringBuffer hexString = new StringBuffer(2 * s.length);
			
			for (int i = 0; i < s.length; i++)
			{
				hexString.append(hexDigits[(s[i] & 0x00F0) >> 4]); // hi nibble
				hexString.append(hexDigits[s[i] & 0x000F]);        // lo nibble
			}
			
			return hexString.toString();
		}
	private Long calculCheckSum(Object[] r) throws Exception {
		Long retval;
		StringBuffer Buff = new StringBuffer();

		// Loop through fields
		for (int i = 0; i < data.fieldnr; i++) {
			String fieldvalue = getInputRowMeta()
					.getString(r, data.fieldnrs[i]);
			Buff.append(fieldvalue);
		}

		if (meta.getCheckSumType().equals("CRC32")) {
			CRC32 crc32 = new CRC32();
			crc32.update(Buff.toString().getBytes());
			retval = new Long(crc32.getValue());
		} else {
			Adler32 adler32 = new Adler32();
			adler32.update(Buff.toString().getBytes());
			retval = new Long(adler32.getValue());
		}

		return retval;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (CheckSumMeta) smi;
		data = (CheckSumData) sdi;

		if (super.init(smi, sdi)) {
			if (Const.isEmpty(meta.getResultFieldName())) {
				logError(BaseMessages.getString(PKG, "CheckSum.Error.ResultFieldMissing"));
				return false;
			}
			return true;
		}
		return false;
	}

}