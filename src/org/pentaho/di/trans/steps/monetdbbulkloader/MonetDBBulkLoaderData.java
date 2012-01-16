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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import java.io.OutputStream;

import org.pentaho.di.core.database.Database;
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
public class MonetDBBulkLoaderData extends BaseStepData implements StepDataInterface
{
	public Database db;

	public int    keynrs[];         // nr of keylookup -value in row...

	public StreamLogger errorLogger;

	public Process mClientlProcess;

	public StreamLogger outputLogger;

	public OutputStream monetOutputStream;

	public byte[] quote;
	public byte[] separator;
	public byte[] newline;

	public PGConnection pgdb;
	
	public ValueMetaInterface monetDateMeta;
	public ValueMetaInterface monetNumberMeta;
	
	public int bufferSize;
	
	public byte[][] rowBuffer;

	public int bufferIndex;

	public String schemaTable;
	
	/**
	 *  Default constructor.
	 */
	public MonetDBBulkLoaderData()
	{
		super();

		db=null;
	}
}