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


package org.pentaho.di.core.gui;

import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.step.StepMeta;

public interface GCInterface extends PrimitiveGCInterface {

  public void drawJobEntryIcon( int x, int y, JobEntryCopy jobEntryCopy, float magnification );

  public void drawJobEntryIcon( int x, int y, JobEntryCopy jobEntryCopy );

  public void drawStepIcon( int x, int y, StepMeta stepMeta, float magnification );

  public void drawStepIcon( int x, int y, StepMeta stepMeta );
}
