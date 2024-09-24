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

package org.pentaho.di.trans.steps.valuemapper;

import java.util.Hashtable;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class ValueMapperData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface previousMeta;
  public RowMetaInterface outputMeta;

  public int keynr;

  public Hashtable<String, String> hashtable;

  public int emptyFieldIndex;

  public ValueMetaInterface stringMeta;
  public ValueMetaInterface outputValueMeta;
  public ValueMetaInterface sourceValueMeta;

  public ValueMapperData() {
    super();

    hashtable = null;

    stringMeta = new ValueMetaString( "string" );
  }

}
