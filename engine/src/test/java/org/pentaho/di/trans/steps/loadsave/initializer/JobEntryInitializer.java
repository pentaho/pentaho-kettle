/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.loadsave.initializer;

import org.pentaho.di.job.entry.JobEntryInterface;

public abstract class JobEntryInitializer<T extends JobEntryInterface>
  implements InitializerInterface<JobEntryInterface> {

  @Override
  public abstract void modify( JobEntryInterface object );

}
