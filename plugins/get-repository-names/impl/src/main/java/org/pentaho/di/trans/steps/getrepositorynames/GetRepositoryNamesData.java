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


package org.pentaho.di.trans.steps.getrepositorynames;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class GetRepositoryNamesData extends BaseStepData implements StepDataInterface {
  public int filenr;
  public long rownr;

  public RowMetaInterface outputRowMeta;
  public List<RepositoryElementMetaInterface> list;

  public GetRepositoryNamesData() {
    super();

    filenr = 0;
  }
}
