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

package org.pentaho.di.ui.core.database.dialog;

import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * This class has been adapted to use the XUL version of the DatabaseExplorerDialog instead.
 * The old DatabaseExplorerDialog has been renamed to DatabaseExplorerDialogLegacy
 */
public class DatabaseExplorerDialog extends XulDatabaseExplorerDialog {

	public DatabaseExplorerDialog(Shell parent, int style, DatabaseMeta conn, List<DatabaseMeta> databases) {
		super(parent, conn, databases, false);
	}

	public DatabaseExplorerDialog(Shell parent, int style, DatabaseMeta conn, List<DatabaseMeta> databases, boolean aLook) {
		super(parent, conn, databases, aLook);
	}
}
/*
public class DatabaseExplorerDialog extends DatabaseExplorerDialogLegacy {

public DatabaseExplorerDialog(Shell parent, int style, DatabaseMeta conn, List<DatabaseMeta> databases) {
  super(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN, conn, databases, false);
}

public DatabaseExplorerDialog(Shell parent, int style, DatabaseMeta conn, List<DatabaseMeta> databases, boolean aLook) {
  super(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN, conn, databases, aLook);
}
}
*/