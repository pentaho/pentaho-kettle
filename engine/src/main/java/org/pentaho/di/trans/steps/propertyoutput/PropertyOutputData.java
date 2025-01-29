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


package org.pentaho.di.trans.steps.propertyoutput;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Output rows to Properties file and create a file.
 *
 * @author Samatar
 * @since 13-Apr-2008
 */

public class PropertyOutputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;
  private static final String DATE_FORMAT = "yyyy-MM-dd H:mm:ss";
  DateFormat dateParser;

  public int indexOfKeyField;
  public int indexOfValueField;

  public int indexOfFieldfilename;
  public HashSet<String> KeySet;
  public FileObject file;
  public String filename;

  public Properties pro;

  public String previousFileName;

  public PropertyOutputData() {
    super();

    dateParser = new SimpleDateFormat( DATE_FORMAT );

    indexOfKeyField = -1;
    indexOfValueField = -1;

    indexOfFieldfilename = -1;
    file = null;
    previousFileName = "";
    KeySet = new HashSet<String>();
    filename = null;
  }

}
