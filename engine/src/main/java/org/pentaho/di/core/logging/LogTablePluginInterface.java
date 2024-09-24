/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.logging;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;

public interface LogTablePluginInterface extends LogTableInterface {

  public enum TableType {
    JOB, TRANS;
  }

  public TableType getType();

  public String getLogTablePluginUIClassname();

  public void setContext( VariableSpace space, HasDatabasesInterface jobMeta );

  // Otherwise identical to the log table interface.

}
