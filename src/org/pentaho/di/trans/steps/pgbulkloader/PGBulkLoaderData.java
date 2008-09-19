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

package org.pentaho.di.trans.steps.pgbulkloader;

import java.io.OutputStream;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.postgresql.PGConnection;

/**
 * Stores data for the GPBulkLoader step.
 *
 * @author Sven Boden
 * @since  20-feb-2005
 */
public class PGBulkLoaderData extends BaseStepData implements StepDataInterface
{
	public Database db;

	public int    keynrs[];         // nr of keylookup -value in row...

	public StreamLogger errorLogger;

	public Process psqlProcess;

	public StreamLogger outputLogger;

	public OutputStream pgOutputStream;

	public byte[] quote;
	public byte[] separator;
	public byte[] newline;

	public PGConnection pgdb;
	
	public int dateFormatChoices[];
	
	public ValueMetaInterface dateMeta;
	public ValueMetaInterface dateTimeMeta;
	
	
	/**
	 *  Default constructor.
	 */
	public PGBulkLoaderData()
	{
		super();

		db=null;
		
		dateMeta = new ValueMeta("date", ValueMetaInterface.TYPE_DATE);
		dateMeta.setConversionMask("yyyy/MM/dd");
		
		dateTimeMeta = new ValueMeta("date", ValueMetaInterface.TYPE_DATE);
		dateTimeMeta.setConversionMask("yyyy/MM/dd HH:mm:ss");
	}
}