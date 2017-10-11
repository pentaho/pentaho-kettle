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

package org.pentaho.di.trans.steps.joinrows;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class JoinRowsData extends BaseStepData implements StepDataInterface {
  public File[] file;
  public FileInputStream[] fileInputStream;
  public DataInputStream[] dataInputStream;
  public RowMetaInterface[] fileRowMeta;

  public int[] size;
  public int[] position;
  public boolean[] restart;
  public RowSet[] rs;
  public List<Object[]>[] cache;

  public boolean caching;

  public FileOutputStream[] fileOutputStream;
  public DataOutputStream[] dataOutputStream;

  public Object[][] joinrow;

  /**
   * Keep track of which file temp file we're using...
   */
  public int filenr;

  public RowMetaInterface outputRowMeta;

  public JoinRowsData() {
    super();
  }

}
