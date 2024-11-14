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


package org.pentaho.di.trans.steps.stepsmetrics;

import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;

/**
 * @author Samatar
 * @since 16-06-2008
 *
 */
public class StepsMetricsData extends BaseStepData implements StepDataInterface {

  boolean continueLoop;
  public ConcurrentHashMap<Integer, StepInterface> stepInterfaces;
  /** The metadata we send out */
  public RowMetaInterface outputRowMeta;

  public String realstepnamefield;
  public String realstepidfield;
  public String realsteplinesinputfield;
  public String realsteplinesoutputfield;
  public String realsteplinesreadfield;
  public String realsteplinesupdatedfield;
  public String realsteplineswrittentfield;
  public String realsteplineserrorsfield;
  public String realstepsecondsfield;

  public StepsMetricsData() {
    super();
    continueLoop = true;

    realstepnamefield = null;
    realstepidfield = null;
    realsteplinesinputfield = null;
    realsteplinesoutputfield = null;
    realsteplinesreadfield = null;
    realsteplinesupdatedfield = null;
    realsteplineswrittentfield = null;
    realsteplineserrorsfield = null;
    realstepsecondsfield = null;
  }
}
