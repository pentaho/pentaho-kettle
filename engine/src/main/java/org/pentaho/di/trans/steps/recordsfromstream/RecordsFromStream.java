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

package org.pentaho.di.trans.steps.recordsfromstream;

import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowsfromresult.RowsFromResult;

public class RecordsFromStream extends RowsFromResult {
  public RecordsFromStream( final StepMeta stepMeta,
                            final StepDataInterface stepDataInterface, final int copyNr,
                            final TransMeta transMeta, final Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }
}
