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

package org.pentaho.di.trans.steps.autodoc;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 14-mar-2010
 */
public class AutoDocData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public List<ReportSubjectLocation> filenames;
  public int fileNameFieldIndex;
  public int fileTypeFieldIndex;
  public Repository repository;
  public RepositoryDirectoryInterface tree;

  public AutoDocData() {
    super();

    filenames = new ArrayList<ReportSubjectLocation>();
  }
}
