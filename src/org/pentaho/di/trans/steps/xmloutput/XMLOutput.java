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

package org.pentaho.di.trans.steps.xmloutput;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class XMLOutput extends BaseStep implements StepInterface
{
	private XMLOutputMeta meta;

	private XMLOutputData data;

	public XMLOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
			Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (XMLOutputMeta) smi;
		data = (XMLOutputData) sdi;

		Object[] r;
		boolean result = true;

		r = getRow(); // This also waits for a row to be finished.

		if ((r != null && linesOutput > 0 && meta.getSplitEvery() > 0 && (linesOutput % meta.getSplitEvery()) == 0))
		{
			// Done with this part or with everything.
			closeFile();

			// Not finished: open another file...
			if (r != null)
			{
				if (!openNewFile())
				{
					logError("Unable to open new file (split #" + data.splitnr + "...");
					setErrors(1);
					return false;
				}
			}
		}

		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		writeRowToFile(getInputRowMeta(), r);
		
		data.outputRowMeta = getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		putRow(data.outputRowMeta, r); // in case we want it to go further...

		if (checkFeedback(linesOutput)) logBasic("linenr " + linesOutput);

		return result;
	}

	private void writeRowToFile(RowMetaInterface rowMeta, Object[] r) throws KettleException
	{
		ValueMetaAndData v;

		try
		{
			if (first)
			{
				data.previousMeta = rowMeta.clone();

				first = false;

				data.fieldnrs = new int[meta.getOutputFields().length];
				for (int i = 0; i < meta.getOutputFields().length; i++)
				{
					data.fieldnrs[i] = data.previousMeta.indexOfValue(meta.getOutputFields()[i].getFieldName());
					if (data.fieldnrs[i] < 0)
					{
						throw new KettleException("Field [" + meta.getOutputFields()[i].getFieldName()+ "] couldn't be found in the input stream!");
					}
				}
			}

			if (meta.getOutputFields() == null || meta.getOutputFields().length == 0)
			{
				/*
				 * Write all values in stream to text file.
				 */

				// OK, write a new row to the XML file:
				data.writer.write((" <" + meta.getRepeatElement() + ">").toCharArray());

				for (int i = 0; i < data.previousMeta.size(); i++)
				{
					// Put a space between the XML elements of the row
					//
					if (i > 0) data.writer.write(' ');

					ValueMetaInterface valueMeta = data.previousMeta.getValueMeta(i);
					Object valueData = r[i];
					
					writeField(new ValueMetaAndData(valueMeta, valueData), -1, valueMeta.getName());
				}
			} 
			else
			{
				/*
				 * Only write the fields specified!
				 */

				// Write a new row to the XML file:
				data.writer.write((" <" + meta.getRepeatElement() + ">").toCharArray());

				for (int i = 0; i < meta.getOutputFields().length; i++)
				{
					XMLField outputField = meta.getOutputFields()[i];

					if (i > 0)
						data.writer.write(' '); // a space between
					// elements

					Object obj = r[data.fieldnrs[i]];

					if (obj instanceof ValueMetaAndData)
						v = (ValueMetaAndData) r[data.fieldnrs[i]];
					else
						v = new ValueMetaAndData(outputField.getElementName(),obj);

					v.getValueMeta().setLength(outputField.getLength(), outputField.getPrecision());

					String element;
					if (outputField.getElementName() != null && outputField.getElementName().length() > 0)
					{
						element = outputField.getElementName();
					} else
					{
						element = v.getValueMeta().getName();
					}
					writeField(v, i, element);
				}
			}
			
			data.writer.write((" </" + meta.getRepeatElement() + ">").toCharArray());
			data.writer.write(Const.CR.toCharArray());
		} 
		catch (Exception e)
		{
			throw new KettleException("Error writing XML row :" + e.toString() + Const.CR + "Row: " + getInputRowMeta().getString(r), e);
		}

		linesOutput++;
	}

	private String formatField(ValueMetaAndData v, int idx) throws KettleValueException
	{
		String retval = "";

		XMLField field = null;
		if (idx >= 0)
		{
			field = meta.getOutputFields()[idx];
		}

		if (v.getValueMeta().isNumeric())
		{
			if (idx >= 0 && field != null && !Const.isEmpty(field.getFormat()))
			{
				if (v.getValueData() == null)
				{
					if (!Const.isEmpty(field.getNullString()))
					{
						retval = field.getNullString();
					} else
					{
						retval = Const.NULL_NUMBER;
					}
				} else
				{
					// Formatting
					if (!Const.isEmpty(field.getFormat()))
					{
						data.df.applyPattern(field.getFormat());
					} else
					{
						data.df.applyPattern(data.defaultDecimalFormat.toPattern());
					}
					// Decimal
					if (!Const.isEmpty(field.getDecimalSymbol()))
					{
						data.dfs.setDecimalSeparator(field.getDecimalSymbol().charAt(0));
					} else
					{
						data.dfs.setDecimalSeparator(data.defaultDecimalFormatSymbols.getDecimalSeparator());
					}
					// Grouping
					if (!Const.isEmpty(field.getGroupingSymbol()))
					{
						data.dfs.setGroupingSeparator(field.getGroupingSymbol().charAt(0));
					} else
					{
						data.dfs
								.setGroupingSeparator(data.defaultDecimalFormatSymbols.getGroupingSeparator());
					}
					// Currency symbol
					if (!Const.isEmpty(field.getCurrencySymbol()))
					{
						data.dfs.setCurrencySymbol(field.getCurrencySymbol());
					} else
					{
						data.dfs.setCurrencySymbol(data.defaultDecimalFormatSymbols.getCurrencySymbol());
					}

					data.df.setDecimalFormatSymbols(data.dfs);

					if (v.getValueMeta().isBigNumber())
					{
						retval = data.df.format(v.getValueData());
					} else if (v.getValueMeta().isNumber())
					{
						retval = data.df.format(v.getValueData());
					} else
					// Integer
					{
						retval = data.df.format(v.getValueData());
					}
				}
			} else
			{
				if (v.getValueData() == null)
				{
					if (idx >= 0 && field != null && !Const.isEmpty(field.getNullString()))
					{
						retval = field.getNullString();
					} else
					{
						retval = Const.NULL_NUMBER;
					}
				} else
				{
					retval = v.toString();
				}
			}
		} else if (v.getValueMeta().isDate())
		{
			if (idx >= 0 && field != null && !Const.isEmpty(field.getFormat()) && v.getValueData() != null)
			{
				if (!Const.isEmpty(field.getFormat()))
				{
					data.daf.applyPattern(field.getFormat());
				} else
				{
					data.daf.applyPattern(data.defaultDateFormat.toPattern());
				}
				data.daf.setDateFormatSymbols(data.dafs);
				retval = data.daf.format(v.getValueData());
			} else
			{
				if (v.getValueData() == null)
				{
					if (idx >= 0 && field != null && !Const.isEmpty(field.getNullString()))
					{
						retval = field.getNullString();
					} else
					{
						retval = Const.NULL_DATE;
					}
				} else
				{
					retval = v.toString();
				}
			}
		} else if (v.getValueMeta().isString())
		{
			if (v.getValueData() == null)
			{
				if (idx >= 0 && field != null && !Const.isEmpty(field.getNullString()))
				{
					retval = field.getNullString();
				} else
				{
					retval = Const.NULL_STRING;
				}
			} else
			{
				retval = v.toString();
			}
		} else if (v.getValueMeta().isBinary())
		{
			if (v.getValueData() == null)
			{
				if (!Const.isEmpty(field.getNullString()))
				{
					retval = field.getNullString();
				} else
				{
					retval = Const.NULL_BINARY;
				}
			} else
			{
				try
				{
					retval = new String(v.getValueMeta().getBinary(v.getValueData()), "UTF-8");
				} catch (UnsupportedEncodingException e)
				{
					// chances are small we'll get here. UTF-8 is
					// mandatory.
					retval = Const.NULL_BINARY;
				}

			}
		} else
		// Boolean
		{
			if (v.getValueData() == null)
			{
				if (idx >= 0 && field != null && !Const.isEmpty(field.getNullString()))
				{
					retval = field.getNullString();
				} else
				{
					retval = Const.NULL_BOOLEAN;
				}
			} else
			{
				retval = v.toString();
			}
		}

		return retval;
	}

	private void writeField(ValueMetaAndData v, int idx, String element) throws KettleStepException
	{
		try
		{
			String str = XMLHandler.addTagValue(element, formatField(v, idx), false);
			if (str != null)
				data.writer.write(str.toCharArray());
		} catch (Exception e)
		{
			throw new KettleStepException("Error writing line :", e);
		}
	}

	public String buildFilename(boolean ziparchive)
	{
		return meta.buildFilename(this, getCopy(), data.splitnr, ziparchive);
	}

	public boolean openNewFile()
	{
		boolean retval = false;
		data.writer = null;

		try
		{
			FileObject file = KettleVFS.getFileObject(buildFilename(true));

			OutputStream outputStream;
			if (meta.isZipped())
			{
				OutputStream fos = KettleVFS.getOutputStream(file, false);
				data.zip = new ZipOutputStream(fos);
				File entry = new File(buildFilename(false));
				ZipEntry zipentry = new ZipEntry(entry.getName());
				zipentry.setComment("Compressed by Kettle");
				data.zip.putNextEntry(zipentry);
				outputStream = data.zip;
			} else
			{
				OutputStream fos = KettleVFS.getOutputStream(file, false);
				outputStream = fos;
			}
			if (meta.getEncoding() != null && meta.getEncoding().length() > 0)
			{
				log.logBasic(toString(), "Opening output stream in encoding: " + meta.getEncoding());
				data.writer = new OutputStreamWriter(outputStream, meta.getEncoding());
				data.writer.write(XMLHandler.getXMLHeader(meta.getEncoding()).toCharArray());
			} else
			{
				log.logBasic(toString(), "Opening output stream in default encoding : " + Const.XML_ENCODING);
				data.writer = new OutputStreamWriter(outputStream);
				data.writer.write(XMLHandler.getXMLHeader(Const.XML_ENCODING).toCharArray());
			}

			// OK, write the header & the parent element:
			data.writer.write(("<" + meta.getMainElement() + ">" + Const.CR).toCharArray());

			retval = true;
		} catch (Exception e)
		{
			logError("Error opening new file : " + e.toString());
		}
		// System.out.println("end of newFile(), splitnr="+splitnr);

		data.splitnr++;

		return retval;
	}

	private boolean closeFile()
	{
		boolean retval = false;

		try
		{
			// Close the parent element
			data.writer.write(("</" + meta.getMainElement() + ">" + Const.CR).toCharArray());

			if (meta.isZipped())
			{
				// System.out.println("close zip entry ");
				data.zip.closeEntry();
				// System.out.println("finish file...");
				data.zip.finish();
				data.zip.close();
			} else
			{
				data.writer.close();
			}

			// System.out.println("Closed file...");

			retval = true;
		} catch (Exception e)
		{
		}

		return retval;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLOutputMeta) smi;
		data = (XMLOutputData) sdi;

		if (super.init(smi, sdi))
		{
			data.splitnr = 0;

			if (openNewFile())
			{
				return true;
			} else
			{
				logError("Couldn't open file " + meta.getFileName());
				setErrors(1L);
				stopAll();
			}
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (XMLOutputMeta) smi;
		data = (XMLOutputData) sdi;

		closeFile();

		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		} 
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
			logError(Const.getStackTracker(t));
			setErrors(1);
			stopAll();
		} 
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}