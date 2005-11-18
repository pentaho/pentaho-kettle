 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step.systemdata;

import java.util.Calendar;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Get information from the System or the supervising transformation.
 * 
 * @author Matt 
 * @since 4-aug-2003
 */
public class SystemData extends BaseStep implements StepInterface
{
	private SystemDataMeta meta;
	private SystemDataData data;
	
	public SystemData(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private Row getSystemData()
	{
		Row row = new Row();
		
		for (int i=0;i<meta.getFieldName().length;i++)
		{
			Calendar cal;

			int argnr=0;
			switch(meta.getFieldType()[i])
			{
			case SystemDataMeta.TYPE_SYSTEM_INFO_SYSTEM_START:
				row.addValue(new Value(meta.getFieldName()[i], getTrans().getCurrentDate()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_SYSTEM_DATE:
				row.addValue(new Value(meta.getFieldName()[i], new Date()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_DATE_FROM:
				row.addValue(new Value(meta.getFieldName()[i], getTrans().getStartDate()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_DATE_TO:
				row.addValue(new Value(meta.getFieldName()[i], getTrans().getEndDate()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_DAY_START:
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_DAY_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_DAY_START: 
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_DAY_END: 
				cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_DAY_START: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_DAY_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_MONTH_START:
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_PREV_MONTH_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, -1);
			    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_MONTH_START: 
				cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_THIS_MONTH_END: 
				cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_MONTH_START: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_NEXT_MONTH_END: 
				cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, 1);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
				cal.set(Calendar.SECOND, 59);
				cal.set(Calendar.MILLISECOND, 999);
				row.addValue(new Value(meta.getFieldName()[i], cal.getTime()));				
				break; 
			case SystemDataMeta.TYPE_SYSTEM_INFO_COPYNR:
				row.addValue(new Value(meta.getFieldName()[i], (double)getCopy()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_NAME:
				row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getName()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_MODIFIED_USER:
				row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getModifiedUser()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_MODIFIED_DATE:
				row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getModifiedDate()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_BATCH_ID:
				row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getBatchId()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_HOSTNAME:
				row.addValue(new Value(meta.getFieldName()[i], Const.getHostname()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_IP_ADDRESS:
				row.addValue(new Value(meta.getFieldName()[i], Const.getIPAddress()));
				break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_FILENAME   : 
			    row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getFilename()));
			    break;
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_01: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_02: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_03: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_04: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_05: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_06: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_07: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_08: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_09: 
			case SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_10: 
				argnr = meta.getFieldType()[i]-SystemDataMeta.TYPE_SYSTEM_INFO_ARGUMENT_01;
				if (argnr<getTransMeta().getArguments().length)
				{
					row.addValue(new Value(meta.getFieldName()[i], getTransMeta().getArguments()[argnr]));
				}
				else
				{
					Value empty = new Value(meta.getFieldName()[i], Value.VALUE_TYPE_STRING);
					empty.setValue("");
					empty.setNull();
					row.addValue(empty);
				}
				break;
			default: break;
			}
		}
		
		return row;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Row r=getSystemData();
		logRowlevel("System info returned: "+r);
		putRow(r);     // Just one row!
		linesRead++;
		setOutputDone();
					
		return false;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SystemDataMeta)smi;
		data=(SystemDataData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
