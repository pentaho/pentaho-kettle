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

package org.pentaho.di.trans.step;

import org.pentaho.di.core.database.DatabaseMeta;

/**
 * An interface for transformation steps that connect to a database table. For example a table output step
 * or a bulk loader. This interface is used by the Agile BI plugin to determine which steps it can
 * model or visualize.
 * @author jamesdixon
 *
 */
public interface TableFrontingStep {

	/**
	 * Returns the database meta for this step
	 * @return
	 */
	public DatabaseMeta getDatabaseMeta();
	
	/**
	 * Returns the table name for this step
	 * @return
	 */
	public String getTableName();

	/**
	 * Returns the schema name for this step.
	 * @return
	 */
	public String getSchemaName();
	
}
