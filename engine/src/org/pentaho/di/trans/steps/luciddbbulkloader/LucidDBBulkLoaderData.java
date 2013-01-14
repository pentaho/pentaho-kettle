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

package org.pentaho.di.trans.steps.luciddbbulkloader;

import java.io.OutputStream;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the LucidDB bulk load step.
 *
 * @author Matt
 * @since  14-nov-2008
 */
public class LucidDBBulkLoaderData extends BaseStepData implements StepDataInterface
{
	public Database db;

	public int    keynrs[];         // nr of keylookup -value in row...
	public ValueMetaInterface bulkFormatMeta [];
    
	public StreamLogger errorLogger;

	public StreamLogger outputLogger;

	public byte[] quote;
	public byte[] separator;
	public byte[] newline;

	public ValueMetaInterface bulkTimestampMeta;
	public ValueMetaInterface bulkDateMeta;
	public ValueMetaInterface bulkNumberMeta;
	
	public int bufferSize;
	
	public byte[][] rowBuffer;

	public int bufferIndex;

	public String schemaTable;

	public String fifoFilename;

	public String bcpFilename;

	public OutputStream fifoStream;

	public LucidDBBulkLoader.SqlRunner sqlRunner;
	
	/**
	 *  Default constructor.
	 */
	public LucidDBBulkLoaderData()
	{
		super();

		db=null;
	}
}
