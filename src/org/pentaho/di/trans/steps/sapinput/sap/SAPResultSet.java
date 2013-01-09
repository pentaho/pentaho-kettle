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

package org.pentaho.di.trans.steps.sapinput.sap;

import java.util.Collection;
import java.util.Vector;

public class SAPResultSet {
	
	private Collection<SAPRow> rows = new Vector<SAPRow>();

	@Override
	public String toString() {
		return "SAPResultSet [rows=" + rows + "]";
	}

	public Collection<SAPRow> getRows() {
		return rows;
	}

	public void setRows(Collection<SAPRow> rows) {
		this.rows = rows;
	}

	public void addRow(SAPRow row) {
		this.rows.add(row);
	}
}
