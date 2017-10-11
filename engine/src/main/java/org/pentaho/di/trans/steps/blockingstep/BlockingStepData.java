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

package org.pentaho.di.trans.steps.blockingstep;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class BlockingStepData extends BaseStepData implements StepDataInterface {
  public List<FileObject> files;
  public List<Object[]> buffer;
  public List<InputStream> fis;
  public List<GZIPInputStream> gzis;
  public List<DataInputStream> dis;
  public List<Object[]> rowbuffer;

  public RowMetaInterface outputRowMeta;

  public int[] fieldnrs; // the corresponding field numbers;
  public FileObject fil;

  public BlockingStepData() {
    super();

    buffer = new ArrayList<Object[]>( BlockingStepMeta.CACHE_SIZE );
    files = new ArrayList<FileObject>();
    fis = new ArrayList<InputStream>();
    dis = new ArrayList<DataInputStream>();
    gzis = new ArrayList<GZIPInputStream>();
    rowbuffer = new ArrayList<Object[]>();
  }
}
