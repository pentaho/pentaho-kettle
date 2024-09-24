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

package org.pentaho.di.trans.steps.file;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;

/**
 * Some base data for file-based input steps.
 *
 * @author Alexander Buloichik
 */
public class BaseFileInputStepData extends BaseStepData {

  public FileErrorHandler dataErrorLineHandler;

  /** Files for process. */
  public FileInputList files;

  /** Current file info. */
  public String filename;
  public int currentFileIndex;
  public FileObject file;

  /** Reader for current file. */
  public IBaseFileInputReader reader;

  public RowMetaInterface outputRowMeta;

  public HashMap<String, Object[]> passThruFields;

  public Object[] currentPassThruFieldsRow;

  public int nrPassThruFields;

  public RowMetaInterface convertRowMeta;

  public int nr_repeats;
  // public boolean isLastFile;

  public Map<String, Boolean> rejectedFiles = new HashMap<String, Boolean>();

  /** File-dependent data for fill additional fields. */
  public String shortFilename;
  public String path;
  public String extension;
  public boolean hidden;
  public Date lastModificationDateTime;
  public String uriName;
  public String rootUriName;
  public Long size;

}
