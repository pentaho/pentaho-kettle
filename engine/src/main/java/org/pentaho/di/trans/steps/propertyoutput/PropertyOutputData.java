/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
