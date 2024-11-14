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


package org.pentaho.di.core;

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.List;

public interface ProvidesModelerMeta extends ProvidesDatabaseConnectionInformation {
  RowMeta getRowMeta( StepDataInterface stepData );
  List<String> getDatabaseFields();
  List<String> getStreamFields();
}
