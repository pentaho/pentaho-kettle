/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.concatfields;

import java.io.UnsupportedEncodingException;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;


/*
 * ConcatFields step - derived from the TextFileOutput step
 * @author jb
 * @since 2012-08-31
 *
 */
public class ConcatFields extends TextFileOutput implements StepInterface
{

	public ConcatFieldsMeta meta;
	public ConcatFieldsData data;

	public ConcatFields(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans); // allocate TextFileOutput
	}

	@Override
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (ConcatFieldsMeta) smi;
		data = (ConcatFieldsData) sdi;

		boolean result = true;
		boolean bEndedLineWrote = false;

		Object[] r = getRow(); // This also waits for a row to be finished.

		if (r != null && first) {
			first = false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// the field precisions and lengths are altered! see TextFileOutputMeta.getFields().
			// otherwise trim(), padding etc. will not work
			data.inputRowMetaModified = getInputRowMeta().clone();
			meta.getFieldsModifyInput(data.inputRowMetaModified, getStepname(), null, null, this);
			
			data.posTargetField=data.outputRowMeta.indexOfValue(meta.getTargetFieldName());
			if (data.posTargetField < 0) {
				throw new KettleStepException("Field [" + meta.getTargetFieldName() + "] couldn't be found in the output stream!");
			}			

			if (!meta.isFileAppended() && (meta.isHeaderEnabled() || meta.isFooterEnabled())) // See if we have to write a header-line)
			{
				if (!meta.isFileNameInField() && meta.isHeaderEnabled() && data.outputRowMeta != null) {
					writeHeader();
					// add an empty line for the header
					Object[] row=new Object[data.outputRowMeta.size()];
					putRowFromStream(row);
				}
			}

			data.fieldnrs = new int[meta.getOutputFields().length];
			for (int i = 0; i < meta.getOutputFields().length; i++) {
				data.fieldnrs[i] = data.inputRowMetaModified.indexOfValue(meta.getOutputFields()[i].getName());
				if (data.fieldnrs[i] < 0) {
					throw new KettleStepException("Field [" + meta.getOutputFields()[i].getName() + "] couldn't be found in the input stream!");
				}
			}
			// prepare for re-map when removeSelectedFields
			if(meta.isRemoveSelectedFields()) {
				data.remainingFieldsInputOutputMapping = new int[data.outputRowMeta.size()-1]; //-1: don't need the new target field
				String[] fieldNames = data.outputRowMeta.getFieldNames();
				for (int i=0; i < fieldNames.length-1; i++) { //-1: don't search the new target field
					data.remainingFieldsInputOutputMapping[i]=data.inputRowMetaModified.indexOfValue(fieldNames[i]);
					if (data.remainingFieldsInputOutputMapping[i] < 0) {
						throw new KettleStepException("Remaining Field [" + fieldNames[i] + "] couldn't be found in the input stream!");
					}
				}
			}
		}

		if ((r == null && data.outputRowMeta != null && meta.isFooterEnabled()) || (r != null && getLinesOutput() > 0 && meta.getSplitEvery() > 0 && ((getLinesOutput() + 1) % meta.getSplitEvery()) == 0)) {
			if (data.outputRowMeta != null) {
				if (meta.isFooterEnabled()) {
					writeHeader();
					// add an empty line for the header
					Object[] row=new Object[data.outputRowMeta.size()];
					putRowFromStream(row);
				}
			}

			if (r == null) {
				// add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote = true;
				putRowFromStream(r);
			}

		}

		if (r == null) // no more input to be expected...
		{
			if (false == bEndedLineWrote) {
				// add tag to last line if needed
				writeEndedLine();
				bEndedLineWrote = true;
				putRowFromStream(r);
			}

			setOutputDone();
			return false;
		}

		// instead of writing to file, writes it to a stream
		writeRowToFile(data.inputRowMetaModified, r);
		putRowFromStream(r);

		if (checkFeedback(getLinesOutput()))
			logBasic("linenr " + getLinesOutput());

		return result;
	}
	
	// reads the row from the stream, flushs, add target field and call putRow() 
	private void putRowFromStream(Object[] r) throws KettleStepException{

		byte[] targetBinary=((ConcatFieldsOutputStream)data.writer).read();
		if(r==null && targetBinary==null) return;  // special condition of header/footer/split

		Object[] outputRowData = null;
		if(!meta.isRemoveSelectedFields()) {
			// reserve room for the target field
			outputRowData = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
		} else {
			// reserve room for the target field and re-map the fields
			outputRowData = new Object[data.outputRowMeta.size()+RowDataUtil.OVER_ALLOCATE_SIZE];
			if (r!=null) {
				//re-map the fields
				for (int i=0; i < data.remainingFieldsInputOutputMapping.length; i++) { // BTW: the new target field is not here
					outputRowData[i]=r[data.remainingFieldsInputOutputMapping[i]];
				}
			}
		}

		// add target field
		if(outputRowData==null) { // special condition of header/footer/split
			outputRowData=new Object[data.outputRowMeta.size()];
		}
		if (targetBinary!=null) {
			if(!data.hasEncoding) {
				outputRowData[data.posTargetField]=new String(targetBinary);
			} else { // handle encoding
				try {
					outputRowData[data.posTargetField]=new String(targetBinary, meta.getEncoding());
				} catch (UnsupportedEncodingException e) {
					throw new KettleStepException("Unsupported encoding: "+meta.getEncoding());
				}
			}
		} else {
			outputRowData[data.posTargetField]=null;
		}
		
		putRow(data.outputRowMeta, outputRowData);		
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ConcatFieldsMeta)smi;
		data=(ConcatFieldsData)sdi;

		// since we can no call the initial init() from BaseStep we have to tweak here
		meta.setDoNotOpenNewFileInit(true); // do not open a file in init
		
		data.writer = new ConcatFieldsOutputStream();

		return super.init(smi, sdi);
	}

	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		super.dispose(smi, sdi);
		// since we can no call the initial dispose() from BaseStep we may need to tweak
		// when the dispose() from TextFileOutput will have bad effects in the future due to changes and call this manually
		// sdi.setStatus(StepExecutionStatus.STATUS_DISPOSED);
		// but we try to avoid
	}


}