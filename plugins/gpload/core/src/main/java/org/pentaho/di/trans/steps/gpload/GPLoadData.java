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


package org.pentaho.di.trans.steps.gpload;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Stores data for the GPLoad step.
 * 
 * @author Sven Boden
 * @since 20-feb-2005
 */
public class GPLoadData extends BaseStepData implements StepDataInterface {
  public Database db;

  /**
   * Default constructor.
   */
  public GPLoadData() {
    super();

    db = null;
  }
}
