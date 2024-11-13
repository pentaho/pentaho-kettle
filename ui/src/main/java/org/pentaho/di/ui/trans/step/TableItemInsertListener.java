/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.step;

import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.row.ValueMetaInterface;

public interface TableItemInsertListener {
  public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v );
}
