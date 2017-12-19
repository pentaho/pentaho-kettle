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

package org.pentaho.di.trans.steps.fuzzymatch;

import java.util.HashSet;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * @author Samatar
 * @since 24-jan-2010
 */
public class FuzzyMatchData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;

  /** used to store values in used to look up things */
  public HashSet<Object[]> look;

  public boolean readLookupValues;

  /** index of main stream field **/
  public int indexOfMainField;

  public int minimalDistance;

  public int maximalDistance;

  public double minimalSimilarity;

  public double maximalSimilarity;

  public String valueSeparator;

  public RowMetaInterface infoMeta;

  public StreamInterface infoStream;

  public boolean addValueFieldName;
  public boolean addAdditionalFields;

  /** index of return fields from lookup stream **/
  public int[] indexOfCachedFields;
  public int nrCachedFields;
  public RowMetaInterface infoCache;

  public FuzzyMatchData() {
    super();
    this.look = new HashSet<Object[]>();
    this.indexOfMainField = -1;
    this.addValueFieldName = false;
    this.valueSeparator = "";
    this.nrCachedFields = 1;
    this.addAdditionalFields = false;
  }

}
