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

package org.pentaho.di.trans.steps.hl7input;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import ca.uhn.hl7v2.parser.Parser;

/**
 * @author Matt
 * @since 24-jan-2005
 * 
 */
public class HL7InputData extends BaseStepData implements StepDataInterface {

  public int messageFieldIndex;
  public Parser parser;
  public RowMetaInterface outputRowMeta;

  public HL7InputData() {
    super();
  }

}
