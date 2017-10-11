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

package org.pentaho.di.trans.steps.sort;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SortRowsData extends BaseStepData implements StepDataInterface {
  public List<FileObject> files;
  public List<Object[]> buffer;
  public int getBufferIndex;

  public List<InputStream> fis;
  public List<GZIPInputStream> gzis;
  public List<DataInputStream> dis;
  public List<Object[]> rowbuffer;
  public List<Integer> bufferSizes;

  // To store rows and file references
  public List<RowTempFile> tempRows;

  public int[] fieldnrs; // the corresponding field numbers;
  public FileObject fil;
  public RowMetaInterface outputRowMeta;
  public int sortSize;
  public boolean compressFiles;
  public int[] convertKeysToNative;
  public boolean convertAnyKeysToNative;

  Comparator<RowTempFile> comparator;
  Comparator<Object[]> rowComparator;

  public int freeCounter;
  public int freeMemoryPct;
  public int minSortSize;
  public int freeMemoryPctLimit;
  public int memoryReporting;

  /*
   * Group Fields Implementation heroic
   */
  public Object[] previous;
  public int[] groupnrs;
  public boolean newBatch;

  public SortRowsData() {
    super();

    files = new ArrayList<FileObject>();
    fis = new ArrayList<InputStream>();
    gzis = new ArrayList<GZIPInputStream>();
    dis = new ArrayList<DataInputStream>();
    bufferSizes = new ArrayList<Integer>();

    previous = null; // Heroic
  }

}
