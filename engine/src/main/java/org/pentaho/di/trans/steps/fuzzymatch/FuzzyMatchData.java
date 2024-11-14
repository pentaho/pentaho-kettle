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
